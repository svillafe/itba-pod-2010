package ar.edu.itba.pod.simul.ui;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.ResourceStock;

public class FeedbackMarket extends FeedBackDecorator<Market>  implements Market {
	
	public FeedbackMarket(FeedbackCallback callback, Market delegate) {
		super(callback, Preconditions.checkNotNull(delegate));
	}

	@Override
	public void offer(ResourceStock stock, int maxQuantity) {
		log(stock, maxQuantity, "OFFER");
		delegate().offer(stock, maxQuantity);
	}

	@Override
	public void offerMore(ResourceStock stock, int amount) {
		log(stock, amount, "OFFER MORE");
		delegate().offerMore(stock, amount);
	}

	@Override
	public void request(ResourceStock stock, int maxQuantity) {
		log(stock, maxQuantity, "REQUEST");
		delegate().request(stock, maxQuantity);
	}

	@Override
	public void requestMore(ResourceStock stock, int amount) {
		log(stock, amount, "REQUEST MORE");
		delegate().requestMore(stock, amount);
	}
	
	public void log(ResourceStock stock, int amount, String op) {
		feedback("%s --> %s: %d of %s (stock: %d)", stock.name(), op, amount, stock.resource(), stock.current());
	}

	@Override
	public MarketData marketData() {
		return delegate().marketData();
	}
}
