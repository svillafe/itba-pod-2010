package ar.edu.itba.pod.simul.ui;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

public class FeedbackMarketManager extends FeedBackDecorator<MarketManager> implements MarketManager {
	
	public FeedbackMarketManager(FeedbackCallback callback, MarketManager delegate) {
		super(callback, Preconditions.checkNotNull(delegate));
	}

	@Override
	public void shutdown() {
		feedback("Closing market ...");
		delegate().shutdown();
		feedback("Market closed");
	}

	@Override
	public void start() {
		feedback("Opening market ...");
		delegate().start();
		feedback("Market opened");
	}
	
	@Override
	public Market market() {
		return new FeedbackMarket(callback(), delegate().market());
	}
	
	@Override
	public MarketInspector inspector() {
		return delegate().inspector();
	}
}
