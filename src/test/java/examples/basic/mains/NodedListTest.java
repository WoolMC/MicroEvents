package examples.basic.mains;

import io.github.microevents.util.NodedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodedListTest {
	public static void main(String[] args) {
		List<List<Integer>> integers = new ArrayList<>();
		for(int x = 0; x < 10; x++)
			integers.add(new ArrayList<>());
		int val = 0;

		for (List<Integer> integer : integers)
			for (int i = 0; i < 10; i++)
				integer.add(val++);

		NodedList<Integer> nodedList = new NodedList<>(integers);
		Iterator<Integer> iterator = nodedList.iterator();

		while(iterator.hasNext())
			System.out.println(iterator.next());

	}
}
