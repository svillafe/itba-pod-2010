package ar.edu.itba.pod.simul.simulation;

/**
 * Simulation inspector that provides information about the simulation mechanics.
 * <p>
 * Note that this inspector provides engine level information, and not simulation
 * specific info. 
 * </p>
 */
public interface SimulationInspector {

	/**
	 * Returns the number of agents running in the simulation
	 * @return
	 */
	public int runningAgents();
}
