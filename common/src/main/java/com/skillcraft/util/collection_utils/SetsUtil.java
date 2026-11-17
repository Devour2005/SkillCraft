package com.skillcraft.util.collection_utils;

import static com.skillcraft.util.collection_utils.StreamsUtil.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class SetsUtil {


	/**
	 * all operation with [source, intermediate and result] elements == null fill be filtered out
	 */
	@UtilityClass
	public static class NoNulls {

		public static <O, I> Set<O> map(Collection<I> source, Function<I, O> mapper) {
			return stream(source).filter(Objects::nonNull).map(mapper).filter(Objects::nonNull).collect(toSet());
		}

		public static <O, T1, I> Set<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, O> mapper2) {
			return stream(source).filter(Objects::nonNull).map(mapper1).filter(Objects::nonNull).map(mapper2).filter(Objects::nonNull).collect(toSet());
		}

		public static <O, T1, T2, I> Set<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, T2> mapper2, Function<T2, O> mapper3) {
			return stream(source).filter(Objects::nonNull).map(mapper1).filter(Objects::nonNull).map(mapper2).filter(Objects::nonNull).map(mapper3)
					.filter(Objects::nonNull).collect(toSet());
		}

		public static <T> Set<T> filter(Collection<T> source, Predicate<T> predicate) {
			return stream(source).filter(Objects::nonNull).filter(predicate).collect(toSet());
		}

		public static <T> Set<T> of(T... t) {
			return stream(t).filter(Objects::nonNull).collect(toSet());
		}

		public static <T> Set<T> of(Collection<T> t) {
			return stream(t).filter(Objects::nonNull).collect(toSet());
		}

		public static <T> Set<T> flat(Collection<Collection<T>> source) {
			return StreamsUtil.NoNulls.flat(source).collect(toSet());
		}

		public static <T, O> Set<O> flatMap(Collection<Collection<T>> source, Function<T, O> mapper) {
			return StreamsUtil.NoNulls.flatMap(source, mapper).collect(toSet());
		}

		public static <T, O> Set<O> mapFlat(Collection<T> source, Function<T, ? extends Collection<O>> mapper) {
			return StreamsUtil.NoNulls.mapFlat(source, mapper).collect(toSet());
		}
	}

	public static <O, I> Set<O> map(Collection<I> source, Function<I, O> mapper) {
		return stream(source).map(mapper).collect(toSet());
	}

	public static <O, T1, I> Set<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, O> mapper2) {
		return stream(source).map(mapper1).map(mapper2).collect(toSet());
	}

	public static <O, T1, T2, I> Set<O> map(Collection<I> source, Function<I, T1> mapper1, Function<T1, T2> mapper2, Function<T2, O> mapper3) {
		return stream(source).map(mapper1).map(mapper2).map(mapper3).collect(toSet());
	}

	public static <O, I> Set<O> map(I[] source, Function<I, O> mapper) {
		return stream(source).map(mapper).collect(toSet());
	}

	public static <T> Set<T> flat(Collection<? extends Collection<T>> source) {
		return StreamsUtil.flat(source).collect(toSet());
	}

	public static <T> Set<T> flat(Collection<? extends Collection<T>> source1, Collection<? extends Collection<T>> source2) {
		return merge(flat(source1), flat(source2));
	}
	public static <T, O> Set<O> flatMap(Collection<Collection<T>> source, Function<T, O> mapper) {
		return StreamsUtil.flatMap(source, mapper).collect(toSet());
	}

	public static <T, O> Set<O> mapFlat(Collection<T> source, Function<T, ? extends Collection<O>> mapper) {
		return StreamsUtil.mapFlat(source, mapper).collect(toSet());
	}
	public static <T> Set<T> filter(Collection<T> source, Predicate<T> predicate) {
		return stream(source).filter(predicate).collect(toSet());
	}

	public static <T> Set<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2) {
		return stream(source).filter(p1).filter(p2).collect(toSet());
	}
	public static <T> Set<T> filter(Collection<T> source, Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
		return stream(source).filter(p1).filter(p2).filter(p3).collect(toSet());
	}

	public static <Y> Set<Y> set(Stream<Y> source) {
		return Optional.ofNullable(source).map(s -> s.collect(toSet())).orElse(emptySet());
	}

	public static <T> Set<T> notBlank(Collection<T> source, Function<T, String> prop) {
		return filter(source, t -> StringUtils.isNotBlank(prop.apply(t)));
	}

	public static <T> Set<T> notNull(Collection<T> source, Function<T, ?> prop) {
		return filter(source, t -> Objects.nonNull(prop.apply(t)));
	}

	public static <T> Set<T> notNulls(Collection<T> source) {
		return filter(source, Objects::nonNull);
	}

	public static <T, U, R> Set<R> list(Map<T, U> source, BiFunction<T, U, R> mapper) {
		return stream(source).map(e -> mapper.apply(e.getKey(), e.getValue())).collect(toSet());
	}

	public static <T> Optional<T> first(Set<T> source) {
		return CollectionsUtil.first(source);
	}

	public static <T> Set<T> remove(Collection<T> source, Collection<T> toRemove) {
		return stream(source)
				.filter(t -> !toRemove.contains(t))
				.collect(toSet());
	}

	public static <T> Set<T> remove(Collection<T> source, T... toRemove) {
		Set<T> r = stream(toRemove).collect(Collectors.toSet());
		return stream(source)
				.filter(t -> !r.contains(t))
				.collect(toSet());
	}

	public static <T> Set<T> merge(Collection<T> s1, T... s2) {
		return merge(s1, of(s2));
	}

	public static <T> Set<T> merge(Collection<T> s1, Collection<T> s2) {
		Set<T> result = new HashSet<>();
		if (s1 != null) {
			result.addAll(s1);
		}
		if (s2 != null) {
			result.addAll(s2);
		}
		return result;
	}

	public static <T> Set<T> of(Collection<T> v) {
		return stream(v).collect(toSet());
	}

	public static <T> Set<T> of(Iterable<T> v) {
		return stream(v).collect(toSet());
	}

	public static <T> Set<T> of(Iterator<T> v) {
		return stream(v).collect(toSet());
	}

	public static <T> Set<T> of(T... v) {
		return stream(v).collect(toSet());
	}

	public static <R, T> Set<T> mapAndFilterToSet(Collection<R> source, Function<R, T> mapperFunction, Predicate<T> filter) {
		return mapFilter(source, mapperFunction, filter);
	}

	public static <R, T> Set<T> mapFilter(Collection<R> source, Function<R, T> mapperFunction, Predicate<T> filter) {
		return StreamsUtil.safeStream(source)
				.map(mapperFunction)
				.filter(filter)
				.collect(toSet());
	}

	public static <R, T> Set<T> filterMap(Collection<R> source, Predicate<R> filter, Function<R, T> mapperFunction) {
		return StreamsUtil.safeStream(source)
				.filter(filter)
				.map(mapperFunction)
				.collect(toSet());
	}

	public static <T> Set<T> safeNew(Collection<T> source) {
		return source == null ? new HashSet<>() : new HashSet<>(source);
	}

	public static <T> Set<T> toNull(Collection<T> source) {
		return CollectionUtils.isEmpty(source) ? null : of(source);
	}

	/**
	 * v - comma separated values eg. 1,2,3
	 */
//	public static Set<Long> longs(String v) {
//		return filterMap(splitByCommaToSet(v), StringUtils::isNotBlank, Long::valueOf);
//	}

	/**
	 * v - comma separated values eg. 1,2,3
	 */
//	public static Set<Integer> ints(String v) {
//		return filterMap(splitByCommaToSet(v), StringUtils::isNotBlank, Integer::valueOf);
//	}

}
