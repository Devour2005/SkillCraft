package com.skillcraft.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
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

	private final RestClient managementRestClient;

	@RequestMapping("/api/**")
	public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
		String query = request.getQueryString();
		String uri = request.getRequestURI() + (query != null ? "?" + query : "");

		try {
			return managementRestClient.method(HttpMethod.valueOf(request.getMethod()))
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
