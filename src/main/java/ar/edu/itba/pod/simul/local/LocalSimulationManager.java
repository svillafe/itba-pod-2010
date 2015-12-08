package ar.edu.itba.pod.simul.local;

import java.util.Collection;

import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;
import ar.edu.itba.pod.thread.doc.NotThreadSafe;

import com.google.common.base.Preconditions;

/**
 * Simulation managerfor local thread-based simulation
 */
@NotThreadSafe
public class LocalSimulationManager implements SimulationManager {
	private final LocalSimulation simulation;
	private boolean started;
	
	public LocalSimulationManager(TimeMapper timeMapper) {
		super();
		simulation = new LocalSimulation(timeMapper);
	}
	
	@Override
	public void start() {
		simulation.start();
		started = true;
	}
	
	@Override
	public void shutdown() {
		simulation().shutdown();
		started = false;
	}
	
	@Override
	public void addAgent(Agent agent) {
		simulation.addAgent(agent);
	}
	@Override
	public Collection<Agent> getAgents() {
		return simulation.getAgents();
	}
	@Override
	public void removeAgent(Agent agent) {
		simulation.removeAgent(agent);
	}
	
	@Override
	public <T> void register(Class<T> type, T instance) {
		simulation.register(type, instance);
	}
	
	@Override
	public LocalSimulation simulation() {
		Preconditions.checkState(started, "No simulation has been started!");
		return simulation;
	}
	
	@Override
	public SimulationInspector inspector() {
		return simulation();
	}

}
