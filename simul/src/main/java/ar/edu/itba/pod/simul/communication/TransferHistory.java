/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Transfer history done in a node
 * 
 * @author POD
 */
public class TransferHistory implements Serializable {
	private final List<TransferHistoryItem> historyItems;
	private final double transactionsPerSecond;

	/**
	 * @param historyItems
	 * @param transactionsPerSecond
	 */
	public TransferHistory(List<TransferHistoryItem> historyItems, double transactionsPerSecond) {
		this.transactionsPerSecond = transactionsPerSecond;
		Preconditions.checkNotNull(historyItems, "history items could not be null");
		this.historyItems = historyItems;
	}

	/**
	 * @return the transactionsPerSecond
	 */
	public double getTransactionsPerSecond() {
		return transactionsPerSecond;
	}

	/**
	 * @return the historyItems
	 */
	public List<TransferHistoryItem> getHistoryItems() {
		return historyItems;
	}
}