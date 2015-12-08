package ar.edu.itba.pod.Legajo47399.simul.comunication.payload;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;

public class NodeMarketDataPayloadImpl implements NodeMarketDataPayload {

	private MarketData myMarketData;
		
	public NodeMarketDataPayloadImpl(MarketData myMarketData) {
		super();
		this.myMarketData = myMarketData;
	}


	@Override
	public MarketData getMarketData() {

		return null;
	}

}
