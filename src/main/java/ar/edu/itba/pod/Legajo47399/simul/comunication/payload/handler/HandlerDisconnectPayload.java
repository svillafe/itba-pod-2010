package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;

public class HandlerDisconnectPayload implements HandlerPayload {
	private final static Logger LOGGER = Logger
			.getLogger(HandlerDisconnectPayload.class);

	private static HandlerPayload instance;
	private Node myNode;

	@Override
	public void execute(Message m) {
		String nodeId = ((DisconnectPayload) m.getPayload())
				.getDisconnectedNodeId();
		LOGGER.info("Handler del Disconect se murio el nodo:"+nodeId+"+++++++++++++++++++++++++++. Me aviso:"+m.getNodeId());
		myNode.removeNode(nodeId);

	}

	private HandlerDisconnectPayload(Node myNode) {
		super();
		this.myNode = myNode;
	}

	public static HandlerPayload getInstance(Node myNode) {
		if (instance == null) {
			instance = new HandlerDisconnectPayload(myNode);
		}
		return instance;
	}

}
