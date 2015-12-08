/**
 * 
 */
package ar.edu.itba.pod.simul.communication.payload;

import ar.edu.itba.pod.simul.market.Resource;


/**
 * Payload for node transfer of resources messages.
 * 
 * This message has not to be send to the entire cluster.
 * It must be guaranteed that the source node and the destination node are different.
 */
public interface ResourceTransferMessagePayload extends Payload {

	/**
	 * Returns the resource been transfered. The return value is never null.
	 * @return Resource been transfered.
	 */
	public Resource getResource();
	
	/**
	 * @return Amount been transfered
	 */
	public int getAmount();
	
	/**
	 * Returns the identification of the node that is sending the resource.
	 * The return value is never null.
	 * @return The node identification
	 */
	public String getSource();
	
	/**
	 * Returns the identification of the node that is receiving the resource.
	 * The return value is never null.
	 * @return The node identification
	 */
	public String getDestination();
}
