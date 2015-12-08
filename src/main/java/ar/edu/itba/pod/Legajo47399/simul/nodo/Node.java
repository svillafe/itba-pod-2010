package ar.edu.itba.pod.Legajo47399.simul.nodo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.ConnectionManagerImpl;
import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedMarketManager;
import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedSimulationManager;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class Node {
	private final static Logger LOGGER = Logger.getLogger("Nodo");

	public static final Integer CANT_NODES = 3;

	private static final long CONSTANTE_SINCR = 250;

	public static final int PULL_LIMMIT = (int) (20 * CONSTANTE_SINCR);

	public static final long TIME_ERASE = 120 * CONSTANTE_SINCR;

	public static final long BALANCING_WAIT = 3 * CONSTANTE_SINCR;
	
	public static final long NODE_LOAD_WAIT = 6 * CONSTANTE_SINCR;

	
	/*Tiempos de transacciones*/
	public static final long TRANSACTION_WAIT_TIMEOUT = 2 * CONSTANTE_SINCR;

	public static final long THREE_PHASE_COMMIT_TIME_OUT = 16 * CONSTANTE_SINCR;
	
	public static final int TOTAL_WAIT = (int) (32 * CONSTANTE_SINCR);

	
	public static final long STAT_WAIT = 4*CONSTANTE_SINCR;

	public static final long WAIT_THE_BALANCER = 20*CONSTANTE_SINCR;

	public static final long ERASE_THREAD_TIME = 20*CONSTANTE_SINCR;

	public static final long DISCONECT_WAIT = 6*CONSTANTE_SINCR;
	

	/*
	 * Medium used for establishing all the different types of connections
	 * needed in the cluster.
	 * 
	 * All the connections used in the cluster must be provided by this medium.
	 */
	private ConnectionManager myConnectionManager;

	/*
	 * Represents the number of agents running on a node.
	 */
	// private Map<String, NodeAgentLoad> theAgentLoads;

	private String nodeId;
	private String host;
	private Integer portNumber;
	private Set<String> clusterNodes;
	// private String coordinator;
	// private Boolean coordinatorProcees;
	private SimulationManager mySimulationManager = null;
	private MarketManager myMarketManager = null;

	public Node(Integer portNumber) {

		InetAddress address = null;
		/* Inicializacion del portnumber, nodeId y direccion de red */
		this.portNumber = portNumber;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			LOGGER.info("Error al intentar leer el ip local");
		}
		this.host = address.getHostAddress();
		this.nodeId = this.host + ":" + portNumber;

		LOGGER.info("The node id is:" + nodeId);

		this.clusterNodes = Collections.synchronizedSet(new HashSet<String>());

		/* Inicializacion del ConnectionManager y del ClusterAdministration */
		try {
			myConnectionManager = new ConnectionManagerImpl(this);
		} catch (RemoteException e) {
			LOGGER.info("Error al intentar crear un CM.");
			e.printStackTrace();
		}

	}

	public Node(String localIp) {
		this(1099);
		this.host = localIp;

	}

	public void bindService() {

		// Bind the remote object's stub in the registry
		try {
			Registry registry = LocateRegistry.createRegistry(this.portNumber);

			registry.bind(ReferenceName.CONNECTION_MANAGER_NAME,
					this.myConnectionManager);
			LOGGER.info("Bind Exitoso");
		} catch (Exception e) {
			System.out.println("ERROR AL BINDEAR EL PUERTO");
			LOGGER.error(this.getNodeId()
					+ ": Error al bindear el connectionManager", e);
		}
	}

	public void createCluster() throws RemoteException {
		myConnectionManager.getClusterAdmimnistration().createGroup();

	}

	public void connectToaCluster(String ipNode) throws RemoteException {

		String node = ipNode + ":" + getMyConnectionManager().getClusterPort();

		myConnectionManager.getClusterAdmimnistration().connectToGroup(node);
		LOGGER.info(this.getNodeId()
				+ ": The node is connected to the cluster successfully.");

	
	}

	public static Node valueOf(String nodeId) {
		Scanner scanner = new Scanner(nodeId);
		scanner.useDelimiter(":");
		String host = scanner.next();
		int port = Integer.parseInt(scanner.next());

		return new Node(host, port);
	}

	public String getHost() {
		return this.host;
	}

	public Set<String> getClusterNodes(){
		return this.clusterNodes;

	}

	

	public ConnectionManager getMyConnectionManager() {
		return myConnectionManager;
	}

	public String getNodeId() {
		return nodeId;
	}

	public Integer getPortNumber() {
		return portNumber;
	}



	private Node(String ipAddress, Integer portNumber) {
		/*
		 * Inicializacion del portnumber, nodeId y direccion de red este
		 * constructor es utilizado para crear un objeto tonto
		 */
		this.portNumber = portNumber;
		this.host = ipAddress;
		this.nodeId = this.host + ":" + portNumber;

		// LOGGER.info("The node id is:" + nodeId);
	}

	public Boolean removeNode(String nodeId) {
		boolean resp = false;
		synchronized (clusterNodes) {
			LOGGER.info("Lo saco de la lista de vecinos a:" + nodeId);
			resp = this.clusterNodes.remove(nodeId);
			LOGGER.info(resp == true ? "Ya lo saque"
					: "!!!ERROR:No tenia ese nodo como vecino");
		}
		LOGGER.info("Lo saco de mi lista de cargas");
		Map<String, NodeAgentLoad> aux = ((DistributedSimulationManager)(this.mySimulationManager)).getTheAgentLoads();
		
		synchronized (aux) {
			aux.remove(nodeId);
		}
			
		
		return resp;

	}

	

	/**
	 * Funcion que devuelve nodos random que no son el nodeId
	 * */
	public List<String> getOtherNRandomNodes(Integer cantnodes, String nodeId) {
		Integer n = null;
		Integer size = this.clusterNodes.size();
		List<String> auxList;
		if (cantnodes < size) {
			n = cantnodes;
		} else if (cantnodes >= size) {
			auxList = new ArrayList<String>(clusterNodes);
			auxList.remove(nodeId);
			return auxList;
		}
		auxList = new ArrayList<String>(clusterNodes);
		auxList.remove(nodeId);
		List<String> resp = new ArrayList<String>();
		Collections.shuffle(auxList);
		for (int i = 0; i < n; i++) {
			resp.add(auxList.get(i));
		}
		return resp;
	}

	public List<String> getNRandomNodes(Integer cantnodes) {

		Integer n = null;
		Integer size = this.clusterNodes.size();
		if (cantnodes < size) {
			n = cantnodes;
		} else if (cantnodes >= size) {
			return new ArrayList<String>(clusterNodes);
		}
		List<String> auxList = new ArrayList<String>(clusterNodes);
		List<String> resp = new ArrayList<String>();
		Collections.shuffle(auxList);
		for (int i = 0; i < n; i++) {
			resp.add(auxList.get(i));
		}
		return resp;
	}

	

	public SimulationManager getMySimulationManager() {
		return mySimulationManager;
	}

	public MarketManager getMyMarketManager(ConnectionManager mgr) {
		if (this.myMarketManager == null) {
			this.myMarketManager = new DistributedMarketManager(mgr);
		}
		return this.myMarketManager;
	}
	
	
	public MarketManager getMyMarketManager(){
		return this.myMarketManager;
	}

	public SimulationManager getMySimulationManager(ConnectionManager mgr,
			TimeMapper timeMappers) {

		if (this.mySimulationManager == null) {
			this.mySimulationManager = new DistributedSimulationManager(this,
					timeMappers);
			//((DistributedSimulationManager)(this.mySimulationManager)).balancearCluster();
		}
		return this.mySimulationManager;
	}

	public void cleanClusterNodes() {

		LOGGER.info("Estoy por borrar mi lista de nodos vecinos.");
		synchronized (clusterNodes) {
			clusterNodes.clear();
		}
		LOGGER.info("Ya la borre.");

	}

	public void unbinService() {
		
		System.exit(0);
		
	}

}
