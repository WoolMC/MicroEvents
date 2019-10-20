package io.github.microevents.util;

/**
 * A pair of an object and an integer
 * @param <A> the object type
 */
public class IntPair<A> {
	public A a;
	public int b;

	public IntPair(A a, int b) {
		this.a = a;
		this.b = b;
	}
}
