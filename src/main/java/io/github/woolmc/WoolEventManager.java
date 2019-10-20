package io.github.woolmc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hervian.lambda.Lambda;
import com.hervian.lambda.LambdaFactory;
import io.github.woolmc.events.EventListener;
import io.github.woolmc.events.*;
import io.github.woolmc.util.IDHolder;
import io.github.woolmc.util.InheritedMap;
import io.github.woolmc.util.NodedList;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class WoolEventManager implements EventManager {
	private static final int MAX_EVENTS = 4095;
	private static final int MAX_LISTENERS = Short.MAX_VALUE * 2;

	protected short eventId = 0;

	protected IDHolder listenerIDHolder = new IDHolder(MAX_LISTENERS);

	protected final Map<Class<? extends Event>, EventHandler> listeners = new HashMap<>();
	protected final Object2ShortMap<Class<? extends Event>> eventKeys = new Object2ShortOpenHashMap<>();
	protected final Short2ObjectMap<Class<? extends Event>> keyEvents = new Short2ObjectOpenHashMap<>();
	protected final Supplier<EventHandler<?>> eventHandlerSupplier;

	/**
	 * Creates a new wool event manager with a default EventHandler
	 *
	 * @param eventHandlerSupplier default event handler supplier
	 */
	public WoolEventManager(Supplier<EventHandler<?>> eventHandlerSupplier) {
		this.eventHandlerSupplier = eventHandlerSupplier;
	}

	/**
	 * Creates a new wool event manager with a default EventHandler
	 *
	 * @see WoolEventManager#WoolEventManager(Supplier)
	 * @see SimpleWoolEventHandler
	 */
	public WoolEventManager() {
		this(SimpleWoolEventHandler::new);
	}

	@Override
	public <T extends Event> int registerListener(Class<T> eventClass, Listener<T> listener, Priority priority, boolean callSupers) {
		if (listenerIDHolder.peek() == -1)
			throw new IllegalArgumentException("cannot register more than " + MAX_LISTENERS + " listeners!");

		int identity = newListenerID(priority, eventClass, callSupers);
		Objects.requireNonNull(listeners.get(eventClass), eventClass + " has not been registered!").register(identity, listener, priority);
		if (callSupers) for (Class<?> aSuper : getSupers(eventClass)) {
			EventHandler<?> eventHandler = listeners.get(aSuper);
			if (eventHandler != null) eventHandler.register(identity, (Listener) listener, priority);
		}

		return identity;
	}

	@Override
	public void unregister(int listenerID) {
		Class<?> event = getEvent(listenerID);
		Objects.requireNonNull(listeners.get(event), "No event with id " + getEvent(listenerID) + " found!").remove(listenerID);
		if (supers(listenerID)) for (Class<?> aSuper : getSupers(event)) {
			EventHandler<?> eventHandler = listeners.get(aSuper);
			if (eventHandler != null) eventHandler.remove(listenerID);
		}
		listenerIDHolder.remove(listenerID >>> 16);
	}

	/**
	 * keep a cache of the super classes to avoid uneeded reflection calls
	 */
	protected Cache<Class<?>, List<Class<?>>> superCache = CacheBuilder.newBuilder().maximumWeight(MAX_EVENTS).weigher((c, l) -> ((List<?>) l).size()).build();

	@Override
	public <T extends Event> void invoke(T event) {
		Objects.requireNonNull(listeners.get(event.getClass()), () -> event.getClass() + " is not registered").invoke(event);
	}

	protected List<Class<?>> getSupers(Class<?> classOf) {
		List<Class<?>> cache = superCache.getIfPresent(classOf);
		return cache == null ? cacheSupers(classOf) : cache;
	}

	protected List<Class<?>> cacheSupers(Class<?> event) {
		List<Class<?>> classes = new LinkedList<>();
		Class<?> current = event.getSuperclass();
		while (current != null && Event.class.isAssignableFrom(current)) {
			classes.add(current);
			current = current.getSuperclass();
		}
		superCache.put(event, classes);
		return classes;
	}


	@Override
	public void registerEvent(Class<? extends Event> eventClass) {
		registerEvent(eventClass, (EventHandler) eventHandlerSupplier.get());
	}

	@Override
	public <T extends Event> void registerEvent(Class<T> eventClass, EventHandler<T> handler) {
		if (eventId < MAX_EVENTS) {
			short id = eventId++;
			listeners.put(eventClass, handler);
			keyEvents.put(id, eventClass);
			eventKeys.put(eventClass, id);
		} else
			throw new IndexOutOfBoundsException("Cannot register more than " + MAX_EVENTS + " events in one event manager!");
	}

	/**
	 * a map that gives the functions that take in an object's class and return it's listener registers
	 */
	private final InheritedMap<Object, Function<Object, Integer>> methods = new InheritedMap<>(Object.class, c -> {
		Method[] objects = c.getDeclaredMethods();
		List<Function<Object, Integer>> declared = new ArrayList<>();
		for (Method method : objects) {
			EventListener listener = method.getAnnotation(EventListener.class);
			if (listener != null) {
				try {
					Lambda lambda = LambdaFactory.create(method);
					Class<?>[] params = method.getParameterTypes();
					if (params.length != 1 || !Event.class.isAssignableFrom(params[0]))
						throw new IllegalArgumentException(method + " has " + params.length + " or is trying to listen to an object that does not implement " + Event.class);
					declared.add(o -> registerListener((Class<? extends Event>) params[0], e -> lambda.invoke_for_void(o, e), listener.priority(), listener.subEvents()));
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}

			}
		}
		return declared;
	});

	@Override
	public List<Integer> registerEventListeners(Object object) {
		NodedList<Function<Object, Integer>> map = methods.getAttributes(object.getClass());
		List<Integer> list = new ArrayList<>(map.size());
		for (Function<Object, Integer> f : map)
			list.add(f.apply(object));
		return list;
	}


	/**
	 * generates a new listener id
	 *
	 * @param priority stored in 3 bits
	 * @param event stored in 12 bits
	 * @return a key that's a combination of the listener id, event, and priority all in one :tiny_potato:
	 */
	protected int newListenerID(Priority priority, Class<?> event, boolean callSupers) {
		return priority.ordinal() | (eventKeys.getShort(event) << 3) | ((callSupers ? 1 : 0) << 15) | (listenerIDHolder.getNew() << 16);
	}

	/**
	 * retrieves the class of the event stored in the listenerid
	 *
	 * @param sig
	 * @return
	 */
	protected Class<? extends Event> getEvent(int sig) {
		return keyEvents.get((short) ((sig >>> 3) & MAX_EVENTS));
	}

	/**
	 * checks if the listener listens to super classes
	 *
	 * @param sig
	 * @return
	 */
	protected static boolean supers(int sig) {
		return ((sig >>> 15) & 1) == 1;
	}
}
