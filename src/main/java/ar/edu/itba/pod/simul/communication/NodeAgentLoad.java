/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

import java.io.Serializable;

/**
 * Represents the number of agents running on a node.
 */
public class NodeAgentLoad implements Serializable {
	private String nodeId;
	private int numberOfAgents;

	/**
	 * @param nodeId
	 *            the node Id
	 * @param numberOfAgents
	 *            the number of agents running on the node
	 */
	public NodeAgentLoad(final String nodeId, final int numberOfAgents) {
		super();
		this.nodeId = nodeId;
		this.numberOfAgents = numberOfAgents;
	}

	/**
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @return the number of Agents running on this node
	 */
	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	/**
	 * @param newLoad
	 */
	public void setNumberOfAgents(int newLoad) {
		this.numberOfAgents = newLoad;
	}
}
