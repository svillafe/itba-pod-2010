package ar.edu.itba.pod.simul.local;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationEvent;
import ar.edu.itba.pod.simul.simulation.SimulationEventHandler;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.time.TimeMapper;
import ar.edu.itba.pod.thread.doc.ThreadSafe;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Local implementation of a simulation.
 * This implementation uses a different threrad for each agent
 */
@ThreadSafe
class LocalSimulation implements Simulation, SimulationInspector {
	private final List<Agent> agents = new CopyOnWriteArrayList<Agent>();
	private final List<SimulationEventHandler> handlers =  new CopyOnWriteArrayList<SimulationEventHandler>();
	private final Map<Class<?>, Object> env = Maps.newHashMap();
	private final TimeMapper timeMapper;

	public LocalSimulation(TimeMapper timeMapper) {
		super();
		this.timeMapper = timeMapper;
	}

	@Override
	public void add(SimulationEventHandler handler) {
		Preconditions.checkArgument(!handlers.contains(handler), "Can't add a handler twice!");
		handlers.add(handler);
	}
	
	@Override
	public void remove(SimulationEventHandler handler) {
		Preconditions.checkArgument(handlers.contains(handler), "Handler not registered!");
		handlers.remove(handler);
	}
	
	@Override
	public void raise(SimulationEvent event) {
		for(SimulationEventHandler handler : handlers) {
			handler.onEvent(event);
		}
	}
	
	@Override
	public void wait(int amount, TimeUnit unit) throws InterruptedException {
		long millis = timeMapper.toMillis(amount, unit);
		Thread.sleep(millis);
	}
	
	public <T> void register(Class<T> type, T instance) {
		env.put(type, instance);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T env(Class<T> param) {
		return (T) env.get(param);
	}

	public void addAgent(Agent agent) {
		Preconditions.checkArgument(!agent.isAlive(), "Can't add an agent that is already started!");
		Preconditions.checkArgument(!agents.contains(agent), "Can't add an agent twice!");
		
		agents.add(agent);
		agent.onBind(this);
	}

	public void removeAgent(Agent agent) {
		Preconditions.checkArgument(agents.contains(agent), "The agent is not part of this simulation!");
		try {
			synchronized (agent) {
				agent.finish();
				agent.wait();
			}
		}
		catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted while removing an agent!");
		}
		agents.remove(agent);
	}

	/**
	 * @return the agents
	 */
	public List<Agent> getAgents() {
		return agents;
	}
	
	@Override
	public int runningAgents() {
		return agents.size();
	}

	public void start() {
		for (Agent agent : agents) {
			agent.start();
		}
	}
	
	/**
	 * Shuts down the simulation by asking agents to stop running.
	 * Note that this method doesn't send any event to the agents
	 */
	public void shutdown() {
		boolean interrupted = false;
		
		for (Agent agent : agents) {
			agent.finish();
		}
		for (Agent agent : agents) {
			try {
				agent.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			throw new IllegalStateException("Interrupted when shutting down agents!");
		}
	}

}
