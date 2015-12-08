package ar.edu.itba.pod.Legajo47399.simul.comunication.payload;

import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceTransferPayloadImpl implements
		ResourceTransferMessagePayload {


	Resource resource;
	int amount;
	String source;
	String destination;
	
	
	

	public ResourceTransferPayloadImpl(Resource resource, int amount,
			String source, String destination) {
		super();
		this.resource = resource;
		this.amount = amount;
		this.source = source;
		this.destination = destination;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getDestination() {
		return destination;
	}
}
