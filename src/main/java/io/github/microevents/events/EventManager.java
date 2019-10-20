package io.github.microevents.events;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;


/**
 * An event handler manages the registering of listeners, and invocation of events
 */
public interface EventManager {
	/**
	 * Registers a new listener to the event handler, not thread-safe
	 * @param eventClass the class of the event
	 * @param listener the listener
	 * @param priority the priority of the listener
	 * @param callSubs whether or not the listener should be registered for sub classes of the event
	 * @param <T> the type of event
	 * @return the listener id
	 */
	<T extends Event> int registerListener(Class<T> eventClass, Listener<T> listener, Priority priority, boolean callSubs);

	/**
	 * removes the listener from the event handler, not thread-safe
	 * @param listenerID the listener's id
	 */
	void unregister(int listenerID);

	/**
	 * throws a new event in the event handler, thread-safe
	 * @param event the object
	 */
	<T extends Event> void invoke(T event);

	/**
	 * registers the event, must be called before any invocation, not thread-safe
	 * @param eventClass
	 */
	void registerEvent(Class<? extends Event> eventClass);

	/**
	 * registers the event with it's own event handler
	 * @param eventClass the class of the event
	 * @param handler the event handler to register with
	 * @param <T> the type of event
	 */
	<T extends Event> void registerEvent(Class<T> eventClass, EventHandler<T> handler);


	/**
	 * registers all the EventListener annotated methods of the object
	 * @see EventListener
	 * @param object the instance
	 * @return the ids of all the created listeners
	 */
	IntList registerEventListeners(Object object);

	/**
	 * registers all the <b>declared</b> static EventListener annotated methods of a class
	 * @param classOf the class to check for
	 * @return the ids of all the created listeners
	 * @see Class#getDeclaredMethods()
	 */
	IntList registerStaticEventListeners(Class<?> classOf);
}
