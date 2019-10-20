package io.github.woolmc;

import io.github.woolmc.events.Cancellable;
import io.github.woolmc.events.Event;
import io.github.woolmc.events.EventHandler;
import io.github.woolmc.events.Listener;
import io.github.woolmc.events.Priority;
import io.github.woolmc.util.Pair;
import java.util.ArrayList;

public class SimpleWoolEventHandler<E extends Event> implements EventHandler<E> {
	protected final ArrayList<Pair<Listener<E>, Integer>>[] listeners = new ArrayList[7];
	protected byte hasAny;

	public SimpleWoolEventHandler() {
		for (int i = 0; i < listeners.length; i++)
			listeners[i] = new ArrayList<>();
	}

	@Override
	public void register(int id, Listener<E> listener, Priority priority) {
		int ordinal = priority.ordinal();
		int flag = 1 << ordinal;
		listeners[ordinal].add(new Pair<>(listener, id));
		hasAny |= flag;
	}

	@Override
	public void remove(int id) {
		listeners[id & 7].removeIf(pair -> pair.b == id);
	}

	@Override
	public void invoke(E event) {
		boolean attempt = invoke(0, event, false);
		for (int x = 1; x < 7 && !attempt; x++)
			attempt = invoke(x, event, true);
	}

	protected boolean invoke(int priority, E event, boolean obeyCancelled) {
		if ((hasAny & (1 << priority)) != 0) if (event instanceof Cancellable) {
			for (Pair<Listener<E>, Integer> pair : listeners[priority])
				if (((Cancellable) event).isCancelled() && obeyCancelled) break;
				else pair.a.accept(event);
			return ((Cancellable) event).isCancelled();
		} else for (Pair<Listener<E>, Integer> pair : listeners[priority])
			pair.a.accept(event);
		return false;
	}
}
