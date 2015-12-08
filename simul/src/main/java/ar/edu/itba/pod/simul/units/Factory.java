package ar.edu.itba.pod.simul.units;

import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;
import ar.edu.itba.pod.thread.doc.ThreadSafe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Production unit that transforms a set of resources into others.
 * It models a factory, where raw materials are used to produce goods
 */
@ThreadSafe
public class Factory extends MarketAgent {
	private final Multiset<Resource> requirements;
	private final Multiset<Resource> production;
	
	private Multiset<Resource> rawResources = ConcurrentHashMultiset.create();
	private Multiset<Resource> refinedResources = ConcurrentHashMultiset.create();
	
	private Factory(FactoryBuilder builder) {
		super(builder);
		requirements = builder.requirements;
		production = builder.production;
	}

	@Override
	public void run() {
		while(!shouldFinish()) {
			try {
				getResources();
				waitForResources();
				waitForWork();
				produceAndSell();
			} catch (InterruptedException e) {
				// this should happen when finishing or when new sotck arrives
			}
		}
	}
	
	private void getResources()  {
		for (Resource res : requirements.elementSet()) {
			if (rawResources.count(res) == 0) {
				market().request(new FactoryRawStock(res), requirements.count(res));
			}
		}
	}
	
	private void waitForResources() throws InterruptedException {
		while(!canBuild()) {
			try {
				Thread.sleep(1000 * 60);
			}
			catch(InterruptedException e) {
				if (shouldFinish()) {
					throw e;
				}
			}
		}
		return;
	}

	
	private void produceAndSell() {
		for (Resource res : requirements.elementSet()) {
			rawResources.remove(res, requirements.count(res));
		}
		
		for (Resource res : production.elementSet()) {
			refinedResources.add(res, production.count(res));
			market().offerMore(new FactoryRefinedStock(res), production.count(res));
		}
	}
	
	private boolean canBuild() {
		for (Resource res : requirements.elementSet()) {
			if (rawResources.count(res) < requirements.count(res)) {
				return false;
			}
		}
		return true;
	}

	/** @see ar.edu.itba.pod.simul.simulation.Agent#getAgentDescriptor()*/
	@Override
	public AgentDescriptor getAgentDescriptor() {
		return new FactoryDescriptor(rawResources, refinedResources, (FactoryBuilder) getBuilder());
	}
	
	public static FactoryBuilder named(String name) {
		return new FactoryBuilder(name);
	}
	
	public static class FactoryBuilder extends MarketAgentBuilder {
		private Multiset<Resource> requirements = HashMultiset.create();
		private Multiset<Resource> production = HashMultiset.create();
		
		private Boolean lastAddedWasRefined;
		
		private FactoryBuilder(String name) {
			super(name);
		}
		
		public FactoryBuilder and(int amount, Resource res) {
			Preconditions.checkArgument(lastAddedWasRefined != null, "Can't call and() before producing() or using()");
			if (lastAddedWasRefined.booleanValue()) {
				production.add(res, amount);
			}
			else {
				requirements.add(res, amount);
			}
			return this;
		}
		
		public FactoryBuilder producing(int amount, Resource res) {
			lastAddedWasRefined = Boolean.TRUE;
			and(amount, res);
			return this;
		}
		
		public FactoryBuilder using(int amount, Resource res) {
			lastAddedWasRefined = Boolean.FALSE;
			and(amount, res);
			return this;
		}
		
		@Override
		public FactoryBuilder every(int workTime, TimeUnit workUnit) {
			super.every(workTime, workUnit);
			return this;
		}
		
		public Factory build() {
			return new Factory(this);
		}
	}
	
	private class FactoryRawStock implements ResourceStock {
		private final Resource resource;

		public FactoryRawStock(Resource resource) {
			super();
			this.resource = resource;
		}
		
		@Override
		public Resource resource() {
			return resource;
		}
		
		@Override
		public int current() {
			return rawResources.count(resource);
		}
		
		@Override
		public void add(int size) {
			rawResources.add(resource, size);
			Factory.this.interrupt();
		}
		
		@Override
		public void remove(int size) {
			throw new AssertionError("Raw stock is never sold");
		}
		
		@Override
		public String name() {
			return Factory.this.name();
		}
	}
	
	private class FactoryRefinedStock implements ResourceStock {
		private final Resource resource;

		public FactoryRefinedStock(Resource resource) {
			super();
			this.resource = resource;
		}
		
		@Override
		public Resource resource() {
			return resource;
		}
		
		@Override
		public int current() {
			return refinedResources.count(resource);
		}
		
		@Override
		public void add(int size) {
			throw new AssertionError("Products are never bought");
		}
		
		@Override
		public void remove(int size) {
			refinedResources.remove(resource, size);
		}
		
		@Override
		public String name() {
			return Factory.this.name();
		}
	}
	
	
	public static class FactoryDescriptor implements AgentDescriptor {
		private Multiset<Resource> rawResources;
		private Multiset<Resource> refinedResources;
		private FactoryBuilder builder;
		
		private FactoryDescriptor(Multiset<Resource> rawResources,
				Multiset<Resource> refinedResources, FactoryBuilder builder) {
			this.builder = builder;
			this.rawResources = rawResources;
			this.refinedResources = refinedResources;
		}
		
		
		/**
		 * @return the rawResources
		 */
		public Multiset<Resource> getRawResources() {
			return rawResources;
		}


		/**
		 * @return the refinedResources
		 */
		public Multiset<Resource> getRefinedResources() {
			return refinedResources;
		}


		public Factory build() {
			final Factory factory = new Factory(builder);
			factory.rawResources.addAll(this.rawResources);
			factory.refinedResources.addAll(refinedResources);
			return factory;
		}
	}

}
