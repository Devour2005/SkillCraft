package com.skillcraft.micronaut.controller;

import com.skillcraft.micronaut.service.ResilientCaller;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.annotation.Serdeable;

@Controller("/status")
public class StatusController {

	private final ResilientCaller resilientCaller;

	public StatusController(ResilientCaller resilientCaller) {
		this.resilientCaller = resilientCaller;
	}

	@Get
	public Status status() {
		return new Status("micronaut-service", "UP", resilientCaller.circuitState().name());
	}

	@Serdeable
	public record Status(String service, String status, String circuitBreaker) {}
}
