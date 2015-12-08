/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

/**
 * Types of messages
 * @author POD
 *
 */
public enum MessageType {
	NODE_AGENTS_LOAD, 				// Node agents load info
	NODE_AGENTS_LOAD_REQUEST,		// Node agents load info request to all nodes
	NODE_MARKET_DATA,				// Node market data
	NODE_MARKET_DATA_REQUEST,		// Node market data request to all nodes
	DISCONNECT, 
	RESOURCE_TRANSFER, 				// Resource been transfer from one node to another node
	RESOURCE_TRANSFER_CANCELED,		// Resource transfer from one node to another node canceled
	RESOURCE_REQUEST,				// Resource request from one node to the cluster
	NEW_MESSAGE_REQUEST,			// New messages request from one node to another node
	NEW_MESSAGE_RESPONSE			// New messages response from one node to another node
}
