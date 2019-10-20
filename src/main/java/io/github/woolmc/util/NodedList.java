package io.github.woolmc.util;

import java.util.AbstractList;
import java.util.List;

/**
 * an immutable list of lists :P
 * @param <E>
 */
public class NodedList<E> extends AbstractList<E> {

	private final List<List<E>> combo;
	private final int size;

	public NodedList(List<List<E>> combo) {
		this.combo = combo;
		size = combo.stream().mapToInt(List::size).sum();
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
}
