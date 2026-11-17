package com.skillcraft.micronaut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.skillcraft.micronaut.controller.StatusController;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class StatusControllerTest {

	@Inject
	@Client("/")
	HttpClient client;

	@Test
	void statusReportsUpAndClosedCircuit() {
		StatusController.Status status = client.toBlocking().retrieve("/status", StatusController.Status.class);
		assertEquals("UP", status.status());
		assertEquals("CLOSED", status.circuitBreaker());
	}
}
