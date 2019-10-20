package io.github.microevents;

import io.github.microevents.events.*;
import io.github.microevents.util.IntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;

public class MicroEventHandler<E extends Event> implements EventHandler<E> {
	// an array organized by priority of all of the listeners
	protected final ObjectArrayList<IntPair<Listener<E>>>[] listeners = new ObjectArrayList[7];
	// if the array has any listeners for that priority, just for faster access
	protected byte hasAny;

	// if the array has any sub listeners for that priority, the bit is true for that index
	protected byte hasSub;

	public MicroEventHandler() {
		for (int i = 0; i < listeners.length; i++)
			listeners[i] = new ObjectArrayList<>();
	}

	@Override
	public void register(int id, Listener<E> listener, Priority priority) {
		int ordinal = priority.ordinal();
		int flag = 1 << ordinal;
		listeners[ordinal].add(new IntPair<>(listener, id));
		hasAny |= flag;
		if (subs(id)) hasSub |= flag;
	}

	@Override
	public void remove(int id) {
		listeners[id & 7].removeIf(pair -> pair.b == id);
	}

	@Override
	public void invoke(E event, boolean sub) {
		if (!(hasAny == 0 || sub && hasSub == 0)) {
			boolean attempt = invoke(0, event, false, sub);
			for (int x = 1; x < 7 && !attempt; x++)
				attempt = invoke(x, event, true, sub);
		}
	}

	private boolean invoke(int priority, E event, boolean obeyCancelled, boolean sub) {
		if ((!sub || (hasSub & (1 << priority)) != 0) && (hasAny & (1 << priority)) != 0)
			if (event instanceof Cancellable) {
				for (IntPair<Listener<E>> pair : listeners[priority])
					if (((Cancellable) event).isCancelled() && obeyCancelled) break;
					else if (!sub || subs(pair.b)) pair.a.listen(event);
				return ((Cancellable) event).isCancelled();
			} else for (IntPair<Listener<E>> pair : listeners[priority])
				pair.a.listen(event);
		return false;
	}

	/**
	 * checks if the listener listens to super classes
	 *
	 * @param sig
	 * @return
	 */
	private static boolean subs(int sig) {
		return ((sig >>> 15) & 1) == 1;
	}
}
