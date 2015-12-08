package ar.edu.itba.pod.Legajo47399.simul.comunication.mlthreads;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.MessageListenerImpl;
import ar.edu.itba.pod.Legajo47399.simul.comunication.MessageWrapper;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerPayload;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.thread.CleanableThread;

public class PullThread extends CleanableThread implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(PullThread.class);

	private Node myNode;
	private MessageListenerImpl myMessageListener;

	public PullThread(Node myNode, MessageListenerImpl messageListenerImpl) {
		this.myNode = myNode;
		this.myMessageListener = messageListenerImpl;
	}

	@Override
	public void run() {
		//LOGGER.info("Run el thread que hace pull.");

		while (!this.shouldFinish()) {
			try {
				Thread.sleep(Node.PULL_LIMMIT);
			} catch (InterruptedException e) {
				LOGGER.info("Me despertaron");
			}

			List<String> lista = myNode.getNRandomNodes(1);
			List<Message> listaMensajes = new ArrayList<Message>();
			for (String s : lista) {
				//LOGGER.info("Le pregunto al nodo:" + s + "sus mensajes nuevos.");
				try {
					listaMensajes.addAll((List<Message>) myNode
							.getMyConnectionManager().getConnectionManager(s)
							.getGroupCommunication().getListener()
							.getNewMessages(myNode.getNodeId()));
				} catch (RemoteException e) {
					//Se cayo el nodo al que se quiere acceder, obvia el caso
					LOGGER.info("Nodo caido");
				}
			}

			MessageWrapper aux;
			Set<MessageWrapper> messages = this.myMessageListener.getMessages();
			Map<MessageType, HandlerPayload> myHandlerMap = this.myMessageListener
					.getHandlerMap();

			for (Message m : listaMensajes) {
				//LOGGER.info("Obtuve estos mensajes:" + listaMensajes + ".");
				//LOGGER.info("Analizando el mensaje: " + m);
				aux = new MessageWrapper(m, System.currentTimeMillis());
				synchronized (messages) {
					if (!MessageListenerImpl.contains(messages, aux)) {
						if (!m.getNodeId().equals(myNode.getNodeId())) {
							//LOGGER.info("Agrego el mensaje: " + m
							//		+ " a la lista de mensajes nuevos.");
							messages.add(aux);
							//LOGGER.info("Ejecutando el mensaje nuevo Pull thread.");
							myHandlerMap.get(aux.getMessage().getType())
									.execute(aux.getMessage());

						}else{
							//LOGGER.info("El mensaje lo envie yo entonces lo ignoro: " + m
							//		+ " entonces lo ignoro.");
						}
					}else{
						//LOGGER.info("El mensaje ya lo tenia entonces lo ignoro: " + m
						//		+ " entonces lo ignoro.");
					}
				}
			}

		}

		LOGGER.info("Terminado el thread de pull.");
	}

}
