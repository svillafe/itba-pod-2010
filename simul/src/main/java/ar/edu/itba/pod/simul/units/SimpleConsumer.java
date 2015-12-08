package ar.edu.itba.pod.simul.units;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.thread.doc.ThreadSafe;

@ThreadSafe
public class SimpleConsumer extends MarketAgent implements ResourceStock {
	private final Resource resource;
	private final AtomicInteger amount = new AtomicInteger();
	
	private SimpleConsumer(SimpleConsumerBuilder builder) {
		super(builder);
		this.resource = builder.resource;
	}
	
	@Override
	public void run() {
		while(!shouldFinish()) {
			try {
				if (amount.get() == 0) {
					market().request(this, rate());
				}
				waitForResources();
				waitForWork();
			} catch (InterruptedException e) {
				// this should happen when finishing or when new sotck arrives
			}
		}
	}
	
	private void waitForResources() throws InterruptedException {
		while(amount.get() < rate()) {
			Thread.sleep(1000 * 60);
		}
	}
	
	@Override
	public Resource resource() {
		return resource;
	}
	
	@Override
	public void add(int size) {
		amount.getAndAdd(size);
		interrupt();
	}
	
	@Override
	public void remove(int size) {
		throw new AssertionError("simple consumer should never sell!");
	}
	
	@Override
	public int current() {
		return amount.get();
	}
	
	@Override
	public String name() {
		return super.name();
	}

	/** @see ar.edu.itba.pod.simul.simulation.Agent#getAgentDescriptor()*/
	@Override
	public AgentDescriptor getAgentDescriptor() {
		return new SimpleConsumerDescriptor(amount, (SimpleConsumerBuilder) getBuilder());
	}
	
	public static SimpleConsumerBuilder named(String name) {
		return new SimpleConsumerBuilder(name);
	}

	public static class SimpleConsumerBuilder extends MarketAgentBuilder {
		private Resource resource;

		private SimpleConsumerBuilder(String name) {
			super(name);
		}
		
		@Override
		public SimpleConsumerBuilder every(int workTime, TimeUnit workUnit) {
			super.every(workTime, workUnit);
			return this;
		}
		
		public SimpleConsumerBuilder consuming(int amount) {
			withRate(amount);
			return this;
		}
		
		public SimpleConsumerBuilder of(Resource resource) {
			this.resource = resource;
			return this;
		}

		public SimpleConsumer build() {
			return new SimpleConsumer(this);
		}
	}
	
	public static class SimpleConsumerDescriptor implements AgentDescriptor {
		private AtomicInteger amount;
		private SimpleConsumerBuilder builder;
		
		/**
		 * 
		 */
		public SimpleConsumerDescriptor(AtomicInteger amount, SimpleConsumerBuilder builder) {
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
		public SimpleConsumerBuilder getBuilder() {
			return builder;
		}

		/** @see ar.edu.itba.pod.simul.communication.AgentDescriptor#build()*/
		@Override
		public Agent build() {
			final SimpleConsumer consumer = new SimpleConsumer(this.builder);
			consumer.amount.set(amount.get());
			return consumer;
		}
		
	}
}
