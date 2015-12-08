package ar.edu.itba.pod.simul.units;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.thread.doc.ThreadSafe;

import com.google.common.base.Preconditions;

/**
 * Agent that operates on a marketplace producing and selling a unique resource
 * <p>
 * The following code shows how to create this agent:
 * </p>
 * <pre>
 * 	 SimpleProducer prod = SimpleProducer.on(simul)
 *                                       .producing(5)
 *                                       .of(resource)
 *                                       .each(4, TimeUnit.Hours)
 *                                       .tradingOn(marketplace)
 *                                       .build();
 * </pre>
 * <p>
 * As a result of the code shown above, a producer agent will be created, and when
 * running it will create 5 units of a resource each 4 hours, and send them to the
 * marketplace.
 * </p>
 */
@ThreadSafe
public class SimpleProducer extends MarketAgent implements ResourceStock {
	private final Resource resource;
	
	private final AtomicInteger amount = new AtomicInteger();

	private SimpleProducer(SimpleProducerBuilder builder) {
		super(builder);
		this.resource = Preconditions.checkNotNull(builder.resource);
	}

	@Override
	public void run() {
		while(!shouldFinish()) {
			try {
				waitForWork();
				amount.getAndAdd(rate());
				market().offerMore(this, rate());
			} catch (InterruptedException e) {
				// this should happen when finishing
			}
		}
	}
	
	@Override
	public Resource resource() {
		return resource;
	}
	
	@Override
	public void add(int size) {
		throw new AssertionError("simple producer should never buy!");
	}
	
	@Override
	public void remove(int size) {
		int newSize = amount.addAndGet(-size);
		if (newSize < 0) {
			amount.addAndGet(size);
			throw new IllegalArgumentException("Attempted to remove more items than available in this stock!");
		}
	}
	
	@Override
	public int current() {
		return amount.get();
	}
	

	/** @see ar.edu.itba.pod.simul.simulation.Agent#getAgentDescriptor()*/
	@Override
	public AgentDescriptor getAgentDescriptor() {
		return new SimpleProducerDescriptor(amount, (SimpleProducerBuilder) getBuilder());
	}
	
	public static SimpleProducerBuilder named(String name) {
		return new SimpleProducerBuilder(name);
	}

	public static class SimpleProducerBuilder extends MarketAgentBuilder {
		private Resource resource;
		
		private SimpleProducerBuilder(String name) {
			super(name);
		}
		
		public SimpleProducerBuilder of(Resource resource) {
			this.resource = resource;
			return this;
		}
		
		public SimpleProducerBuilder producing(int amount) {
			super.withRate(amount);
			return this;
		}

		@Override
		public SimpleProducerBuilder every(int workTime, TimeUnit workUnit) {
			super.every(workTime, workUnit);
			return this;
		}
		
		public SimpleProducer build() {
			return new SimpleProducer(this);
		}
	}
	
	public static class SimpleProducerDescriptor implements AgentDescriptor {
		private final AtomicInteger amount;
		private final SimpleProducerBuilder builder;
		/**
		 * 
		 */
		public SimpleProducerDescriptor(AtomicInteger amount, SimpleProducerBuilder builder) {
			this.builder = builder;
			this.amount = amount;
		}
		/**
		 * @return the amount
		 */
		public AtomicInteger getAmount() {
			return amount;
		}
		/**
		 * @return the builder
		 */
		public SimpleProducerBuilder getBuilder() {
			return builder;
		}
		/** @see ar.edu.itba.pod.simul.communication.AgentDescriptor#build()*/
		@Override
		public Agent build() {
			final SimpleProducer producer = new SimpleProducer(builder);
			producer.amount.set(this.amount.get());
			return producer;
		}
		
		
	}

}
