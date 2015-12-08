package ar.edu.itba.pod.thread.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotations documents that a class is immutable.
 */
@Target(ElementType.TYPE)
public @interface Immutable {
	// No values
}
