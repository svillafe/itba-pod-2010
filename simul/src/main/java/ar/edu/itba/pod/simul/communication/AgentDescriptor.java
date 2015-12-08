/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

import java.io.Serializable;

import ar.edu.itba.pod.simul.simulation.Agent;

/**
 * Describes an Agent
 */
public interface AgentDescriptor extends Serializable {
	/**
	 * @return the agent built based on the data on this descriptor
	 */
	public Agent build();
}
