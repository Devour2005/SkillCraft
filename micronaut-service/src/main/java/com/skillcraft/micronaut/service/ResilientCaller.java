package com.skillcraft.micronaut.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Resilience4j circuit breaker + retry wrapper for outgoing calls made by this
 * service (e.g. to management). Exposed state is used by the /status endpoint.
 */
@Singleton
public class ResilientCaller {

	private final CircuitBreaker circuitBreaker;
	private final Retry retry;

	public ResilientCaller(
			@Value("${app.resilience.circuit-breaker.sliding-window-size:20}") int slidingWindowSize,
			@Value("${app.resilience.circuit-breaker.minimum-calls:10}") int minimumCalls,
			@Value("${app.resilience.circuit-breaker.failure-rate-threshold:50}") float failureRateThreshold,
			@Value("${app.resilience.circuit-breaker.open-state-wait-seconds:15}") long openStateWaitSeconds,
			@Value("${app.resilience.retry.max-attempts:3}") int maxAttempts,
			@Value("${app.resilience.retry.wait-millis:200}") long waitMillis) {
		this.circuitBreaker = CircuitBreaker.of("micronaut-outbound", CircuitBreakerConfig.custom()
				.slidingWindowSize(slidingWindowSize)
				.minimumNumberOfCalls(minimumCalls)
				.failureRateThreshold(failureRateThreshold)
				.waitDurationInOpenState(Duration.ofSeconds(openStateWaitSeconds))
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.build());
		this.retry = Retry.of("micronaut-outbound", RetryConfig.custom()
				.maxAttempts(maxAttempts)
				.waitDuration(Duration.ofMillis(waitMillis))
				.build());
	}

	public <T> T call(Supplier<T> action) {
		return Retry.decorateSupplier(retry, CircuitBreaker.decorateSupplier(circuitBreaker, action)).get();
	}

	public CircuitBreaker.State circuitState() {
		return circuitBreaker.getState();
	}
}
