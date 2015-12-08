package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.DisconnectPayLoadImpl;
import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedSimulationManager;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

public class ClusterAdministrationImpl implements ClusterAdministration {

	private final static Logger LOGGER = Logger
			.getLogger(ClusterAdministrationImpl.class);

	private Node myNode;

	private String groupId = null;

	public ClusterAdministrationImpl(Node myNode) throws RemoteException {
		super();
		this.myNode = myNode;

		UnicastRemoteObject.exportObject(this, 0);

	}

	@Override
	public void createGroup() throws RemoteException {

		if (groupId != null) {
			throw new IllegalStateException("A group already exists");
		}

		try {
			groupId = "Cluster:" + InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		LOGGER.info(myNode.getNodeId() + ": Group created:" + groupId);

	}

	@Override
	public String getGroupId() throws RemoteException {
		return groupId;
	}

	@Override
	public boolean isConnectedToGroup() throws RemoteException {
		return groupId != null;
	}

	@Override
	public void connectToGroup(String initialNode) throws RemoteException {
		Set<String> myNodes;

		LOGGER.info("El nodo que me pasan:" + initialNode);
		if (isConnectedToGroup()) {
			throw new IllegalStateException(
					"The node is already connected to a cluster.");
		}
		if (myNode.getNodeId().equals(initialNode)) {
			throw new IllegalStateException(
					"The initialNode can not be equal to the  new node.");
		}
		myNodes = myNode.getClusterNodes();

		ClusterAdministration remoteCA = myNode.getMyConnectionManager()
				.getConnectionManager(initialNode).getClusterAdmimnistration();

		
		this.groupId = remoteCA.getGroupId();

		Collection<String> resp = null;
		try {
			resp = (Collection<String>) remoteCA.addNewNode(myNode.getNodeId());
		} catch (Exception e) {
			LOGGER.info("Falla de conexion");
			System.out.println("No se pudo conectar");
			return;
		}
		/* Completo mi lista con los nodos que pasaron */
		synchronized (myNodes) {
			
			System.out.println("Los nodos son:"+myNodes);
			System.out.println("Resp vale"+resp);
			if(resp==null){
				System.out.println("resp es null");
			}
			myNodes.addAll(resp);
		}
		//LOGGER.info(myNode.getNodeId() + ": La lista que me pasaron es:" + resp
		//		+ "--Fin de lista--");
		/* Borro mi nodo del set de nodos del cluster que me paso el nodo B */
		myNodes.remove(myNode.getNodeId());
		/* Agrego al otro nodo */
		synchronized (myNodes) {
			myNodes.add(initialNode);
		}
		/* Guardo el id del grupo */
		this.groupId = remoteCA.getGroupId();
		//LOGGER.info(myNode.getNodeId()
		//		+ ": El groupId obtenido de forma remota es:" + this.groupId);

	}

	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {

		LOGGER.info(myNode.getNodeId()
				+ ": Entrando a add new node method. Parametro:" + newNode);

		Set<String> myNodes;
		if (this.groupId == null) {
			throw new IllegalStateException(
					"This node is not connected to any cluster.");
		}

	
		ClusterAdministration remoteCA = myNode.getMyConnectionManager()
				.getConnectionManager(newNode).getClusterAdmimnistration();
		if (!this.groupId.equals(remoteCA.getGroupId())) {
			throw new IllegalArgumentException(
					"The destination group must be the same to the target group ");
		}

		myNodes = myNode.getClusterNodes();

		LOGGER.info(myNode.getNodeId() + ": Pregunto si tengo el nodo:"
				+ newNode);

		if (!myNodes.contains(newNode)) {

			/*
			 * Agrego al nodo que me invoco a mi lista de nodos pertenecientes
			 * al cluster, si ya lo tenia no pasa porque es un cluster.
			 */
			LOGGER.info(myNode.getNodeId() + ": Agrego a:" + newNode
					+ "a mi lista de nodos");
			myNodes.add(newNode);

			/*
			 * Le aviso a Node.CANTNODES nodos del cluster que se conecto un
			 * nuevo nodo al mismo. Seleccionos CantNodos de forma randon
			 * excluyendo el nodo que se agrego al cluster.
			 */
			Iterable<String> selectedNodes = myNode.getOtherNRandomNodes(
					Node.CANT_NODES, newNode);

			LOGGER.info(myNode.getNodeId() + ":Nodos Seleccionados:"
					+ selectedNodes);

			for (String s : selectedNodes) {
				LOGGER.info(myNode.getNodeId() + ": Le aviso al nodo:" + s);
				ConnectionManager remoteCM = myNode.getMyConnectionManager()
						.getConnectionManager(s);
				remoteCM.getClusterAdmimnistration().addNewNode(newNode);
				LOGGER.info(myNode.getNodeId() + ": Ya le avise al nodo:" + s);
			}
		}

		/* Devuelvo mi lista de nodos */
		return myNodes;

	}

	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {

		if (this.groupId == null) {
			throw new IllegalArgumentException(
					"The node can't disconnect beacuse there not cluster create.");
		}

		if (nodeId.equals(myNode.getNodeId())) {
			
			/* Si yo me voy tengo que balancear la carga */
						
			// Balancear la carga.

			((DistributedSimulationManager) this.myNode
					.getMySimulationManager()).balancearClusterSalida();
			
			
			LOGGER.info("Le aviso a los demas");
			// Les aviso a los demas que ya no estoy
			try {
				broadcastDisconect(nodeId);
			} catch (RemoteException e) {
				LOGGER.info("Fallo el broadcast");
			}

			LOGGER.info("Borro mi lista de vecino");
			/* Borro mi lista de vecinos */
			myNode.cleanClusterNodes();

			/*Apago todos mis threads*/
			((MessageListenerImpl)(myNode.getMyConnectionManager().getGroupCommunication().getListener())).stopThreads();
		
			/* Ya no pertenezco a ningun grupo */
			if (nodeId.equals(myNode.getNodeId())) {
				this.groupId = null;
			}
	
			//Apago mi simulacion y mi market
			
			SimulationManager sim = myNode.getMySimulationManager(null, null);
			LOGGER.info("Apago la simulacion...");
			sim.shutdown();
			
			MarketManager marketManager= myNode.getMyMarketManager(null);
			LOGGER.info("Apago el marketManager...");
			marketManager.shutdown();
			LOGGER.info("Todo Apagado.");
			
			System.exit(0);
			

		} else {

			/*
			 * Descubro que se va otro nodo, lo saco de mi lista de vecinos y
			 * envio el mensaje broadcast. si no lo tenia no reenvio por que
			 * significa que yo ya me habia dado cuenta de antes.
			 */
			//System.out.println("Se fue otro nodo");
			Boolean resp = myNode.removeNode(nodeId);

			/* Propago la voz */
			if (resp) {
				LOGGER.info("Lo tenia entonces lo propago.");
				broadcastDisconect(nodeId);
			}

		}
	}

	private void broadcastDisconect(String nodeId) throws RemoteException {

		myNode.getMyConnectionManager()
				.getGroupCommunication()
				.broadcast(
						new Message(myNode.getNodeId(), System
								.currentTimeMillis(), MessageType.DISCONNECT,
								new DisconnectPayLoadImpl(nodeId)));

	}

}
