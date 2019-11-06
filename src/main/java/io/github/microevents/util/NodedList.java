package io.github.microevents.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * an immutable list of lists :P
 * @param <E> the stored type
 */
public class NodedList<E> extends AbstractList<E> {

	// the references to all of the lists
	final List<List<E>> combo;

	// the total size of all of the lists
	final int size;

	/**
	 * creates a new NodedList from the lists provided
	 * @param combo the lists that this will use
	 */
	public NodedList(List<List<E>> combo) {
		this.combo = combo;
		int sum = 0;
		for (List<E> es : combo) {
			int i = es.size();
			sum += i;
		}
		size = sum;
	}


	@Override
	public E get(int index) {
		for (List<E> es : combo)
			if (index - es.size() < 0) return es.get(index);
			else index -= es.size();
		throw new IndexOutOfBoundsException(index + " out of bounds!");
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Iterator<E> iterator() {
		return new NodedListIterator<>(this);
	}
}
