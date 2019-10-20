package io.github.woolmc.events;

public interface EventHandler<T extends Event> {
	/**
	 * register a new listener with the id
	 * @param id the id of the listener
	 * @param listener the listener
	 * @param priority it's priority
	 */
	void register(int id, Listener<T> listener, Priority priority);

	/**
	 * removes a listener with the given id, expect O(N) time removal
	 * @param id
	 */
	void remove(int id);

	/**
	 * invokes the event inside the event handler
	 * @param event
	 */
	void invoke(T event);
}
