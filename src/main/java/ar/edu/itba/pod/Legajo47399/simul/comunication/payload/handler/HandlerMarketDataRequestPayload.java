package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedMarket;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;
import ar.edu.itba.pod.simul.market.NodeMarketDataPayloadImpl;

public class HandlerMarketDataRequestPayload implements HandlerPayload {
	

	private final static Logger LOGGER = Logger
			.getLogger(HandlerMarketDataRequestPayload.class);

	private static HandlerPayload instance;
	private Node myNode;

	private HandlerMarketDataRequestPayload(Node myNode) {
		super();
		this.myNode = myNode;
	}

	@Override
	public void execute(Message m) {
		String quienEnvia = m.getNodeId();
		// Armo el marketData
		
		
		MarketData aux =((DistributedMarket)(myNode
				.getMyMarketManager(this.myNode.getMyConnectionManager())
				.market())).fatherMarketData();
		
		
		NodeMarketDataPayload resp = new NodeMarketDataPayloadImpl(aux);

		// Envio el payload armado
		try {
			this.myNode
					.getMyConnectionManager()
					.getGroupCommunication()
					.send(new Message(myNode.getNodeId(),
							System.currentTimeMillis(),
							MessageType.NODE_MARKET_DATA, resp), quienEnvia);
		} catch (RemoteException e) {
			LOGGER.info("Falla el send");
			//Falla el send
			return;
		}

	}

	public static HandlerPayload getInstance(Node myNode) {
		if (instance == null) {
			instance = new HandlerMarketDataRequestPayload(myNode);
		}
		return instance;
	}

}
