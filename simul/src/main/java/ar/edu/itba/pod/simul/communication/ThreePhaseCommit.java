package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Medium used to establish a consistent commit between two nodes.
 * <p>
 * All the changes done in the application must end in a consistent state. In order to guarantee this condition, a
 * three-phase commit must be used. All the methods must be called in an specific order
 */
public interface ThreePhaseCommit extends Remote {

	/**
	 * Called by the coordinator to gather votes for doing a commit. The cohorts can return true or false, whether they
	 * are ready or not. If false is returned, the coordinator aborts the commit. If true is returned, the coordinator
	 * calls preCommit method in all the cohorts. The cohorts will wait for the timeout set by parameter before
	 * canceling the process. Once the cohort is prepared to commit, only the same coordinator can invoke preCommit and
	 * doCommit until the whole process is done.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public boolean canCommit(String coordinatorId, long timeout) throws RemoteException;

	/**
	 * If the cohort is in prepared state, a commit is done. Only the same coordinator that invoked canCommit can invoke
	 * this method. Otherwise, an IllegalArgumentException is thrown. If it is invoked before canCommit method, and
	 * IllegalStateException is thrown. At this stage, the cohort do make the commit.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public void preCommit(String coordinatorId) throws RemoteException;

	/**
	 * Method called by the coordinator to change to commit state after receiving OK from all. Only the same coordinator
	 * that invoked preCommit can invoke this method. Otherwise, an IllegalArgumentException is thrown. If it is invoked
	 * before preCommit method, and IllegalStateException is thrown. After this method, any coordinator can start the
	 * whole process.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public void doCommit(String coordinatorId) throws RemoteException;

	/**
	 * Method called when an abort message is received during the commit. All the changes done must be reverted. If it
	 * is invoked before canCommit method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	public void abort() throws RemoteException;

	/**
	 * Method called when the cohort waits more than the timeout set for doing the commit. All the changes done must be
	 * reverted. If it is invoked before canCommit method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	public void onTimeout() throws RemoteException;

}
