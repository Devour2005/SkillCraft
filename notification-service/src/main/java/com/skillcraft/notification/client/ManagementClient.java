package com.skillcraft.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "management", url = "${app.management-service.base-url}")
public interface ManagementClient {

	@GetMapping("/api/users/{id}")
	ManagementUserDto getUser(@PathVariable("id") Long id);
}
