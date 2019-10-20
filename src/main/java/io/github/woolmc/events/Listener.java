package io.github.woolmc.events;

import io.github.woolmc.events.Event;
import java.util.function.Consumer;

public interface Listener<T extends Event> extends Consumer<T> {}
