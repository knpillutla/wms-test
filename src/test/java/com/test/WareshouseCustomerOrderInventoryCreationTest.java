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
import com.example.picking.dto.requests.PickConfirmRequestDTO;
import com.example.picking.dto.responses.PickDTO;
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
public class WareshouseCustomerOrderInventoryCreationTest {
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
		EventReceiver inventoryEventReceiver = new EventReceiver("whse-wmsinventorycreator-consumer",
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
		EventReceiver orderEventReceiver = new EventReceiver("whse-wmscustomerordercreator-consumer",
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
		String batchNbr = invokeOrderFulfillmentForWarehouse(busName, locnNbr, company, division, busUnit,
				RandomStringUtils.random(6, true, false), 5);
		Assert.assertEquals(numOfOrderLines, orderCreatedEventList.size());
		this.pickAllOrders(busName, locnNbr, company, division, busUnit, 
				numOfOrders, numOfOrderLines, batchNbr, RandomStringUtils.random(6, true, false));
	}

	// @Test
	public void createLowPickEvent() throws Exception {
		// create low pick event to start order fulfillment
		EventReceiver orderPlannedEventReceiver = new EventReceiver("whse-ordercreator-consumer",
				wmsStreams.ORDERS_OUTPUT);
		LowPickEvent lowPickEvent = new LowPickEvent("XYZ", 3456, "71", "", "", "", "", "");
		EventPublisher.send(wmsStreams.outboundPick(), lowPickEvent, lowPickEvent.getHeaderMap());
		List<OrderPlannedEvent> orderPlannedEventList = orderPlannedEventReceiver.getEvent(OrderPlannedEvent.class);
		System.out.println("Orders Planned Created....");
		Assert.assertEquals(1, orderPlannedEventList.size());
	}

	public String invokeOrderFulfillmentForWarehouse(String busName, Integer locnNbr, String company, String division,
			String busUnit, String userId, int numOfOrdersInBatch) {
		OrderFulfillmentRequestDTO orderFulfillmentReq = new OrderFulfillmentRequestDTO();
		orderFulfillmentReq.setBusName(busName);
		orderFulfillmentReq.setLocnNbr(locnNbr);
		orderFulfillmentReq.setCompany(company);
		orderFulfillmentReq.setDivision(division);
		orderFulfillmentReq.setBusUnit(busUnit);
		orderFulfillmentReq.setWarehouseMode(true);
		orderFulfillmentReq.setNumOfOrders(numOfOrdersInBatch);
		String batchNbr = "";
		RestTemplate restTemplate = new RestTemplate();
		OrderFulfillmentResponseDTO response = restTemplate.postForObject(
				"http://localhost:9296/orders/v1/" + busName + "/" + locnNbr, orderFulfillmentReq,
				OrderFulfillmentResponseDTO.class);
		batchNbr = response.getBatchNbr();
		System.out.println("order fulfillment response for warehouse:" + response);
		return batchNbr;
	}

	public void pickAllOrders(String busName, Integer locnNbr, String company, String division, String busUnit,
			int numOfOrders, int numOfOrderLines, String batchNbr, String userId) {
		RestTemplate restTemplate = new RestTemplate();
		int numOfPicks = numOfOrders * numOfOrderLines;
		for (int i = 0; i < numOfPicks; i++) {
			String pickAssignURL = "http://localhost:9076/picking/v1/" + busName + "/" + locnNbr + "/picks/next/" + batchNbr + "/" + userId;
			System.out.println("pick assign url:" + pickAssignURL);
			// assign next pick
			PickDTO pickDTO = restTemplate.postForObject(pickAssignURL, null, PickDTO.class);
			System.out.println("next pick:" + pickDTO);
			// pick confirm the pick
			PickConfirmRequestDTO pickConfirmObj = new PickConfirmRequestDTO(pickDTO.getId(), pickDTO.getOrderId(),
					pickDTO.getBatchNbr(), pickDTO.getBusName(), pickDTO.getLocnNbr(), pickDTO.getBusUnit(),
					pickDTO.getCompany(), pickDTO.getDivision(), pickDTO.getOrderNbr(), pickDTO.getLocnBrcd(),
					pickDTO.getItemBrcd(), pickDTO.getQty(), 5 + RandomStringUtils.random(19, false, true), userId);
			String pickConfirmURL = "http://localhost:9076/picking/v1/" + busName + "/" + locnNbr + "/picks/"
					+ pickDTO.getId();
			System.out.println("pick confirm url:" + pickConfirmURL);
			PickDTO pickConfirmDTO = restTemplate.postForObject(pickConfirmURL, pickConfirmObj, PickDTO.class);
			System.out.println("Pick Confirm Reponse:" + pickConfirmDTO);
		}
	}

}
