package com.skillcraft.gateway.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Wraps calls into management with the circuit breaker and, for idempotent
 * calls only, a retry. Retry sits outside the breaker so that every attempt is
 * recorded by it, and a call is not retried once the breaker is open.
 */
@Component
@RequiredArgsConstructor
public class ManagementCaller {

	private final CircuitBreaker managementCircuitBreaker;
	private final Retry managementRetry;

	public <T> T safe(Supplier<T> call) {
		return CircuitBreaker.decorateSupplier(managementCircuitBreaker, call).get();
	}

	public <T> T idempotent(Supplier<T> call) {
		Supplier<T> withBreaker = CircuitBreaker.decorateSupplier(managementCircuitBreaker, call);
		return Retry.decorateSupplier(managementRetry, withBreaker).get();
	}

	public void safeRun(Runnable call) {
		safe(() -> {
			call.run();
			return null;
		});
	}
}
