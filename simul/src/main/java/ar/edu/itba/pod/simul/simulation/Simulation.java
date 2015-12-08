package ar.edu.itba.pod.simul.simulation;

import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.thread.doc.ThreadSafe;

/**
 * Interface providing common methods for running a simulation based on parallel 
 * execution of different agents
 */
@ThreadSafe
public interface Simulation {
	/**
	 * Waits a given amount of time. This method takes care of simulation speed-up,
	 * so it is the preferred way to let a running unit wait.
	 * <p>
	 * It is encouraged to use real time intervals when calling this method, as then
	 * the simulation could be brought up to speed smoothly or slowed as needed.
	 * </p>
	 * @param amount amount of units 
	 * @param unit Time Unit
	 * @throws IllegalArgumentException if amount is negative
	 * @throws InterruptedException if there is a notification
	 */
	public void wait(int amount, TimeUnit unit) throws InterruptedException;
	
	/**
	 * Adds a simulation event handler.
	 * The handler will receive events about the simulation. Of special interest is the
	 * END_SIMULATION event, which signals that the simulation is finishing.
	 * @param handler The handler that should receive the event
	 */
	public void add(SimulationEventHandler handler);

	public void remove(SimulationEventHandler handler);

	/**
	 * Raises an event and sends it to all registered listeners.
	 * <p>No guarantee is made on whether the event will be sent sinchronously
	 * or asynchronously</p>
	 * @param event The event to be raised
	 */
	public void raise(SimulationEvent event);
	
	/**
	 * Retrieves an object from the simulation environment
	 * <p>
	 * This method allows a simulation to provide to it's agents with shared
	 * environment objects
	 * </p>
	 * @param param The class of the environment object to retrieve
	 * @return The environment object
	 * @throws IllegalStateException if the object is not found in the environment
	 */
	public <T> T env(Class<T> param);
}
