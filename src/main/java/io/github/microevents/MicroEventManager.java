package io.github.microevents;

import com.hervian.lambda.Lambda;
import com.hervian.lambda.LambdaFactory;
import io.github.microevents.events.*;
import io.github.microevents.util.IDHolder;
import io.github.microevents.util.InheritedMap;
import io.github.microevents.util.NodedList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * The standard implementation for the {@link EventManager}
 */
public class MicroEventManager implements EventManager {
	private static final Logger LOGGER = Logger.getLogger("MicroEventManager");

	// max events supported, due to id system
	private static final int MAX_EVENTS = 4095;

	// max listeners at any given time supported
	private static final int MAX_LISTENERS = Short.MAX_VALUE * 2;

	// currently unassigned event id
	private short eventId = 0;

	// stores all available ids for listeners
	private IDHolder listenerIDHolder = new IDHolder(MAX_LISTENERS);

	// map of the class of the events to its handler
	private final Map<Class<? extends Event>, EventHandler> listeners = new Object2ObjectOpenHashMap<>();

	// map of event class to event key for generating listener ids
	private final Object2ShortMap<Class<? extends Event>> eventKeys = new Object2ShortOpenHashMap<>();

	// map of event keys to event class for parsing listener ids
	private final Short2ObjectMap<Class<? extends Event>> keyEvents = new Short2ObjectOpenHashMap<>();

	// the default event handler supplier
	private final Supplier<EventHandler<?>> eventHandlerSupplier;

	/**
	 * Creates a new wool event manager with a default EventHandler
	 *
	 * @param eventHandlerSupplier default event handler supplier
	 */
	public MicroEventManager(Supplier<EventHandler<?>> eventHandlerSupplier) {
		this.eventHandlerSupplier = eventHandlerSupplier;
	}

	/**
	 * Creates a new wool event manager with a default EventHandler
	 *
	 * @see MicroEventManager#MicroEventManager(Supplier)
	 * @see MicroEventHandler
	 */
	public MicroEventManager() {
		this(MicroEventHandler::new);
	}

	@Override
	public <T extends Event> int registerListener(Class<T> eventClass, Listener<T> listener, Priority priority, boolean callSubs) {
		if (listenerIDHolder.full()) // check if the id holder has exceeded capacity
			throw new IllegalArgumentException("cannot register more than " + MAX_LISTENERS + " listeners!");
		int identity = newListenerID(priority, eventClass, callSubs);
		Objects.requireNonNull(listeners.get(eventClass), eventClass + " has not been registered!").register(identity, listener, priority);
		if (callSubs) for (Class<?> aSuper : getSupers(eventClass)) {
			EventHandler<?> eventHandler = listeners.get(aSuper);
			if (eventHandler != null) eventHandler.register(identity, (Listener) listener, priority);
		}

		return identity;
	}

	@Override
	public void unregister(int listenerID) {
		Class<? extends Event> event = getEvent(listenerID);
		Objects.requireNonNull(listeners.get(event), "No event with id " + getEvent(listenerID) + " found!").remove(listenerID);
		listenerIDHolder.remove(listenerID >>> 16);
	}

	/**
	 * keep a cache of the super classes to avoid unneeded reflection calls
	 */
	protected Map<Class<? extends Event>, List<Class<? extends Event>>> eventSubcache = new Object2ObjectOpenHashMap<>();

	@Override
	public <T extends Event> void invoke(T event) {
		Class<? extends Event> eventClass = event.getClass();
		invokeFor(event, eventClass, false);
		for (Class<? extends Event> aSuper : getSupers(eventClass))
			invokeFor(event, aSuper, true);
	}

	private <T extends Event> void invokeFor(T event, Class<? extends Event> clazz, boolean forSubs) {
		EventHandler handler = listeners.get(clazz);
		assert handler != null || forSubs : clazz + " not registered!";
		if (handler != null) handler.invoke(event, forSubs);
	}

	private List<Class<? extends Event>> getSupers(Class<? extends Event> classOf) {
		List<Class<? extends Event>> cache = eventSubcache.get(classOf);
		return cache == null ? cacheSupers(classOf) : cache;
	}

	private List<Class<? extends Event>> cacheSupers(Class<? extends Event> event) { // TODO replace with getSubclasses
		ObjectList<Class<? extends Event>> classes = new ObjectArrayList<>();
		Class<?> current = event.getSuperclass();
		while (current != null && Event.class.isAssignableFrom(current)) {
			classes.add((Class<? extends Event>) current);
			current = current.getSuperclass();
		}

		eventSubcache.put(event, classes);
		return classes;
	}


	@Override
	public void registerEvent(Class<? extends Event> eventClass) {
		registerEvent(eventClass, (EventHandler) eventHandlerSupplier.get());
	}

	@Override
	public <T extends Event> void registerEvent(Class<T> eventClass, EventHandler<T> handler) {
		assert eventId < MAX_EVENTS : "Cannot register more than " + MAX_EVENTS + " events in one event manager!";
		short id = eventId++;
		listeners.put(eventClass, handler);
		keyEvents.put(id, eventClass);
		eventKeys.put(eventClass, id);
	}

	/**
	 * a map that gives the functions that take in an object's class and return it's listener registers
	 */
	private final InheritedMap<Object, Object2IntFunction<Object>> instanceMethods = new InheritedMap<>(Object.class, c -> {
		Method[] objects = c.getDeclaredMethods();
		ObjectList<Object2IntFunction<Object>> declared = new ObjectArrayList<>();
		for (Method method : objects)
			if (!(Modifier.isStatic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers()))) { // only for instance methods
				EventListener listener = method.getAnnotation(EventListener.class);
				if (listener != null) try {
					Class<?>[] params = method.getParameterTypes();
					if (params.length != 1)
						throw new IllegalArgumentException(method + " has " + params.length + " parameters! Should have 1!");
					else if (!Event.class.isAssignableFrom(params[0]))
						throw new IllegalArgumentException(method + " is trying to listen to " + params[0] + " which does not extend " + Event.class);

					Lambda lambda = LambdaFactory.create(method); // for faster method invoke
					declared.add(o -> registerListener((Class<? extends Event>) params[0], e -> lambda.invoke_for_void(o, e), listener.priority(), listener.subEvents()));
					if (!Modifier.isFinal(method.getModifiers())) // TODO find a performant way to check for super methods
						LOGGER.warning(method + " is not final, it is recommended to make listener methods final as subclasses that override them will get called twice per event!");
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
			}
		return declared;
	});

	@Override
	public IntList registerEventListeners(Object object) {
		NodedList<Object2IntFunction<Object>> map = instanceMethods.getAttributes(object.getClass());
		IntList list = new IntArrayList(map.size());
		for (Object2IntFunction<Object> f : map)
			list.add(f.applyAsInt(object));
		return list;
	}

	@Override
	public IntList registerStaticEventListeners(Class<?> classOf) {
		IntList integers = new IntArrayList();
		for (Method method : classOf.getDeclaredMethods())
			if (Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers())) {
				EventListener listener = method.getAnnotation(EventListener.class);
				if (listener != null) try {
					Class<?>[] params = method.getParameterTypes();
					if (params.length != 1)
						throw new IllegalArgumentException(method + " has " + params.length + " parameters! Should have 1!");
					else if (!Event.class.isAssignableFrom(params[0]))
						throw new IllegalArgumentException(method + " is trying to listen to " + params[0] + " which does not extend " + Event.class);
					Lambda lambda = LambdaFactory.create(method);
					integers.add(registerListener((Class<? extends Event>) params[0], lambda::invoke_for_void, listener.priority(), listener.subEvents()));
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			}
		return integers;
	}


	/**
	 * generates a new listener id
	 *
	 * @param priority stored in 3 bits
	 * @param event stored in 12 bits
	 * @return a key that's a combination of the listener id, event, and priority all in one :tiny_potato:
	 */
	private int newListenerID(Priority priority, Class<?> event, boolean callSubclasses) {
		return priority.ordinal() | (eventKeys.getShort(event) << 3) | ((callSubclasses ? 1 : 0) << 15) | (listenerIDHolder.next() << 16);
	}

	/**
	 * retrieves the class of the event stored in the listenerid
	 *
	 * @param sig
	 * @return
	 */
	private Class<? extends Event> getEvent(int sig) {
		return keyEvents.get((short) ((sig >>> 3) & MAX_EVENTS));
	}


}
