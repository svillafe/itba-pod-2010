package ar.edu.itba.pod.simul.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Medium used for establishing all the different types of connections needed in the cluster.
 * <p>
 * All the connections used in the cluster must be provided by this medium.
 */
public interface ConnectionManager extends Remote {

	/**
	 * Returns the port number used in all the cluster communications. It is expected that the same port number is used
	 * in the whole cluster.
	 * 
	 * @return Port number
	 */
	public int getClusterPort() throws RemoteException;

	/**
	 * Finds a ConnectionManager in the cluster using the target node. All the exceptions are wrapped in a
	 * NoConnectionAvailableException. Always a GroupCommunication is returned, unless an exception is thrown.
	 * 
	 * @param nodeId
	 *            The node where to look for the ConnectionManager.
	 * @return The connection manager of the node.
	 * @throws RemoteException
	 */
	public ConnectionManager getConnectionManager(String nodeId) throws RemoteException;

	/**
	 * Finds a GroupCommunication in the cluster using the target node. All the exceptions are wrapped in a
	 * NoConnectionAvailableException. Always a GroupCommunication is returned, unless an exception is thrown.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public ClusterCommunication getGroupCommunication() throws RemoteException;

	/**
	 * Finds a ClusterCommunication in the cluster using the target node. All the exceptions are wrapped in a
	 * NoConnectionAvailableException. Always a ClusterCommunication is returned, unless an exception is thrown.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public ClusterAdministration getClusterAdmimnistration() throws RemoteException;

	/**
	 * Finds a NodeCommunication in the cluster using the target node. All the exceptions are wrapped in a
	 * NoConnectionAvailableException. Always a NodeCommunication is returned, unless an exception is thrown.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Transactionable getNodeCommunication() throws RemoteException;

	/**
	 * Finds a SimulationCommunication in the cluster using the target node. All the exceptions are wrapped in a
	 * NoConnectionAvailableException. Always a SimulationCommunication is returned, unless an exception is thrown.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public SimulationCommunication getSimulationCommunication() throws RemoteException;

	/**
	 * Finds a ThreePhaseCommit in the cluster using the target node. All the exceptions are wrapped in a
	 * NoConnectionAvailableException. Always a ThreePhaseCommit is returned, unless an exception is thrown.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException;
}
