package com.skillcraft.notification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirrors just the fields this service needs from management's UserDto -
 * everything else in the response body is ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManagementUserDto(Long id, String email) {}
