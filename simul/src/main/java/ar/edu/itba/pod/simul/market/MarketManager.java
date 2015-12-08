package ar.edu.itba.pod.simul.market;


/**
 * Interface for managing the market
 */
public interface MarketManager {
	/**
	 * Starts market operations
	 */
	public void start();
	
	/**
	 * Finishes ongoing opeerations and shuts down the market
	 */
	public void shutdown();
	
	
	/**
	 * Returns a market instance ready to be used. The instance returned may 
	 * or may not be reused, buy it's lifecycle will be managed by this manager
	 * <p>
	 * Note that even on the case where this method returns different objects
	 * on each call, those objects represent one logical market.
	 * </p>
	 * @return a market instance managed by this object
	 */
	public Market market();
	
	
	/**
	 * Returns an inspector capable of retrieving market information
	 * @return an inspector for the market managed by this object
	 */
	public MarketInspector inspector();
}
