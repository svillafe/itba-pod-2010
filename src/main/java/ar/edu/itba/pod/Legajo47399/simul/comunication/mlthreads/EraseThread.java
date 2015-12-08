package ar.edu.itba.pod.Legajo47399.simul.comunication.mlthreads;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.Legajo47399.simul.comunication.MessageListenerImpl;
import ar.edu.itba.pod.Legajo47399.simul.comunication.MessageWrapper;
import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.thread.CleanableThread;

public class EraseThread extends CleanableThread {

	private final static Logger LOGGER = Logger.getLogger(EraseThread.class);

	private Node myNode;
	private MessageListenerImpl myMessageListener;

	public EraseThread(Node myNode, MessageListenerImpl myMessageListener) {
		this.myNode = myNode;
		this.myMessageListener = myMessageListener;
	}

	@Override
	public void run() {
		LOGGER.info("Run el thread que hace erase.");
		Set<MessageWrapper> myMessages = myMessageListener.getMessages();
		Set<MessageWrapper> aux = new TreeSet<MessageWrapper>();
		boolean salir = false;

		while (!this.shouldFinish()) {

			try {
				Thread.sleep(Node.ERASE_THREAD_TIME);
			} catch (InterruptedException e) {
				LOGGER.info("Me despertaron.");
			}

			synchronized (myMessages) {
				// LOGGER.info("Los mensajes cargados son:"+myMessages);

				Iterator<MessageWrapper> itr = myMessages.iterator();
				salir = false;
				while (itr.hasNext() && !salir) {

					MessageWrapper element = itr.next();

					if (System.currentTimeMillis() - element.getNodeTimeStamp() > Node.TIME_ERASE) {
						aux.add(element);

					} else {
						salir = true;
					}
				}

				myMessages.removeAll(aux);
				// LOGGER.info("Borrado los mensajes viejos:" + aux);

			}
		}

		LOGGER.info("Terminado el thread de erase.");
	}
}
