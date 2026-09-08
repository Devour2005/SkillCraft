package com.skillcraft.gateway.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

	private final KafkaTemplate<Object, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publish(String topic, String key, Object event) {
		try {
			String payload = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(topic, key, payload);
		} catch (JsonProcessingException ex) {
			log.error("Failed to serialize event for topic {}: {}", topic, ex.getMessage());
		}
	}
}
