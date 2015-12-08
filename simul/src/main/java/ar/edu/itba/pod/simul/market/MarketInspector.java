package ar.edu.itba.pod.simul.market;

import java.util.Collection;

/**
 * Market inspector allows gathering information about market operations
 * taking place 
 */
public interface MarketInspector {
	/**
	 * Returns the current resources being traded at the market.
	 * <p>
	 * This method is not required to (but may) return resources that had previously
	 * been traded in the market, but have no outstanding bids
	 * </p>
	 */
	public Collection<ResourceDemand> managedResources();

	/**
	 * Returns the list of outstanding bids for a given resource
	 * @param resource Resource to look for
	 * @return A list of outstanding bids
	 */
	public Collection<BidInfo> bidsFor(Resource resource);
	
	/**
	 * Returns the number of commited transactions
	 * @return
	 */
	public int transactionCount();
	
	/**
	 * Returns the number of selling bids in the market
	 * @return
	 */
	public int sellingCount();
	
	/**
	 * Returns the number of buying bids on the market
	 * @return
	 */
	public int buyingCount();
}
