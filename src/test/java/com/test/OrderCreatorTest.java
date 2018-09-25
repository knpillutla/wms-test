package com.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.h2.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderLineCreationRequestDTO;
import com.example.test.service.EventPublisher;

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
public class OrderCreatorTest {
	@Autowired
	WMSStreams wmsStreams;

	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();

	@Test
	public void createNewOrders() throws Exception {
		EventReceiver receiver = new EventReceiver("wmsinventorycreator-consumer", "orders-out");
		String externalBatchNbr = RandomStringUtils.random(10, false, true);
		for (int i = 0; i < 50; i++) {
			List<OrderLineCreationRequestDTO> orderLines = new ArrayList();
			for (int line = 1; line <= 4; line++) {
				Random rand = new Random();
				String upc = RandomStringUtils.random(20, false, true);
				Integer qty = rand.nextInt(9);
				OrderLineCreationRequestDTO orderLine = new OrderLineCreationRequestDTO(line, upc, qty, qty, "", "");
				orderLines.add(orderLine);
			}
			Date orderDttm = DateUtils.addDays(new java.util.Date(), 0);
			Date shipDttm = DateUtils.addDays(orderDttm, 5);
			Date deliveryDttm = DateUtils.addDays(shipDttm, 7);
			String deliveryType=  RandomStringUtils.randomAlphabetic(2, 2);
			OrderCreationRequestDTO orderReq = new OrderCreationRequestDTO("XYZ", 3456, "", "", "71", externalBatchNbr,
					"F"+RandomStringUtils.random(9, false, true), orderDttm, shipDttm, deliveryDttm,deliveryType , false, "",
					"Website", "OrderDownload", "", "", "Krishna", orderLines);
			//EventPublisher.send(wmsStreams.inboundOrders(), orderReq);
			List<OrderCreatedEvent> orderCreatedEventList = receiver.getEvent(OrderCreatedEvent.class);
			System.out.println("Received Event:" + orderCreatedEventList.get(0));
		}
		System.out.println("End");
	}
}
