package com.skillcraft.gateway.event;

/**
 * Mirrors com.skillcraft.event.KafkaTopics in management - topic names must
 * match exactly since both sides publish/consume the same logical events.
 */
public final class KafkaTopics {

	public static final String USER_REGISTERED = "user.registered";

	private KafkaTopics() {
	}
}
