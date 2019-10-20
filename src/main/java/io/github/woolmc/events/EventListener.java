package io.github.woolmc.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation used by event managers to autogenerate listeners for methods
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
	/**
	 * the priority of the event
	 * @see Priority
	 * @return defaults to normal
	 */
	Priority priority() default Priority.NORMAL;

	/**
	 * whether or not the event should be invoked if an event that extends the parent event is invoked
	 * @return defaults to false
	 */
	boolean subEvents() default false;
}
