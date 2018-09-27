package com.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.example.inventory.dto.events.ASNUPCReceivedEvent;
import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.order.dto.events.CustomerOrderCreatedEvent;
import com.example.order.dto.events.CustomerOrderDownloadEvent;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.events.OrderPackedEvent;
import com.example.order.dto.events.OrderPlannedEvent;
import com.example.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.example.order.dto.requests.OrderFulfillmentRequestDTO;
import com.example.order.dto.responses.OrderFulfillmentResponseDTO;
import com.example.packing.dto.events.PackCreatedEvent;
import com.example.packing.dto.requests.PackConfirmRequestDTO;
import com.example.packing.dto.responses.PackDTO;
import com.example.picking.dto.events.LowPickEvent;
import com.example.picking.dto.events.PickConfirmationEvent;
import com.example.picking.dto.events.PickCreatedEvent;
import com.example.picking.dto.requests.PickConfirmRequestDTO;
import com.example.picking.dto.responses.PickDTO;
import com.example.shipping.dto.events.ShipCreatedEvent;
import com.example.shipping.dto.events.ShipRoutingCompletedEvent;
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
	List<CustomerOrderCreatedEvent> customerOrderCreatedEventList = new ArrayList();

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
		EventReceiver custOrderEventReceiver = new EventReceiver("whse-customerOrder-consumer",
				wmsStreams.CUSTOMER_ORDERS_OUTPUT);
		EventReceiver inventoryEventReceiver = new EventReceiver("whse-inventory-consumer",
				wmsStreams.INVENTORY_OUTPUT);
		EventReceiver orderEventReceiver = new EventReceiver("whse-order-consumer", wmsStreams.ORDERS_OUTPUT);
		EventReceiver pickEventReceiver = new EventReceiver("whse-pick-consumer", wmsStreams.PICK_OUTPUT);
		EventReceiver packEventReceiver = new EventReceiver("whse-pack-consumer", wmsStreams.PACK_OUTPUT);
		EventReceiver shipEventReceiver = new EventReceiver("whse-ship-consumer", wmsStreams.SHIP_OUTPUT);

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
		List<CustomerOrderCreationRequestDTO> orderCreationReqList = CustomerOrderCreator
				.createNewCustomerOrders(invnCreatedEventList, numOfOrders, numOfOrderLines);
		for (CustomerOrderCreationRequestDTO orderCreationReq : orderCreationReqList) {
			CustomerOrderDownloadEvent orderDloadEvent = new CustomerOrderDownloadEvent(orderCreationReq);
			EventPublisher.send(wmsStreams.inboundCustomerOrders(), orderDloadEvent, orderDloadEvent.getHeaderMap());
			List<CustomerOrderCreatedEvent> tmpCustomerOrderEventList = custOrderEventReceiver
					.getEvent(CustomerOrderCreatedEvent.class);
			customerOrderCreatedEventList.addAll(tmpCustomerOrderEventList);
		}
		Assert.assertEquals(1, customerOrderCreatedEventList.size());
		System.out.println("CustomerOrders Created....");
		List<OrderCreatedEvent> orderEventList = orderEventReceiver.getEvent(OrderCreatedEvent.class);
		Assert.assertEquals(1, orderEventList.size());
		
		// plan order fulfillment
		String batchNbr = invokeOrderFulfillmentForWarehouse(busName, locnNbr, company, division, busUnit,
				RandomStringUtils.random(6, true, false), 5);
		// ensure picks are created
		List<PickCreatedEvent> pickCreatedEventList = pickEventReceiver.getEvent(PickCreatedEvent.class);
		Assert.assertEquals(1, pickCreatedEventList.size());

		// ensure ship is created
		List<ShipCreatedEvent> shipEventList = shipEventReceiver.getEvent(ShipCreatedEvent.class);
		Assert.assertEquals(1, shipEventList.size());

		// ensure ship is routed
		List<ShipRoutingCompletedEvent> shipRoutingCompletedEventList = shipEventReceiver.getEvent(ShipRoutingCompletedEvent.class);
		Assert.assertEquals(1, shipRoutingCompletedEventList.size());

		String containerNbr = this.pickAllOrders(busName, locnNbr, company, division, busUnit, numOfOrders,
				numOfOrderLines, batchNbr, RandomStringUtils.random(6, true, false));
		List<PickConfirmationEvent> pickConfirmationEventList = pickEventReceiver.getEvent(PickConfirmationEvent.class);
		Assert.assertEquals(1, pickConfirmationEventList.size());
		// ensure packs are created
		List<PackCreatedEvent> packCreatedEventList = packEventReceiver.getEvent(PackCreatedEvent.class);
		Assert.assertEquals(1, packCreatedEventList.size());

		// pack the task
		packAllOrders(busName, locnNbr, company, division, busUnit, numOfOrders, numOfOrderLines, batchNbr,
				RandomStringUtils.random(6, true, false), containerNbr);

		List<OrderPackedEvent> orderPackedEventList = orderEventReceiver.getEvent(OrderPackedEvent.class);
		Assert.assertEquals(1, orderPackedEventList.size());
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

	public String pickAllOrders(String busName, Integer locnNbr, String company, String division, String busUnit,
			int numOfOrders, int numOfOrderLines, String batchNbr, String userId) {
		RestTemplate restTemplate = new RestTemplate();
		int numOfPicks = numOfOrders * numOfOrderLines;
		String containerNbr = 5 + RandomStringUtils.random(19, false, true);
		for (int i = 0; i < numOfPicks; i++) {
			String pickAssignURL = "http://localhost:9076/picking/v1/" + busName + "/" + locnNbr + "/picks/next/"
					+ batchNbr + "/" + userId;
			System.out.println("pick assign url:" + pickAssignURL);
			// assign next pick
			PickDTO pickDTO = restTemplate.postForObject(pickAssignURL, null, PickDTO.class);
			System.out.println("next pick:" + pickDTO);
			// pick confirm the pick
			PickConfirmRequestDTO pickConfirmObj = new PickConfirmRequestDTO(pickDTO.getId(), pickDTO.getOrderId(),
					pickDTO.getBatchNbr(), pickDTO.getBusName(), pickDTO.getLocnNbr(), pickDTO.getBusUnit(),
					pickDTO.getCompany(), pickDTO.getDivision(), pickDTO.getOrderNbr(), pickDTO.getLocnBrcd(),
					pickDTO.getItemBrcd(), pickDTO.getQty(), containerNbr, userId);
			String pickConfirmURL = "http://localhost:9076/picking/v1/" + busName + "/" + locnNbr + "/picks/"
					+ pickDTO.getId();
			System.out.println("pick confirm url:" + pickConfirmURL);
			PickDTO pickConfirmDTO = restTemplate.postForObject(pickConfirmURL, pickConfirmObj, PickDTO.class);
			System.out.println("Pick Confirm Reponse:" + pickConfirmDTO);
		}
		return containerNbr;
	}

	public void packAllOrders(String busName, Integer locnNbr, String company, String division, String busUnit,
			int numOfOrders, int numOfOrderLines, String batchNbr, String userId, String containerNbr) {
		RestTemplate restTemplate = new RestTemplate();
		int numOfPicks = numOfOrders * numOfOrderLines;
		String packsGETURL = "http://localhost:9298/packing/v1/" + busName + "/" + locnNbr + "/packs/container/"
				+ containerNbr;
		System.out.println("pack getpacks url:" + packsGETURL);
		// List<PackDTO> packDTOList = restTemplate.getForObject(packsGETURL,
		// List.class);
		ResponseEntity<List<PackDTO>> response = restTemplate.exchange(packsGETURL, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<PackDTO>>() {
				});
		List<PackDTO> packDTOList = response.getBody();

		System.out.println("pack getpacks response:" + packDTOList);
		for (int i = 0; i < packDTOList.size(); i++) {
			PackDTO packDTO = packDTOList.get(i);
			// pack confirm
			PackConfirmRequestDTO packConfirmObj = new PackConfirmRequestDTO(packDTO.getId(), packDTO.getOrderId(),
					packDTO.getBatchNbr(), packDTO.getBusName(), packDTO.getLocnNbr(), packDTO.getBusUnit(),
					packDTO.getCompany(), packDTO.getDivision(), packDTO.getOrderNbr(), packDTO.getItemBrcd(),
					packDTO.getQty(), containerNbr, "", userId);
			String packConfirmURL = "http://localhost:9298/packing/v1/" + busName + "/" + locnNbr + "/packs/"
					+ packDTO.getId();
			System.out.println("pack confirm url:" + packConfirmURL);
			PackDTO pickConfirmDTO = restTemplate.postForObject(packConfirmURL, packConfirmObj, PackDTO.class);
			System.out.println("Pack Confirm Reponse:" + pickConfirmDTO);
		}
	}

	// @Test
	public void pickBatch() {
		String busName = "XYZ";
		Integer locnNbr = 3456;
		String busUnit = "71";
		String company = "IE";
		String division = "09";
		String userId = "Krishna";
		int numOfOrders = 1;
		int numOfOrderLines = 1; // num of order lines
		String batchNbr = "20180927110815";

		this.pickAllOrders(busName, locnNbr, company, division, busUnit, numOfOrders, numOfOrderLines, batchNbr,
				userId);
	}

}
