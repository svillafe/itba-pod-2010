package ar.edu.itba.pod.simul.communication.payload;

import ar.edu.itba.pod.simul.market.Resource;

/**
 * Payload for request of resource messages.
 * This payload is sent in the message to inform that a certain node requires a certain resource.
 * The requesting node must inform the resource and the amount needed.
 * 
 * This message has to be send to the entire cluster.
 */
public interface ResourceRequestPayload extends Payload {

	/**
	 * Returns the resource been requested by the node. The return value is never null.
	 * @return Resource been requested
	 */
	public Resource getResource();
	
	/**
	 * @return Amount been requested
	 */
	public int getAmountRequested();
}
