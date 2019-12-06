package examples.basic.mains;

import examples.basic.BasicEvent;
import examples.basic.SubBasicEvent;
import io.github.microevents.MicroEventManager;
import io.github.microevents.events.EventListener;
import io.github.microevents.events.EventManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class StressTesting {
	public static void main(String[] args) {

		EventManager manager = new MicroEventManager();
		manager.registerEvent(BasicEvent.class);
		manager.registerEvent(SubBasicEvent.class);


		test(() -> manager.registerEventListeners(new StressTesting()), "warmup list", 100);
		test(() -> manager.invoke(new SubBasicEvent()), "warmup event", ITERATIONS);


		test(() -> manager.invoke(new SubBasicEvent()), "sub test", ITERATIONS);

		test(() -> manager.invoke(new BasicEvent("Hey!")), "nosub test", ITERATIONS);

		// on my laptop

		// 1000000 iterations of    warmup took   66ms (66ns/op)
		// 1000000 iterations of  sub test took   39ms (39ns/op)
		// 1000000 iterations of nosub tes took   77ms (77ns/op)

		//550ms
		//2805ms
	}

	private static final int ITERATIONS = 1_000_000;

	public static void test(Runnable runnable, String name, int iters) {
		long start = System.nanoTime();
		for(int x = 0; x < iters; x++)
			runnable.run();
		long elapsed = System.nanoTime()-start;
		System.out.printf("%d iterations of %9.9s took %4dms (%dns/op)\n", iters, name, elapsed/1_000_000, elapsed/iters);

	}

	@EventListener
	public void noSub(BasicEvent event) {
		// do nothing, this is just an overhead test
	}

	@EventListener(subEvents = true)
	public void sub(BasicEvent event) {
		// same here
	}

	@EventListener
	public void instanceListener(BasicEvent event) {

	}
}
