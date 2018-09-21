package com.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.h2.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderLineCreationRequestDTO;
import com.example.test.service.EventPublisher;

import junit.framework.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		"spring.cloud.stream.bindings.inventory-in.group=wmsinventory-producer",
		"spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		"spring.cloud.stream.kafka.binder.brokers=localhost:29092" }, classes = { EventPublisher.class,
				WMSStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableBinding(WMSStreams.class)
public class CreateInventory {
	@Autowired
	WMSStreams wmsStreams;
	
	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();
	
	@Test
	public void createNewInventory() throws Exception {
		EventReceiver receiver = new EventReceiver("wmsinventorycreator-consumer", "inventory-out");
		for (int i = 0; i < 50; i++) {
			Random rand = new Random();
			String upc = "73747374" + rand.nextInt(10000);
			Integer qty = rand.nextInt(26);
			String aisle = new Integer(rand.nextInt(20)).toString();
			aisle = StringUtils.pad(aisle, 2,"0", false);
			String position = new Integer(rand.nextInt(50)).toString();
			aisle = StringUtils.pad(aisle, 2,"0", false);
			position = StringUtils.pad(aisle, 2,"0", true);
			String areaZone = RandomStringUtils.randomAlphabetic(2, 2);
			String level = RandomStringUtils.randomAlphabetic(1, 1);
			InventoryCreationRequestDTO inventoryReq = new InventoryCreationRequestDTO("XYZ", 3456, "71", "", "", areaZone+aisle+level+position,
					RandomStringUtils.random(20, false, true), qty, "", false, "Krishna");
			System.out.println("Creating Inventory:" + i +":" +inventoryReq);
			EventPublisher.send(wmsStreams.inboundInventory(), inventoryReq);
			List<InventoryCreatedEvent> inventoryEventList = receiver.getEvent(InventoryCreatedEvent.class);
			System.out.println("Received Event:" + inventoryEventList.get(0));
			invnCreatedEventList.addAll(inventoryEventList);
		}
		System.out.println("End");
		Assert.assertEquals(50, invnCreatedEventList.size());
	}
}
