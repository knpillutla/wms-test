package com.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventReceiver {
	Consumer consumer;
	String topicName;
	ObjectMapper mapper = new ObjectMapper();
	public EventReceiver(String consumerGroup, String topicName) {
		this.topicName = topicName;
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.56.1:29092");
//		 props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"USER\" password=\"PASSWORD\";");
//		 props.put("security.protocol", "SASL_SSL");
//		 props.put("sasl.mechanism", "PLAIN");
//		 props.put("ssl.protocol", "TLSv1.2");
//		 props.put("ssl.enabled.protocols", "TLSv1.2");
//		 props.put("ssl.endpoint.identification.algorithm", "HTTPS");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset", true);
		props.put("group.id", consumerGroup);
		props.put("enable.auto.commit",true);
		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topicName));
		ConsumerRecords<String, String> records = consumer.poll(10000);
		consumer.commitSync();
	}
	
	public <T> List<T> getEvent(Class<T> cls) throws Exception {
		List<T> eventList = new ArrayList();
		ConsumerRecords<String, String> records = consumer.poll(10000);
		consumer.commitSync();
		int recordCount = records.count();
		System.out.println("count of records:" + recordCount);
		ConsumerRecord record = null;
		int i = 0;
		Iterator itr = records.iterator();
		while (itr.hasNext()) {
			i++;
			record = (ConsumerRecord) itr.next();
			System.out.println("Event Receiver, record.value:" + record.value().toString());
			T obj = (T) mapper.readValue(record.value().toString(), cls);
			eventList.add(obj);
			System.out.println("Event Receiver, received event:" + obj);
			System.out.println("loop:" + i + ",record count:" + recordCount);
		}
		return eventList;
	}}
