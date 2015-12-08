package ar.edu.itba.pod.simul.simulation;

import java.io.Serializable;

/**
 * Agent state used for pausing and resuming agents
 * <p>
 * At least, it contains the agent's name, but agent subclasses are expected
 * to return specializations of this class that include aditional information.
 * </p>
 * <p>
 * Agent state is expected to be serializable, as it should provide a mean to continue
 * the agent processing later in time, o even in another JVM instance.
 * </p>
 */
public class AgentState implements Serializable {
	private final String name;

	public AgentState(Agent agent) {
		super();
		this.name = agent.name();
	}
	
	public String name() {
		return name;
	}
}
