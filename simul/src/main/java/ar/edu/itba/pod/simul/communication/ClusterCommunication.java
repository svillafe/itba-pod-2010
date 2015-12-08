package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Medium used for all the necessary communications for the cluster. Each node must have one implementation of this
 * interface in order to communicate with the rest of the nodes in the cluster.
 */
public interface ClusterCommunication extends Remote {

	/**
	 * A broadcast message is created and the nodes are informed of the message. The sender node sends the message to N
	 * random nodes. In each iteration, the node is informed if this message is new or not. This information is used to
	 * determine whether to continue or not with the broadcast.
	 * 
	 * @param message
	 *            The message to send to the cluster
	 */
	public void broadcast(Message message) throws RemoteException;

	/**
	 * A point to point communication is established between the two nodes and the message is sent. In case that the
	 * destination node has already received the message, false is returned. Otherwise, true is returned. The receiver
	 * must determine if the new messages has to be send in broadcast to the rest of the nodes or not.
	 * 
	 * @param message
	 *            The message to send to the cluster
	 * @param node
	 *            The destination node
	 * @return True if it is a new message
	 */
	public boolean send(Message message, String nodeId) throws RemoteException;

	/**
	 * @return The message listener of the current node.
	 * @throws RemoteException
	 */
	public MessageListener getListener() throws RemoteException;
}
