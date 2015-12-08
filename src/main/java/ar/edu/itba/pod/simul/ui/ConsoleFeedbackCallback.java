package ar.edu.itba.pod.simul.ui;

import org.joda.time.DateTime;

/**
 * Feedback callback that prints messages in the console
 */
public class ConsoleFeedbackCallback implements FeedbackCallback {
	@Override
	public void print(String format, Object... params) {
		String date = new DateTime().toString("HH:mm:ss");
		System.out.printf("%s --> %s\n", date, String.format(format, params));
	}
}
