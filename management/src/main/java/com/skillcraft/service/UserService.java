package com.skillcraft.service;

import com.skillcraft.domain.UserRole;
import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UpdateUserRequest;
import com.skillcraft.domain.dto.UserDto;
import java.util.Collection;
import java.util.List;

public interface UserService {

	UserDto createUser(CreateUserRequest request);
	UserDto getUserById(Long id);
	List<UserDto> getUsersByRoles(Collection<UserRole> roles);
	UserDto updateUser(Long id, UpdateUserRequest request);
	UserDto deactivateUser(Long id);
	void deleteUser(Long id);
}
