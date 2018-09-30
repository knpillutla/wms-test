package com.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EventReceiver {
	Consumer consumer;
	String topicName;
	ObjectMapper mapper = new ObjectMapper();

	public EventReceiver(String consumerGroup, String topicName) {
		this.topicName = topicName;
		Properties props = new Properties();
		//props.put("bootstrap.servers", "192.168.56.1:29092");
		props.put("bootstrap.servers", "35.239.238.83:9092");
		props.put("auto.create.topics.enable", "false");
//		props.put("advertised.host.name", "35.239.238.83");
//		props.put("advertised.listeners", "PLAINTEXT://35.239.238.83:9092");
//		 props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"USER\" password=\"PASSWORD\";");
//		 props.put("security.protocol", "SASL_SSL");
//		 props.put("sasl.mechanism", "PLAIN");
//		 props.put("ssl.protocol", "TLSv1.2");
//		 props.put("ssl.enabled.protocols", "TLSv1.2");
//		 props.put("ssl.endpoint.identification.algorithm", "HTTPS");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("spring.cloud.stream.kafka.bindings." + topicName + ".consumer.autoCommitOffset", false);
		props.put("logging.level.org.apache.kafka","TRACE");
		//props.put("spring.cloud.stream.kafka.bindings." + topicName + "inventory-out.consumer.enable.auto.commit", false);
		props.put("enable.auto.commit", false);
		props.put("auto.commit.interval.ms", "1000");
		props.put("group.id", consumerGroup);
		consumer = new KafkaConsumer<>(props);
		//Collection<TopicPartition> topicPartitionList = null;
		consumer.subscribe(Arrays.asList(topicName));
		// consumer.poll(0);
		// consumer.commitSync();
/*		consumer.subscribe(Arrays.asList(topicName), new ConsumerRebalanceListener() {
			Map<TopicPartition, Long> offsetMap = new HashMap();
			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
				// do nothing
				long position = 0 ;
				System.out.println("Revoked started:" + consumer);
				Map<TopicPartition, Long> offsets = consumer.endOffsets(partitions);
				for (Entry<TopicPartition, Long> entry : offsets.entrySet()) {
					position = consumer.position(entry.getKey());
					offsetMap.put(entry.getKey(), position);
					System.out.println("revoked, consumer position:" + position);
				}
				System.out.println("Revoked ended:" + consumer);
			}
			

			@Override
			public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
				Map<TopicPartition, Long> offsets = consumer.endOffsets(partitions);
				for (Entry<TopicPartition, Long> entry : offsets.entrySet()) {
					// this only gets executed the first time, we do poll. So, on a creation of the
					// consumer, we need to do a poll to make sure.
					long position = consumer.position(entry.getKey());
					Long lastRevokedPosition = offsetMap.get(entry.getKey());
					System.out.println("Assigning offset, current position:" + position + ", lastRevokedPosition:" + lastRevokedPosition);
					if(lastRevokedPosition != null) 
					{
						position = lastRevokedPosition;
						System.out.println("Seeting current position to last revoked position:" + position);
					}
					consumer.seek(entry.getKey(), position);
					System.out
					.println("consumer, seeking position to :" + entry.getKey() + ": position:" + position);

				}
			}
		});*/
		
		
/*		Map<TopicPartition, Long> offsets = consumer.endOffsets(topicPartitionList);
		for (Entry<TopicPartition, Long> entry : offsets.entrySet()) {
			// this only gets executed the first time, we do poll. So, on a creation of the
			// consumer, we need to do a poll to make sure.
			System.out.println(entry.getKey() + "," + entry.getValue());
			consumer.seek(entry.getKey(), entry.getValue());
			System.out
					.println("consumer:" + entry.getKey() + ": position:" + consumer.position(entry.getKey()));
			consumer.seek(entry.getKey(), consumer.position(entry.getKey()));
			System.out
			.println("consumer, seeking position to :" + entry.getKey() + ": position:" + (consumer.position(entry.getKey())));

		}*/
		consumer.poll(100);
		consumer.commitSync();
	}

	public <T> List<T> getEvent(Class<T> cls) throws Exception {
		List<T> eventList = new ArrayList();
		ConsumerRecords<String, String> records;
		while ((records = consumer.poll(2000)).count() > 0) {
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
		}
		consumer.commitSync();
		//consumer.commitSync();
		System.out.println("count of records:" + eventList.size());
		return eventList;
	}
}
