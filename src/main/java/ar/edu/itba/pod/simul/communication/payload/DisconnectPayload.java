/**
 * 
 */
package ar.edu.itba.pod.simul.communication.payload;


/**
 * Payload for node disconnection messages.
 * This payload is sent in the messages to inform that a node has been disconnected from the cluster.
 * Disconnection messages are sent in two cases:
 * 1. A node is being removed and before shutting down, it informs to the cluster.
 * 2. A node has crashed and another node finds out.
 * 
 * This message has to be send to the entire cluster.
 */
public interface DisconnectPayload extends Payload {
	
	/**
	 * The node that has been disconnected.
	 * The identification can be the same of the node that sends the original message
	 * in the case that it is being shut down. Otherwise, the node that sends the message
	 * and the node been disconnected are different
	 * 
	 * @return the disconnected nodeId 
	 */
	String getDisconnectedNodeId();
}
