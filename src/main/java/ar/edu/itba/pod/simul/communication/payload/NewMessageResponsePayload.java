package ar.edu.itba.pod.simul.communication.payload;

import ar.edu.itba.pod.simul.communication.Message;

/**
 * Payload for new messages response.
 * This payload is sent in the messages to respond for new messages to the destination node.
 * 
 * This message has not to be send to the entire cluster.
 */
public interface NewMessageResponsePayload extends Payload {

	/**
	 * TODO
	 * @return
	 */
	public Iterable<Message> getMessages();
}
