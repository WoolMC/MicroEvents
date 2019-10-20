package io.github.microevents.events;


/**
 * an event handler is responsible for executing its listeners for 1 event, it must obey the specified orders specified in
 * {@link Priority} and remove listeners correctly
 * @param <T>
 */
public interface EventHandler<T extends Event> {
	/**
	 * register a new listener with the id
	 * @param id the id of the listener
	 * @param listener the listener
	 * @param priority it's priority
	 */
	void register(int id, Listener<T> listener, Priority priority);

	/**
	 * removes a listener with the given id, do not expect fast removal of listeners
	 * @param id the id of the listener
	 */
	void remove(int id);

	/**
	 * invokes the event inside the event handler
	 * @param event an event of this event handler
	 * @param forSubListeners if the event should only be invoked for listeners that listen to sub events
	 */
	void invoke(T event, boolean forSubListeners);
}
