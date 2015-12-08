package ar.edu.itba.pod.simul.local;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.TransferHistory;
import ar.edu.itba.pod.simul.communication.TransferHistoryItem;
import ar.edu.itba.pod.simul.market.BidInfo;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceDemand;
import ar.edu.itba.pod.simul.market.ResourceStock;
import ar.edu.itba.pod.thread.CleanableThread;
import ar.edu.itba.pod.thread.doc.ThreadSafe;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

/**
 * Market implementation using a local thread.
 * This market implementaiton uses a thread to look for possible matches, and
 * join buyers and sellers
 */
@ThreadSafe
public class LocalMarket extends CleanableThread implements Market, MarketInspector  {
	protected final Multiset<ResourceStock> selling = ConcurrentHashMultiset.create();
	protected final Multiset<ResourceStock> buying = ConcurrentHashMultiset.create();
	private final List<TransferHistoryItem> history = Lists.newArrayList();
	private volatile int transactionCount;
	private Date startTime;
	@Override
	public Collection<ResourceDemand> managedResources() {
		Multiset<Resource> needed= HashMultiset.create();
		Multiset<Resource> offered= HashMultiset.create();
		Set<Resource> resources = Sets.newHashSet();

		for (Multiset.Entry<ResourceStock> entry : selling.entrySet()) {
			offered.add(entry.getElement().resource(), entry.getCount());
			resources.add(entry.getElement().resource());
		}
		for (Multiset.Entry<ResourceStock> entry : buying.entrySet()) {
			needed.add(entry.getElement().resource(), entry.getCount());
			resources.add(entry.getElement().resource());
		}

		Set<ResourceDemand> result = Sets.newHashSet();
		for (Resource res : resources) {
			ResourceDemand demand = ResourceDemand.on(res)
												  .needed(needed.count(res))
												  .offered(offered.count(res))
												  .build();
			result.add(demand);
		}
		return result;
	}
	
	@Override
	public Collection<BidInfo> bidsFor(Resource resource) {
		List<BidInfo> info = Lists.newArrayList();

		for (Multiset.Entry<ResourceStock> entry : selling.entrySet()) {
			info.add(BidInfo.forSell(entry.getElement().name(), entry.getElement().resource(), entry.getCount()));
		}
		for (Multiset.Entry<ResourceStock> entry : buying.entrySet()) {
			info.add(BidInfo.forBuy(entry.getElement().name(), entry.getElement().resource(), entry.getCount()));
		}
		return info;
	}
	
	@Override
	public int transactionCount() {
		return transactionCount;
	}
	
	@Override
	public int buyingCount() {
		return buying.elementSet().size();
	}
	
	@Override
	public int sellingCount() {
		return selling.elementSet().size();
	}

	@Override
	public void offer(ResourceStock stock, int maxQuantity) {
		selling.setCount(stock, maxQuantity);
		interrupt();
	}
	
	@Override
	public void offerMore(ResourceStock stock, int amount) {
		selling.add(stock, amount);
		interrupt();
	}
	
	@Override
	public void request(ResourceStock stock, int maxQuantity) {
		buying.setCount(stock, maxQuantity);
		interrupt();
	}
	
	@Override
	public void requestMore(ResourceStock stock, int amount) {
		buying.add(stock, amount);
		interrupt();
	}
	
	@Override
	public void run() {
		this.startTime = new Date();
		while(!shouldFinish()) {
			try {
				matchBothEnds();
				Thread.sleep(1000 * 60);
			} catch (InterruptedException e) {
				// either shutdown, or new contacts offered
			}
		}
	}

	/**
	 * Attempts to match buyer and sellers. This implementations uses a very simple
	 * approach:
	 * For each buyer, all sellers of the resource are searched, and each of them either
	 * sells everything it has, or up to the amount needed. No attempt is done to 
	 * match the best buyer and seller and reduce the number of operations.
	 */
	protected void matchBothEnds() {
		for (ResourceStock buyer : buying) {
			for (ResourceStock seller : selling) {
				if (buyer.resource().equals(seller.resource())) {
					transfer(buyer, seller);
				}
			}
		}
	}

	/**
	 * Transfer resources between two stocks.
	 * This method uses compensation to recover from race conditions instead of
	 * locking to avoid them altogether. Arguably, this scheme provides better
	 * concurrency performance. 
	 * @param buyer
	 * @param seller
	 * @return the amount transfered
	 */
	protected int transfer(ResourceStock buyer, ResourceStock seller) {
		while(true) {
			int wanted = buying.count(buyer);
			int available = selling.count(seller);
			int transfer = Math.min(available, wanted);
			
			if (transfer == 0) {
				return 0;
			}
	
			boolean procured = selling.setCount(seller, available, available - transfer);
			if (procured) {
				boolean sent = buying.setCount(buyer, wanted, wanted - transfer);
				if (sent) {
					try {
						seller.remove(transfer);
					}
					catch (RuntimeException e) {
						selling.add(seller, transfer);
						buying.remove(buyer, transfer);
						continue;
					}
					try {
						buyer.add(transfer);
					}
					catch (RuntimeException e) {
						// market takes care of what was sold. 
						// TODO: To fully solve this case, 2PCommit or 3PC is required. Is it worth?   
						buying.remove(buyer, transfer);
						continue;
					}
					logTransfer(seller, buyer, transfer);
					
					return transfer;
				}
				else {
					// Compensation. restore what we took from the order!
					selling.add(seller, transfer);
				}
			}
			// Reaching here mean we hit a race condition. Try again.
		}
	}
	
	
	public void logTransfer(ResourceStock from, ResourceStock to, int amount) {
		transactionCount++;
		createhistory(from, to, amount);
		System.out.printf("SELL: from %s to %s --> %d of %s\n", from.name(), to.name(), amount, from.resource());
	}

	/**
	 * @param from source stock 
	 * @param to dest stock
	 * @param amount transfered
	 */
	protected void createhistory(ResourceStock from, ResourceStock to, int amount) {
		createHistory(from.name(), to.name(), from.resource(), amount);
	}
	
	/**
	 * @param from source stock 
	 * @param to dest stock
	 * @param amount transfered
	 */
	protected void createHistory(String fromName, String toName, Resource resource, int amount) {
		history.add(new TransferHistoryItem(fromName, toName, resource, amount));
	}
	
	/**
	 * @return the transfer history in this market
	 */
	private TransferHistory getHistory() {
		return new TransferHistory(history, getTransactionsPerSecond());
	}
	@Override
	public MarketData marketData() {
		final Multiset<Resource> buying = getResourceSet(this.buying);
		final Multiset<Resource> selling = getResourceSet(this.selling);
		final TransferHistory history = getHistory();
		return new MarketData(buying, selling, history);
	}
	/**
	 * @param resourceSet the resource to count
	 * @return
	 */
	private Multiset<Resource> getResourceSet(Multiset<ResourceStock> resourceSet) {
		final Multiset<Resource> resources = ConcurrentHashMultiset.create();
		for (ResourceStock stock : resourceSet) {
			resources.add(stock.resource(), resourceSet.count(stock));
		}
		return resources;
	}

	/**
	 * @return the average number of transactions per second
	 */
	private double getTransactionsPerSecond() {
		long time = System.currentTimeMillis() - startTime.getTime();
		return ((double)transactionCount())/time;
	}
}
