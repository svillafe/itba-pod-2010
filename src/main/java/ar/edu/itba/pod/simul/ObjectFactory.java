package ar.edu.itba.pod.simul;

import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

/**
 * Factory that creates manager instances to be used by an application
 * <p>
 * This interface allows to encapsulate the actual implementation details
 * of the interfaces, so that a client may use the system regardless of
 * how it was implemented:
 * </p>
 * <p>
 * <code><pre>
 * 	ObjectFactory factory = new MyObjectFactory();
 *  ConnectionManager conn = factory.createConnectionManager("xxx.xxx.xxx.xxx");
 *  MarketManager market = factory.createMarketManager(conn);
 *  SimulationManager simul = factory.createSimulationManager(conn);
 *  simul.register(Marker.class, market.market());
 *  
 *  ...
 *  
 *  simul.start();
 * 
 * </pre></code>
 * </p>
 */
public interface ObjectFactory {
	/**
	 * Creates a connection manager bound to the given local IP as a single
	 * node. This method is intended to create the initial node of a group
	 * @param localIp The ip used to bind the group to
	 * @return A new connection manager 
	 */
	public ConnectionManager createConnectionManager(String localIp);

	/**
	 * Creates a connection manager bound to the given local IP and joins
	 * an existing group reachable at the given address
	 * @return A new connection manager 
	 */
	public ConnectionManager createConnectionManager(String localIp, String groupIp);

	/**
	 * Creates a marker manager that uses the underlying connection manager
	 * as the underlying communication facility.
	 * @param mgr
	 * @return a new market manager
	 */
	public MarketManager getMarketManager(ConnectionManager mgr);

	/**
	 * Creates a simulation manager that uses the given connection manager 
	 * as the underlying communication facility. 
	 * @param mgr 
	 * @return A new simulation manager
	 */
	public SimulationManager getSimulationManager(ConnectionManager mgr, TimeMapper timeMappers);
	
}
