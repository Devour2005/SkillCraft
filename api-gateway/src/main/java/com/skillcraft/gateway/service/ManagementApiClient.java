package com.skillcraft.gateway.service;

import com.skillcraft.gateway.domain.GatewayUser;
import com.skillcraft.gateway.domain.UserRole;
import com.skillcraft.gateway.dto.AdminCourseView;
import com.skillcraft.gateway.dto.AdminUpdateCourseRequest;
import com.skillcraft.gateway.dto.AdminUpdateUserRequest;
import com.skillcraft.gateway.dto.AdminUserView;
import com.skillcraft.gateway.repository.GatewayUserRepository;
import com.skillcraft.gateway.security.JwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Typed calls into management on behalf of the currently logged-in admin
 * session. A fresh, short-lived JWT is minted per call from the session's
 * principal rather than stored, so the web UI never needs to manage tokens.
 */
@Service
@RequiredArgsConstructor
public class ManagementApiClient {

	private final RestClient managementRestClient;
	private final GatewayUserRepository userRepository;
	private final JwtService jwtService;

	public List<AdminUserView> getUsersByRoles(Authentication authentication, List<UserRole> roles) {
		Object[] roleNames = roles.stream().map(Enum::name).toArray();
		return managementRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/users").queryParam("role", roleNames).build())
				.header(HttpHeaders.AUTHORIZATION, bearer(authentication))
				.retrieve()
				.body(new ParameterizedTypeReference<List<AdminUserView>>() {});
	}

	public AdminUserView getUser(Authentication authentication, Long id) {
		return managementRestClient.get()
				.uri("/api/users/{id}", id)
				.header(HttpHeaders.AUTHORIZATION, bearer(authentication))
				.retrieve()
				.body(AdminUserView.class);
	}

	public AdminUserView updateUser(Authentication authentication, Long id, AdminUpdateUserRequest request) {
		return managementRestClient.put()
				.uri("/api/users/{id}", id)
				.header(HttpHeaders.AUTHORIZATION, bearer(authentication))
				.body(request)
				.retrieve()
				.body(AdminUserView.class);
	}

	public List<AdminCourseView> getAllCourses(Authentication authentication) {
		return managementRestClient.get()
				.uri("/api/courses/all")
				.header(HttpHeaders.AUTHORIZATION, bearer(authentication))
				.retrieve()
				.body(new ParameterizedTypeReference<List<AdminCourseView>>() {});
	}

	public AdminCourseView getCourse(Authentication authentication, Long id) {
		return managementRestClient.get()
				.uri("/api/courses/{id}", id)
				.header(HttpHeaders.AUTHORIZATION, bearer(authentication))
				.retrieve()
				.body(AdminCourseView.class);
	}

	public AdminCourseView updateCourse(Authentication authentication, Long id, AdminUpdateCourseRequest request) {
		return managementRestClient.put()
				.uri("/api/courses/{id}", id)
				.header(HttpHeaders.AUTHORIZATION, bearer(authentication))
				.body(request)
				.retrieve()
				.body(AdminCourseView.class);
	}

	private String bearer(Authentication authentication) {
		GatewayUser user = userRepository.findByEmailIgnoreCase(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("Authenticated user vanished: " + authentication.getName()));
		return "Bearer " + jwtService.issueToken(user);
	}
}
