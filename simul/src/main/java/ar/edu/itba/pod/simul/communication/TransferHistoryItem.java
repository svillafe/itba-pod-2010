/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.simul.market.Resource;

/**
 * Represents a transfer done in a node
 * 
 * @author POD
 */
public class TransferHistoryItem implements Serializable {
	private String sourceAgentName;
	private String destAgentName;
	private Resource resource;
	private int amount;

	/**
	 * @param sourceAgentName
	 *            Agent name who sold the resource
	 * @param destAgentName
	 *            Agent name who bought the resource
	 * @param resource
	 *            transfered resource
	 * @param amount
	 *            amount of transfered resource
	 */
	public TransferHistoryItem(String sourceAgentName, String destAgentName, Resource resource, int amount) {
		Preconditions.checkNotNull(sourceAgentName, "source agentName could not be null");
		Preconditions.checkNotNull(destAgentName, "dest agentName could not be null");
		Preconditions.checkNotNull(resource, "resource could not be null");
		Preconditions.checkArgument(amount > 0, "amount should be > 0");

		this.sourceAgentName = sourceAgentName;
		this.destAgentName = destAgentName;
		this.resource = resource;
		this.amount = amount;
	}

	/**
	 * @return the sourceAgentName
	 */
	public String getSourceAgentName() {
		return sourceAgentName;
	}

	/**
	 * @return the destAgentName
	 */
	public String getDestAgentName() {
		return destAgentName;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
}
