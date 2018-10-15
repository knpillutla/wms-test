package com.threedsoft.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.threedsoft.customer.order.dto.events.CustomerOrderCreatedEvent;
import com.threedsoft.customer.order.dto.events.CustomerOrderDownloadEvent;
import com.threedsoft.customer.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.threedsoft.inventory.dto.events.InventoryCreatedEvent;
import com.threedsoft.inventory.dto.events.InventoryReceivedEvent;
import com.threedsoft.inventory.dto.requests.InventoryCreationRequestDTO;
import com.threedsoft.test.service.EventPublisher;

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
public class WarehouseCustomerOrderCreationTest {
	@Autowired
	WMSStreams wmsStreams;
	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();
	List<CustomerOrderCreatedEvent> customerOrderCreatedEventList = new ArrayList();

	String customerOrderPort = "9010";
	String orderPlannerPort = "9011";
	String inventoryPort = "9012";
	String pickingPort = "9013";
	String packingPort = "9014";
	String shippingPort = "9015";
	String SERVICE_NAME="WarehouseCustomerOrderCreationTest";
	
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

		List<InventoryCreationRequestDTO> invnCreationReqList = InventoryCreator
				.createNewInventoryRecords(numOfOrders * numOfOrderLines);
		for (InventoryCreationRequestDTO inventoryReq : invnCreationReqList) {
			InventoryReceivedEvent upcReceivedEvent = new InventoryReceivedEvent(inventoryReq,SERVICE_NAME);
			EventPublisher.send(wmsStreams.inboundInventory(), upcReceivedEvent, upcReceivedEvent.getHeaderMap());
			List<InventoryCreatedEvent> inventoryEventList = inventoryEventReceiver
					.getEvent(InventoryCreatedEvent.class);
			invnCreatedEventList.addAll(inventoryEventList);
		}
		System.out.println("Inventory Created....");

		Assert.assertEquals(numOfOrders*numOfOrderLines, invnCreatedEventList.size());
		List<CustomerOrderCreationRequestDTO> orderCreationReqList = CustomerOrderCreator
				.createNewCustomerOrders(invnCreatedEventList, numOfOrders, numOfOrderLines);

		for (CustomerOrderCreationRequestDTO orderCreationReq : orderCreationReqList) {
			CustomerOrderDownloadEvent orderDloadEvent = new CustomerOrderDownloadEvent(orderCreationReq, SERVICE_NAME);
			EventPublisher.send(wmsStreams.inboundCustomerOrders(), orderDloadEvent, orderDloadEvent.getHeaderMap());
			List<CustomerOrderCreatedEvent> tmpCustomerOrderEventList = custOrderEventReceiver
					.getEvent(CustomerOrderCreatedEvent.class);
			customerOrderCreatedEventList.addAll(tmpCustomerOrderEventList);
		}
		Assert.assertEquals(1, customerOrderCreatedEventList.size());
		System.out.println("CustomerOrders Created....");
	}
}
