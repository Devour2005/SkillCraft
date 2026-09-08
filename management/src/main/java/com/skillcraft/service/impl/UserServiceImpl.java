package com.skillcraft.service.impl;

import com.skillcraft.domain.Manager;
import com.skillcraft.domain.StaffProfile;
import com.skillcraft.domain.Student;
import com.skillcraft.domain.User;
import com.skillcraft.domain.UserRole;
import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UpdateUserRequest;
import com.skillcraft.domain.dto.UserDto;
import com.skillcraft.domain.mapper.UserMapper;
import com.skillcraft.event.DomainEventPublisher;
import com.skillcraft.event.KafkaTopics;
import com.skillcraft.event.UserRegisteredEvent;
import com.skillcraft.repository.ManagerRepository;
import com.skillcraft.repository.StaffProfileRepository;
import com.skillcraft.repository.StudentRepository;
import com.skillcraft.repository.UserRepository;
import com.skillcraft.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final StudentRepository studentRepository;
	private final ManagerRepository managerRepository;
	private final StaffProfileRepository staffProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final DomainEventPublisher eventPublisher;

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
		createRoleProfile(savedUser);

		eventPublisher.publish(KafkaTopics.USER_REGISTERED, String.valueOf(savedUser.getId()),
				new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName(),
						savedUser.getLastName(), savedUser.getRole().name(), Instant.now()));

		return userMapper.toDto(savedUser);
	}

	private void createRoleProfile(User user) {
		switch (user.getRole()) {
			case STUDENT -> studentRepository.save(Student.builder().user(user).build());
			case MANAGER -> managerRepository.save(Manager.builder().user(user).build());
			case TEACHER, ACCOUNTANT -> staffProfileRepository.save(StaffProfile.builder().user(user).build());
			case ADMIN -> { /* ADMIN is identified solely by User.role, no dedicated profile table */ }
		}
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto getUserById(Long id) {
		return userRepository.findById(id)
				.map(userMapper::toDto)
				.orElseThrow(() -> new EntityNotFoundException(String.format("User with id = %s not found ", id)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserDto> getUsersByRoles(Collection<UserRole> roles) {
		return userRepository.findAllByRoleInOrderByLastNameAscFirstNameAsc(roles)
				.stream()
				.map(userMapper::toDto)
				.toList();
	}

	@Override
	@Transactional
	public UserDto updateUser(Long id, UpdateUserRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(String.format("User with id = %s not found ", id)));

		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setPhone(request.phone());
		user.setIsActive(request.isActive());

		return userMapper.toDto(user);
	}
}