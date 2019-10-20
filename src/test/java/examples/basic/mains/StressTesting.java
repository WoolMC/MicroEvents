package examples.basic.mains;

import examples.basic.BasicEvent;
import examples.basic.SubBasicEvent;
import io.github.microevents.MicroEventManager;
import io.github.microevents.events.EventListener;
import io.github.microevents.events.EventManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Supplier;

public class StressTesting {
	public static void main(String[] args) {

		EventManager manager = new MicroEventManager();
		manager.registerEvent(BasicEvent.class);
		manager.registerEvent(SubBasicEvent.class);

		manager.registerStaticEventListeners(StressTesting.class);

		test(() -> manager.invoke(new SubBasicEvent()), "warmup");

		test(() -> manager.invoke(new SubBasicEvent()), "sub test");

		test(() -> manager.invoke(new BasicEvent("Hey!")), "nosub test");

		// on my laptop

		// 1000000 iterations of    warmup took   66ms (66ns/op)
		// 1000000 iterations of  sub test took   39ms (39ns/op)
		// 1000000 iterations of nosub tes took   77ms (77ns/op)

	}

	private static final int ITERATIONS = 1_000_000;
	public static <T> List<T> test(Supplier<T> supplier, String name) {
		long start = System.nanoTime();

		List<T> arr = new ObjectArrayList<>();
		for(int x = 0; x < ITERATIONS; x++)
			arr.add(supplier.get());
		long elapsed = System.nanoTime()-start;

		System.out.printf("%d iterations of %9.9s took %4dms (%dns/op)\n", ITERATIONS, name, elapsed/1_000_000, elapsed/ITERATIONS);
		return arr;
	}

	public static void test(Runnable runnable, String name) {
		long start = System.nanoTime();

		for(int x = 0; x < ITERATIONS; x++)
			runnable.run();
		long elapsed = System.nanoTime()-start;


		System.out.printf("%d iterations of %9.9s took %4dms (%dns/op)\n", ITERATIONS, name, elapsed/1_000_000, elapsed/ITERATIONS);
	}

	@EventListener
	public static void noSub(BasicEvent event) {
		// do nothing, this is just an overhead test
	}

	@EventListener(subEvents = true)
	public static void sub(BasicEvent event) {
		// same here
	}

	@EventListener
	public void instanceListener(BasicEvent event) {

	}
}
