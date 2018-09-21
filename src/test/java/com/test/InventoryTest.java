package com.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.test.service.EventPublisher;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		"spring.cloud.stream.bindings.inventory-in.group=wmstest-producer",
		"spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		"spring.cloud.stream.kafka.binder.brokers=localhost:29092" }, classes = { EventPublisher.class,
				WMSStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableBinding(WMSStreams.class)
public class InventoryTest {
	@Autowired
	WMSStreams wmsStreams;

	/*
	 * @Value("${userBucket.path}") private String userBucketPath;
	 */
	@Test
	public void createNewInventory() throws Exception {
		InventoryCreationRequestDTO inventoryReq = new InventoryCreationRequestDTO("XYZ", 3456, "71", "", "", "AB0101",
				"0000345353", 2, "", false, "Krishna");
		KafkaConsumer consumer = this.createKafkaConsumer();
		EventPublisher.send(wmsStreams.inboundInventory(), inventoryReq);
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
			InventoryCreatedEvent invCreatedEvent = getInventoryCreatedEvent(record);
			System.out.println("invCreatedEvent:" + invCreatedEvent);
			System.out.println("loop:" + i + ",record count:" + recordCount);
		}
		System.out.println("End");
	}

	public InventoryCreatedEvent getInventoryCreatedEvent(ConsumerRecord record) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		InventoryCreatedEvent inventoryCreatedEvent;
		inventoryCreatedEvent = mapper.readValue(record.value().toString(), InventoryCreatedEvent.class);
		return inventoryCreatedEvent;
	}
	
	public KafkaConsumer createKafkaConsumer() {
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
		props.put("group.id", "WMSTest");
		props.put("enable.auto.commit",true);
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList("inventory-out"));
		ConsumerRecords<String, String> records = consumer.poll(10000);
		consumer.commitSync();
//		consumer.commitSync();
		return consumer;
	}

}
