package com.skillcraft.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ManagementClientConfig {

	@Bean
	public RestClient managementRestClient(@Value("${app.management-service.base-url}") String baseUrl) {
		return RestClient.builder()
				.baseUrl(baseUrl)
				.build();
	}
}
