package io.github.microevents.util;

import java.util.Iterator;
import java.util.List;

public class NodedListIterator<E> implements Iterator<E> {
	private NodedList<E> list;
	private List<E> currentList; // the current list being iterated
	private int index = 0; // current index
	private int listPos = 0; // position of the current list in the combo
	private int listIndex = 0; // index of 0 in the current list in the combo

	public NodedListIterator(NodedList<E> list) {
		this.list = list;
		if (list.combo.size() > 0) currentList = list.combo.get(0);
	}

	@Override
	public boolean hasNext() {
		return list.size != index && currentList != null && (index < listIndex + currentList.size() || list.combo.size() > listPos + 1);
	}

	@Override
	public E next() {
		E val;
		if (index < listIndex + currentList.size()) val = currentList.get(index - listIndex);
		else {
			listIndex += currentList.size();
			do {
				if(listPos+1 == list.combo.size())
					return null;
				currentList = list.combo.get(++listPos);
			} while (currentList.size() == 0);
			val = currentList.get(0);
		}
		index++;
		return val;
	}
}
