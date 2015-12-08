package ar.edu.itba.pod.Legajo47399.simul.comunication.payload;

import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceRequestPayloadImpl implements ResourceRequestPayload {

	Integer amount;
	Resource resource;
	
	
	public ResourceRequestPayloadImpl(Integer amount, Resource resource) {
		this.amount =amount;
		this.resource=resource;
	}

	
	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public int getAmountRequested() {
		return amount;
	}

}
