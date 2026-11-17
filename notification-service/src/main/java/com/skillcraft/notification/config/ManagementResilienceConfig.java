package com.skillcraft.notification.config;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j policies for Feign calls into management. Connectivity problems
 * and 5xx answers count as failures; 4xx (e.g. 404 for an unknown user) do not.
 */
@Configuration
public class ManagementResilienceConfig {

	private static boolean isServerSideFailure(Throwable t) {
		return t instanceof RetryableException
				|| (t instanceof FeignException fe && (fe.status() >= 500 || fe.status() == -1));
	}

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
				.recordException(ManagementResilienceConfig::isServerSideFailure)
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
				.retryOnException(ManagementResilienceConfig::isServerSideFailure)
				.build();
		return Retry.of("management", config);
	}
}
