package ar.edu.itba.pod.Legajo47399.simul.distributed;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.ConnectionManagerImpl;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.NodeMarketDataRequestPayloadImpl;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.ResourceRequestPayloadImpl;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;

import com.google.common.base.Preconditions;

public class DistributedMarketManager implements MarketManager {

	private final static Logger LOGGER = Logger
	.getLogger(DistributedMarket.class);
	
	private ConnectionManager myConnectionManager;
	private DistributedMarket market;

	public DistributedMarketManager(ConnectionManager mgr) {
		super();
		this.myConnectionManager = mgr;
	}

	@Override
	public void start() {
		market = new DistributedMarket(this);
		market.start();

	}

	@Override
	public void shutdown() {
		market.finish();

	}

	@Override
	public Market market() {
		Preconditions.checkState(market != null,
		"There is no active market to be retrieved");
		return market;
	}

	@Override
	public MarketInspector inspector() {
		return (DistributedMarket)market();
	}

	public void requestResources(Integer amount,Resource resource) {
		try {
			//LOGGER.info("ENVIO DE REQUEST DE: CANTIDAD:"+amount+" RECURSO:"+resource.name());
			this.myConnectionManager.getGroupCommunication().broadcast(
					new Message(((ConnectionManagerImpl) this.myConnectionManager)
							.getNodeId(), System.currentTimeMillis(),
							MessageType.RESOURCE_REQUEST,
							new ResourceRequestPayloadImpl(amount, resource)));
		} catch (RemoteException e) {
			LOGGER.info("Fallo el broadcast");
			return;
		} catch (IllegalStateException e){
			LOGGER.info("Fallo el broadcast");
			return;
		}

	}

	public void marketDatarequest() {
		
		try {
			LOGGER.info("ENVIO DE REQUEST DE MARKET DATA");
			this.myConnectionManager.getGroupCommunication().broadcast(
					new Message(((ConnectionManagerImpl) this.myConnectionManager)
							.getNodeId(), System.currentTimeMillis(),
							MessageType.NODE_MARKET_DATA_REQUEST,
							new  NodeMarketDataRequestPayloadImpl()));
		} catch (RemoteException e) {
			LOGGER.info("Fallo le broadcast");
			return;
		} catch (IllegalStateException e){
			LOGGER.info("Fallo el broadcast");
			return;
		}

	
		
	}

}