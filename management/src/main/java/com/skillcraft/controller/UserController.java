package com.skillcraft.controller;

import com.skillcraft.domain.UserRole;
import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UpdateUserRequest;
import com.skillcraft.domain.dto.UserDto;
import com.skillcraft.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public List<UserDto> getUsersByRole(@RequestParam List<UserRole> role) {
		return userService.getUsersByRoles(role);
	}

	@GetMapping("/{id}")
	public UserDto getUser(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
		return userService.updateUser(id, request);
	}
}
