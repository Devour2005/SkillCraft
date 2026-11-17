package com.skillcraft.util.collection_utils;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ArraysUtil {

	public static <T> Stream<T> stream(T... array) {
		return StreamsUtil.stream(array);
	}

	public static <T> boolean contains(T[] array, T v) {
		Predicate<T> eq = a -> Objects.equals(a, v);
		return containsBy(array, eq);
	}

	public static <T> boolean containsBy(T[] array, Predicate<T> eq) {
		return stream(array).anyMatch(eq);
	}
}