package com.skillcraft.gateway.controller;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import com.skillcraft.gateway.exception.UpstreamServerErrorException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

/**
 * The actual "gateway" part: every request that made it past
 * JwtAuthenticationFilter is forwarded to management verbatim (including the
 * Authorization header, so management can validate the JWT again itself).
 * Management is the sole source of truth for business-level authorization
 * (@PreAuthorize on its controllers) and business logic.
 */
@RestController
@RequiredArgsConstructor
public class GatewayProxyController {

	private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
			"connection", "keep-alive", "transfer-encoding", "upgrade",
			"proxy-authenticate", "proxy-authorization", "te", "trailer",
			"host", "content-length"
	);

	private static final Set<String> IDEMPOTENT_METHODS = Set.of("GET", "HEAD", "OPTIONS");

	private final RestClient managementRestClient;
	private final CircuitBreaker managementCircuitBreaker;
	private final Retry managementRetry;

	@RequestMapping("/api/**")
	public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
		String query = request.getQueryString();
		String uri = request.getRequestURI() + (query != null ? "?" + query : "");

		Supplier<ResponseEntity<byte[]>> forward = () -> {
			ResponseEntity<byte[]> response = managementRestClient.method(HttpMethod.valueOf(request.getMethod()))
					.uri(uri)
					.headers(headers -> copyRequestHeaders(request, headers))
					.body(body == null ? new byte[0] : body)
					.exchange((req, res) -> {
						byte[] responseBody = res.getBody().readAllBytes();
						HttpHeaders responseHeaders = new HttpHeaders();
						res.getHeaders().forEach((name, values) -> {
							if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
								responseHeaders.addAll(name, values);
							}
						});
						return ResponseEntity.status(res.getStatusCode()).headers(responseHeaders).body(responseBody);
					});
			// exchange() does not throw on 5xx, so surface it to the breaker/retry as a failure
			if (response.getStatusCode().is5xxServerError()) {
				throw new UpstreamServerErrorException(response);
			}
			return response;
		};

		Supplier<ResponseEntity<byte[]>> guarded = CircuitBreaker.decorateSupplier(managementCircuitBreaker, forward);
		if (IDEMPOTENT_METHODS.contains(request.getMethod())) {
			guarded = Retry.decorateSupplier(managementRetry, guarded);
		}

		try {
			return guarded.get();
		} catch (UpstreamServerErrorException ex) {
			// Pass management's own 5xx answer through unchanged
			return ex.getResponse();
		} catch (CallNotPermittedException ex) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.header(HttpHeaders.CONTENT_TYPE, "application/json")
					.body("{\"message\":\"management service temporarily unavailable (circuit open)\"}".getBytes());
		} catch (ResourceAccessException ex) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
					.body(("{\"message\":\"management service unavailable\"}").getBytes());
		}
	}

	private void copyRequestHeaders(HttpServletRequest request, HttpHeaders target) {
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames == null) {
			return;
		}
		Collections.list(headerNames).forEach(name -> {
			if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
				Collections.list(request.getHeaders(name)).forEach(value -> target.add(name, value));
			}
		});
	}
}
