package ar.edu.itba.pod.simul.market;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.thread.doc.ThreadSafe;


/**
 * Medium used to trade resources. Used by production units to aquire and sell
 * resources.
 * The marketplace can impose constraints on the actual operations, such as time
 * based constraints (more resources require more time), or even trade conditions
 * (i.e.: max market stock for a resource).
 */
@ThreadSafe
public interface Market {
	/**
	 * Places a request to buy certain amount of resources, and add them to the given stock
	 * <p>
	 * Note that if there is another request for the same stock, it will be overwritten.
	 * If maxQuantity == 0, then the request is removed form the market
	 * </p>
	 * <p>
	 * When resources are available, the stock will be notified about the transaction.
	 * Note that there is no guarantee that the transaction will involve the quantity
	 * requested. If the transaction involves less resources, the request will be 
	 * updated. Once all the requested resources are delivered, the request will be removed
	 * </p>
	 * @param stock The stock where to operate
	 * @param maxQuantity Maximum quantity to buy
	 * @return 
	 */
	public void request(ResourceStock stock, int maxQuantity);

	/**
	 * Request additional resources. If there was no previous request one is created.
	 * Else, the number of requiered resources is increased
	 * @param stock The stock where to operate
	 * @param amount amount of new resources to require
	 * @see #request(ResourceStock, int)
	 */
	public void requestMore(ResourceStock stock, int amount);
	
	/**
	 * Places a request to sell certain amount of resources, and remove them from the given 
	 * stock
	 * <p>
	 * Note that if there is another request for the same stock, it will be overwritten.
	 * if maxQuantity == 0, then the request is removed from the market
	 * </p>
	 * <p>
	 * The stock will be notified about the transaction when possible.
	 * Note that there is no guarantee that the transaction will involve the quantity
	 * requested. If the transaction involves less resources, the request will be 
	 * updated. Once all the requested resources are delivered, the request will be removed
	 * </p>
	 * @param stock the resource stock
	 * @param maxQuantity The maximum quantity to sell
	 * @return
	 */
	public void offer(ResourceStock stock, int maxQuantity);
	
	/**
	 * Sends additional resources to the market. 
	 * If there was no previous request one is created. Else, the number of offered resources is increased
	 * @param stock The stock where to operate
	 * @param amount amount of new resources to offer
	 * @see #offer(ResourceStock, int)
	 */
	public void offerMore(ResourceStock stock, int amount);
	
	
	/**
	 * @return the market data. It includes transfer history, transactions
	 * per second and current snapshot of selling and buying resources in stock.
	 */
	MarketData marketData();
}
