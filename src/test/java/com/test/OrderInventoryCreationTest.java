package com.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.inventory.dto.events.ASNUPCReceivedEvent;
import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.events.OrderDownloadEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
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
public class OrderInventoryCreationTest {
	@Autowired
	WMSStreams wmsStreams;
	
	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();
	List<OrderCreatedEvent> orderCreatedEventList = new ArrayList();
	
	@Test
	public void createInventoryAndOrdersOneOrdeLinePerOrder() throws Exception {
		int numOfUPCS = 1;
		EventReceiver inventoryEventReceiver = new EventReceiver("wmsinventorycreator-consumer", "inventory-out");
		List<InventoryCreationRequestDTO> invnCreationReqList = InventoryCreator.createNewInventoryRecords(numOfUPCS);
		for (InventoryCreationRequestDTO inventoryReq : invnCreationReqList) {
			ASNUPCReceivedEvent upcReceivedEvent = new ASNUPCReceivedEvent(inventoryReq.getBusName(), inventoryReq.getLocnNbr(), inventoryReq.getBusUnit(), inventoryReq.getItemBrcd(), inventoryReq.getQty());
			EventPublisher.send(wmsStreams.inboundInventory(), upcReceivedEvent, upcReceivedEvent.getHeaderMap());
			List<InventoryCreatedEvent> inventoryEventList = inventoryEventReceiver.getEvent(InventoryCreatedEvent.class);
			invnCreatedEventList.addAll(inventoryEventList);
		}
		System.out.println("Inventory Created....");
		
		Assert.assertEquals(numOfUPCS, invnCreatedEventList.size());
		EventReceiver orderEventReceiver = new EventReceiver("wmsordercreator-consumer", "orders-out");
		List<OrderCreationRequestDTO> orderCreationReqList = OrderCreator.createNewOrders(invnCreatedEventList);
		for (OrderCreationRequestDTO orderCreationReq : orderCreationReqList) {
			OrderDownloadEvent orderDloadEvent = new OrderDownloadEvent(orderCreationReq);
			EventPublisher.send(wmsStreams.inboundOrders(), orderDloadEvent,orderDloadEvent.getHeaderMap());
			List<OrderCreatedEvent> orderEventList = orderEventReceiver.getEvent(OrderCreatedEvent.class);
			orderCreatedEventList.addAll(orderEventList);
		}
		System.out.println("Orders Created....");
		Assert.assertEquals(numOfUPCS, orderCreatedEventList.size());
	}

}
