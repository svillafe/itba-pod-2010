package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.ResourceTransferPayloadImpl;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.thread.CleanableThread;

public class TransactionableImpl implements Transactionable {

	private final static Logger LOGGER = Logger
			.getLogger(TransactionableImpl.class);

	private Node myNode;
	private String theOtherNode;
	private AtomicBoolean haveExchange;
	private AtomicInteger exchangeCant;
	private Resource exchangeResource;
	private CleanableThread timeoutThread;

	private TransactionableState myState;

	public TransactionableImpl(Node myNode) throws RemoteException {
		super();
		this.myNode = myNode;
		this.myState = TransactionableState.NO_STATE;
		this.haveExchange = new AtomicBoolean();
		this.exchangeCant = new AtomicInteger();
		UnicastRemoteObject.exportObject(this, 0);

	}

	@Override
	public void beginTransaction(String remoteNodeId, long timeout)
			throws RemoteException {
		ConnectionManager remoteCM;

		/* Si mi estado no es NO_STATE entonces estoy en una transaccion */
		if (myState != TransactionableState.NO_STATE) {
			LOGGER.info("Error me encuentro en una transaccion.");
			throw new IllegalStateException("Ya estoy en una transaccion.");
		}

		/* Obtengo el Connection Manager del otro nodo */
		remoteCM = myNode.getMyConnectionManager().getConnectionManager(
				remoteNodeId);

		try {
			remoteCM.getNodeCommunication().acceptTransaction(
					myNode.getNodeId());
		} catch (RemoteException e) {
			/*
			 * Si lanza una excepcion el acceptTransaction significa que esta
			 * ocupado con otra Transaccion entonces retorno.
			 */
			LOGGER.info("El accept Transaction fallo");
			return;
		}

		/*
		 * Si llego hasta aca significa que el otro nodo puede hacer una
		 * transaccion
		 */
		synchronized (myState) {
			this.myState = TransactionableState.BEGIN_TRANSACTIONABLE_STATE;
		}

		this.theOtherNode = remoteNodeId;
		this.haveExchange.set(false);

		/* Lanzo el timer local */
		timeoutThread = new TransactionableTimer((int) timeout, this);
		timeoutThread.start();
	}

	@Override
	public void acceptTransaction(String remoteNodeId) throws RemoteException {

		if (myState != TransactionableState.NO_STATE)
			/* Si estoy en una transaccion espero */
			try {
				Thread.sleep(Node.TRANSACTION_WAIT_TIMEOUT);
			} catch (InterruptedException e) {
				
				//e.printStackTrace();
			}
		if (myState != TransactionableState.NO_STATE) {
			/* Si sigo en una transaccion fallo */
			throw new IllegalStateException("The node is on a transaction.");

		}
		/* Hago un rollback para setear todo en cero */
		rollback();
		synchronized (myState) {
			this.myState = TransactionableState.BEGIN_TRANSACTIONABLE_STATE;
		}

		this.theOtherNode = remoteNodeId;
	}

	@Override
	public void endTransaction() throws RemoteException {

		if (this.myState == TransactionableState.NO_STATE) {
			throw new IllegalStateException(
					"You are not in a transaction context.");
		}

		/* Llegue al endTransaction entonces mato al timer */
		if (timeoutThread.isAlive()) {
			timeoutThread.interrupt();
		}

		synchronized (myState) {
			this.myState = TransactionableState.DOING_3PC;
		}

		threePhaseCommit();

		synchronized (myState) {
			this.myState = TransactionableState.END_TRANSACTION_STATE;
		}

	}

	private void threePhaseCommit() throws RemoteException {

		ThreePhaseCommit my3PC = null;
		ThreePhaseCommit remote3PC = null;
		boolean resp;

		my3PC = myNode.getMyConnectionManager().getThreePhaseCommit();
		remote3PC = myNode.getMyConnectionManager()
				.getConnectionManager(theOtherNode).getThreePhaseCommit();

		LOGGER.info("Hago mi canCommit");
		resp = my3PC.canCommit(myNode.getNodeId(),
				Node.THREE_PHASE_COMMIT_TIME_OUT);

		if (resp) {

			try {
				remote3PC.canCommit(myNode.getNodeId(),
						Node.THREE_PHASE_COMMIT_TIME_OUT);

				my3PC.preCommit(myNode.getNodeId());
				remote3PC.preCommit(myNode.getNodeId());

				my3PC.doCommit(myNode.getNodeId());
				remote3PC.doCommit(myNode.getNodeId());

			} catch (RemoteException e) {
				LOGGER.info("Hubo un error en la transaccion Abortar...");
				my3PC.abort();
				remote3PC.abort();
				LOGGER.info("Transaccion Abortada.");

			}
		}
	}

	@Override
	public void exchange(Resource resource, int amount, String sourceNode,
			String destinationNode) throws RemoteException {
		if (this.myState != TransactionableState.BEGIN_TRANSACTIONABLE_STATE) {
			throw new IllegalStateException(
					"You are not in a transaction context.");
		}
		if (sourceNode.equals(destinationNode)) {
			throw new IllegalStateException(
					"The source node and the remote node must be diferent.");
		}
		if (!((sourceNode.equals(myNode.getNodeId()) && destinationNode
				.equals(theOtherNode)) || (sourceNode.equals(theOtherNode) && destinationNode
				.equals(myNode.getNodeId())))) {
			throw new IllegalStateException(
					"The nodes are not the same of the transaction context.");
		}

		/* Se guarda el contexto de transaccion */
		this.exchangeCant.set(amount);
		this.exchangeResource = resource;
		synchronized (myState) {
			this.myState = TransactionableState.EXCHANGE_STATE;
		}
		this.haveExchange.set(true);
		return;
	}

	@Override
	public Payload getPayload() throws RemoteException {
		if (myState == TransactionableState.NO_STATE) {
			throw new IllegalStateException("You must be on a transanction.");
		}

		if (this.haveExchange.get() == false) {
			throw new IllegalStateException(
					"You must to do an exchange before to get the payload.");
		}

		return new ResourceTransferPayloadImpl(this.exchangeResource,
				this.exchangeCant.get(), myNode.getNodeId(), theOtherNode);

	}

	@Override
	public void rollback() throws RemoteException {

		/* Vuelvo al estado inicial */
		this.exchangeCant.set(0);
		this.exchangeResource = null;
		this.haveExchange.set(false);
		this.theOtherNode = null;
		synchronized (myState) {
			myState = TransactionableState.NO_STATE;
		}
		/* Mato al timer */
		this.finishTimer();
	}

	public TransactionableState getMyState() {
		return myState;
	}

	public void setMyState(TransactionableState a) {
		synchronized (myState) {
			myState = a;
		}

	}

	public void finishTimer() {
		/*Mato al timer de timeout*/
		if (timeoutThread != null && timeoutThread.isAlive()) {

			timeoutThread.interrupt();
			try {
				timeoutThread.join();
			} catch (InterruptedException a) {

			}
		}
		this.timeoutThread=null;
	}

	public void timeoutCumplido() {
		/*Significa que no llegue al endTransaction entonces solo hago un rollback de todo*/
		try {
			this.rollback();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

}
