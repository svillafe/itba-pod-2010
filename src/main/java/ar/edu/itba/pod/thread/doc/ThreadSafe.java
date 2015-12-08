package ar.edu.itba.pod.thread.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotations documents that a class is thread-safe.
 * <p> If used on an interface, it means that any implementation of the 
 * interface must be threadsafe </p>
 */
@Target(ElementType.TYPE)
public @interface ThreadSafe {
	// No values
}
