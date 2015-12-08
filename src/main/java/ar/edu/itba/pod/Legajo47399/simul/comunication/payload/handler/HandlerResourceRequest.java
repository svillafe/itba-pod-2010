package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedMarket;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.Multiset;

public class HandlerResourceRequest implements HandlerPayload {
	private final static Logger LOGGER = Logger
			.getLogger(HandlerResourceRequest.class);
	private static HandlerPayload instance;
	private Node myNode;

	private HandlerResourceRequest(Node myNode) {
		super();
		this.myNode = myNode;
	}

	@Override
	public void execute(Message m) {
		// LOGGER.info(myNode.getNodeId() +
		// "MensajeRecibido dentro del handler:");
		LOGGER.info("MENSAJE RECIBIDO *****************************************************************");
		ResourceRequestPayload elMensaje = (ResourceRequestPayload) m
				.getPayload();

		Integer cantidadSolicitada = elMensaje.getAmountRequested();
		Resource recursoPedido = elMensaje.getResource();
		Integer cantidadExchange;
		Market myMarket;
		Multiset<ResourceStock> yoVendo;

		try{
		myMarket = this.myNode.getMyMarketManager(
				this.myNode.getMyConnectionManager()).market();
		}catch(IllegalStateException e){
			LOGGER.info("Todavia no tengo mercado para atender esta solicitud");
			return;
		}
		yoVendo = ((DistributedMarket) myMarket).getSelling();

		cantidadExchange = obtenerRecursoLocal(yoVendo, recursoPedido,
				cantidadSolicitada);

		// LOGGER.info("Llego un nuevo pedido de recursos.");

		if (cantidadExchange == null) {
			// LOGGER.info("No tengo recursos, no contesto");
			return;
		}
		/* Tengo recursos entonces empiezo la transaccion */

		// LOGGER.info("Tengo recurso, empiezo transaccion");

		Transactionable myNodeCommunication = null;
		Transactionable theOtherCommunication = null;

		try {
			myNodeCommunication = myNode.getMyConnectionManager()
					.getNodeCommunication();
			theOtherCommunication = myNode.getMyConnectionManager()
					.getConnectionManager(m.getNodeId()).getNodeCommunication();
		} catch (RemoteException e) {
			/* Se cayo el otro nodo entonces no hago transaccion alguna */
			return;
		}

		boolean error = false;

		try {
			myNodeCommunication
					.beginTransaction(m.getNodeId(), Node.TOTAL_WAIT);
		} catch (IllegalStateException e) {
			/* Ya estoy en una transaccion entonces no tengo mas nada apra hacer */
			error = true;
			return;
		} catch (RemoteException e) {
			/* Falla mi begin transaccion entonces vuelvo */
			error = true;
			return;
		}

		if (!error) {
			try {
				myNodeCommunication.exchange(recursoPedido, cantidadExchange,
						myNode.getNodeId(), m.getNodeId());
				theOtherCommunication.exchange(recursoPedido, cantidadExchange,
						myNode.getNodeId(), m.getNodeId());
			} catch (RemoteException e1) {
				e1.printStackTrace();
				/* Fallo algun exchange entonces hago los rollback y vuelvo */
				try {
					myNodeCommunication.rollback();
					theOtherCommunication.rollback();
				} catch (RemoteException e) {
					LOGGER.info("Falla remota del rollback");
				}

				return;

			} catch (IllegalStateException e2) {
				/* Fallo algun exchange entonces hago un rollback y vuelvo */
				try {
					myNodeCommunication.rollback();
					theOtherCommunication.rollback();
				} catch (RemoteException e) {
					LOGGER.info("Falla remota del rollback");
				}
				return;
			}

			try {
				myNodeCommunication.endTransaction();
			} catch (RemoteException e) {
				/* Fallo el endTransaction entonces hago los rollback y vuelvo */
				LOGGER.info("Fallo el endTransaction, por una falla remota");
				try {
					myNodeCommunication.rollback();
					theOtherCommunication.rollback();
				} catch (RemoteException e1) {
					//Falla el enlace del otro nodo no hago nada.
					LOGGER.info("Fallo el rollback del otro nodo");
				}
				return;
			}
			/* Antes de irme vuelvo todo a la normalidad */
			try {
				myNodeCommunication.rollback();
				theOtherCommunication.rollback();
			} catch (RemoteException e1) {
				//Falla el enlace del otro nodo no hago nada.
				LOGGER.info("Fallo el rollback del otro nodo");
			}
			return;
		}
	}

	private Integer obtenerRecursoLocal(Multiset<ResourceStock> yoVendo,
			Resource recursoPedido, Integer cantidadSolicitada) {
		Integer resp = null;
		synchronized (yoVendo) {
			for (ResourceStock a : yoVendo) {
				if (recursoPedido.equals(a.resource())) {
					resp = Math.min(yoVendo.count(a), cantidadSolicitada);
				}
			}
		}
		return resp;

	}

	public static HandlerPayload getInstance(Node myNode) {
		if (instance == null) {
			instance = new HandlerResourceRequest(myNode);
		}
		return instance;
	}

}
