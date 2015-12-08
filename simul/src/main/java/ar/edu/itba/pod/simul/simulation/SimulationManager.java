package ar.edu.itba.pod.simul.simulation;

import java.util.Collection;


/**
 * Simulation manager.
 * <p>
 * Provides methods for managing the configuration and lifecycle of a simulation
 * </p>
 */
public interface SimulationManager {
	/**
	 * Start the simulation
	 */
	public void start();
	
	
	/**
	 * Shutdown the simulation. Waits till the last agent finishes running 
	 * before returning
	 * @throws IllegalStateException if the simulaiton is not running
	 */
	public void shutdown();
	
	/**
	 * Adds an agent to the simulation. The simulation may or may be not running
	 * @param agent Agent to add
	 */
	public void addAgent(Agent agent);

	/**
	 * Removes an agent from the simulation. This method will return when the 
	 * agent has finished execution and has been successfullt removed 
	 * @param agent Agent to remove
	 */
	public void removeAgent(Agent agent);
	
	/**
	 * Returns the simulation instance that agents of the simulation will use
	 * @return The simulation instance to use
	 */
	public Simulation simulation();
	
	/**
	 * Returns an inspector that can provide further information and statistics 
	 * about the simulation 
	 * @return The simulation inspector
	 */
	public SimulationInspector inspector();

	/**
	 * Registers an object instance in the simulation environment
	 * <p>
	 * Objects in the environment can be recalled by agents when needed.
	 * This provides a layer that allows agents to integrate with shared environments
	 * provided by the simulation
	 * </p>
	 * <p>
	 * This way, agents do not need to depend upon creation or need to store and 
	 * locate shared environment objects when resuming operation.
	 * </p>
	 * @param type The type use to register the object 
	 * @param instance The object instance to store in the environment
	 */
	public <T> void register(Class<T> type, T instance);
	
	
	/**
	 * Returns the collection of running agents
	 * @return the running agents
	 */
	public Collection<Agent> getAgents();
}
