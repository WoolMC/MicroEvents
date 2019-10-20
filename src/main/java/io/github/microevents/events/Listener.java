package io.github.microevents.events;

import java.util.function.Consumer;

/**
 * An action to perform when an event is invoked
 * @param <E> the type the listener is listening too
 */
public interface Listener<E extends Event> extends Consumer<E> {
	@Override
	default void accept(E e) {
		listen(e);
	}

	/**
	 * the action to perform
	 * @param event the event
	 */
	void listen(E event);
}
