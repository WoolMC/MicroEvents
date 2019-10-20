package io.github.woolmc.util;

import java.util.*;
import java.util.function.Function;

/**
 * represents a map that stores a relation between classes, and items that they can inherit, like methods (mostly a reflection util)
 * @param <A> the parent class type
 * @param <T> the object type
 */
public class InheritedMap<A, T> {
	private final Map<Class<? extends A>, List<T>> attributes = new HashMap<>();
	private final Map<Class<? extends A>, NodedList<T>> cache = new HashMap<>();
	private final Class<A> parentType;
	private final Function<Class<? extends A>, List<T>> attributeSupplier;

	public InheritedMap(Class<A> parentClass, Function<Class<? extends A>, List<T>> attributeSupplier) {
		this.parentType = parentClass;
		this.attributeSupplier = attributeSupplier;
	}

	public NodedList<T> getAttributes(Class<? extends A> type) {
		NodedList<T> nodedList = cache.get(type);
		if(nodedList == null) {
			List<List<T>> lists = new ArrayList<>();
			while (type != null && parentType.isAssignableFrom(type)) {
				lists.add(map(type));
				type = (Class) type.getSuperclass();
			}
			nodedList = new NodedList<>(lists);
			cache.put(type, nodedList);
		}
		return nodedList;
	}

	private List<T> map(Class<? extends A> type) {
		List<T> types = attributes.get(type);
		if(types == null) {
			types = attributeSupplier.apply(type);
			attributes.put(type, types);
		}
		return types;
	}
}
