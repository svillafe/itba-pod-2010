package ar.edu.itba.pod.simul.ui;

/**
 * Simple callback interface used to provide feedback
 */
public interface FeedbackCallback {
	/**
	 * Sends feedback following the <code>String.format</code> semantics 
	 * @param format The string format
	 * @param params The (optional) parameters
	 */
	public void print(String format, Object... params);
}
