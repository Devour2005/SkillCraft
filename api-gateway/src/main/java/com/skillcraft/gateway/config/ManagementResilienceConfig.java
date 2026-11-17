package com.skillcraft.gateway.config;

import com.skillcraft.gateway.exception.UpstreamServerErrorException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Resilience4j policies for calls from the gateway into management.
 * Only connectivity problems and 5xx responses count as failures - 4xx are
 * legitimate business answers and must neither trip the breaker nor be retried.
 */
@Configuration
public class ManagementResilienceConfig {

	@Bean
	public CircuitBreaker managementCircuitBreaker(
			@Value("${app.resilience.circuit-breaker.sliding-window-size:20}") int slidingWindowSize,
			@Value("${app.resilience.circuit-breaker.minimum-calls:10}") int minimumCalls,
			@Value("${app.resilience.circuit-breaker.failure-rate-threshold:50}") float failureRateThreshold,
			@Value("${app.resilience.circuit-breaker.open-state-wait-seconds:15}") long openStateWaitSeconds) {
		CircuitBreakerConfig config = CircuitBreakerConfig.custom()
				.slidingWindowSize(slidingWindowSize)
				.minimumNumberOfCalls(minimumCalls)
				.failureRateThreshold(failureRateThreshold)
				.waitDurationInOpenState(Duration.ofSeconds(openStateWaitSeconds))
				.permittedNumberOfCallsInHalfOpenState(3)
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.recordExceptions(ResourceAccessException.class, HttpServerErrorException.class, UpstreamServerErrorException.class)
				.build();
		return CircuitBreaker.of("management", config);
	}

	@Bean
	public Retry managementRetry(
			@Value("${app.resilience.retry.max-attempts:3}") int maxAttempts,
			@Value("${app.resilience.retry.wait-millis:200}") long waitMillis) {
		RetryConfig config = RetryConfig.custom()
				.maxAttempts(maxAttempts)
				.waitDuration(Duration.ofMillis(waitMillis))
				.retryExceptions(ResourceAccessException.class, HttpServerErrorException.class, UpstreamServerErrorException.class)
				.build();
		return Retry.of("management", config);
	}
}
