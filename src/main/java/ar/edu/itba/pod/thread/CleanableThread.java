package ar.edu.itba.pod.thread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread capable of performing a clean shutdown
 */
public class CleanableThread extends Thread {
	private final AtomicBoolean finish = new AtomicBoolean();

	/**
	 * Check if the this thread should finish (because the simulation is shutting down)
	 * @return true if this thread should finish
	 */
	protected final boolean shouldFinish() {
		return finish.get();
	}

	/**
	 * Notify the agent that it should finish and start a clean shutdown
	 */
	public void finish() {
		finish.set(true);
		interrupt();
	}
	
}
