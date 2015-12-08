package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.NodeAgentLoadRequestPayloadImpl;
import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedSimulationManager;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

public class SimulationCommunicationImpl implements SimulationCommunication {

	private final static Logger LOGGER = Logger
			.getLogger(SimulationCommunicationImpl.class);

	private Node myNode;

	public SimulationCommunicationImpl(Node myNode) throws RemoteException {
		super();
		this.myNode = myNode;
		UnicastRemoteObject.exportObject(this, 0);
	}

	@Override
	public void startAgent(AgentDescriptor descriptor) throws RemoteException {
		LOGGER.info("Comienza el StartAgent.");

		SimulationManager myManager = myNode.getMySimulationManager();
		Map<String, NodeAgentLoad> myAgentsLoads = ((DistributedSimulationManager) myNode.getMySimulationManager()).getTheAgentLoads();

		Agent a = descriptor.build();
		

		LOGGER.info("Iniciando el Agente:"+a.name());

		try {
			a.onBind(myNode.getMySimulationManager().simulation());
			a.start();
			
		} catch (Exception e) {
			LOGGER.info("PROBLEMA:" + e.getMessage());
		}

		LOGGER.info("El Agente inciado-------------------->.");

		((DistributedSimulationManager) myManager).addAgentSim(a);

		Integer oldAgent = 0;

		synchronized (myAgentsLoads) {
			oldAgent = myAgentsLoads.get(myNode.getNodeId())
					.getNumberOfAgents();
			myAgentsLoads.put(myNode.getNodeId(),
					new NodeAgentLoad(myNode.getNodeId(), oldAgent + 1));
		}
		LOGGER.info("Termina el start Agent.");
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		/*
		 * Como en el nuevo algoritmo yo soy el coordinador entonces pido las
		 * cargas de una y veo cual es la minima y listo
		 */
		/* Borro las cargas que tengo (por si se cayo algun nodo) */
		((DistributedSimulationManager) myNode.getMySimulationManager())
				.limpiarCargas();
		/* Pido las cargas de todos */
		myNode.getMyConnectionManager()
				.getGroupCommunication()
				.broadcast(
						new Message(myNode.getNodeId(), System
								.currentTimeMillis(),
								MessageType.NODE_AGENTS_LOAD_REQUEST,
								new NodeAgentLoadRequestPayloadImpl()));

		//System.out.println("antesde a la espera");
		/* Espero la respuesta */
		try {
			Thread.sleep(Node.NODE_LOAD_WAIT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println("despues");
		

		/* Calculo el nodo de menor carga */
		NodeAgentLoad resp = calcularAgenteCargaMinima();

		return resp;
	}

	private NodeAgentLoad calcularAgenteCargaMinima() {
		Map<String, NodeAgentLoad> myLoads = ((DistributedSimulationManager) myNode
				.getMySimulationManager()).getTheAgentLoads();

		NodeAgentLoad resp = new NodeAgentLoad("nada", Integer.MAX_VALUE);
		synchronized (myLoads) {
			for (NodeAgentLoad a : myLoads.values()) {
				//System.out.println("nombre:"+a.getNodeId()+"Carga:"+a.getNumberOfAgents());
				if (a.getNumberOfAgents() < resp.getNumberOfAgents()) {
					resp = a;
				}
			}
		}
		//System.out.println("Seleccionado:"+resp.getNodeId());
		return resp;
	}

	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {

		// LOGGER.info("Una nueva notificacion de:" + newLoad.getNodeId() + "-"
		// + newLoad.getNumberOfAgents());
		Map<String, NodeAgentLoad> myAgentsLoads = ((DistributedSimulationManager) myNode
				.getMySimulationManager()).getTheAgentLoads();
		synchronized (myAgentsLoads) {
			myAgentsLoads.put(newLoad.getNodeId(), newLoad);
		}

	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
			throws RemoteException {

		Map<String, NodeAgentLoad> myAgentsLoads = ((DistributedSimulationManager) myNode.getMySimulationManager()).getTheAgentLoads();
		SimulationManager myManager = myNode.getMySimulationManager();
		Integer cantAgents = numberOfAgents;
		
		if (numberOfAgents > myAgentsLoads.get(myNode.getNodeId()).getNumberOfAgents()) {
			cantAgents = myAgentsLoads.get(myNode.getNodeId()).getNumberOfAgents();
		}

		List<Agent> misAgentes = (List<Agent>) myManager.getAgents();
		List<AgentDescriptor> resp = new ArrayList<AgentDescriptor>();
		List<Agent> borrar = new ArrayList<Agent>();
		Agent ag;

		synchronized (misAgentes) {
			Iterator<Agent> i = misAgentes.iterator();

			Integer j = 0;
			while (i.hasNext() && j < cantAgents) {
				ag = i.next();
				try {
					synchronized (ag) {
						
						ag.finish();
						ag.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				resp.add(ag.getAgentDescriptor());
				borrar.add(ag);

				j++;
			}
			
			misAgentes.removeAll(borrar);

			NodeAgentLoad myLoad = myAgentsLoads.get(myNode.getNodeId());
			myLoad.setNumberOfAgents(myLoad.getNumberOfAgents() - borrar.size());
			myAgentsLoads.put(myNode.getNodeId(), myLoad);
		}
		return resp;
	}

}

