package com.threedsoft.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.threedsoft.customer.order.dto.events.CustomerOrderCreatedEvent;
import com.threedsoft.customer.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.threedsoft.customer.order.dto.requests.CustomerOrderLineCreationRequestDTO;
import com.threedsoft.inventory.dto.events.InventoryCreatedEvent;
import com.threedsoft.test.service.EventPublisher;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		"spring.cloud.stream.bindings.inventory-in.group=wmsorder-producer",
		"spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		"spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		"spring.cloud.stream.kafka.binder.brokers=localhost:29092" }, classes = { EventPublisher.class,
				WMSStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableBinding(WMSStreams.class)
public class CustomerOrderCreatorTest {
	@Autowired
	WMSStreams wmsStreams;

	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();

	@Test
	public void createNewCustomerOrders() throws Exception {
		EventReceiver receiver = new EventReceiver("wmsinventorycreator-consumer", "orders-out");
		String externalBatchNbr = RandomStringUtils.random(10, false, true);
		for (int i = 0; i < 50; i++) {
			List<CustomerOrderLineCreationRequestDTO> orderLines = new ArrayList();
			for (int line = 1; line <= 4; line++) {
				Random rand = new Random();
				String upc = RandomStringUtils.random(20, false, true);
				Integer qty = rand.nextInt(9);
				CustomerOrderLineCreationRequestDTO orderLine = new CustomerOrderLineCreationRequestDTO(line, upc, qty, qty, "", "");
				orderLines.add(orderLine);
			}
			LocalDateTime orderDttm = LocalDateTime.now();
			LocalDateTime shipDttm = orderDttm.plusDays(5);
			LocalDateTime deliveryDttm = orderDttm.plusDays(7);
			String deliveryType=  RandomStringUtils.randomAlphabetic(2, 2);
			CustomerOrderCreationRequestDTO orderReq = new CustomerOrderCreationRequestDTO("XYZ", 3456, "", "", "71", externalBatchNbr,
					"F"+RandomStringUtils.random(9, false, true), orderDttm, shipDttm, deliveryDttm,deliveryType , false, "",
					"Website", "CustomerOrderDownload", "", "", "Krishna", orderLines);
			//EventPublisher.send(wmsStreams.inboundCustomerOrders(), orderReq);
			List<CustomerOrderCreatedEvent> orderCreatedEventList = receiver.getEvent(CustomerOrderCreatedEvent.class);
			System.out.println("Received Event:" + orderCreatedEventList.get(0));
		}
		System.out.println("End");
	}
}
