package com.skillcraft.util.collection_utils;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.MapUtils.getInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Lombok;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapsUtil {


	public static void moveIfMissing(Map<String, Object> from, Map<String, Object> to, String key) {
		if (from.containsKey(key)) {
			if (!to.containsKey(key)) {
				to.put(key, from.get(key));
			}
		}
	}

	public static <T> void setIfMissing(Map<String, Object> map, String key, Supplier<T> value) {
		if (!map.containsKey(key)) {
			map.put(key, value.get());
		}
	}

	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
		if (map == null) {
			return null;
		}
		return map.entrySet()
				.stream()
				.filter(entry -> Objects.equals(entry.getValue(), value))
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	public static <T> void set(Map<String, Object> source, Map<String, Object> destination, String key) {
		if (source.containsKey(key)) {
			destination.put(key, source.get(key));
		} else {
			destination.remove(key);
		}
	}

	@Deprecated
	public static Map<String, Object> map(Object obj) {
		ObjectMapper oMapper = new ObjectMapper();

		SimpleModule m = new SimpleModule();
		m.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
				if (value != null) {
					g.writeEmbeddedObject(value);
				}
			}
		});
		oMapper.registerModule(m);
		oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		oMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		oMapper.setDefaultPropertyInclusion(
				JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
		oMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		MapType mapType = oMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		return oMapper.convertValue(obj, mapType);
	}

	public static <K, V, VR> Map<K, VR> map(Map<K, V> map, Function<V, VR> value) {
		if (isEmpty(map)) {
			return emptyMap();
		}
		Map<K, VR> rez = new HashMap<>();
		map.forEach((k, v) -> rez.put(k, value.apply(v)));
		return rez;
	}

	public static <K, V, KR, VR> Map<KR, VR> map(Map<K, V> map, Function<K, KR> key, Function<V, VR> value) {
		if (isEmpty(map)) {
			return emptyMap();
		}
		Map<KR, VR> rez = new HashMap<>();
		map.forEach((k, v) -> rez.put(key.apply(k), value.apply(v)));
		return rez;
	}

	public static <K, V> Map<K, V> toMap(Collection<V> source, Function<V, K> key) {
		return StreamsUtil.stream(source).collect(Collectors.toMap(key, Function.identity()));
	}

	public static <K, V> Map<K, V> toMap(Iterable<V> source, Function<V, K> key) {
		return StreamsUtil.stream(source).collect(Collectors.toMap(key, Function.identity()));
	}

	public static <K, V, VR> Map<K, VR> toMap(Collection<V> source, Function<V, K> key, Function<V, VR> value) {
		return StreamsUtil.stream(source).collect(Collectors.toMap(key, value));
	}

	public static <K, V> Map<K, V> toMap(Entry<K, V>... source) {
		return StreamsUtil.stream(source).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	public static <K, V, VR> HashMap<K, VR> toHashMap(Iterable<V> source, Function<V, K> key, Function<V, VR> value) {
		HashMap<K, VR> map = new HashMap<>();
		StreamsUtil.stream(source).forEach(v -> map.put(key.apply(v), value.apply(v)));
		return map;
	}

	public static <K, V, VR> HashMap<K, VR> toHashMap(Collection<V> source, Function<V, K> key, Function<V, VR> value) {
		HashMap<K, VR> map = new HashMap<>();
		StreamsUtil.stream(source).forEach(v -> map.put(key.apply(v), value.apply(v)));
		return map;
	}

	public static <R, K, V> Map<K, V> toMap(Collection<R> source, Function<R, K> keyMapper, Function<R, V> valueMapper, BinaryOperator<V> mergeFunction) {
		return StreamsUtil.stream(source)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
	}

	public static <R, K, V> Map<K, V> toMap(Iterable<R> source, Function<R, K> keyMapper, Function<R, V> valueMapper) {
		return StreamsUtil.stream(source)
				.collect(Collectors.toMap(keyMapper, valueMapper));
	}

	public static <K, V> Map<K, V> toMap(Iterable<V> source, Function<V, K> keyMapper, BinaryOperator<V> mergeFunction) {
		return StreamsUtil.stream(source)
				.collect(Collectors.toMap(keyMapper, Function.identity(), mergeFunction));
	}

	public static <R, K, V> Map<K, V> toMap(Iterable<R> source, Function<R, K> keyMapper, Function<R, V> valueMapper, BinaryOperator<V> mergeFunction) {
		return StreamsUtil.stream(source)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
	}

	public static <R, K, V> Map<K, V> toMapMerge(Iterable<R> source, Function<R, K> keyMapper, Function<R, V> valueMapper) {
		return StreamsUtil.stream(source)
				.filter(Objects::nonNull)
				.filter(o -> Objects.nonNull(keyMapper.apply(o)))
				.collect(Collectors.toMap(keyMapper, valueMapper, (o1, o2) -> o1));
	}

	public static <K, V> Map<K, V> toMapMerge(Iterable<V> source, Function<V, K> keyMapper) {
		return StreamsUtil.stream(source)
				.filter(Objects::nonNull)
				.filter(o -> Objects.nonNull(keyMapper.apply(o)))
				.collect(Collectors.toMap(keyMapper, Function.identity(), (o1, o2) -> o1));
	}

	public static <K, V, C extends Collection<V>, M extends Map<K, C>> M groupBy(Collection<V> list,
			Function<? super V, ? extends K> classifier, Supplier<M> mapSupplier, Supplier<C> collectionSupplier) {
		return StreamsUtil.stream(list).collect(
				groupingBy(classifier, mapSupplier, toCollection(collectionSupplier))
		);
	}

	private static Integer toInteger(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			return ((Number) o).intValue();
		}
		return Integer.valueOf(o.toString().trim());
	}

	public static void putNotNUll(Map<String, Object> map, String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	public static <T, KEY> Map<KEY, List<T>> appendGroup(Map<KEY, List<T>> source, Collection<T> col, Function<T, KEY> key) {
		CollectionsUtil.forEach(col, v -> {
			KEY k = key.apply(v);
			List<T> list;
			if (!source.containsKey(k)) {
				list = new ArrayList<>();
				source.put(k, list);
			} else {
				list = source.get(k);
			}
			list.add(v);
		});
		return source;
	}

	public static <T, KEY> Map<KEY, List<T>> group(Collection<T> col, Function<T, KEY> key) {
		return StreamsUtil.stream(col).collect(groupingBy(key));
	}

	public static <T, KEY, V> Map<KEY, List<V>> group(Collection<T> col, Function<T, KEY> key, Function<T, V> value) {
		return StreamsUtil.stream(col).collect(groupingBy(key, HashMap::new, mapping(value, toList())));
	}

	public static <T, KEY> Map<KEY, Set<T>> groupSet(Collection<T> col, Function<T, KEY> key) {
		return StreamsUtil.stream(col).collect(groupingBy(key, toSet()));
	}

	public static <T, KEY, V> Map<KEY, Set<V>> groupSet(Collection<T> col, Function<T, KEY> key, Function<T, V> value) {
		return StreamsUtil.stream(col).collect(groupingBy(key, HashMap::new, mapping(value, toSet())));
	}

	public static Integer getFirstNotNull(Map<String, Object> map, String... keys) {
		for (int i = 0; i < keys.length; i++) {
			Integer v = getInteger(map, keys[i]);
			if (Objects.nonNull(v)) {
				return v;
			}
		}
		return null;
	}

	public static Map<String, Object> getMap(Object o, String key) {
		if (o instanceof Map) {
			Object v = ((Map<?, ?>) o).get(key);
			if (v instanceof Map) {
				return (Map<String, Object>) v;
			}
		}
		return null;
	}

	public static Map<String, Object> of(Object... args) {
		Map<String, Object> map = new HashMap<>();
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i += 2) {
				String key = Lombok.checkNotNull(args[i], "'key' must be not null").toString();
				if (args[i + 1] != null) {
					map.put(key, args[i + 1]);
				}
			}
		}

		return map;
	}

	public static <K, V> boolean isEmpty(Map<K, V> map) {
		return map == null || map.isEmpty();
	}

	public static <K, V> Map<K, V> filter(Map<K, V> map, Collection<K> keys) {
		if (isEmpty(map) || CollectionsUtil.isEmpty(keys)) {
			return emptyMap();
		}
		return map.entrySet()
				.stream()
				.filter(e -> keys.contains(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static <K, V> Map<K, V> remove(Map<K, V> map, Collection<K> filterOutKeys) {
		if (isEmpty(map) || CollectionsUtil.isEmpty(filterOutKeys)) {
			return safe(map);
		}
		return map.entrySet()
				.stream()
				.filter(e -> !filterOutKeys.contains(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static <K, V> boolean isNotEmpty(Map<K, V> map) {
		return !isEmpty(map);
	}

	public static <K, V> boolean contains(Map<K, V> m, K k) {
		if (isNotEmpty(m)) {
			return m.get(k) != null;
		}
		return false;
	}

	public static <K, V> Map<K, V> safeNew(Map<K, V> m) {
		if (m == null) {
			return new HashMap<>();
		}
		return new HashMap<>(m);
	}


	public static <K, V> Map<K, V> safe(Map<K, V> map) {
		if (map == null) {
			return Collections.emptyMap();
		}
		return map;
	}

	public static <K, V> Map<K, V> filter(Map<K, V> map, Predicate<? super Entry<K, V>> valueFilter) {
		return safe(map).entrySet().stream()
				.filter(valueFilter)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static <K, V> V get(Map<K, V> map, K key) {
		if (map != null) {
			return map.get(key);
		}
		return null;
	}

	public static <K, V> Set<V> get(Map<K, V> map, Collection<K> keys) {
		if (map != null && keys != null) {
			Set<V> r = new HashSet<>();
			keys.forEach(key -> {
				if (key != null) {
					V v = map.get(key);
					if (v != null) {
						r.add(v);
					}
				}
			});
			return r;
		}
		return emptySet();
	}

	public static <K, V> void forEach(Map<K, V> map, BiConsumer<? super K, ? super V> action) {
		safe(map).forEach(action);
	}

	public static <K, V> void forEach(Map<K, V> map, Consumer<Entry<K, V>> action) {
		safe(map).entrySet().forEach(action);
	}

	public static Map<String, Object> treeMap(Map<String, Object> map) {
		return new TreeMap<>(safe(map));
	}

	public static <K, V> Map<K, V> addIf(Map<K, V> target, Map<K, V> source, Predicate<Entry<? super K, ? super V>> predicate) {
		forEach(source, e -> {
			if (predicate.test(e)) {
				target.put(e.getKey(), e.getValue());
			}
		});
		return target;
	}

	public static class HashMapAdder<K, V> extends HashMap<K, V> {

		public HashMapAdder<K, V> add(K key, V value) {
			put(key, value);
			return this;
		}
	}

	public static <K, V> HashMapAdder<K, V> adderMap() {
		return new HashMapAdder<>();
	}
}
