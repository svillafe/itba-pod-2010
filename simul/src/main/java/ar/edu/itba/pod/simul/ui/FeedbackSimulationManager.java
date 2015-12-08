package ar.edu.itba.pod.simul.ui;

import java.util.Collection;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

/**
 * Simulation Manager decorator that provides feedback on the simulation setup and teardown
 */
public class FeedbackSimulationManager extends FeedBackDecorator<SimulationManager> implements SimulationManager {

	public FeedbackSimulationManager(FeedbackCallback callback, SimulationManager delegate) {
		super(callback, Preconditions.checkNotNull(delegate));
	}

	public void addAgent(Agent agent) {
		feedback("Adding agent.");
		delegate().addAgent(agent);
	}

	public void removeAgent(Agent agent) {
		feedback("Removing agent.");
		delegate().removeAgent(agent);
	}

	@Override
	public void start() {
		feedback("Starting simulation...");
		delegate().start();
		feedback("Simulation started");
	}
	
	public void shutdown() {
		feedback("Ending simulation...");
		delegate().shutdown();
		feedback("Simulation ended");
	}
	
	@Override
	public <T> void register(Class<T> type, T instance) {
		feedback("Registering type %s", type.getSimpleName());
		delegate().register(type, instance);
	}
	
	@Override
	public Simulation simulation() {
		return delegate().simulation();
	}

	@Override
	public SimulationInspector inspector() {
		return delegate().inspector();
	}

	@Override
	public Collection<Agent> getAgents() {
		return delegate().getAgents();
	}
}
