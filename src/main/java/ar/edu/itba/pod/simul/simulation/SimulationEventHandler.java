package ar.edu.itba.pod.simul.simulation;

/**
 * Interface used by classes that react to simulation events
 */
public interface SimulationEventHandler {
	/**
	 * Reacts on a given simulation event
	 * @param event The event that is being pushed o the client
	 */
	public void onEvent(SimulationEvent event);
}
