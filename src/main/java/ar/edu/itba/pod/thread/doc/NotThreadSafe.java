package ar.edu.itba.pod.thread.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation documents that a class is not thread safe.
 * <p>
 * Although any class not documented as thread safe should be considered as
 * unsafe, this annotation can be used to document classes that may be used in
 * concurrent scenarios but where require external sinchronization by design
 * </p>
 */
@Target(ElementType.TYPE)
public @interface NotThreadSafe {
	// no values
}
