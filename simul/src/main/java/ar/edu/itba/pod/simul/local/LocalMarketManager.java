package ar.edu.itba.pod.simul.local;

import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

import com.google.common.base.Preconditions;

public class LocalMarketManager implements MarketManager {
	private LocalMarket market;
	
	@Override
	public void start() {
		market = new LocalMarket();
		market.start();
	}
	
	@Override
	public void shutdown() {
		market.finish();
	}
	
	@Override
	public LocalMarket market() {
		Preconditions.checkState(market != null, "There is no active market to be retrieved");
		return market;
	}
	
	@Override
	public MarketInspector inspector() {
		return market();
	}
}
