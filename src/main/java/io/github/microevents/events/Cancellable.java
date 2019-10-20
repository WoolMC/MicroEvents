package io.github.microevents.events;

/**
 * {@link Event}s that can be cancelled should implement this interface,
 * in {@link io.github.microevents.MicroEventManager#invoke(Event)} it checks if the event
 * implements cancellable in order to short circuit cancelled events
 */
public interface Cancellable extends Event {
	/**
	 * sets the cancelled status of the event
	 * @param val if the event is cancelled
	 */
	void cancel(boolean val);

	/**
	 * if the current event has already been cancelled
	 * @return true if cancelled
	 */
	boolean isCancelled();
}
