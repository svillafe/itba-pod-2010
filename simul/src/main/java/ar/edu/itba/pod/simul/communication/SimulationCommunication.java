/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Medium used for all the necessary communications related with the simulation.
 */
public interface SimulationCommunication extends Remote {

	/**
	 * Starts a new agent on this node.
	 * 
	 * @param descriptor
	 *            of the agent to start
	 */
	public void startAgent(AgentDescriptor descriptor) throws RemoteException;

	/**
	 * If this node is working as the load balancer coordinator, it returns the node with the slower number of running
	 * agents. Otherwise, it returns null. Note that if this method returns null, its because this node is not the
	 * coordinator, so the caller SHOULD be the new coordinator and broadcasts in order to know the agent load of every
	 * node.
	 */
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException;

	/**
	 * this method should be called to inform the coordinator of a change in the load of a node
	 * 
	 * @param newLoad
	 */
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException;

	/**
	 * This method is called for asking the receiving node, to release a number of agents so they can be started into
	 * another node. The main goal is to balance the agents load between nodes
	 * 
	 * @param numberOfAgents
	 * @return the released agents
	 */
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents) throws RemoteException;
}
