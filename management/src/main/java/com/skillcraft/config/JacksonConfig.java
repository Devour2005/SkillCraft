package com.skillcraft.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Boot 4's Jackson autoconfiguration module doesn't register a plain
 * ObjectMapper bean on its own here (Kafka's DomainEventPublisher needs one
 * injected directly, not just the one Spring MVC builds internally for its
 * message converter) - declared explicitly so both get the same instance,
 * with ISO-8601 dates instead of Jackson's numeric-timestamp default.
 */
@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
}
