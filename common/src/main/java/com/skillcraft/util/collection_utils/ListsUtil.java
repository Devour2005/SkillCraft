package com.skillcraft.util.collection_utils;

import static com.skillcraft.util.collection_utils.CollectionsUtil.isNotEmpty;
import static com.skillcraft.util.collection_utils.MapsUtil.isEmpty;
import static com.skillcraft.util.collection_utils.NumbersUtils.toInteger;
import static com.skillcraft.util.collection_utils.StreamsUtil.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class ListsUtil {

	/**
	 * all operation with [source, intermediate and result] elements == null fill be filtered out
	 */
	@UtilityClass
	public static class NoNulls {

		public static <O, I> List<O> map(Collection<I> source, Function<I, O> mapper) {
			return stream(source).filter(Objects::nonNull).map(mapper).filter(Objects::nonNull).collect(toList());
		}

		public static <O, T1, I> List<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, O> mapper2) {
			return stream(source).filter(Objects::nonNull).map(mapper1).filter(Objects::nonNull).map(mapper2).filter(Objects::nonNull).collect(toList());
		}

		public static <O, T1, T2, I> List<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, T2> mapper2, Function<T2, O> mapper3) {
			return stream(source).filter(Objects::nonNull).map(mapper1).filter(Objects::nonNull).map(mapper2).filter(Objects::nonNull).map(mapper3)
					.filter(Objects::nonNull).collect(toList());
		}

		public static <T> List<T> filter(Collection<T> source, Predicate<T> predicate) {
			return stream(source).filter(Objects::nonNull).filter(predicate).collect(toList());
		}

		public static <T> List<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2) {
			return stream(source).filter(Objects::nonNull).filter(p1).filter(p2).collect(toList());
		}

		public static <T> List<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
			return stream(source).filter(Objects::nonNull).filter(p1).filter(p2).filter(p3).collect(toList());
		}

		public static <T> List<T> of(T... t) {
			return stream(t).filter(Objects::nonNull).collect(toList());
		}

		public static <T> List<T> of(Collection<T> t) {
			return stream(t).filter(Objects::nonNull).collect(toList());
		}

		public static <T> List<T> flat(Collection<Collection<T>> source) {
			return StreamsUtil.NoNulls.flat(source).collect(toList());
		}

		public static <T, O> List<O> flatMap(Collection<Collection<T>> source, Function<T, O> mapper) {
			return StreamsUtil.NoNulls.flatMap(source, mapper).collect(toList());
		}

		public static <T, O> List<O> mapFlat(Collection<T> source, Function<T, ? extends Collection<O>> mapper) {
			return StreamsUtil.NoNulls.mapFlat(source, mapper).collect(toList());
		}
	}

	public static <O, I> List<O> map(Collection<I> source, Function<I, O> mapper) {
		return stream(source).map(mapper).collect(toList());
	}


	public static <O, I> List<O> mapI(Collection<I> source, BiFunction<Integer, I, O> mapper) {
		if (isNotEmpty(source)) {
			int index = 0;
			List<O> r = new ArrayList<>(source.size());
			for (I i : source) {
				r.add(mapper.apply(index, i));
				index++;
			}
			return r;
		}
		return emptyList();
	}

	public static <O, T1, I> List<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, O> mapper2) {
		return stream(source).map(mapper1).map(mapper2).collect(toList());
	}

	public static <O, T1, T2, I> List<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, T2> mapper2, Function<T2, O> mapper3) {
		return stream(source).map(mapper1).map(mapper2).map(mapper3).collect(toList());
	}

	public static <O, I> List<O> map(I[] source, Function<I, O> mapper) {
		return stream(source).map(mapper).collect(toList());
	}

	public static <O, I> List<O> filterMap(Collection<I> source, Predicate<I> filter, Function<I, O> mapper) {
		return stream(source).filter(filter).map(mapper).collect(toList());
	}

	public static <O, I> List<O> filterMap(I[] source, Predicate<I> filter, Function<I, O> mapper) {
		return stream(source).filter(filter).map(mapper).collect(toList());
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> predicate) {
		return stream(source).filter(predicate).collect(toList());
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2) {
		return stream(source).filter(p1).filter(p2).collect(toList());
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
		return stream(source).filter(p1).filter(p2).filter(p3).collect(toList());
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2, Predicate<T> p3, Predicate<T> p4) {
		return stream(source).filter(p1).filter(p2).filter(p3).filter(p4).collect(toList());
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2, Predicate<T> p3, Predicate<T> p4, Predicate<T> p5) {
		return stream(source).filter(p1).filter(p2).filter(p3).filter(p4).filter(p5).collect(toList());
	}

	public static <T> List<T> filter(T[] source, Predicate<T> predicate) {
		return stream(source).filter(predicate).collect(toList());
	}

	public static <T> List<T> concat(Collection<T> source1, Collection<T> source2) {
		return Stream.concat(stream(source1), stream(source2)).collect(toList());
	}

	public static <T> List<T> concat(Collection<T> source1, T... source2) {
		return Stream.concat(stream(source1), stream(source2)).collect(toList());
	}

	public static <Y> List<Y> list(Stream<Y> source) {
		return Optional.ofNullable(source).map(s -> s.collect(toList())).orElse(emptyList());
	}

	public static <T> List<T> notBlank(Collection<T> source, Function<T, String> prop) {
		return filter(source, t -> StringUtils.isNotBlank(prop.apply(t)));
	}

	public static <T> List<T> notNull(Collection<T> source, Function<T, ?> prop) {
		return filter(source, t -> Objects.nonNull(prop.apply(t)));
	}

	public static <T> List<T> notNulls(Collection<T> source) {
		return filter(source, Objects::nonNull);
	}

	public static <T, U, R> List<R> list(Map<T, U> source, BiFunction<T, U, R> mapper) {
		return stream(source).map(e -> mapper.apply(e.getKey(), e.getValue())).collect(toList());
	}

	public static <T> List<T> flat(Collection<Collection<T>> source) {
		return StreamsUtil.flat(source).collect(toList());
	}

	public static <T> List<T> flat(Collection<Collection<T>> source1, Collection<Collection<T>> source2) {
		return merge(flat(source1), flat(source2));
	}

	public static <T, O> List<O> flatMap(Collection<Collection<T>> source, Function<T, O> mapper) {
		return StreamsUtil.flatMap(source, mapper).collect(toList());
	}

	public static <T, O> List<O> mapFlat(Collection<T> source, Function<T, ? extends Collection<O>> mapper) {
		return StreamsUtil.mapFlat(source, mapper).collect(toList());
	}

	public static <T> List<T> set(List<T> source, Consumer<T> consumer) {
		stream(source).forEach(consumer);
		return source;
	}

	public static <T> List<T> of(T... t) {
		return stream(t).collect(toList());
	}

	public static <T> List<T> of(Collection<T> t) {
		return stream(t).collect(toList());
	}


	public static <T> List<T> of(Iterable<T> v) {
		return stream(v).collect(toList());
	}

	public static <T> List<T> of(Iterator<T> v) {
		return stream(v).collect(toList());
	}

	public static <T> List<T> fill(int limit, Function<Integer, T> map) {
		List<T> list = new ArrayList<>();
		for (int i = 0; i < limit; i++) {
			list.add(map.apply(i));
		}
		return list;
	}

	public static <T> List<T> merge(Collection<T> s1, Collection<T> s2) {
		List<T> result = new ArrayList<>();
		if (s1 != null) {
			result.addAll(s1);
		}
		if (s2 != null) {
			result.addAll(s2);
		}
		return result;
	}

	public static <T> List<T> merge(Collection<T> s1, T... t) {
		List<T> result = new ArrayList<>();
		if (s1 != null) {
			result.addAll(s1);
		}
		if (t != null) {
			result.addAll(of(t));
		}
		return result;
	}

	public static <T> long size(List<T> source) {
		return stream(source).count();
	}

	public static <R, T> List<T> mapToList(Iterable<R> source, Function<R, T> mapperFunction) {
		return StreamSupport.stream(source.spliterator(), false)
				.map(mapperFunction)
				.collect(toList());
	}

	public static <T> List<T> safeNew(Collection<T> source) {
		return source == null ? new ArrayList<>() : new ArrayList<>(source);
	}

	public static <O, T> List<T> list(O o, Function<O, Collection<T>> mapper) {
		if (o == null || mapper == null) {
			return emptyList();
		}
		return stream(mapper.apply(o))
				.collect(Collectors.toList());
	}

	public static <T> List<T> distinct(Collection<T> source) {
		return stream(source).distinct().collect(toList());
	}

	public static <T, F> List<T> distinct(Collection<T> source, Function<T, F> key) {
		return new ArrayList<>(stream(source).collect(Collectors.toMap(key, Function.identity(), (o1, o2) -> o1))
				.values());
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


	public static <T> List<T> mergeCol(Collection<T>... elements) {
		return Stream.of(elements)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public static <O, I> List<O> mapFilter(Collection<I> source, Function<I, O> mapper, Predicate<O> filter) {
		return stream(source).map(mapper).filter(filter).collect(toList());
	}

	public static <K, V, R> List<R> toListFromMap(Map<K, V> source, BiFunction<K, V, R> function) {
		return stream(source).map(e -> function.apply(e.getKey(), e.getValue())).collect(Collectors.toList());
	}


	public static List<Integer> getIntList(Map<String, Object> a, String key) {
		return (List<Integer>) a.get(key);
	}

	public static List<Integer> getIntListFromDoubleList(Map<String, Object> a, String key) {
		if (isEmpty(a) || !a.containsKey(key)) {
			return Collections.emptyList();
		}
		List<?> doubleList = (List) a.get(key);
		List<Integer> intList = new ArrayList<>();
		doubleList.forEach(d -> intList.add(toInteger(d)));
		return intList;
	}

	public static List<Integer> getIntListFromObject(Map<String, Object> source, String listKay, String fieldName) {
		if (source.get(listKay) instanceof Collection) {
			Collection coll = (Collection) source.get(listKay);
			List<Integer> intList = new ArrayList<>();
			coll.forEach(d -> {
				if (d instanceof Map) {
					intList.add(toInteger(((Map) d).get(fieldName)));
				}
			});
			return intList;
		}
		return Collections.emptyList();
	}


	public static List<String> getStringList(Map<String, Object> map, String key) {
		Object o = map.get(key);
		if (o == null) {
			return null;
		} else if (o instanceof List) {
			return (List<String>) o;
		} else if (o instanceof Collection) {
			return ListsUtil.map((Collection) o, String::valueOf);
		} else if (o.getClass().isArray()) {
			List<String> r = new ArrayList<>();
			for (int i = 0; i < Array.getLength(o); i++) {
				r.add(String.valueOf(Array.get(o, i)));
			}
			return r;
		}
		throw new IllegalArgumentException(map + " " + key + " contains wrong type");
	}

	public static <T> List<T> toNull(Collection<T> list) {
		return CollectionUtils.isEmpty(list) ? null : of(list);
	}

	public static <T> List<T> sort(Collection<T> list) {
		return stream(list).sorted().collect(toList());
	}

	public static <T> List<T> sort(Collection<T> list, Comparator<? super T> comparator) {
		return stream(list).sorted(comparator).collect(toList());
	}

	/**
	 * v - comma separated values eg. 1,2,3
	 */
//	public static List<Long> longs(String v) {
//		return filterMap(splitByCommaToSet(v), StringUtils::isNotBlank, Long::valueOf);
//	}

	/**
	 * v - comma separated values eg. 1,2,3
	 */
//	public static List<Integer> ints(String v) {
//		return filterMap(splitByCommaToList(v), StringUtils::isNotBlank, Integer::valueOf);
//	}
}
