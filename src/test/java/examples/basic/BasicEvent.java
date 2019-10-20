package examples.basic;

import io.github.microevents.events.Cancellable;
import java.util.Objects;

/**
 * {@link Cancellable} extends {@link io.github.microevents.events.Event} so no need to add it again
 */
public class BasicEvent implements Cancellable {
	private String value;
	private boolean cancelled;

	public BasicEvent(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void cancel(boolean val) {
		cancelled = val;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BasicEvent)) return false;

		BasicEvent event = (BasicEvent) o;

		if (cancelled != event.cancelled) return false;
		return Objects.equals(value, event.value);
	}

	@Override
	public int hashCode() {
		int result = value != null ? value.hashCode() : 0;
		result = 31 * result + (cancelled ? 1 : 0);
		return result;
	}
}
