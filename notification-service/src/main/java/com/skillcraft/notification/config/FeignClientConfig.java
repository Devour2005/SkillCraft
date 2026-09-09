package com.skillcraft.notification.config;

import com.skillcraft.notification.security.ServiceJwtIssuer;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * Attaches a fresh service JWT to every outgoing Feign call - management
 * validates every request's Authorization header itself, regardless of caller.
 */
@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

	private final ServiceJwtIssuer serviceJwtIssuer;

	@Bean
	public RequestInterceptor managementAuthInterceptor() {
		return template -> template.header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceJwtIssuer.issueServiceToken());
	}
}
