package ar.edu.itba.pod.simul.simulation;

import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.thread.CleanableThread;

/**
 * Base agent used for simulations.
 * Provides simple common simulation methods, as well as processing of common
 * events
 */
public abstract class Agent extends CleanableThread implements SimulationEventHandler {
	private final FinishEventHandler finishHandler = new FinishEventHandler(this);
	private final String name;
	
	private Simulation simulation;

	/**
	 * Creates a new agent
	 * @param name
	 */
	public Agent(String name) {
		super();
		this.name = name;
	}
	
	/**
	 * Creates a new agent with a previously saved state
	 * @param state The starting state. Actual semantic depends on the agent implementation
	 */
	public Agent(AgentState state) {
		super();
		this.name = state.name();
	}
	
	/**
	 * Callback method that is called when receiving an event
	 * <p>
	 * Note that this method is usually called from a different thread than the
	 * one the agent is running, so care has to be taken to ensure that the class
	 * remains threadsafe
	 * </p>
	 * <p>
	 * Standard events, such as simulation shutdown, are not passed to this method 
	 * </p>
	 * @param event The event
	 */
	@Override
	public void onEvent(SimulationEvent event) {
		return;
	}
	
	/**
	 * Method called when the agent is bound to a simulation
	 * <p>
	 * Overrides to this method should call <code>super.onBind()</code> to ensure
	 * proper registration
	 * </p>
	 * @param simulation Simulation where the agent should be bound
	 */
	public void onBind(Simulation simulation) {
		if (this.isAlive()) {
			throw new IllegalStateException("Can't bind to a simulation when agent is running!");
		}
		this.simulation = simulation;
		simulation.add(finishHandler);
	}
	
	/**
	 * Notify the agent that it should finish and start a clean shutdown
	 */
	@Override
	public void finish() {
		simulation.remove(finishHandler);
		super.finish();
	}
	
	/**
	 * Retrieve an object from the simulation environment
	 * @param param The type of the envirment object
	 * @return The object or {@link IllegalStateException} if no object is registered
	 */
	protected <T> T env(Class<T> param) {
		return simulation.env(param);
	}
	
	/**
	 * Finishes the agent, but returns a state that allows to instantiate a new instance of the agent later in time
	 * <p>
	 * This method and the equivalent agent constructor allow to pause a simulation and later continue, or just pause an
	 * agent, for example, to move it to another location.
	 * </p> 
	 * <p>
	 * Note that pausing an agent actually makes it's thread to finish. Thus there is no resume method. A new agent should be 
	 * created with the saved state and started (after binding it to a simulation) to resume operation.
	 * </p>
	 * @return The cuirrent agent's state
	 */
	public AgentState pause() {
		throw new UnsupportedOperationException();
	}
	
	public String name() {
		return name;
	}

	protected final void waitFor(int amount, TimeUnit unit) throws InterruptedException {
		simulation.wait(amount, unit);
	}

	/**
	 * Agent main method.
	 * <p>
	 * This method should run during the whole agent's lifetime. returning from this
	 * method efectively finishes the running agent.
	 * <p>
	 * Implementations should periodically check the <code>shouldFinish()</code> method to ensure proper
	 * clean shutdown. Also, note that finishing the agent will interrupt the thread.
	 * </p>
	 * @see java.lang.Thread#run()
	 */
	@Override
	public abstract void run();
	
	
	/**
	 * @return a descriptor for this agent. With this descriptor it is possible
	 * to recreate the agent in the current state anywhere else.
	 */
	public abstract AgentDescriptor getAgentDescriptor();
	
	/**
	 * Private event handler so that Agent is not required to implement {@link SimulationEventHandler}
	 * <p>
	 * This handler takes care of notifying
	 * </p> 
	 * @author pablo
	 */
	private static class FinishEventHandler implements SimulationEventHandler {
		private final Agent agent;

		public FinishEventHandler(Agent agent) {
			super();
			this.agent = agent;
		}

		@Override
		public void onEvent(SimulationEvent event) {
			if (event instanceof EndSimulationEvent) {
				agent.finish();
			}
		}
	}
}
