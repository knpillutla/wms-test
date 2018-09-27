package com.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.example.inventory.dto.events.ASNUPCReceivedEvent;
import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.order.dto.events.CustomerOrderCreatedEvent;
import com.example.order.dto.events.CustomerOrderDownloadEvent;
import com.example.order.dto.events.OrderPlannedEvent;
import com.example.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.example.order.dto.requests.OrderFulfillmentRequestDTO;
import com.example.order.dto.responses.OrderFulfillmentResponseDTO;
import com.example.picking.dto.events.LowPickEvent;
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
public class SmallStoreCustomerOrderInventoryCreationTest {
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
		int numOfOrders = 1;
		int numOfOrderLines = 1; // num of order lines
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

		Assert.assertEquals(numOfOrderLines, invnCreatedEventList.size());
		EventReceiver orderEventReceiver = new EventReceiver("ss-wmscustomerordercreator-consumer",
				wmsStreams.CUSTOMER_ORDERS_OUTPUT);
		List<CustomerOrderCreationRequestDTO> orderCreationReqList = CustomerOrderCreator
				.createNewCustomerOrders(invnCreatedEventList, numOfOrders, numOfOrderLines);
		for (CustomerOrderCreationRequestDTO orderCreationReq : orderCreationReqList) {
			CustomerOrderDownloadEvent orderDloadEvent = new CustomerOrderDownloadEvent(orderCreationReq);
			EventPublisher.send(wmsStreams.inboundCustomerOrders(), orderDloadEvent, orderDloadEvent.getHeaderMap());
			List<CustomerOrderCreatedEvent> orderEventList = orderEventReceiver
					.getEvent(CustomerOrderCreatedEvent.class);
			orderCreatedEventList.addAll(orderEventList);
		}
		System.out.println("CustomerOrders Created....");
		invokeOrderFulfillmentForSmallStore(busName, locnNbr, company, division, busUnit,
				RandomStringUtils.random(6, true, false), 5);
		Assert.assertEquals(numOfOrderLines, orderCreatedEventList.size());
		/*
		 * // create low pick event to start order fulfillment EventReceiver
		 * orderPlannedEventReceiver = new EventReceiver("ordercreator-consumer",
		 * wmsStreams.ORDERS_OUTPUT); LowPickEvent lowPickEvent = new
		 * LowPickEvent("XYZ",3456, "71", "", "", "", "", "");
		 * EventPublisher.send(wmsStreams.outboundPick(),
		 * lowPickEvent,lowPickEvent.getHeaderMap()); List<OrderPlannedEvent>
		 * orderPlannedEventList = orderEventReceiver.getEvent(OrderPlannedEvent.class);
		 * System.out.println("Orders Planned Created....");
		 * Assert.assertEquals(numOfUPCS, orderPlannedEventList.size());
		 */
	}

	// @Test
	public void createLowPickEvent() throws Exception {
		// create low pick event to start order fulfillment
		EventReceiver orderPlannedEventReceiver = new EventReceiver("ss-ordercreator-consumer", wmsStreams.ORDERS_OUTPUT);
		LowPickEvent lowPickEvent = new LowPickEvent("XYZ", 3456, "71", "", "", "", "", "");
		EventPublisher.send(wmsStreams.outboundPick(), lowPickEvent, lowPickEvent.getHeaderMap());
		List<OrderPlannedEvent> orderPlannedEventList = orderPlannedEventReceiver.getEvent(OrderPlannedEvent.class);
		System.out.println("Orders Planned Created....");
		Assert.assertEquals(1, orderPlannedEventList.size());
	}

	public void invokeOrderFulfillmentForSmallStore(String busName, Integer locnNbr, String company, String division,
			String busUnit, String userId, int numOfOrdersInBatch) {
		OrderFulfillmentRequestDTO orderFulfillmentReq = new OrderFulfillmentRequestDTO();
		orderFulfillmentReq.setBusName(busName);
		orderFulfillmentReq.setLocnNbr(locnNbr);
		orderFulfillmentReq.setCompany(company);
		orderFulfillmentReq.setDivision(division);
		orderFulfillmentReq.setBusUnit(busUnit);
		orderFulfillmentReq.setSmallStoreMode(true);
		orderFulfillmentReq.setNumOfOrders(numOfOrdersInBatch);
		RestTemplate restTemplate = new RestTemplate();
		OrderFulfillmentResponseDTO response = restTemplate.postForObject(
				"http://localhost:9296/orders/v1/" + busName + "/" + locnNbr, orderFulfillmentReq,
				OrderFulfillmentResponseDTO.class);
		System.out.println("rest response:" + response);
	}

}