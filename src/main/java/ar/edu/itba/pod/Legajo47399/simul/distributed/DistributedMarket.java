package ar.edu.itba.pod.Legajo47399.simul.distributed;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.TransferHistory;
import ar.edu.itba.pod.simul.local.LocalMarket;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class DistributedMarket extends LocalMarket {

	private final static Logger LOGGER = Logger
			.getLogger(DistributedMarket.class);

	/* Lo que me mandan de afuera */
	protected final Multiset<Resource> externalRepository = ConcurrentHashMultiset
			.create();
	private DistributedMarketManager myMarketManager;

	private Double  outTranfers;

	public DistributedMarket(DistributedMarketManager distributedMarketManager) {
		super();
		this.myMarketManager = distributedMarketManager;
	}
	
	/**
	 * Attempts to match buyer and sellers. This implementations uses a very
	 * simple approach: For each buyer, all sellers of the resource are
	 * searched, and each of them either sells everything it has, or up to the
	 * amount needed. No attempt is done to match the best buyer and seller and
	 * reduce the number of operations.
	 */
	@Override
	protected void matchBothEnds() {
		LOGGER.info("ACA ENTRO UNO");
		for (ResourceStock buyer : buying) {
			LOGGER.info("ACA ENTRO DOS");
			for (ResourceStock seller : selling) {
				if (buyer.resource().equals(seller.resource())) {
					// LOGGER.info("Se buscan:"+buying.count(buyer)+"Tengo en seller:"+selling.count(seller));
					LOGGER.info(buyer.name()+" Encontre algo en la cola interna");
					Integer resp = transfer(buyer, seller);
					if (resp != 0) {
						LOGGER.info("ENCONTRADO EN SELLING, SE TRANSFIRIERON:"
								+ buyer.name() + "<==" + resp + " de "
								+ buyer.resource() + "<==" + seller.name());
					}
				}
			}
			if (buying.count(buyer) != 0) {
				/* Me fijo si lo tenia de una transaccion anterior */
				for (Resource outResource : externalRepository) {
					if (buyer.resource().equals(outResource)) {
						LOGGER.info(buyer.name()+"ENCONTRADO EN LA COLA EXTERNA.");
						Integer resp = transferResource(buyer, outResource);
						if (resp != 0) {
							LOGGER.info("SE TRANSFIRIERON:" + buyer.name()
									+ "<====" + resp + " de "
									+ buyer.resource());
						}
					}
				}

				/*
				 * Debo pedir afuera mi recurso, el handler de la respuesta es
				 * el encargado de cargarmelo
				 */
				
				if (buying.count(buyer) != 0) {
					LOGGER.info(buyer.name()+" SOLICITUD RECURSOS:" + buying.count(buyer)+" DE "+buyer.resource());
					this.myMarketManager.requestResources(buying.count(buyer),
							buyer.resource());

				}
			}
		}
	}

	private Integer transferResource(ResourceStock buyer, Resource outResource) {
		while (true) {
			int wanted = buying.count(buyer);
			int available = externalRepository.count(outResource);
			int transfer = Math.min(available, wanted);

			if (transfer == 0) {
				return 0;
			}

			boolean procured = externalRepository.setCount(outResource,
					available, available - transfer);
			if (procured) {
				boolean sent = buying
						.setCount(buyer, wanted, wanted - transfer);
				if (sent) {
					try {
						buyer.add(transfer);
					} catch (RuntimeException e) {
						buying.remove(buyer, transfer);
						continue;
					}
					myLogTransfer(buyer, transfer,outResource);
					return transfer;
				} else {
		
					externalRepository.add(outResource, transfer);
				}
			}
		}

	}

	@Override
	public MarketData marketData() {
		MarketData miResp = super.marketData();
		this.outTranfers=(double) 0;
		this.myMarketManager.marketDatarequest();
		
		LOGGER.info("Esperando que me respondan....");
		try {
			Thread.sleep(Node.STAT_WAIT);
		} catch (InterruptedException e) {
			LOGGER.info("Me despertaron");
		}
		
		return new MarketData(miResp.getBuying(), miResp.getSelling()
				,new TransferHistory(miResp.getHistory().getHistoryItems() ,miResp.getHistory(
						).getTransactionsPerSecond( )+ outTranfers));
		
	}
	
	
	private void myLogTransfer(ResourceStock buyer, int transfer,
			Resource outResource) {
		transactionCount++;
		
	}

	/**
	 * Transfer resources between two stocks. This method uses compensation to
	 * recover from race conditions instead of locking to avoid them altogether.
	 * Arguably, this scheme provides better concurrency performance.
	 * 
	 * @param buyer
	 * @param seller
	 * @return the amount transfered
	 */
	@Override
	protected int transfer(ResourceStock buyer, ResourceStock seller) {
		while (true) {
			int wanted = buying.count(buyer);
			int available = selling.count(seller);
			int transfer = Math.min(available, wanted);

			if (transfer == 0) {
				return 0;
			}

			boolean procured = selling.setCount(seller, available, available
					- transfer);
			if (procured) {
				boolean sent = buying
						.setCount(buyer, wanted, wanted - transfer);
				if (sent) {
					try {
						seller.remove(transfer);
					} catch (RuntimeException e) {
						selling.add(seller, transfer);
						buying.remove(buyer, transfer);
						continue;
					}
					try {
						buyer.add(transfer);
					} catch (RuntimeException e) {
						// market takes care of what was sold.
						// To fully solve this case, 2PCommit or 3PC is
						// required. Is it worth?
						buying.remove(buyer, transfer);
						continue;
					}
					logTransfer(seller, buyer, transfer);

					return transfer;
				} else {
					// Compensation. restore what we took from the order!
					selling.add(seller, transfer);
				}
			}
			// Reaching here mean we hit a race condition. Try again.
		}
	}
	
	

	public Multiset<ResourceStock> getSelling() {
		return this.selling;
	}

	public Multiset<ResourceStock> getBuying() {
		return this.buying;
	}

	public Multiset<Resource> getExternalRepository() {
		return this.externalRepository;
	}

	public MarketData fatherMarketData() {
		return super.marketData();
	}

	public void sumMarketData(double transactionsPerSecond) {

		synchronized (outTranfers) {
			outTranfers+=transactionsPerSecond;
		}
		
	}

}
