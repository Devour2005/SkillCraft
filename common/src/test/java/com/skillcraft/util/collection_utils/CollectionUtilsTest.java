package com.skillcraft.util.collection_utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CollectionUtilsTest {

	@Test
	void listsMapIsNullSafe() {
		assertEquals(List.of(2, 4), ListsUtil.map(List.of(1, 2), i -> i * 2));
		assertTrue(ListsUtil.map((List<Integer>) null, i -> i * 2).isEmpty());
	}

	@Test
	void collectionsHelpers() {
		assertTrue(CollectionsUtil.isEmpty(null));
		assertTrue(CollectionsUtil.anyMatch(List.of("a", "b"), "b"::equals));
		assertFalse(CollectionsUtil.anyMatch(null, "b"::equals));
	}

	@Test
	void arraysContains() {
		assertTrue(ArraysUtil.contains(new String[] {"x", "y"}, "y"));
		assertFalse(ArraysUtil.contains(null, "y"));
	}

	@Test
	void setsMapDeduplicates() {
		assertEquals(2, SetsUtil.map(Arrays.asList(1, 2, 2), i -> i).size());
	}

	@Test
	void numbersFuzzyEquals() {
		assertTrue(NumbersUtils.equals(1.000001, 1.0));
		assertFalse(NumbersUtils.equals(1.1, 1.0));
		assertTrue(NumbersUtils.equals(null, 0));
	}
}
