package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageListener extends Remote {

	/**
	 * Method invoked when a new message arrives in the node.
	 * <p>
	 * Each message must be processed in a different way, depending on the payload.
	 * 
	 * @param message
	 *            The message
	 * @return true if the message is new for the node
	 */
	public boolean onMessageArrive(Message message) throws RemoteException;

	/**
	 * Returns all the new messages from the node that were not synchronized with the remote node. Only fowardable
	 * messages are sent.
	 * 
	 * @param remoteNodeId
	 *            The remote node that asks for new messages
	 * @return
	 */
	public Iterable<Message> getNewMessages(String remoteNodeId) throws RemoteException;
}
