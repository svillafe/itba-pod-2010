package ar.edu.itba.pod.thread.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This interface documents locks that guard a given property against
 */
@Target(ElementType.FIELD)
public @interface GuardedBy {
	/**
	 * Pointer to the lock that guards access to this resource 
	 * @return
	 */
	String value();
}
