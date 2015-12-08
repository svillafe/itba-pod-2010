package ar.edu.itba.pod.Legajo47399.simul.comunication;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.thread.CleanableThread;

public class ThreePhaseCommitTimer extends CleanableThread {

	private final static Logger LOGGER = Logger
			.getLogger(TransactionableImpl.class);

	private long timeout;

	private ThreePhaseCommitImpl my3PC;

	public ThreePhaseCommitTimer(ThreePhaseCommitImpl my3PC, long total) {
		this.timeout = total;
		this.my3PC = my3PC;
	}

	public void run() {

		LOGGER.info("Empieza el 3PC timer");
		try {
			Thread.sleep(this.timeout);
		} catch (InterruptedException a) {
			LOGGER.info("Mataron el timer del 3PC");
			return;
		}

		if (!Thread.interrupted()) {
			timeout();
		}

	}

	public void timeout() {
		LOGGER.info("3PC Timer Cumplido, llamo al onTimeOut");
		try {
			my3PC.onTimeout();
		} catch (RemoteException e) {
			// falla el abort por una falla remota.
			LOGGER.info("Falla el abort por un falla remota");

		}
	}
}
