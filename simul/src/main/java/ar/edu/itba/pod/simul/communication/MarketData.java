/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

import java.io.Serializable;

import ar.edu.itba.pod.simul.market.Resource;

import com.google.common.collect.Multiset;

/**
 * Snapshot from a node market
 * 
 * @author POD
 * 
 */
public class MarketData implements Serializable {
	private Multiset<Resource> buying;
	private Multiset<Resource> selling;
	private TransferHistory history;

	/**
	 * @param buying
	 *            buying resources and amount
	 * @param selling
	 *            selling resources and amount
	 * @param history
	 *            transfer history
	 */
	public MarketData(Multiset<Resource> buying, Multiset<Resource> selling, TransferHistory history) {
		super();
		this.buying = buying;
		this.selling = selling;
		this.history = history;
	}

	/**
	 * @return the buying
	 */
	public Multiset<Resource> getBuying() {
		return buying;
	}

	/**
	 * @return the selling
	 */
	public Multiset<Resource> getSelling() {
		return selling;
	}

	/**
	 * @return the history
	 */
	public TransferHistory getHistory() {
		return history;
	}

}
