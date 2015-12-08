package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerImpl extends UnicastRemoteObject implements
		ConnectionManager {

	private final static Logger LOGGER = Logger
			.getLogger(ConnectionManagerImpl.class);

	private ClusterAdministration myClusterAdministration;
	private ClusterCommunication myClusterCommunication;
	private SimulationCommunication mySimulationCommunication;
	private ThreePhaseCommit myThreePhaseCommit;
	private Transactionable myTransactionable;
	
	/* El nodo que instancia el CM */
	private Node myNode;

	public ConnectionManagerImpl(Node myNode) throws RemoteException {
		super();
		this.myNode = myNode;
		this.myClusterAdministration = new ClusterAdministrationImpl(myNode);
		this.myClusterCommunication = new ClusterCommunicationImpl(myNode);
		this.mySimulationCommunication = new SimulationCommunicationImpl(myNode);
		this.myThreePhaseCommit= new ThreePhaseCommitImpl(myNode);
		this.myTransactionable = new TransactionableImpl(myNode);
		
	}

	@Override
	public int getClusterPort() throws RemoteException {
		return 1099;
	}

	@Override
	public ConnectionManager getConnectionManager(String nodeId)
			throws RemoteException {
		Node remoteNode= Node.valueOf(nodeId);
		  																						
		if(nodeId.equals(myNode.getNodeId())){
			return this;
		}
		
		final Registry registry = LocateRegistry.getRegistry(remoteNode.getHost(),remoteNode.getPortNumber());
		ConnectionManager stub = null;
		
		
		try {
			stub = (ConnectionManager) registry
					.lookup(ReferenceName.CONNECTION_MANAGER_NAME);
		} catch (NotBoundException e) {

			LOGGER.info("Se detecta la caida de un nodo");
			System.out.println("Se detecta la caida de un nodo");
			/*mando mensaje de desconexion*/
			this.myClusterAdministration.disconnectFromGroup(nodeId);
			
			throw new RemoteException("El nodo al que querias acceder se cayo");
			
		} catch (ConnectException e){
			
			LOGGER.info("Se detecta la caida de un nodo");
			System.out.println("Se detecta la caida de un nodo");
			
			/*mando mensaje de desconexion*/
			this.myClusterAdministration.disconnectFromGroup(nodeId);
			
			throw new RemoteException("El nodo al que querias acceder se cayo.");
		}

		return stub;
	}

	@Override
	public ClusterCommunication getGroupCommunication() throws RemoteException {
		return myClusterCommunication;
	}

	@Override
	public ClusterAdministration getClusterAdmimnistration()
			throws RemoteException {
		return myClusterAdministration;
	}

	@Override
	public Transactionable getNodeCommunication() throws RemoteException {
		return this.myTransactionable;
	}

	@Override
	public SimulationCommunication getSimulationCommunication()
			throws RemoteException {
		return mySimulationCommunication;
	}

	@Override
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
		return this.myThreePhaseCommit;
	}

	public String test(String string) {
		return (myNode.getNodeId())+string;
		
	}
	
	public String getNodeId(){
		return myNode.getNodeId();
	}
	
	

	

}
