package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedMarket;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.Multiset;

public class ThreePhaseCommitImpl implements ThreePhaseCommit {

	private final static Logger LOGGER = Logger
			.getLogger(ThreePhaseCommit.class);
	private Node myNode;

	private String threePCCoordinator;
	private ThreePhaseCommitState myState;
	private Thread timer;
	private AtomicBoolean cambiarEstado;

	public ThreePhaseCommitImpl(Node myNode) throws RemoteException {
		super();
		this.myNode = myNode;
		this.myState = ThreePhaseCommitState.NO_STATE;
		this.threePCCoordinator = null;
		UnicastRemoteObject.exportObject(this, 0);
		this.cambiarEstado = new AtomicBoolean(false);

	}

	@Override
	public boolean canCommit(String coordinatorId, long timeout)
			throws RemoteException {

		if (myState == ThreePhaseCommitState.NO_STATE
				|| myState == ThreePhaseCommitState.FINALIZE) {

			synchronized (myState) {
				myState = ThreePhaseCommitState.CAN_COMMIT;
			}

			threePCCoordinator = coordinatorId;

			if (timer != null && timer.isAlive()) {

				timer.interrupt();
				try {
					timer.join();
				} catch (InterruptedException a) {

				}
			}

			timer = new ThreePhaseCommitTimer(this, timeout);
			timer.start();
			return true;
		}
		return false;
	}

	@Override
	public void preCommit(String coordinatorId) throws RemoteException {
		if (myState != ThreePhaseCommitState.CAN_COMMIT) {

			throw new IllegalStateException(
					"You have to invoke a CanComit before");
		}

		if (!coordinatorId.equals(threePCCoordinator)) {
			throw new IllegalArgumentException(
					"Only the same coordinator that invoked canCommit can invoke this method.");
		}

		synchronized (myState) {
			myState = ThreePhaseCommitState.PRE_COMMIT;
		}

	}

	@Override
	public void doCommit(String coordinatorId) throws RemoteException {
		/* Verificaciones */
		if (this.myState != ThreePhaseCommitState.PRE_COMMIT) {
			throw new IllegalStateException(
					"You must invoke the precommit before this method.");
		}
		if (!this.threePCCoordinator.equals(coordinatorId)) {
			throw new IllegalArgumentException("The coordinator do not match.");
		}

		timer.interrupt();

		synchronized (myState) {
			myState = ThreePhaseCommitState.DO_COMMIT;
		}

		/*
		 * Nueva version, no me mando el auto mensaje sino que manejo todo desde
		 * aca
		 */

		/* Le pido al coordinador los recursos de la transferencia */

		Transactionable coordinatorTransaction = myNode
				.getMyConnectionManager().getConnectionManager(coordinatorId)
				.getNodeCommunication();

		ResourceTransferMessagePayload thePayload = (ResourceTransferMessagePayload) coordinatorTransaction
				.getPayload();

		this.procesarTransferencia(thePayload);

		myState = ThreePhaseCommitState.FINALIZE;

	}

	private void procesarTransferencia(ResourceTransferMessagePayload msj) {

		Integer cantidad;
		Resource recurso;
		recurso = msj.getResource();
		cantidad = msj.getAmount();

		DistributedMarket myMarket = (DistributedMarket) (myNode
				.getMyMarketManager().market());

		Multiset<Resource> externalRepository = myMarket
				.getExternalRepository();
		Multiset<ResourceStock> selling = myMarket.getSelling();

		if (msj.getDestination().equals(myNode.getNodeId())) {

			/* Los recursos son para mi, los agrego a mi repositorio externo */
			LOGGER.info("Los recursos son para mi.");

			synchronized (externalRepository) {
				if (externalRepository.count(recurso) != 0) {
					LOGGER.info("<<<-----------*AGREGADO AL REPOSITORIO EXTERNO:"
							+ cantidad + " DE " + recurso);
					externalRepository.add(recurso, cantidad);
				} else {
					LOGGER.info("<<<-----------**AGREGADO AL REPOSITORIO EXTERNO:"
							+ cantidad + " DE " + recurso);
					externalRepository.setCount(recurso, cantidad);
				}
			}

		} else {
			/* Los recursos se van de mi mercado local, los descuento */
			LOGGER.info("Saco los recurso de mi mercado local.");

			synchronized (selling) {

				for (ResourceStock seller : selling) {
					
					if(cantidad==0){
						return;
					}
					
					if (recurso.equals(seller.resource())) {

						int available = selling.count(seller);

						Integer aTransferir = Math.min(available, cantidad);

						if (aTransferir >= 0) {
							if (cantidad > 0) {
								LOGGER.info("------------->>>SE VAN DEL SELLING:"
										+ cantidad
										+ " DE "
										+ seller.resource()
										+ " PRODUCIDO POR " + seller.name());
							}
							selling.setCount(seller, available,available - aTransferir);

							try{
							seller.remove(aTransferir);
							}catch(IllegalArgumentException e){
								//System.out.println("FALAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
								
							}
							
							cantidad = cantidad-aTransferir;
						}
					}
				}
			}
		}
	}

	@Override
	public void abort() throws RemoteException {
		LOGGER.info("ENTRANDO AL ABORT.");

		switch (myState) {
		case NO_STATE:
			throw new IllegalStateException(
					"No se puede abortar una transaccion que no se inicio.");
		case CAN_COMMIT:
			LOGGER.info("Abort en el CanCommit");
			if (timer != null && timer.isAlive()) {
				timer.interrupt();
				try {
					timer.join();
				} catch (InterruptedException e) {
					// Auto-generated catch block
					LOGGER.info("ERROR:" + e.getMessage());
				}
			}
			timer = null;
			myNode.getMyConnectionManager().getNodeCommunication().rollback();
			this.myState = ThreePhaseCommitState.NO_STATE;
			break;

		case PRE_COMMIT:
			LOGGER.info("Abort en el PreCommit");
			if (timer != null && timer.isAlive()) {
				timer.interrupt();
				try {
					timer.join();
				} catch (InterruptedException e) {
					LOGGER.info("ERROR:" + e.getMessage());
				}
			}
			timer = null;
			myNode.getMyConnectionManager().getNodeCommunication().rollback();
			this.myState = ThreePhaseCommitState.NO_STATE;
			break;

		case DO_COMMIT:
			LOGGER.info("Abort en el DoCommit");
			if (timer != null && timer.isAlive()) {
				timer.interrupt();
				try {
					timer.join();
				} catch (InterruptedException e) {
					LOGGER.info("ERROR:" + e.getMessage());
				}
			}
			timer = null;
			myNode.getMyConnectionManager().getNodeCommunication().rollback();
			this.myState = ThreePhaseCommitState.NO_STATE;
			break;

		case FINALIZE:
			/* El caso que hay que revertir la transaccion hecha */
			LOGGER.info("Abort en el Finalize");

			// Este caso tendo que revertir todo.
			LOGGER.info("Caso choto que es un dolor de cabeza. Encontraremos el mono de jade antes de la proxima luna llena");

			Transactionable coordinatorTransaction = myNode
					.getMyConnectionManager()
					.getConnectionManager(threePCCoordinator)
					.getNodeCommunication();

			ResourceTransferMessagePayload thePayload = (ResourceTransferMessagePayload) coordinatorTransaction
					.getPayload();

			Integer cantidad = thePayload.getAmount();
			Resource recurso = thePayload.getResource();

			Multiset<Resource> externalRepository = ((DistributedMarket) (myNode
					.getMyMarketManager(this.myNode.getMyConnectionManager())
					.market())).getExternalRepository();
			// Multiset<ResourceStock> selling = ((DistributedMarket) (myNode
			// .getMyMarketManager(this.myNode.getMyConnectionManager())
			// .market())).getSelling();

			if (thePayload.getDestination().equals(myNode.getNodeId())) {
				LOGGER.info("Soy al que le entregaban recursos entronces en el abort me los saco.");
				synchronized (externalRepository) {
					if (externalRepository.count(recurso) - cantidad >= 0) {
						externalRepository.setCount(recurso,
								externalRepository.count(recurso) - cantidad);
					}
				}

			} else {
				LOGGER.info("Soy el que entregaba recursos entronces en el abort me agrego.");
				synchronized (externalRepository) {
					if (externalRepository.count(recurso) != 0) {
						externalRepository.add(recurso, cantidad);
					} else {
						externalRepository.setCount(recurso, cantidad);
					}
				}

			}

			if (timer != null && timer.isAlive()) {
				timer.interrupt();
				try {
					timer.join();
				} catch (InterruptedException e) {
					LOGGER.info("ERROR:" + e.getMessage());
				}
			}
			timer = null;
			myNode.getMyConnectionManager().getNodeCommunication().rollback();
			this.myState = ThreePhaseCommitState.NO_STATE;
			break;
		default:
			break;
		}
		LOGGER.info("Termina el abort.");
	}

	/**
	 * Method called when the cohort waits more than the timeout set for doing
	 * the commit. All the changes done must be reverted. If it is invoked
	 * before canCommit method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	@Override
	public void onTimeout() throws RemoteException {

		switch (this.myState) {
		case CAN_COMMIT:
			LOGGER.info("Timeout en el Abort.");
			this.abort();
			break;

		case PRE_COMMIT:
			LOGGER.info("Timeout en el PreCommit.");
			this.doCommit(this.threePCCoordinator);

		case DO_COMMIT:
			LOGGER.info("Timeout en el DoCommit.");
			break;
		case FINALIZE:
			LOGGER.info("Timeout en el Finalize.");
			myState = ThreePhaseCommitState.NO_STATE;
		default:
			break;
		}

	}

	public void setMyState(ThreePhaseCommitState finalize) {
		myState = finalize;

	}

	public AtomicBoolean getCambiarEstado() {
		return cambiarEstado;
	}

}
