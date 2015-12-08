/**
 * 
 */
package ar.edu.itba.pod.simul.communication.payload;

import ar.edu.itba.pod.simul.communication.MarketData;


/**
 * Message sent by nodes with their market transfer data
 * @author POD
 *
 */
public interface NodeMarketDataPayload extends Payload {

	/**
	 * @return the market data in a node
	 */
	MarketData getMarketData();

}