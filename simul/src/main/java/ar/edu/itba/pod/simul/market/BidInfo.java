package ar.edu.itba.pod.simul.market;

import ar.edu.itba.pod.thread.doc.Immutable;

/**
 * Information about a bid in the market
 */
@Immutable
public class BidInfo {
	private final String source;
	private final Resource resource;
	private final int amount;
	private final boolean buying;

	public BidInfo(String source, Resource resource, int amount, boolean buying) {
		super();
		this.source = source;
		this.resource = resource;
		this.amount = amount;
		this.buying = buying;
	}
	
	public static BidInfo forBuy(String source, Resource resource, int amount) {
		return new BidInfo(source, resource, amount, true);
	}
	public static BidInfo forSell(String source, Resource resource, int amount) {
		return new BidInfo(source, resource, amount, false);
	}

	/**
	 * Return the bid's source, identifying the origin of the bid
	 * @return
	 */
	public String source() {
		return source;
	}

	/**
	 * Returns the resource being traded
	 * @return
	 */
	public Resource resource() {
		return resource;
	}

	/**
	 * Returns the number of units of the resource being traded
	 * @return
	 */
	public int amount() {
		return amount;
	}

	/**
	 * Returns true if the bid is for buying the resource
	 * @return
	 */
	public boolean isBuying() {
		return buying;
	}

	/**
	 * Returns true if the bid is for selling the resource
	 * @return
	 */
	public boolean isSelling() {
		return !buying;
	}
}
