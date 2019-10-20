package io.github.microevents.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.*;
import java.util.function.Function;

/**
 * represents a map that stores a relation between classes, and items that they can inherit, like methods (mostly a reflection util)
 * @param <P> the parent class type
 * @param <O> the object/attribute type
 */
public class InheritedMap<P, O> {
	// a map of the classes to their attributes
	private final Map<Class<? extends P>, List<O>> attributes = new Object2ObjectOpenHashMap<>();
	// a map of classes to their and inherited attributes
	private final Map<Class<? extends P>, NodedList<O>> cache = new Object2ObjectOpenHashMap<>();
	// the parent type
	private final Class<P> parentType;
	// a class to attributes converter
	private final Function<Class<? extends P>, List<O>> attributeSupplier;

	/**
	 * creates a new inherited map with the given parent class and attribute supplier
	 * @param parentClass the parent object
	 * @param attributeSupplier the converter
	 */
	public InheritedMap(Class<P> parentClass, Function<Class<? extends P>, List<O>> attributeSupplier) {
		this.parentType = parentClass;
		this.attributeSupplier = attributeSupplier;
	}

	/**
	 * gets all of the classes attributes, inherited and owned, this is cached
	 * @param type the class
	 * @return an immutable list of all the attributes
	 */
	public NodedList<O> getAttributes(Class<? extends P> type) {
		NodedList<O> nodedList = cache.get(type);
		if(nodedList == null) {
			List<List<O>> lists = new ObjectArrayList<>();
			while (type != null && parentType.isAssignableFrom(type)) {
				List<O> ok = map(type);
				if(ok.size() > 0)
					lists.add(map(type));
				type = (Class) type.getSuperclass();
			}
			nodedList = new NodedList<>(lists);
			cache.put(type, nodedList);
		}
		return nodedList;
	}

	private List<O> map(Class<? extends P> type) {
		List<O> types = attributes.get(type);
		if(types == null) {
			types = attributeSupplier.apply(type);
			attributes.put(type, types);
		}
		return types;
	}
}
