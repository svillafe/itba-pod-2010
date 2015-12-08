package ar.edu.itba.pod.Legajo47399.simul.comunication.payload;

import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;

public class DisconnectPayLoadImpl implements DisconnectPayload {

	String nodeId;
	
	
	
	public DisconnectPayLoadImpl(String nodeId) {
		super();
		this.nodeId = nodeId;
	}


	@Override
	public String getDisconnectedNodeId() {
		return nodeId;
	}

}
