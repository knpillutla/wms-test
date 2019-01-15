package com.threedsoft.customerorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.threedsoft.customerorder.service.dto.CustomerOrderDTO;
import com.threedsoft.customerorder.service.dto.CustomerOrderLineAttributeDTO;
import com.threedsoft.customerorder.service.dto.CustomerOrderLineDTO;
import com.threedsoft.test.EventReceiver;
import com.threedsoft.test.EventResourceConverter;
import com.threedsoft.test.service.EventPublisher;
import com.threedsoft.util.dto.events.WMSEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		// "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.JsonDeserializer",
		// "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
		// "spring.kafka.consumer.JsonDeserializer.VALUE_DEFAULT_TYPE=com.threedsoft.util.dto.events.WMSEvent",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		// "spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		// "spring. cloud.stream.bindings.inventory-in.group=wmsorder-invn-producer",
		// "spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		// "spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		// "spring.cloud.stream.default.consumer.headerMode=raw",
		// "spring.cloud.stream.default.producer.headerMode=raw",
		// "spring.cloud.stream.kafka.binder.headers[0]=eventName",
		"spring.cloud.stream.kafka.binder.headers=eventName",
		"spring.cloud.stream.kafka.binder.auto-create-topics=false",
		// "spring.cloud.stream.kafka.binder.brokers=kafka",
		"spring.jackson.serialization.WRITE_DATES_AS_TIMESTAMPS=false",
		"spring.cloud.stream.kafka.binder.brokers=localhost" }, classes = { EventPublisher.class,
				CustomerOrderStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableBinding(CustomerOrderStreams.class)
public class CustomerOrderCreationTest {
	@Autowired
	CustomerOrderStreams wmsStreams;
	List<WMSEvent> customerOrderCreatedEventList = new ArrayList();
	String kafkaHost = "localhost:9092";
	EventReceiver custOrderEventReceiver = new EventReceiver("CustomerOrderReceiver", wmsStreams.CUSTOMER_ORDERS_OUTPUT,
			kafkaHost);
	String SERVICE_NAME = "CustomerOrderTest";

	public CustomerOrderDTO createCustomerOrder(int numOfItems, int numOfItemAttribs) {
		CustomerOrderDTO customerOrderDTO = new CustomerOrderDTO();
		customerOrderDTO.setBusName("XYZ");// .setLogin("johndoe");
		customerOrderDTO.setFacilityNbr("3434");
		customerOrderDTO.setOrderNbr("F" + RandomStringUtils.randomNumeric(9));
		customerOrderDTO.setShipByDttm(Instant.now());
		customerOrderDTO.setExpectedDeliveryDttm(Instant.now());
		customerOrderDTO.setOrderDttm(Instant.now());
		customerOrderDTO.setCustFirstName("john");
		customerOrderDTO.setCustLastName("doe");
		customerOrderDTO.setCustMiddltName("N");
		customerOrderDTO.setDelAddr1("222 carolina st");
		customerOrderDTO.setDelCity("charlotte");
		customerOrderDTO.setDelZipCode("9393");
		customerOrderDTO.setCreatedBy("amazon");
		customerOrderDTO.setUpdatedBy("amazon");
		customerOrderDTO.setOrderLines(createCustomerOrderLines(numOfItems, numOfItemAttribs));
		return customerOrderDTO;
	}

	public Set<CustomerOrderLineDTO> createCustomerOrderLines(int numOfItems, int numOfAttribs) {
		Set<CustomerOrderLineDTO> orderLines = new HashSet();
		for (int i = 0; i < numOfItems; i++) {
			CustomerOrderLineDTO customerOrderLineDTO = new CustomerOrderLineDTO();
			customerOrderLineDTO.setLineNbr(i + 1);
			customerOrderLineDTO.setItemBrcd(RandomStringUtils.randomNumeric(10));
			customerOrderLineDTO.setQty(RandomUtils.nextInt(50));
			customerOrderLineDTO.setOrigQty(customerOrderLineDTO.getQty());
			Set<CustomerOrderLineAttributeDTO> attribs = new HashSet();
			for (int j = 0; j < numOfAttribs; j++) {
				CustomerOrderLineAttributeDTO attrib = new CustomerOrderLineAttributeDTO();
				attrib.setKey(RandomStringUtils.randomAlphabetic(8));
				attrib.setValue(RandomStringUtils.randomAlphabetic(15));
				attribs.add(attrib);
			}
			customerOrderLineDTO.setAttributes(attribs);
			orderLines.add(customerOrderLineDTO);
		}
		return orderLines;
	}

	public void createCustomerOrderList(int numOfOrders, int numOfItems, int numOfItemAttribs) {
		for (int i = 0; i < numOfOrders; i++) {
			CustomerOrderDTO customerOrderDTO = createCustomerOrder(numOfItems, numOfItemAttribs);
			WMSEvent customerOrderEvent = new WMSEvent("NewCustomerOrderEvent", customerOrderDTO.getBusName(),
					customerOrderDTO.getFacilityNbr(), customerOrderDTO.getCompany(), customerOrderDTO.getDivision(),
					null, "order", customerOrderDTO.getOrderNbr(), "customerorder", customerOrderDTO);
			EventPublisher.send(wmsStreams.inboundCustomerOrders(), customerOrderEvent,
					customerOrderEvent.getHeaderMap());
		}
	}

	@Test
	public void assertThatCustomerOrdersAreCreated() throws Exception {
		int numOfOrders = 10;
		int numOfItemsForEachOrder = 2;
		int numOfAttribsForEachItem = 3;
		createCustomerOrderList(numOfOrders, numOfItemsForEachOrder, numOfAttribsForEachItem);
		List<WMSEvent> wmsEventList = custOrderEventReceiver.getEvent(WMSEvent.class);
		assertNotNull(wmsEventList);
		assertEquals(numOfOrders, wmsEventList.size());
		for (WMSEvent event : wmsEventList) {
			System.out.println("Received event:" + event);

			CustomerOrderDTO createdCustomerOrderDTO = EventResourceConverter
					.getObject(event.getEventResource(), CustomerOrderDTO.class);
			assertEquals(numOfItemsForEachOrder, createdCustomerOrderDTO.getOrderLines().size());
			for (CustomerOrderLineDTO orderLine : createdCustomerOrderDTO.getOrderLines()) {
				assertEquals(numOfAttribsForEachItem, orderLine.getAttributes().size());
			}
		}
	}
}
