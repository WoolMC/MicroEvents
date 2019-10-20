package io.github.woolmc.events;

import io.github.woolmc.events.Event;

public interface Cancellable extends Event {
	/**
	 * sets the cancelled status of the event
	 * @param val if the event is cancelled
	 */
	void cancel(boolean val);
	boolean isCancelled();
}
