package ar.edu.itba.pod.simul.market;

import ar.edu.itba.pod.thread.doc.Immutable;

/**
 *
 */
@Immutable
public class ResourceDemand {
	private final Resource resource;
	private final int needed;
	private final int offered;

	private ResourceDemand(ResourceDemandBuilder builder) {
		super();
		this.resource = builder.resource;
		this.needed = builder.needed;
		this.offered = builder.offered;
	}
	
	public Resource resource() {
		return resource;
	}

	public int needed() {
		return needed;
	}

	public int offered() {
		return offered;
	}
	
	public int net() {
		return offered - needed;
	}

	public static ResourceDemandBuilder on(Resource resource) {
		return new ResourceDemandBuilder(resource);
	}
	
	public static class ResourceDemandBuilder {
		Resource resource;
		int needed;
		int offered;

		private ResourceDemandBuilder(Resource resource) {
			super();
			this.resource = resource;
		}
		
		public ResourceDemandBuilder needed(int needed) {
			this.needed = needed;
			return this;
		}

		public ResourceDemandBuilder offered(int offered) {
			this.offered = offered;
			return this;
		}
		
		public ResourceDemand build() {
			return new ResourceDemand(this);
		}
	}
}
