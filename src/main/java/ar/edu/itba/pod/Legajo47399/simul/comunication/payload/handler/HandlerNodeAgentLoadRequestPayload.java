package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import java.rmi.RemoteException;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.NodeAgentLoadPayloadImpl;
import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedSimulationManager;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;

public class HandlerNodeAgentLoadRequestPayload implements HandlerPayload {
	private static HandlerPayload instance;
	private Node myNode;
	private final static Logger LOGGER = Logger
			.getLogger(HandlerNodeAgentLoadRequestPayload.class);

	private HandlerNodeAgentLoadRequestPayload(Node myNode) {
		super();
		this.myNode = myNode;
	}

	@Override
	public void execute(Message m) {

		/* con el nuevo algoritmo este handler solo tiene que devolver su carga */
		//System.out.println("Llego un pedido de carga.");

		Map<String, NodeAgentLoad> myAgentsLoad;
		
		try {
			myAgentsLoad = ((DistributedSimulationManager) myNode
					.getMySimulationManager()).getTheAgentLoads();
		} catch (NullPointerException e) {
			LOGGER.info("Vuelvo por que todavia no tengo simulacion para preguntarle mi carga");
			//System.out.println("Vuelvo por que todavi no tengo siulacion (Borrar este comentario, HandlerNodeAgentLoadImpl)");
			return;
		}
		
		NodeAgentLoad myNodeAgentLoad;
		synchronized (myAgentsLoad) {
			myNodeAgentLoad = myAgentsLoad.get(myNode.getNodeId());
		}

		/*
		 * Si no estoy en modo "en proceso coordinador", respondo los mensajes
		 * con mi carga
		 */
		try {
			myNode.getMyConnectionManager()
					.getGroupCommunication()
					.send(new Message(this.myNode.getNodeId(),
							System.currentTimeMillis(),
							MessageType.NODE_AGENTS_LOAD,
							new NodeAgentLoadPayloadImpl(myNodeAgentLoad)),
							m.getNodeId());
		} catch (RemoteException e) {
			LOGGER.info("Fallo el send");
			return;
		}

	}

	public static HandlerPayload getInstance(Node myNode) {
		if (instance == null) {
			instance = new HandlerNodeAgentLoadRequestPayload(myNode);
		}
		return instance;
	}

}
