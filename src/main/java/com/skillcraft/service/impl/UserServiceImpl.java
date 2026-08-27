package com.skillcraft.service.impl;

import com.skillcraft.domain.User;
import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UserDto;
import com.skillcraft.domain.mapper.UserMapper;
import com.skillcraft.repository.UserRepository;
import com.skillcraft.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserDto createUser(CreateUserRequest request) {
		String email = request.email();
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new IllegalArgumentException(String.format("User with email %s already exists", email));
		}

		User user = userMapper.toEntity(request);
		user.setPasswordHash(passwordEncoder.encode(request.password()));

		User savedUser = userRepository.save(user);

		//TODO: Add record in a table Student/Teacher/Manager depending on role (request.role())

		return userMapper.toDto(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto getUserById(Long id) {
		return userRepository.findById(id)
				.map(userMapper::toDto)
				.orElseThrow(() -> new EntityNotFoundException(String.format("User with id = %s not found ", id)));
	}


}