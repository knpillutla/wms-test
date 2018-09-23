package com.example.test.service;

import java.util.Map;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventPublisher {
	public static void send(MessageChannel msgChannel, Object obj, Map headerMap) {
		log.info("Sending Msg {}", obj);
		MessageHeaderAccessor msgHdrAccessor = new MessageHeaderAccessor();
		msgHdrAccessor.copyHeadersIfAbsent(headerMap);
		msgChannel.send(MessageBuilder.withPayload(obj)
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
				.setHeaders(msgHdrAccessor)
				.build());
		log.info("Completed Sending Msg {}", obj);
	}
}
