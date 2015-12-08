package ar.edu.itba.pod.Legajo47399.simul.comunication.payload;

import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;

public class NodeAgentLoadPayloadImpl implements NodeAgentLoadPayload {

	private NodeAgentLoad myNodeAgentLoad;
	
	public NodeAgentLoadPayloadImpl(NodeAgentLoad nal){
		this.myNodeAgentLoad=nal;
	}
	
	
	@Override
	public int getLoad() {
		return myNodeAgentLoad.getNumberOfAgents();
		
	}

}
