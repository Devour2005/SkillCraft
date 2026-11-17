package com.skillcraft.util.collection_utils;

import static org.apache.commons.collections4.MapUtils.getNumber;

import com.skillcraft.util.collection_utils.StreamsUtil.NoNulls;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MapUtils;

@UtilityClass
public class NumbersUtils {

	private static final double EQUAL_DELTA = .00001;

	public static <K> BigDecimal getBigDecimal(Map<? super K, ?> map, K key) {
		Number answer = getNumber(map, key);
		return toDecimal(answer);
	}

	public static BigDecimal toDecimal(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof BigDecimal) {
			return (BigDecimal) o;
		} else if (o instanceof Number) {
			return BigDecimal.valueOf(((Number) o).doubleValue());
		} else {
			try {
				return BigDecimal.valueOf(NumberFormat.getInstance().parse(o.toString().trim()).doubleValue());
			} catch (ParseException e) {
				return null;
			}
		}
	}

	public static Integer toInteger(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Integer) {
			return (Integer) o;
		} else if (o instanceof Number) {
			return ((Number) o).intValue();
		} else {
			try {
				return NumberFormat.getInstance().parse(o.toString().trim()).intValue();
			} catch (ParseException e) {
				return null;
			}
		}
	}

	public static boolean equals(Map<String, Object> map1, Map<String, Object> map2, String... keys) {
		if (keys == null || keys.length == 0) {
			throw new IllegalArgumentException("'keys' not provided!");
		}

		for (String key : keys) {
			Number n1 = MapUtils.getNumber(map1, key, 0);
			Number n2 = MapUtils.getNumber(map2, key, 0);
			if (!equals(n1, n2)) {
				return false;
			}
		}

		return true;
	}

	public static boolean equals(Number n1, Number n2) {
		return fuzzyEquals(n1 == null ? .0 : n1.doubleValue(), n2 == null ? .0 : n2.doubleValue(), EQUAL_DELTA);
	}

	/** Same contract as Guava's DoubleMath.fuzzyEquals: equal within tolerance, or both NaN. */
	private static boolean fuzzyEquals(double a, double b, double tolerance) {
		return Double.compare(a, b) == 0 || Math.abs(a - b) <= tolerance || (Double.isNaN(a) && Double.isNaN(b));
	}

	public static BigDecimal toBigDecimalValue(float value) {
		return new BigDecimal(value).setScale(4, RoundingMode.HALF_EVEN);
	}

	public static BigDecimal toBigDecimalValue(double value) {
		return new BigDecimal(value).setScale(4, RoundingMode.HALF_EVEN);
	}

	public static BigDecimal decimal(Map<String, Object> map, String key) {
		Object v = map.get(key);
		if (v == null) {
			return null;
		}
		return toDecimal(v);
	}

	public static BigDecimal sum(Collection<BigDecimal> data) {
		return StreamsUtil.noNulls(data).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public static <T> BigDecimal sum(Collection<T> data, Function<T, BigDecimal> map) {
		return NoNulls.map(data, map).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public static <T1, T2> BigDecimal sum(Collection<T1> data, Function<T1, T2> map1, Function<T2, BigDecimal> map2) {
		return NoNulls.map(data, map1, map2).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public static <T> BigDecimal filterSum(Collection<T> data, Predicate<T> filter, Function<T, BigDecimal> map) {
		return NoNulls.filterMap(data, filter, map).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public static int sumInt(Collection<Integer> data) {
		return NoNulls.toInt(data).sum();
	}

	public static <T> int sumInt(Collection<T> data, Function<T, Integer> map) {
		return NoNulls.toInt(data, map).sum();
	}

	public static <T1, T2> int sumInt(Collection<T1> data, Function<T1, T2> map1, Function<T2, Integer> map2) {
		return NoNulls.toInt(data, map1, map2).sum();
	}

	public static <T> int filterSumInt(Collection<T> data, Predicate<T> filter, Function<T, Integer> map) {
		return NoNulls.filterMapToInt(data, filter, map).sum();
	}

	public static long sumLong(Collection<Long> data) {
		return NoNulls.toLong(data).sum();
	}

	public static <T> long sumLong(Collection<T> data, Function<T, Long> map) {
		return NoNulls.toLong(data, map).sum();
	}

	public static <T1, T2> long sumLong(Collection<T1> data, Function<T1, T2> map1, Function<T2, Long> map2) {
		return NoNulls.toLong(data, map1, map2).sum();
	}

	public static <T> long filterSumLong(Collection<T> data, Predicate<T> filter, Function<T, Long> map) {
		return NoNulls.filterMapToLong(data, filter, map).sum();
	}

	public static OptionalInt maxInt(Collection<Integer> data) {
		return NoNulls.toInt(data).max();
	}

	public static <T> OptionalInt maxInt(Collection<T> data, Function<T, Integer> map) {
		return NoNulls.toInt(data, map).max();
	}

	public static <T1, T2> OptionalInt maxInt(Collection<T1> data, Function<T1, T2> map1, Function<T2, Integer> map2) {
		return NoNulls.toInt(data, map1, map2).max();
	}

	public static <T> OptionalInt filterMaxInt(Collection<T> data, Predicate<T> filter, Function<T, Integer> map) {
		return NoNulls.filterMapToInt(data, filter, map).max();
	}

	public static OptionalLong maxLong(Collection<Long> data) {
		return NoNulls.toLong(data).max();
	}

	public static <T> OptionalLong maxLong(Collection<T> data, Function<T, Long> map) {
		return NoNulls.toLong(data, map).max();
	}

	public static <T1, T2> OptionalLong maxLong(Collection<T1> data, Function<T1, T2> map1, Function<T2, Long> map2) {
		return NoNulls.toLong(data, map1, map2).max();
	}

	public static <T> OptionalLong filterMaxLong(Collection<T> data, Predicate<T> filter, Function<T, Long> map) {
		return NoNulls.filterMapToLong(data, filter, map).max();
	}

	public static OptionalInt minInt(Collection<Integer> data) {
		return NoNulls.toInt(data).min();
	}

	public static <T> OptionalInt minInt(Collection<T> data, Function<T, Integer> map) {
		return NoNulls.toInt(data, map).min();
	}

	public static <T1, T2> OptionalInt minInt(Collection<T1> data, Function<T1, T2> map1, Function<T2, Integer> map2) {
		return NoNulls.toInt(data, map1, map2).min();
	}

	public static <T> OptionalInt filterMinInt(Collection<T> data, Predicate<T> filter, Function<T, Integer> map) {
		return NoNulls.filterMapToInt(data, filter, map).min();
	}

	public static OptionalLong minLong(Collection<Long> data) {
		return NoNulls.toLong(data).min();
	}

	public static <T> OptionalLong minLong(Collection<T> data, Function<T, Long> map) {
		return NoNulls.toLong(data, map).min();
	}

	public static <T1, T2> OptionalLong minLong(Collection<T1> data, Function<T1, T2> map1, Function<T2, Long> map2) {
		return NoNulls.toLong(data, map1, map2).min();
	}

	public static <T> OptionalLong filterMinLong(Collection<T> data, Predicate<T> filter, Function<T, Long> map) {
		return NoNulls.filterMapToLong(data, filter, map).min();
	}

	public static List<Integer> box(int[] v) {
		if (v == null || v.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(v).boxed().collect(Collectors.toList());
	}

	public static List<Long> box(long[] v) {
		if (v == null || v.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(v).boxed().collect(Collectors.toList());
	}
}
