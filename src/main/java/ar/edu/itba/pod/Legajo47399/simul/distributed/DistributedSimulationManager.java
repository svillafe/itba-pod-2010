package ar.edu.itba.pod.Legajo47399.simul.distributed;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.NodeAgentLoadRequestPayloadImpl;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class DistributedSimulationManager implements SimulationManager {

	private final static Logger LOGGER = Logger
			.getLogger(DistributedMarketManager.class);

	private final DistributedSimulation simulation;

	private List<Agent> theAgents;
	private ConnectionManager myCm;
	private Map<String, NodeAgentLoad> theAgentLoads;
	private Node myNode;

	private final Map<Class<?>, Object> env = Maps.newHashMap();

	private boolean started = false;

	public DistributedSimulationManager(Node myNode, TimeMapper timeMapper) {
		this.myCm = myNode.getMyConnectionManager();
		this.myNode = myNode;
		this.theAgents = Collections.synchronizedList(new ArrayList<Agent>());
		theAgentLoads = Collections
				.synchronizedMap(new HashMap<String, NodeAgentLoad>());

		/* Inicialmente tengo cero agentes */
		synchronized (theAgentLoads) {
			theAgentLoads.put(myNode.getNodeId(),
					new NodeAgentLoad(myNode.getNodeId(), 0));
		}

		this.simulation = new DistributedSimulation(timeMapper, this);

		/* Cada vez que creo la simulacion Balaceo por default */
		// this.balancearCluster();

	}

	@Override
	public void start() {
		for (Agent a : theAgents) {
			a.start();
		}
		started = true;
		((DistributedSimulationManager) (myNode.getMySimulationManager()))
				.balancearNodos();
	}

	@Override
	public void shutdown() {
		boolean interrupted = false;

		/* Apago la simulacion, apagando todos mis agentes */
		for (Agent agent : theAgents) {
			agent.finish();
		}
		for (Agent agent : theAgents) {
			try {
				agent.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			throw new IllegalStateException(
					"Interrupted when shutting down agents!");
		}

	}

	@Override
	public void addAgent(Agent a) {
		/* Nuevo algoritmo: pienso que soy lo mas y me Autoproclamo cordinador */
		NodeAgentLoad nodoElegido = null;
		AgentDescriptor ad = a.getAgentDescriptor();
		boolean error = false;

		try {
			nodoElegido = myCm.getSimulationCommunication()
					.getMinimumNodeKnownLoad();
		} catch (RemoteException e) {
			LOGGER.info("Error");
			//e.printStackTrace();
			error = true;
		}

		if (!error) {
			try {
				myCm.getConnectionManager(nodoElegido.getNodeId())
						.getSimulationCommunication().startAgent(ad);
			} catch (RemoteException e) {
				LOGGER.info("Justo fallo el nodo que antes elegistes");

			}
		}
	}

	@Override
	public void removeAgent(Agent agent) {
		synchronized (this.theAgents) {
			agent.finish();
			try {
				agent.wait();
			} catch (InterruptedException e) {

			}

		}
		this.theAgents.remove(agent);
	}

	@Override
	public Simulation simulation() {
		Preconditions.checkState(started, "No simulation has been started!");
		return simulation;
	}

	@Override
	public SimulationInspector inspector() {
		return simulation;
	}

	@Override
	public <T> void register(Class<T> type, T instance) {
		env.put(type, instance);

	}

	@Override
	public Collection<Agent> getAgents() {
		return this.theAgents;
	}

	@SuppressWarnings("unchecked")
	public <T> T getEnv(Class<T> param) {
		return (T) env.get(param);
	}

	/* Balanceo de Carga */
	public void balancearClusterSalida() {

		if (myNode.getClusterNodes().size() == 0) {
			return;
		}

		// me aseguro que si hay un nodo caido no lo tenga en la lista

		NodeAgentLoad miCarga;

		synchronized (theAgentLoads) {
			// LOGGER.info("Borro toda la lista de cargas");
			miCarga = theAgentLoads.get(myNode.getNodeId());
			theAgentLoads.clear();
			theAgentLoads.put(myNode.getNodeId(), miCarga);
		}

		/* 1 pido todas las cargas de todos */
		try {
			myCm.getGroupCommunication().broadcast(
					new Message(myNode.getNodeId(), System.currentTimeMillis(),
							MessageType.NODE_AGENTS_LOAD_REQUEST,
							new NodeAgentLoadRequestPayloadImpl()));
		} catch (RemoteException e) {
			LOGGER.info("Falla broadcast.");
			// return;
		}

		/* Espero que me lleguen los mensajes */
		try {
			Thread.sleep(Node.DISCONECT_WAIT);
		} catch (InterruptedException e) {
			LOGGER.info("Me despertaron");
		}

		/* 2 Saco de a un agente mio y lo agrego en el nodo que menos tiene */
		Integer cantidad = miCarga.getNumberOfAgents();

		for (int i = 0; i < cantidad; i++) {
			LOGGER.info("Entro aca:" + i);
			/* Calculo el minimo */
			NodeAgentLoad fuente = calcularLoadMinimo(myNode.getNodeId());

			/* Migro un agente mio */
			List<AgentDescriptor> agente;
			try {
				agente = (List<AgentDescriptor>) myNode
						.getMyConnectionManager().getSimulationCommunication()
						.migrateAgents(1);
			} catch (RemoteException e) {
				LOGGER.error("Falla aca");
				continue;
			}

			/* Starteo el agente en el otro nodo */
			if (agente.size() != 0) {
				try {
					myNode.getMyConnectionManager()
							.getConnectionManager(fuente.getNodeId())
							.getSimulationCommunication()
							.startAgent(agente.get(0));
				} catch (RemoteException e) {
					LOGGER.error("Falla el tratar de startear el otro nodo.");
					continue;
				}

				/* Actualizo la lista de cargas local */
				synchronized (this.theAgentLoads) {

					theAgentLoads
							.put(fuente.getNodeId(),
									new NodeAgentLoad(fuente.getNodeId(),
											fuente.getNumberOfAgents() + 1));
				}

			}

		}

	}

	public void balancearNodos() {

		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() {
		 */

		// me aseguro que si hay un nodo caido no lo tenga en la lista
		synchronized (theAgentLoads) {
			// LOGGER.info("Borro toda la lista de cargas");
			NodeAgentLoad auxi = theAgentLoads.get(myNode.getNodeId());
			theAgentLoads.clear();

			theAgentLoads.put(myNode.getNodeId(), auxi);

		}

		try {
			myCm.getGroupCommunication().broadcast(
					new Message(myNode.getNodeId(), System.currentTimeMillis(),
							MessageType.NODE_AGENTS_LOAD_REQUEST,
							new NodeAgentLoadRequestPayloadImpl()));
		} catch (RemoteException e) {
			LOGGER.info("Fallo el broadcast");
			// e.printStackTrace();
		}

		/* Espero que me lleguen los mensajes */
		try {
			Thread.sleep(Node.BALANCING_WAIT);
		} catch (InterruptedException e) {
			LOGGER.info("Me despertaron");
		}

		try {
			balanceoDeCarga();
		} catch (RemoteException e) {
			System.out.println("No pude balancear:" + e.getMessage());
			LOGGER.info("Error:" + e.getMessage());
		}
		System.out.println("Balanceo terminado puede agregar otro nodo.");
		// }

		// }).start();

	}

	private void balanceoDeCarga() throws RemoteException {

		int difMaxMin = 0;
		int cantTotalAgentes = 0;
		int avgAgentes = 0;
		boolean salir = false;

		SimulationCommunication simComDestino = null;
		SimulationCommunication simComFuente = null;

		List<AgentDescriptor> aMigrar = new ArrayList<AgentDescriptor>();

		/*
		 * Implementacion del nuevo algoritmo de balanceo (Robin Hood): Por cada
		 * vuelta veo cual tiene mas carga y cual tiene menos y traspaso los
		 * nodos que le faltan al de menor para llegar al promedio. El algoritmo
		 * termina cuando la diferencia entre el minimo y el maximo es 1 o 0
		 */

		/* Cuento la cantidad total de agentes en el Cluster */
		for (NodeAgentLoad n : theAgentLoads.values()) {
			cantTotalAgentes = cantTotalAgentes + n.getNumberOfAgents();
		}

		avgAgentes = (cantTotalAgentes / this.theAgentLoads.values().size());

		NodeAgentLoad fuente = null;
		NodeAgentLoad destino = null;

		while (!salir) {
			/* Borro la lista de migraciones */
			aMigrar.clear();

			/* Calculo el maximo */
			fuente = calcularLoadMaximo();
			/* Calculo el minimo */
			destino = calcularLoadMinimo();

			/* Obtengo los simulation communication de ambos */
			simComFuente = myNode.getMyConnectionManager()
					.getConnectionManager(fuente.getNodeId())
					.getSimulationCommunication();
			simComDestino = myNode.getMyConnectionManager()
					.getConnectionManager(destino.getNodeId())
					.getSimulationCommunication();

			/* Obtengo la diferencia entre el maximo y el minimo */
			difMaxMin = fuente.getNumberOfAgents()
					- destino.getNumberOfAgents();

			/*
			 * Si la fuente y el destino son el mismo nodo o la diferencia entre
			 * el mayor y el menor es <= 1 entonces salgo del ciclo.
			 */
			if (fuente.getNodeId().equals(destino.getNodeId())) {
				/* Si el maximo y el minimo son el mismo termino */
				salir = true;
			} else {
				boolean deboActualizar = false;
				switch (difMaxMin) {

				case 1:
					salir = true;
					break;

				case 2:
					/*
					 * Separo el caso de dos agentes por que puede ser que un
					 * nodo tenga 3 otro 1 entonces se va a quedar ciclando
					 * pasandoce dos agentes todo el tiempo (6 6 4)
					 */
					aMigrar.addAll(simComFuente.migrateAgents(1));

					if (aMigrar.size() != 0) {
						simComDestino.startAgent(aMigrar.get(0));
						deboActualizar = true;
					}
					break;

				default:
					aMigrar.addAll(simComFuente.migrateAgents(fuente
							.getNumberOfAgents() - avgAgentes));

					if (aMigrar.size() != 0) {

						/* Starteo todos los agentes en el destino */
						for (AgentDescriptor a : aMigrar) {
							simComDestino.startAgent(a);
						}
						deboActualizar = true;
					}
					break;

				}

				if (deboActualizar) {
					/*
					 * Si no soy yo (las mias las actualizo en el
					 * startAgent)entonces actualizo las cargas
					 */
					if (!fuente.getNodeId().equals(myNode.getNodeId()))
						this.theAgentLoads.put(
								fuente.getNodeId(),
								new NodeAgentLoad(fuente.getNodeId(), fuente
										.getNumberOfAgents() - aMigrar.size()));

					/*
					 * Si no soy yo (las mias las actualizo en el
					 * startAgent)entonces actualizo las cargas
					 */
					if (!destino.getNodeId().equals(myNode.getNodeId()))
						this.theAgentLoads.put(
								destino.getNodeId(),
								new NodeAgentLoad(destino.getNodeId(), destino
										.getNumberOfAgents() + aMigrar.size()));
				}

			}
		}

		LOGGER.info("Termino el balanceo de carga");
	}

	private NodeAgentLoad calcularLoadMinimo() {

		NodeAgentLoad resp = new NodeAgentLoad("nodeId", Integer.MAX_VALUE);

		for (NodeAgentLoad myNodeLoad : this.theAgentLoads.values()) {
			if (myNodeLoad.getNumberOfAgents() < resp.getNumberOfAgents()) {
				resp = myNodeLoad;
			}
		}
		return resp;
	}

	private NodeAgentLoad calcularLoadMinimo(String nodeId) {
		NodeAgentLoad resp = new NodeAgentLoad("nodeId", Integer.MAX_VALUE);

		for (NodeAgentLoad nodeLoad : this.theAgentLoads.values()) {
			if (nodeLoad.getNumberOfAgents() < resp.getNumberOfAgents()
					&& (nodeId != nodeLoad.getNodeId())) {
				resp = nodeLoad;
			}
		}
		return resp;

	}

	private NodeAgentLoad calcularLoadMaximo() {
		NodeAgentLoad resp = new NodeAgentLoad("nodeId", Integer.MIN_VALUE);

		for (NodeAgentLoad nodeLoad : this.theAgentLoads.values()) {
			if (nodeLoad.getNumberOfAgents() > resp.getNumberOfAgents()) {
				resp = nodeLoad;
			}
		}
		return resp;
	}

	public Map<String, NodeAgentLoad> getTheAgentLoads() {

		return this.theAgentLoads;

	}

	public void addAgentSim(Agent agent) {
		synchronized (theAgents) {
			theAgents.add(agent);
		}
	}

	public void limpiarCargas() {
		/* Limpio las cargas */
		synchronized (theAgentLoads) {
			// LOGGER.info("Borro toda la lista de cargas");
			NodeAgentLoad auxi = theAgentLoads.get(myNode.getNodeId());
			theAgentLoads.clear();
			theAgentLoads.put(myNode.getNodeId(), auxi);

		}

	}

}
