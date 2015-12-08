package ar.edu.itba.pod.Legajo47399.simul.distributed;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationEvent;
import ar.edu.itba.pod.simul.simulation.SimulationEventHandler;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.time.TimeMapper;

import com.google.common.base.Preconditions;

public class DistributedSimulation implements Simulation, SimulationInspector {

	private final TimeMapper timeMapper;
	private DistributedSimulationManager mySM;
	private final List<SimulationEventHandler> handlers = new CopyOnWriteArrayList<SimulationEventHandler>();

	public DistributedSimulation(TimeMapper timeMapper,
			DistributedSimulationManager mySM) {
		super();
		this.timeMapper = timeMapper;
		this.mySM = mySM;

	}

	@Override
	public int runningAgents() {
		return mySM.getAgents().size();
	}

	@Override
	public void wait(int amount, TimeUnit unit) throws InterruptedException {
		long millis = timeMapper.toMillis(amount, unit);
		Thread.sleep(millis);
	}

	@Override
	public void add(SimulationEventHandler handler) {
		Preconditions.checkArgument(!handlers.contains(handler),
				"Can't add a handler twice!");
		handlers.add(handler);

	}

	@Override
	public void remove(SimulationEventHandler handler) {
		Preconditions.checkArgument(handlers.contains(handler),
				"Handler not registered!");
		handlers.remove(handler);

	}

	@Override
	public void raise(SimulationEvent event) {
		for (SimulationEventHandler handler : handlers) {
			handler.onEvent(event);
		}

	}

	@Override
	public <T> T env(Class<T> param) {
		return (T) (mySM.getEnv(param));
	}

}
