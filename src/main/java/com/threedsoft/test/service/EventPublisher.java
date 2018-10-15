package com.threedsoft.test.service;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventPublisher {
	public static void send(MessageChannel msgChannel, Object obj, Map headerMap) {
		log.info("Sending Msg {}", obj);
		
		MessageHeaderAccessor msgHdrAccessor = new MessageHeaderAccessor();
		msgHdrAccessor.copyHeadersIfAbsent(headerMap);
		Message msgObj = MessageBuilder.withPayload(obj)
		.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
		.setHeaders(msgHdrAccessor)
		.build();
		log.info("Sending msg with headers and payload:{}", msgObj.getHeaders() + "," + msgObj.getPayload());
		msgChannel.send(msgObj);
		log.info("Completed Sending Msg {}", obj);
	}
	
	@Bean
	@Primary
	public ObjectMapper serializingObjectMapper() {
	    ObjectMapper objectMapper = new ObjectMapper();
	    JavaTimeModule javaTimeModule = new JavaTimeModule();
//	    javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer());
	//    javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
	    objectMapper.registerModule(javaTimeModule);
	    return objectMapper;
	}		
}
