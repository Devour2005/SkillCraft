package com.skillcraft.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The browser-facing, session-based side of the gateway: login/register pages
 * and the admin panel. Runs after {@link SecurityConfig} (order 1), so it only
 * ever sees requests that aren't /auth/** or /api/**.
 */
@Configuration
@Order(2)
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/register").permitAll()
						.requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/admin", true)
						.failureUrl("/login?error")
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll());

		return http.build();
	}
}
