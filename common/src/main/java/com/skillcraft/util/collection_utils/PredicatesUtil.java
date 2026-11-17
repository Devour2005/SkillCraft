package com.skillcraft.util.collection_utils;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PredicatesUtil {

	public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2) {
		return t -> p1.test(t) && p2.test(t);
	}

	public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
		return t -> p1.test(t) && p2.test(t) && p3.test(t);
	}

	public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2) {
		return t -> p1.test(t) || p2.test(t);
	}

	public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
		return t -> p1.test(t) || p2.test(t) || p3.test(t);
	}

	public static <T> Predicate<? super T> and(Predicate<T>[] ps) {
		return (Predicate<T>) t -> {
			if (Objects.nonNull(ps)) {
				for (Predicate<T> p : ps) {
					if (!p.test(t)) {
						return false;
					}
				}
			}
			return true;
		};
	}

	public static <O, T> Predicate<O> eq(Function<O, T> mapper, T value) {
		return o -> Objects.equals(value, mapper.apply(o));
	}
}
