package com.skillcraft.service;

import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UserDto;

public interface UserService {

	UserDto createUser(CreateUserRequest request);
	UserDto getUserById(Long id);
}
