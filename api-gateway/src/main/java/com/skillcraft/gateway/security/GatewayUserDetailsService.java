package com.skillcraft.gateway.security;

import com.skillcraft.gateway.domain.GatewayUser;
import com.skillcraft.gateway.repository.GatewayUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Backs the session-based login form (see WebSecurityConfig). The stateless
 * /auth/login JSON endpoint uses AuthService directly instead - this class is
 * only for the browser-facing form login.
 */
@Service
@RequiredArgsConstructor
public class GatewayUserDetailsService implements UserDetailsService {

	private final GatewayUserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		GatewayUser user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return User.builder()
				.username(user.getEmail())
				.password(user.getPasswordHash())
				.authorities("ROLE_" + user.getRole().name())
				.disabled(!Boolean.TRUE.equals(user.getIsActive()))
				.build();
	}
}
