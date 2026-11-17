package com.skillcraft.util.collection_utils;

import static com.skillcraft.util.collection_utils.PredicatesUtil.eq;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CollectionsUtil {

	public static <T> boolean contains(Collection<T> set, T v) {
		if (isEmpty(set)) {
			return false;
		}
		return set.contains(v);
	}

	public static <T> boolean isEmpty(Collection<T> s) {
		return org.apache.commons.collections4.CollectionUtils.isEmpty(s);
	}

	public static <T> boolean isNotEmpty(Collection<T> s) {
		return !isEmpty(s);
	}

	public static <T> long size(Collection<T> source) {
		return StreamsUtil.stream(source).count();
	}

	public static <T> Optional<T> first(Collection<T> l, Predicate<T> p) {
		return StreamsUtil.stream(l).filter(p).findFirst();
	}

	public static <T, V> Optional<T> first(Collection<T> l, Function<T, V> mapper, V value) {
		return StreamsUtil.stream(l).filter(eq(mapper, value)).findFirst();
	}

	public static <T> Optional<T> first(Collection<T> l, Predicate<T> p1, Predicate<T> p2) {
		return StreamsUtil.stream(l).filter(p1).filter(p2).findFirst();
	}

	public static <T> T firstOrNull(Collection<T> l, Predicate<T> p) {
		return first(l, p).orElse(null);
	}

	public static <T> Optional<T> first(Collection<T> l) {
		return StreamsUtil.stream(l).findFirst();
	}

	public static <T> T firstOrNull(Collection<T> l) {
		return first(l).orElse(null);
	}

	public static <T> boolean anyMatch(Collection<T> source, Predicate<T> p) {
		return StreamsUtil.stream(source).anyMatch(p);
	}

	/**
	 * @param source - null  and empty returns TRUE!!!
	 */
	public static <T> boolean allMatch(Collection<T> source, Predicate<T> p) {
		return StreamsUtil.stream(source).allMatch(p);
	}

	public static <T, R> boolean equal(Collection<T> one, Collection<R> two, BiPredicate<T, R> matcher) {
		Collection<T> s1 = one == null ? emptyList() : one;
		Collection<R> s2 = two == null ? emptyList() : two;
		if (s1.size() != s2.size()) {
			return false;
		}
		List<T> l1 = new ArrayList<>(s1);
		List<R> l2 = new ArrayList<>(s2);
		for (R r : l2) {
			l1 = removeFirst(l1, t -> matcher.test(t, r));
		}
		return l1.size() == 0;
	}

	public static <T> void forEach(T[] source, Consumer<T> consumer) {
		StreamsUtil.stream(source).forEach(consumer);
	}

	public static <T> void forEach(Collection<T> source, Consumer<T> consumer) {
		StreamsUtil.stream(source).forEach(consumer);
	}

	public static <T> List<T> removeFirst(Collection<T> source, Predicate<T> matcher) {
		List<T> rez = new ArrayList<>(source == null ? emptyList() : source);
		Iterator<T> it = rez.iterator();
		while (it.hasNext()) {
			if (matcher.test(it.next())) {
				it.remove();
				return rez;
			}
		}
		return rez;
	}

	public static <T> boolean containsAnyBy(Collection<T> source, Predicate<T> eq) {
		return anyMatch(source, eq);
	}
}
