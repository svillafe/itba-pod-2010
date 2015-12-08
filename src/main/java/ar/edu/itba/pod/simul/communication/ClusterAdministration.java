package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Medium used for all the necessary communications for the cluster administration.
 */
public interface ClusterAdministration extends Remote {

	/**
	 * Creates a new group where the nodes can connect in order to build a cluster.
	 * <p>
	 * If a group already exists, an IllegalStateException is thrown.
	 */
	public void createGroup() throws RemoteException;

	/**
	 * Returns the name of the group that the node is connected to.
	 * <p>
	 * If the node is not connected, null is returned.
	 * 
	 * @return Unmodifiable group name.
	 */
	public String getGroupId() throws RemoteException;

	/**
	 * @return Whether a connection to a group is established.
	 */
	public boolean isConnectedToGroup() throws RemoteException;

	/**
	 * Connects a node to a group using the initial node as the entry point.
	 * <p>
	 * If the new node is already connected to a group, an IllegalStateException is thrown.
	 * <p>
	 * If the new node is the same to the initial node, an IllegalArgumentException is thrown.
	 * 
	 * @param initialNode
	 */
	public void connectToGroup(String initialNode) throws RemoteException;

	/**
	 * Adds the target node to the destination node. The destination node returns a collection of nodes that belongs to
	 * the cluster.
	 * <p>
	 * The size of the collection may not be the same of the number of nodes in the cluster or the number of nodes that
	 * the destination node knows. The destination node is not returned in the collection. The size of the collection
	 * can be zero. If the destination node is not connected to a cluster, an IllegalStateException is thrown. If the
	 * destination group is not the same to the target group, an IllegalArgumentException is thrown.
	 * 
	 * @param newNode
	 *            the target node
	 * @return Unmodifiable collection of nodes
	 */
	public Iterable<String> addNewNode(String newNode) throws RemoteException;

	/**
	 * A broadcast message is created informing the nodes of the cluster that the node sent by parameter has left the
	 * cluster.
	 * <p>
	 * The sender node sends the message to N random nodes. In each iteration, the node is informed if this message is
	 * new or not. This information is used to determine whether to continue or not with the broadcast. The sender node
	 * can be the same node that is been disconnected. If the node is already disconnected, an IllegalArgumentException
	 * is thrown.
	 */
	public void disconnectFromGroup(String nodeId) throws RemoteException;

}
