package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedSimulationManager;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;

public class HandlerNodeAgentLoadPayload implements HandlerPayload {

	private final static Logger LOGGER = Logger
			.getLogger(HandlerNodeAgentLoadPayload.class);

	private static HandlerPayload instance;
	private Node myNode;

	private HandlerNodeAgentLoadPayload(Node myNode) {
		super();
		this.myNode = myNode;
	}

	@Override
	public void execute(Message m) {

		LOGGER.info("----------------Recibo un mensaje con carga:"
				+ ((NodeAgentLoadPayload) (m.getPayload())).getLoad());
		NodeAgentLoad newAgentNodeInfo = new NodeAgentLoad(m.getNodeId(),
				((NodeAgentLoadPayload) (m.getPayload())).getLoad());
		Map<String, NodeAgentLoad> myNodeInfo = ((DistributedSimulationManager) myNode
				.getMySimulationManager()).getTheAgentLoads();

		synchronized (myNodeInfo) {
			myNodeInfo.put(m.getNodeId(), newAgentNodeInfo);
		}
		LOGGER.info("Fin del handler del mensaje");

	}

	public static HandlerPayload getInstance(Node myNode) {
		if (instance == null) {
			instance = new HandlerNodeAgentLoadPayload(myNode);
		}
		return instance;
	}

}
