package io.github.microevents.events;

import java.util.function.Consumer;

/**
 * An action to perform when an event is invoked
 * @param <E> the type the listener is listening too
 */
public interface Listener<E extends Event> extends Consumer<E> {
	@Override
	default void accept(E e) {
		try {
			listen(e);
		} catch (Throwable t) {
			System.out.println(getClass()+" threw an exception!");
			t.printStackTrace();
		}
	}

	/**
	 * the action to perform
	 * @param event the event
	 */
	void listen(E event);
}
