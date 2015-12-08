package ar.edu.itba.pod.Legajo47399.simul.comunication;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.thread.CleanableThread;

public class TransactionableTimer extends CleanableThread {

	private final static Logger LOGGER = Logger
			.getLogger(TransactionableImpl.class);

	
	private int total;

	private TransactionableImpl myTransaction;

	public TransactionableTimer(int total, TransactionableImpl myTransactionable) {

		this.total = total;
		this.myTransaction = myTransactionable;
	}

	public void run() {
		LOGGER.info("Empieza el timer del transaction");
		try{
			Thread.sleep(this.total);
		}
		catch (InterruptedException a) {
			return;
		}
		

		if (!Thread.interrupted()){
			/*Si no me interrumpieron llamo a mi funcion de timeout*/
			LOGGER.info("ERROR:Llegue al timeout y la transaccion no termino .");
			timeout();
		}else{
			LOGGER.info("Mataron al TimeOut");
		}
			

	}

	public void timeout() {
		myTransaction.timeoutCumplido();
	}
}