package com.skillcraft.util.collection_utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

@UtilityClass
public class StreamsUtil {

	public static <T> Stream<T> stream(Collection<T> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return Stream.empty();
		}
		return collection.stream();
	}

	public static <K, V> Stream<Entry<K, V>> stream(Map<K, V> source) {
		if (source == null) {
			return Stream.empty();
		}
		return source.entrySet().stream();
	}

	public static <T> Stream<T> stream(Iterable<T> t) {
		if (t == null) {
			return Stream.empty();
		}
		return StreamSupport.stream(t.spliterator(), false);
	}

	public static <T> Stream<T> stream(Iterator<T> iterator) {
		if (iterator == null) {
			return Stream.empty();
		}
		return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
	}

	public static <T> Stream<T> stream(Stream<T> source) {
		if (source == null) {
			return Stream.empty();
		}
		return source;
	}

	public static <R> Stream<R> safeStream(Collection<R> source) {
		return stream(source);
	}

	public static <T> Stream<T> stream(T... array) {
		return array == null ? Stream.empty() : Stream.of(array);
	}

	public static <T> Stream<T> sort(Collection<T> source, Comparator<T> comparator) {
		return sort(stream(source), comparator);
	}

	public static <T> Stream<T> sort(Stream<T> source, Comparator<T> comparator) {
		return source.sorted(comparator);
	}

	public static <T> Stream<T> noNulls(Collection<T> collection) {
		return noNulls(stream(collection));
	}

	public static <T> Stream<T> noNulls(Stream<T> source) {
		return stream(source).filter(Objects::nonNull);
	}

	public static <T> Stream<T> flat(Collection<? extends Collection<T>> source) {
		return stream(source)
				.filter(CollectionUtils::isNotEmpty)
				.flatMap(Collection::stream);
	}

	public static <T, O> Stream<O> flatMap(Collection<Collection<T>> source, Function<T, O> mapper) {
		return stream(source)
				.filter(CollectionUtils::isNotEmpty)
				.flatMap(Collection::stream)
				.map(mapper);
	}

	public static <T, O> Stream<O> mapFlat(Collection<T> source, Function<T, ? extends Collection<O>> mapper) {
		return stream(source)
				.map(mapper)
				.filter(CollectionUtils::isNotEmpty)
				.flatMap(Collection::stream);
	}


	/**
	 * all operation with [source, intermediate and result] elements == null fill be filtered out
	 */
	@UtilityClass
	public static class NoNulls {

		private static final ToIntFunction<Integer> TO_INT = i -> i;
		private static final ToLongFunction<Long> TO_LONG = i -> i;

		public static <T> Stream<T> filter(Collection<T> collection, Predicate<T> filter) {
			return filter(stream(collection), filter);
		}

		public static <T> Stream<T> filter(Stream<T> source, Predicate<T> filter) {
			return noNulls(noNulls(source).filter(filter));
		}

		public static <T, R> Stream<R> map(Collection<T> collection, Function<T, R> mapper) {
			return map(stream(collection), mapper);
		}

		public static <T, R> Stream<R> map(Stream<T> source, Function<T, R> mapper) {
			return noNulls(noNulls(source).map(mapper));
		}

		public static <T, R1, R> Stream<R> map(Collection<T> collection, Function<T, R1> mapper1, Function<R1, R> mapper2) {
			return map(stream(collection), mapper1, mapper2);
		}

		public static <T, R1, R> Stream<R> map(Stream<T> source, Function<T, R1> mapper1, Function<R1, R> mapper2) {
			return noNulls(noNulls(noNulls(source).map(mapper1)).map(mapper2));
		}

		public static <T, R> Stream<R> filterMap(Collection<T> collection, Predicate<T> filter, Function<T, R> mapper) {
			return filterMap(stream(collection), filter, mapper);
		}

		public static <T, R> Stream<R> filterMap(Stream<T> source, Predicate<T> filter, Function<T, R> mapper) {
			return map(filter(source, filter), mapper);
		}

		public static IntStream toInt(Stream<Integer> stream) {
			return noNulls(stream).mapToInt(TO_INT);
		}

		public static IntStream toInt(Collection<Integer> collection) {
			return noNulls(collection).mapToInt(TO_INT);
		}

		public static IntStream toInt(Stream<Integer> source, Predicate<Integer> filter) {
			return toInt(filter(source, filter));
		}

		public static IntStream toInt(Collection<Integer> collection, Predicate<Integer> filter) {
			return toInt(filter(collection, filter));
		}

		public static <T> IntStream toInt(Collection<T> collection, Function<T, Integer> mapper) {
			return toInt(noNulls(collection), mapper);
		}

		public static <T> IntStream toInt(Stream<T> source, Function<T, Integer> mapper) {
			return toInt(map(source, mapper));
		}

		public static <T, R1> IntStream toInt(Collection<T> collection, Function<T, R1> mapper1, Function<R1, Integer> mapper2) {
			return toInt(map(collection, mapper1, mapper2));
		}

		public static <T, R1> IntStream toInt(Stream<T> source, Function<T, R1> mapper1, Function<R1, Integer> mapper2) {
			return toInt(map(source, mapper1, mapper2));
		}

		public static <T> IntStream filterMapToInt(Collection<T> collection, Predicate<T> filter, Function<T, Integer> mapper) {
			return toInt(filterMap(collection, filter, mapper));
		}

		public static <T> IntStream filterMapToInt(Stream<T> source, Predicate<T> filter, Function<T, Integer> mapper) {
			return toInt(filterMap(source, filter, mapper));
		}

		public static LongStream toLong(Stream<Long> stream) {
			return noNulls(stream).mapToLong(TO_LONG);
		}

		public static LongStream toLong(Collection<Long> collection) {
			return noNulls(collection).mapToLong(TO_LONG);
		}

		public static LongStream toLong(Stream<Long> source, Predicate<Long> filter) {
			return toLong(filter(source, filter));
		}

		public static LongStream toLong(Collection<Long> collection, Predicate<Long> filter) {
			return toLong(filter(collection, filter));
		}

		public static <T> LongStream toLong(Collection<T> collection, Function<T, Long> mapper) {
			return toLong(noNulls(collection), mapper);
		}

		public static <T> LongStream toLong(Stream<T> source, Function<T, Long> mapper) {
			return toLong(map(source, mapper));
		}

		public static <T, R1> LongStream toLong(Collection<T> collection, Function<T, R1> mapper1, Function<R1, Long> mapper2) {
			return toLong(map(collection, mapper1, mapper2));
		}

		public static <T, R1> LongStream toLong(Stream<T> source, Function<T, R1> mapper1, Function<R1, Long> mapper2) {
			return toLong(map(source, mapper1, mapper2));
		}

		public static <T> LongStream filterMapToLong(Collection<T> collection, Predicate<T> filter, Function<T, Long> mapper) {
			return toLong(filterMap(collection, filter, mapper));
		}

		public static <T> LongStream filterMapToLong(Stream<T> source, Predicate<T> filter, Function<T, Long> mapper) {
			return toLong(filterMap(source, filter, mapper));
		}

		public static <T> Stream<T> flat(Collection<Collection<T>> source) {
			return stream(source)
					.filter(CollectionUtils::isNotEmpty)
					.map(ListsUtil::notNulls)
					.filter(CollectionUtils::isNotEmpty)
					.flatMap(Collection::stream)
					.filter(Objects::nonNull);
		}

		public static <T, O> Stream<O> flatMap(Collection<Collection<T>> source, Function<T, O> mapper) {
			return stream(source)
					.filter(CollectionUtils::isNotEmpty)
					.map(ListsUtil::notNulls)
					.filter(CollectionUtils::isNotEmpty)
					.flatMap(Collection::stream)
					.map(mapper)
					.filter(Objects::nonNull);
		}

		public static <T, O> Stream<O> mapFlat(Collection<T> source, Function<T, ? extends Collection<O>> mapper) {
			return stream(source)
					.filter(Objects::nonNull)
					.map(mapper)
					.map(ListsUtil::notNulls)
					.filter(CollectionUtils::isNotEmpty)
					.flatMap(Collection::stream)
					.filter(Objects::nonNull);
		}
	}
}