package ar.edu.itba.pod.simul.units;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.simulation.Agent;

public abstract class MarketAgent extends Agent {
	private final int rate;
	private MarketAgentBuilder builder;
	
	private final int workTime;
	private final TimeUnit workUnit;

	public MarketAgent(MarketAgentBuilder builder) {
		super(builder.name);
		this.builder = builder;
		this.rate = builder.rate;
		this.workTime = builder.workTime;
		this.workUnit = builder.workUnit;
	}
	
	/**
	 * @return the builder
	 */
	protected final MarketAgentBuilder getBuilder() {
		return builder;
	}

	public final void waitForWork() throws InterruptedException {
		waitFor(workTime, workUnit);
	}
	
	protected final int rate() {
		return rate;
	}
	
	protected final Market market() {
		return env(Market.class);
	}

	/**
	 * Base class builder for MarketAgents. Child classes should use this class
	 * to carry the desired values
	 */
	static class MarketAgentBuilder implements Serializable {
		private String name;
		
		private int rate;
		private int workTime;
		private TimeUnit workUnit;

		protected MarketAgentBuilder(String name) {
			super();
			this.name = name;
		}
		
		protected MarketAgentBuilder withRate(int rate) {
			this.rate = rate;
			return this;
		}

		protected MarketAgentBuilder every(int workTime, TimeUnit workUnit) {
			this.workTime = workTime;
			this.workUnit = workUnit;
			return this;
		}
	}
}
