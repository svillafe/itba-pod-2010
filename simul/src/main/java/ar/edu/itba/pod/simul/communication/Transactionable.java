package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

/**
 * Medium used for all the necessary point to point communications.
 */
public interface Transactionable extends Remote {

	/**
	 * A transaction context is created between two nodes. Changes can between two nodes can only be done in a
	 * transaction context. One node can only be in one transaction.
	 * <p>
	 * If one node that is in a transaction context tries to create a new one, an IllegalStateException is thrown. If
	 * one that is in a transaction receives another request, the sender node is blocked until a transaction context can
	 * be created or is ended by time out and an IllegalStateException is thrown.
	 * 
	 * @param remoteNodeId
	 *            The remote node to establish the transaction context
	 * @param timeOut
	 *            Time limit to wait for a connection
	 * @throws RemoteException
	 */
	public void beginTransaction(String remoteNodeId, long timeout) throws RemoteException;

	/**
	 * Request to create a transaction context to a remote node.
	 * <p>
	 * When a node tries to create a transaction with another node, it must verify that the remote node can accept the
	 * connection and is not been used in another transaction. This method is used to check this conditions. As the
	 * remote node may already be in a transaction context, a certain amount of time is used to wait if the remote node
	 * ends the transaction. In case that the node is still blocked by another transaction, an IllegalStateException is
	 * thrown. No guaranty of the order in which the transactions request are queued is done. If a node is blocked and
	 * two different nodes make a request, when the node is free, any of the two previous node can be selected to create
	 * a new transaction context.
	 * 
	 * @param remoteNodeId
	 *            The remote node to establish the connection
	 * @throws RemoteException
	 */
	public void acceptTransaction(String remoteNodeId) throws RemoteException;

	/**
	 * Finalize a transaction context. All the changes done in the context are persisted, and the two nodes are free for
	 * creating new transaction context.
	 * <p>
	 * If no transaction context exists, an exception is thrown
	 * 
	 * @throws RemoteException
	 */
	public void endTransaction() throws RemoteException;

	/**
	 * Exchanges a certain amount of a resource between two nodes.
	 * <p>
	 * This method can only be called in a transaction context, otherwise an IllegalStateException is thrown. This
	 * method can only be called once in a transaction context, otherwise, an IllegalStateException is thrown. The
	 * remote node and the destination node must be different. If not, an IllegalStateException is thrown. The remote
	 * node and the destination node must be the same nodes of the transaction context, otherwise an
	 * IllegalStateException is thrown. Note that the exchange is not executed but is enqueued. Only when the
	 * transaction context is closed, the changes are applied.
	 * 
	 * @param resource
	 *            The resource being exchanged between the nodes
	 * @param amount
	 *            Positive number of amount being exchanged
	 * @param sourceNode
	 *            The source node of the resource
	 * @param destinationNode
	 *            The destination node of the resource
	 */
	public void exchange(Resource resource, int amount, String sourceNode, String destinationNode) throws RemoteException;

	/**
	 * Creates and returns the payload for the exchange messages.
	 * <p>
	 * This method can only be called in a transaction context, otherwise an IllegalStateException is thrown. This
	 * method can only be called after an exchange is done, otherwise an IllegalStateException is thrown.
	 * 
	 * @return The payload
	 * @throws RemoteException
	 */
	public Payload getPayload() throws RemoteException;

	/**
	 * Reverts all the changes pending in the transaction context. If this method is invoked outside a transaction
	 * context, an IllegalStateException is thrown. If an error occurs in the revert, an RuntimeException is thrown.
	 * Otherwise, it is guaranteed that the changes are reverted.
	 */
	public void rollback() throws RemoteException;

}
