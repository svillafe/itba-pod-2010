/**
 * 
 */
package ar.edu.itba.pod.simul.communication.payload;


/**
 * Message sent by nodes with their agent load
 * @author POD
 *
 */
public interface NodeAgentLoadPayload extends Payload {

	/**
	 * @return the number of agents running in the node
	 */
	int getLoad();

}