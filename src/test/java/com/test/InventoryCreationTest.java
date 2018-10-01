package com.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.customer.order.dto.events.CustomerOrderCreatedEvent;
import com.example.inventory.dto.events.ASNUPCReceivedEvent;
import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.test.service.EventPublisher;

import junit.framework.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		"spring.cloud.stream.bindings.inventory-in.group=wmsorder-invn-producer",
		"spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		"spring.cloud.stream.kafka.binder.brokers=localhost:29092" }, classes = { EventPublisher.class,
				WMSStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableBinding(WMSStreams.class)
public class InventoryCreationTest {
	@Autowired
	WMSStreams wmsStreams;
	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();
	List<CustomerOrderCreatedEvent> orderCreatedEventList = new ArrayList();

	@Test
	public void createInventoryAndCustomerOrdersOneOrdeLinePerCustomerOrder() throws Exception {
		String busName = "XYZ";
		Integer locnNbr = 3456;
		String busUnit = "71";
		String company = "IE";
		String division = "09";
		String userId = "Krishna";
		int numOfOrders = 5;
		int numOfOrderLines = 5; // num of order lines
		EventReceiver inventoryEventReceiver = new EventReceiver("ss-wmsinventorycreator-consumer",
				wmsStreams.INVENTORY_OUTPUT);
		List<InventoryCreationRequestDTO> invnCreationReqList = InventoryCreator
				.createNewInventoryRecords(numOfOrders * numOfOrderLines);
		for (InventoryCreationRequestDTO inventoryReq : invnCreationReqList) {
			ASNUPCReceivedEvent upcReceivedEvent = new ASNUPCReceivedEvent(inventoryReq.getBusName(),
					inventoryReq.getLocnNbr(), inventoryReq.getBusUnit(), inventoryReq.getItemBrcd(),
					inventoryReq.getQty());
			EventPublisher.send(wmsStreams.inboundInventory(), upcReceivedEvent, upcReceivedEvent.getHeaderMap());
			List<InventoryCreatedEvent> inventoryEventList = inventoryEventReceiver
					.getEvent(InventoryCreatedEvent.class);
			invnCreatedEventList.addAll(inventoryEventList);
		}
		System.out.println("Inventory Created....");

		Assert.assertEquals(numOfOrders*numOfOrderLines, invnCreatedEventList.size());
	}
}
