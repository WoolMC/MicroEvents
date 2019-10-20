package examples.basic.mains;

import examples.basic.BasicEvent;
import examples.basic.SubBasicEvent;
import io.github.microevents.MicroEventManager;
import io.github.microevents.events.EventListener;
import io.github.microevents.events.EventManager;
import io.github.microevents.events.Priority;

/**
 * An example class demonstrating how to use the event manager
 * @author devan
 */
public class BasicExample {
	public static void main(String[] args) {
		EventManager manager = new MicroEventManager(); // MicroEventManager is the default implementation
		manager.registerEvent(BasicEvent.class); // we must register the event before we call it or register any listeners for it
		manager.registerEvent(SubBasicEvent.class); // even subclasses must be registered

		manager.registerListener(
			BasicEvent.class, // this is the type of event we are listening to
			b -> b.cancel(true), // this is the action we want to perform
			Priority.TOP, // this is our priority
			false // this states whether we want to listen to events that are subclasses of BasicEvent
		);

		BasicExample example = new BasicExample(); // this is our instance
		manager.registerEventListeners(example); // this will register all of the methods, and don't worry, this is fast
		manager.registerStaticEventListeners(BasicExample.class); // if you want to register static listeners, use this method

		manager.invoke(new BasicEvent("Hey!")); // invokes the manager with the event

		// prints ->

		// Event was cancelled!
		// Hello from static!
		// examples.basic.BasicEvent@...

		System.out.println();

		// only the BasicExample#staticEventListener(BasicEvent) will receive this event
		// since it's the only listener that listens to subclasses of the event
		manager.invoke(new SubBasicEvent());

		// prints ->

		// Hello from static!
		// examples.basic.SubBasicEvent@6193b845

		// notice that it doesn't print "Event was cancelled!"
		// that's because our first listener does not listen to sub events
		// so it's never invoked
	}

	// the first listener cancels the event, and is of the lowest priority, so this method will never execute
	@EventListener(priority = Priority.FINAL)
	public final void eventListener(BasicEvent event) {
		System.out.println(event);
	}

	// even though the first listener is registered first and cancels the event
	// Priority.TOP ignores cancels, so even if any listener with TOP priority cancels it, we will still receive the event
	// sub events means that subclasses of basic event will still be invoked
	@EventListener(priority = Priority.TOP, subEvents = true)
	public static void staticEventListener(BasicEvent event) {
		if(event.isCancelled())
			System.out.println("Event was cancelled!");
		System.out.println("Hello from static!");
		System.out.println(event);
	}
}
