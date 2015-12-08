package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.ThreePhaseCommitImpl;
import ar.edu.itba.pod.Legajo47399.simul.comunication.ThreePhaseCommitState;
import ar.edu.itba.pod.Legajo47399.simul.comunication.TransactionableImpl;
import ar.edu.itba.pod.Legajo47399.simul.comunication.TransactionableState;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.ResourceTransferPayloadImpl;
import ar.edu.itba.pod.Legajo47399.simul.distributed.DistributedMarket;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.Multiset;

public class HandlerResourceTransferPayload implements HandlerPayload {
	private final static Logger LOGGER = Logger
			.getLogger(HandlerResourceTransferPayload.class);
	private static HandlerPayload instance;
	private Node myNode;

	@Override
	public void execute(Message m) {
		//LOGGER.info(myNode.getNodeId()
		//		+ "Mensaje Recibido dentro del handler de  resource transfer payload:");


		ResourceTransferMessagePayload msj = ((ResourceTransferPayloadImpl) m
				.getPayload());
		ThreePhaseCommitImpl my3pc = null;
		try {
			my3pc = ((ThreePhaseCommitImpl) this.myNode
					.getMyConnectionManager().getThreePhaseCommit());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AtomicBoolean aux = my3pc.getCambiarEstado();

		TransactionableImpl myTrans = null;

		try {
			myTrans = ((TransactionableImpl) this.myNode
					.getMyConnectionManager().getNodeCommunication());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Integer cantidad;
		Resource recurso;
		recurso = msj.getResource();
		cantidad = msj.getAmount();

		DistributedMarket myMarket = (DistributedMarket) myNode
				.getMyMarketManager(this.myNode.getMyConnectionManager())
				.market();
		Multiset<Resource> externalRepository = myMarket
				.getExternalRepository();
		Multiset<ResourceStock> selling = myMarket.getSelling();

		if (msj.getDestination().equals(myNode.getNodeId())) {

			
			synchronized (externalRepository) {
				if (externalRepository.count(recurso) != 0) {
					LOGGER.info("<<<-----------*AGREGADO AL REPOSITORIO EXTERNO:"+cantidad+" DE "+ recurso);
					externalRepository.add(recurso, cantidad);
				} else {
					LOGGER.info("<<<-----------**AGREGADO AL REPOSITORIO EXTERNO:"+cantidad+" DE "+ recurso);
					externalRepository.setCount(recurso, cantidad);
				}
			}

		} else {
			//LOGGER.info("Entro aca parte local");
			/* Sacar del mercado la cantidad de recurso */
			synchronized (selling) {
				for (ResourceStock seller : selling) {
					if (recurso.equals(seller.resource())) {

						int available = selling.count(seller);
						//LOGGER.info("El recurso disponible: " + available
						//		+ " y la cantidad pedida es:" + cantidad);
						if (available - cantidad >= 0) {
							//LOGGER.info("lo descuento");
							if (cantidad > 0){
								LOGGER.info("------------->>>SE VAN DEL SELLING:"+cantidad+" DE "+seller.resource()+" PRODUCIDO POR "+seller.name());
							}
							selling.setCount(seller, available, available
									- cantidad);
							seller.remove(cantidad);
							cantidad = 0;

						}

					}
				}
			}
		}

		if (aux.get()) {
			my3pc.setMyState(ThreePhaseCommitState.NO_STATE);
		}

		//LOGGER.info("------------------>>>>>>>>>>>>>>>>>Seteo la variable de transactionable como END_TRANSACTION");
		myTrans.setMyState(TransactionableState.END_TRANSACTION_STATE);
		
			
		
		//LOGGER.info("----------------------------------------------->>>>>>>>>>>>>>>>>LLAMO AL FINISH DEL TIMERTRANSACTIONABLE");
		//myTrans.finishTimer();

	}

	private HandlerResourceTransferPayload(Node myNode) {
		this.myNode = myNode;
	}

	public static HandlerPayload getInstance(Node myNode) {
		if (instance == null) {
			instance = new HandlerResourceTransferPayload(myNode);
		}
		return instance;
	}

}
