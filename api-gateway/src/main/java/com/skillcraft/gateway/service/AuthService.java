package com.skillcraft.gateway.service;

import com.skillcraft.gateway.domain.GatewayStudent;
import com.skillcraft.gateway.domain.GatewayUser;
import com.skillcraft.gateway.domain.UserRole;
import com.skillcraft.gateway.dto.AuthResponse;
import com.skillcraft.gateway.dto.LoginRequest;
import com.skillcraft.gateway.dto.RegisterRequest;
import com.skillcraft.gateway.event.DomainEventPublisher;
import com.skillcraft.gateway.event.KafkaTopics;
import com.skillcraft.gateway.event.UserRegisteredEvent;
import com.skillcraft.gateway.repository.GatewayStudentRepository;
import com.skillcraft.gateway.repository.GatewayUserRepository;
import com.skillcraft.gateway.security.JwtService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final GatewayUserRepository userRepository;
	private final GatewayStudentRepository studentRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final DomainEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		GatewayUser user = userRepository.findByEmailIgnoreCase(request.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		if (!Boolean.TRUE.equals(user.getIsActive())) {
			throw new BadCredentialsException("This account is disabled");
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		return toAuthResponse(user);
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new IllegalArgumentException("A user with this email already exists");
		}

		GatewayUser user = GatewayUser.builder()
				.email(request.email())
				.passwordHash(passwordEncoder.encode(request.password()))
				.firstName(request.firstName())
				.lastName(request.lastName())
				.role(UserRole.STUDENT)
				.isActive(true)
				.build();

		GatewayUser savedUser = userRepository.save(user);
		studentRepository.save(GatewayStudent.builder().user(savedUser).build());

		eventPublisher.publish(KafkaTopics.USER_REGISTERED, String.valueOf(savedUser.getId()),
				new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName(),
						savedUser.getLastName(), savedUser.getRole().name(), Instant.now()));

		return toAuthResponse(savedUser);
	}

	private AuthResponse toAuthResponse(GatewayUser user) {
		String token = jwtService.issueToken(user);
		return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), user.getId(), user.getEmail(), user.getRole());
	}
}
