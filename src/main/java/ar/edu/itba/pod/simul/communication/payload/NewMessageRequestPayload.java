package ar.edu.itba.pod.simul.communication.payload;

/**
 * Payload for new messages request.
 * This payload is sent in the messages to ask for new messages to the destination node.
 * The destination node must only send the new fowardable messages received since the last synchronization.
 * If no new messages exists, no reply is sent.
 * If no previous synchronization exists, all the available messages must be sent.
 * 
 * This message has not to be send to the entire cluster.
 */
public interface NewMessageRequestPayload extends Payload {

	
}
