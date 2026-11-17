package com.skillcraft.util;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import com.skillcraft.domain.User;
import com.skillcraft.domain.UserRole;
import com.skillcraft.util.collection_utils.ArraysUtil;
import java.util.function.Predicate;
import org.springframework.util.Assert;

public class UserUtil {

	public static boolean hasRole(User user, UserRole... roles) {
		Assert.notNull(user, "user not provided");
		Assert.notNull(roles, "roles not provided");
		Predicate<UserRole> isRole = role -> role == user.getRole();
		return ArraysUtil.containsBy(roles, isRole);
	}

	public static boolean hasRole(User user, String... roles) {
		Assert.notNull(user, "user not provided");
		Assert.notNull(roles, "roles not provided");
		Predicate<String> isRole = role -> equalsIgnoreCase(role, user.getRole().name());
		return ArraysUtil.containsBy(roles, isRole);
	}
}
