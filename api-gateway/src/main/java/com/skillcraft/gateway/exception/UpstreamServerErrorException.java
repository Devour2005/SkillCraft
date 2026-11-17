package com.skillcraft.gateway.exception;

import lombok.Getter;
import org.springframework.http.ResponseEntity;

/**
 * Carries a 5xx answer from management through the circuit breaker / retry
 * decorators, so it is counted as a failure but can still be passed to the
 * caller unchanged.
 */
@Getter
public class UpstreamServerErrorException extends RuntimeException {

	private final transient ResponseEntity<byte[]> response;

	public UpstreamServerErrorException(ResponseEntity<byte[]> response) {
		super("management responded " + response.getStatusCode(), null, false, false);
		this.response = response;
	}

}
