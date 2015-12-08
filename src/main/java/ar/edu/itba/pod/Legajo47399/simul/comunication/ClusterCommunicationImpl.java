package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class ClusterCommunicationImpl implements ClusterCommunication {

	private Node myNode;
	private MessageListener myMessageListener;
	private final static Logger LOGGER = Logger
			.getLogger(ClusterCommunicationImpl.class);

	public ClusterCommunicationImpl(Node myNode) throws RemoteException {
		super();
		this.myNode = myNode;
		this.myMessageListener = new MessageListenerImpl(myNode);
		UnicastRemoteObject.exportObject(this, 0);
	}

	@Override
	public void broadcast(Message message) throws RemoteException {

		if (myNode.getMyConnectionManager().getClusterAdmimnistration()
				.getGroupId() == null) {
			throw new IllegalStateException(
					"You must be connected to a cluster before sending a broadcast message.");
		}
		/*
		 * Gossiping Implementation (al creador del mensaje no le mando el
		 * mensaje)
		 */
		List<String> selectedNodes = myNode.getOtherNRandomNodes(
				Node.CANT_NODES, message.getNodeId());
		boolean salir = false;

		for (int i = 0; i < selectedNodes.size() && !salir; i++) {

			if (send(message, selectedNodes.get(i)) == false) {
				salir = true;
			}
		}

	}

	@Override
	public boolean send(Message message, String nodeId) throws RemoteException {
		if (myNode.getMyConnectionManager().getClusterAdmimnistration()
				.getGroupId() == null) {
			throw new IllegalStateException(
					"You must be connected to a cluster before sending a message.");
		}
		//LOGGER.info(myNode.getNodeId() + ": Entro a send del mensaje");

		return myNode.getMyConnectionManager().getConnectionManager(nodeId)
				.getGroupCommunication().getListener().onMessageArrive(message);
	}

	@Override
	public MessageListener getListener() throws RemoteException {

		return myMessageListener;
	}

}
