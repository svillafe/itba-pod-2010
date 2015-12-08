package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedMarket;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;

public class HandlerMarketDataPayload implements HandlerPayload {

	private static HandlerPayload instance;
	private Node myNode;
	
	@Override
	public void execute(Message m) {

		NodeMarketDataPayload a = (NodeMarketDataPayload) m.getPayload();
		
		DistributedMarket aux = (DistributedMarket) myNode.getMyMarketManager(
				myNode.getMyConnectionManager()).market();
		
		
		aux.sumMarketData(a.getMarketData().getHistory().getTransactionsPerSecond());
		
		
		

	}

	private HandlerMarketDataPayload(Node myNode) {
		super();
		this.myNode = myNode;
	}

	public static HandlerPayload getInstance(Node myNode) {
		if(instance==null){
			instance=new HandlerMarketDataPayload(myNode);
		}
		return instance;
	}
}
