package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.mlthreads.EraseThread;
import ar.edu.itba.pod.Legajo47399.simul.comunication.mlthreads.PullThread;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerDisconnectPayload;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerMarketDataPayload;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerMarketDataRequestPayload;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerNodeAgentLoadPayload;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerNodeAgentLoadRequestPayload;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerPayload;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerResourceRequest;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerResourceTransferCanceled;
import ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler.HandlerResourceTransferPayload;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.thread.CleanableThread;

public class MessageListenerImpl extends CleanableThread implements
		MessageListener, Runnable {

	private final static Logger LOGGER = Logger
			.getLogger(MessageListenerImpl.class);

	private final BlockingQueue<Message> messagesDispatcher;

	private Set<MessageWrapper> messages;
	private Map<String, Long> nodeSync;
	private Thread myMessageInstance;
	private Thread pullThread;
	private Thread eraseThread;
	private Node myNode;
	private Map<MessageType, HandlerPayload> handlerMap;

	public MessageListenerImpl(Node myNode) throws RemoteException {

		super();

		this.myNode = myNode;
		UnicastRemoteObject.exportObject(this, 0);
		this.messagesDispatcher = new LinkedBlockingQueue<Message>();
		messages = Collections.synchronizedSet(new TreeSet<MessageWrapper>());
		this.nodeSync = new ConcurrentHashMap<String, Long>();
		this.handlerMap = new HashMap<MessageType, HandlerPayload>();

		this.handlerMap.put(MessageType.DISCONNECT,
				HandlerDisconnectPayload.getInstance(myNode));
		this.handlerMap.put(MessageType.NODE_AGENTS_LOAD,
				HandlerNodeAgentLoadPayload.getInstance(myNode));
		this.handlerMap.put(MessageType.NODE_AGENTS_LOAD_REQUEST,
				HandlerNodeAgentLoadRequestPayload.getInstance(myNode));
		this.handlerMap.put(MessageType.NODE_MARKET_DATA,
				HandlerMarketDataPayload.getInstance(myNode));
		this.handlerMap.put(MessageType.NODE_MARKET_DATA_REQUEST,
				HandlerMarketDataRequestPayload.getInstance(myNode));
		this.handlerMap.put(MessageType.RESOURCE_REQUEST,
				HandlerResourceRequest.getInstance(myNode));
		this.handlerMap.put(MessageType.RESOURCE_TRANSFER,
				HandlerResourceTransferPayload.getInstance(myNode));
		this.handlerMap.put(MessageType.RESOURCE_TRANSFER_CANCELED,
				HandlerResourceTransferCanceled.getInstance(myNode));

		LOGGER.info(myNode.getNodeId() + ": Lanzo el thread de MessageListener");
		/* Lanzo el thread del messageListener */
		this.myMessageInstance = this;
		this.myMessageInstance.start();
		LOGGER.info(myNode.getNodeId() + ": MessageListener thread lanzado");

		LOGGER.info(myNode.getNodeId()
				+ ": Lanzo el thread que hace pull de mensajes");
		/* Lanzo el thread del messageListener */
		this.pullThread = new PullThread(myNode, this);
		this.pullThread.start();
		LOGGER.info(myNode.getNodeId() + ": Pull Thread lanzado");

		LOGGER.info(myNode.getNodeId()
				+ ": Lanzo el thread que hace borra los mensajes de mensajes.");
		/* Lanzo el thread del messageListener */
		this.eraseThread = new EraseThread(myNode, this);
		this.eraseThread.start();
		LOGGER.info(myNode.getNodeId() + ": Erase Thread lanzado");

	}

	public Map<MessageType, HandlerPayload> getHandlerMap() {
		return handlerMap;
	}

	public Set<MessageWrapper> getMessages() {
		return messages;
	}

	@Override
	public boolean onMessageArrive(Message message) throws RemoteException {

		MessageWrapper arriveMessage = new MessageWrapper(message,
				System.currentTimeMillis());
		boolean resp;
		// LOGGER.info(myNode.getNodeId() + ": Llego un mensaje:");

		synchronized (messages) {
			if (!MessageListenerImpl.contains(messages, arriveMessage)) {

				if (isABroadcastMessage(arriveMessage.getMessage())) {

					// LOGGER.info(myNode.getNodeId()
					// + ": Lo agrego a la cola historica");
					messages.add(arriveMessage);
				}

				// LOGGER.info(myNode.getNodeId()
				// + ": Lo agrego a la cola del dispatcher");
				this.messagesDispatcher.offer(message);
				// LOGGER.info(myNode.getNodeId() +
				// ": Agregado al dispatcher.");
				resp = true;
			} else {
				// LOGGER.info("El mensaje ya lo tenia devuelvo False.");
				resp = false;
			}
		}

		return resp;
	}

	private boolean isABroadcastMessage(Message message) {
		MessageType type = message.getType();

		if (type.equals(MessageType.DISCONNECT)
				|| type.equals(MessageType.NODE_AGENTS_LOAD_REQUEST)
				|| type.equals(MessageType.NODE_MARKET_DATA_REQUEST)
				|| type.equals(MessageType.RESOURCE_REQUEST)) {

			return true;
		} else {
			return false;
		}

	}

	public static boolean contains(Set<MessageWrapper> messages,
			MessageWrapper arriveMessage) {

		for (MessageWrapper m : messages) {
			if (m.getMessage().equals(arriveMessage.getMessage())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId)
			throws RemoteException {

		// LOGGER.info("EL nodo:" + remoteNodeId +
		// " solicito los mensajes nuevos");

		List<Message> resp = new ArrayList<Message>();
		Long lastRemoteTime = this.nodeSync.get(remoteNodeId);
		if (lastRemoteTime == null) {
			lastRemoteTime = (long) 0;
		}
		Long timeUpdate = null;

		synchronized (this.messages) {

			Iterator<MessageWrapper> iterator = this.messages.iterator();
			timeUpdate = System.currentTimeMillis();
			// LOGGER.info("El tiempo de update es:" + timeUpdate
			// + "El ultimo tiempo remoto de update:" + lastRemoteTime);
			while (iterator.hasNext()) {

				MessageWrapper aux = iterator.next();
				if (aux.getNodeTimeStamp() >= lastRemoteTime) {
					// LOGGER.info("El mensaje:" + aux
					// + " se agrego a la respuesta.");
					resp.add(aux.getMessage());
				}
			}
			this.nodeSync.put(remoteNodeId, timeUpdate);
		}

		return resp;
	}

	@Override
	public void run() {
		while (!this.shouldFinish()) {
			try {
				// LOGGER.info(myNode.getNodeId()
				// + ": Dentro del run buscando un mensaje");
				this.dispatcher(this.messagesDispatcher.take());

			} catch (InterruptedException e) {
				// e.printStackTrace();
				Thread.interrupted();
			}
		}
	}

	private void dispatcher(final Message m) {

		// LOGGER.info("Inicio del dispatcher.");
		if (isABroadcastMessage(m)) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					// LOGGER.info("Thread creado para mandar mensajes broadcast.");
					try {
						myNode.getMyConnectionManager().getGroupCommunication()
								.broadcast(m);
					} catch (RemoteException e) {
						LOGGER.info("Fallo el broadcast");
						// Auto-generated catch block
						// e.printStackTrace();
					} catch (IllegalStateException e) {
						LOGGER.info("Fallo el broadcast");
					}
					// LOGGER.info("Muerte del thread para hacer broadcast.");

				}
			}).start();
		}

		// new Thread(new Runnable() {

		/*
		 * Vuelo la creacion de un nuevo thread voy a procesar todo de manera
		 * secuencial
		 */
		// @Override
		// public void run() {
		// LOGGER.info("Thread creado para ejecutar el mensaje sacado del dispatcher.");
		try {
			handlerMap.get(m.getType()).execute(m);
		} catch (Exception e) {
			LOGGER.info("Atrape una excepcion que no tuvo que ocurrir, continuo procesando otros mensajes");
			//e.printStackTrace();
		}
		// LOGGER.info("Muerte del thread para hacer ejecutar.");

		// }
		// }).start();
		// LOGGER.info("Fin del dispatcher.");

	}

	public void stopThreads() {
		try {
			((CleanableThread) this.eraseThread).finish();
			((CleanableThread) this.eraseThread).join();
			((CleanableThread) this.pullThread).finish();
			this.pullThread.join();
			((CleanableThread) this.myMessageInstance).finish();
			this.myMessageInstance.join();
		} catch (InterruptedException e) {
			LOGGER.info("Fallo la interrupcion.");
		}

	}
}