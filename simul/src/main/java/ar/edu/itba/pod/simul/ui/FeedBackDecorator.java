package ar.edu.itba.pod.simul.ui;


/**
 * Abstract decorator class that encapsulates having a delegate and a feedback
 * target
 * @param <T>
 */
public abstract class FeedBackDecorator<T> {
	private final FeedbackCallback callback;
	private final T delegate;

	public FeedBackDecorator(FeedbackCallback callback, T delegate) {
		super();
		this.callback = callback;
		this.delegate = delegate;
	}
	
	protected void feedback(String format, Object... params) {
		callback.print(format, params);
	}
	
	protected FeedbackCallback callback() {
		return callback;
	}
	
	protected T delegate() {
		return delegate;
	}
}
