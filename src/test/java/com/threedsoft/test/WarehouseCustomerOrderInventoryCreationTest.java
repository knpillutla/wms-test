package com.threedsoft.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.threedsoft.customer.order.dto.events.CustomerOrderCreatedEvent;
import com.threedsoft.customer.order.dto.events.CustomerOrderDownloadEvent;
import com.threedsoft.customer.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.threedsoft.inventory.dto.events.InventoryAllocatedEvent;
import com.threedsoft.inventory.dto.events.InventoryCreatedEvent;
import com.threedsoft.inventory.dto.events.InventoryReceivedEvent;
import com.threedsoft.inventory.dto.requests.InventoryCreationRequestDTO;
import com.threedsoft.order.dto.events.OrderCreatedEvent;
import com.threedsoft.order.dto.events.OrderPackedEvent;
import com.threedsoft.order.dto.events.OrderPickedEvent;
import com.threedsoft.order.dto.events.OrderPlannedEvent;
import com.threedsoft.order.dto.requests.OrderFulfillmentRequestDTO;
import com.threedsoft.order.dto.responses.OrderFulfillmentResourceDTO;
import com.threedsoft.order.dto.responses.OrderResourceDTO;
import com.threedsoft.packing.dto.events.PackConfirmationEvent;
import com.threedsoft.packing.dto.events.PackCreatedEvent;
import com.threedsoft.packing.dto.requests.PackConfirmRequestDTO;
import com.threedsoft.packing.dto.responses.PackResourceDTO;
import com.threedsoft.picking.dto.events.PickConfirmationEvent;
import com.threedsoft.picking.dto.events.PickCreatedEvent;
import com.threedsoft.picking.dto.requests.PickConfirmRequestDTO;
import com.threedsoft.picking.dto.responses.PickResourceDTO;
import com.threedsoft.shipping.dto.events.ShipRoutingCompletedEvent;
import com.threedsoft.test.service.EventPublisher;
import com.threedsoft.user.dto.requests.UserCreationRequestDTO;
import com.threedsoft.user.dto.responses.UserResourceDTO;
import com.threedsoft.util.dto.events.EventResourceConverter;

import junit.framework.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		//"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.JsonDeserializer",
		//"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
		//"spring.kafka.consumer.JsonDeserializer.VALUE_DEFAULT_TYPE=com.threedsoft.util.dto.events.WMSEvent",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		//"spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		//"spring.cloud.stream.bindings.inventory-in.group=wmsorder-invn-producer",
		//"spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		//"spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		//"spring.cloud.stream.default.consumer.headerMode=raw",
		//"spring.cloud.stream.default.producer.headerMode=raw",
		//"spring.cloud.stream.kafka.binder.headers[0]=eventName",
		"spring.cloud.stream.kafka.binder.headers=eventName",
		"spring.cloud.stream.kafka.binder.auto-create-topics=false",
		"spring.cloud.stream.kafka.binder.brokers=10.0.75.1:29092",
		"spring.jackson.serialization.WRITE_DATES_AS_TIMESTAMPS=false",
//		"spring.cloud.stream.kafka.binder.brokers=35.236.192.133:9092"
		},
		classes = { EventPublisher.class,
				WMSStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableBinding(WMSStreams.class)
public class WarehouseCustomerOrderInventoryCreationTest {
	@Autowired
	WMSStreams wmsStreams;
	List<InventoryCreatedEvent> invnCreatedEventList = new ArrayList();
	List<CustomerOrderCreatedEvent> customerOrderCreatedEventList = new ArrayList();



	EventReceiver custOrderEventReceiver = new EventReceiver("whse-customerOrder-consumer",
			wmsStreams.CUSTOMER_ORDERS_OUTPUT);
	EventReceiver inventoryEventReceiver = new EventReceiver("whse-inventory-consumer",
			wmsStreams.INVENTORY_OUTPUT);
	EventReceiver orderEventReceiver = new EventReceiver("whse-order-consumer", wmsStreams.ORDERS_OUTPUT);
	EventReceiver pickEventReceiver = new EventReceiver("whse-pick-consumer", wmsStreams.PICK_OUTPUT);
	EventReceiver packEventReceiver = new EventReceiver("whse-pack-consumer", wmsStreams.PACK_OUTPUT);
	EventReceiver shipEventReceiver = new EventReceiver("whse-ship-consumer", wmsStreams.SHIP_OUTPUT);
	String SERVICE_NAME="WMSWarehouseEndToEndTest";
	static {
	    //for localhost testing only for https
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return true;
	        }
	    });
	}	
	
	String customerOrderPort = "9010";
	String orderPlannerPort = "9011";
	String inventoryPort = "9012";
	String pickingPort = "9013";
	String packingPort = "9014";
	String shippingPort = "9015";
	String eventMonitorPort = "9016";
	String userPort = "9017";
	String orderPlannerServiceHost = "localhost";
	String pickingServiceHost = "localhost";
	String packingServiceHost = "localhost";
	String inventoryServiceHost = "localhost";
	String customerOrderServiceHost = "localhost";
	String shippingServiceHost = "localhost";
	String eventMonitorServiceHost = "localhost";
	String userServiceHost = "localhost";
	
	// gcp ports
//	String configPort = "32444";
//	String customerOrderPort = "32445";
//	String inventoryPort = "32446";
//	String orderPlannerPort = "32447";
//	String packingPort = "32448";
//	String pickingPort = "32449";
//	String shippingPort = "32450";
//	String eventMonitorPort = "32450";
/*	String orderPlannerServiceHost = "the3dsoft.com";
	String pickingServiceHost = "the3dsoft.com";
	String packingServiceHost = "the3dsoft.com";
	String inventoryServiceHost = "the3dsoft.com";
	String customerOrderServiceHost = "the3dsoft.com";
	String shippingServiceHost = "the3dsoft.com";
	String eventMonitorServiceHost = "the3dsoft.com";
	String userServiceHost = "the3dsoft.com";
	String configPort = "8888";
	String customerOrderPort = "443";
	String inventoryPort = "443";
	String orderPlannerPort = "443";
	String packingPort = "443";
	String pickingPort = "443";
	String shippingPort = "443";
	String eventMonitorPort = "443";
	String userPort = "443";
*/	
	
	public void createUser() {
		RestTemplate restTemplate = new RestTemplate();
		String busName ="XYZ";
		Integer locnNbr = 3456;
		String userCreateURL = "https://" +userServiceHost + ":" + userPort + "/users/v1/" + busName + "/" + locnNbr
				+ "/user";
		System.out.println("user createUser url:" + userCreateURL);
		UserCreationRequestDTO userCreationReq = new UserCreationRequestDTO();
		userCreationReq.setUserName("julerl@gmail.com");
		userCreationReq.setFirstName("john");
		userCreationReq.setLastName("uler");
		userCreationReq.setMiddleName(RandomStringUtils.random(4,true, false));
		userCreationReq.setAddr1(RandomStringUtils.random(4,false, true));
		userCreationReq.setAddr2(RandomStringUtils.random(10,true, false));
		userCreationReq.setAddr3(RandomStringUtils.random(10,true, false));
		userCreationReq.setBusName(busName);
		userCreationReq.setDefLocnNbr(locnNbr);
		userCreationReq.setAuthType("google");
		userCreationReq.setAuthToken(RandomStringUtils.random(6,true, true));
		userCreationReq.setCity(RandomStringUtils.random(6,true, false));
		userCreationReq.setState(RandomStringUtils.random(2,true, false));
		userCreationReq.setCountry(RandomStringUtils.random(10,true, false));
		userCreationReq.setZipCode(RandomStringUtils.random(5,false, true));
		userCreationReq.setLocale(Locale.getDefault().getDisplayName());
		
		UserResourceDTO userResourceDTO = restTemplate.postForObject(userCreateURL, userCreationReq, UserResourceDTO.class);
		System.out.println("User Create Reponse:" + userResourceDTO);
	}

	@Test
	public void createInventoryAndCustomerOrdersOneOrdeLinePerCustomerOrder() throws Exception {
		String busName = "XYZ";
		Integer locnNbr = 3456;
		String busUnit = "71";
		String company = "IE";
		String division = "09";
		String userId = "Krishna";
		int numOfOrders = 1000;
		int numOfOrderLines = 1; // num of order lines
		int numOfOrdersPerBatch = 20;
		int numOfPickers = 25;
		int numOfPackers = 15;

		List<InventoryCreationRequestDTO> invnCreationReqList = InventoryCreator
				.createNewInventoryRecords(numOfOrders * numOfOrderLines);
		for (InventoryCreationRequestDTO inventoryReq : invnCreationReqList) {
			InventoryReceivedEvent upcReceivedEvent = new InventoryReceivedEvent(inventoryReq,SERVICE_NAME);
			EventPublisher.send(wmsStreams.inboundInventory(), upcReceivedEvent, upcReceivedEvent.getHeaderMap());
		}
		invnCreatedEventList.addAll(inventoryEventReceiver.getEvent(InventoryCreatedEvent.class));
		System.out.println("Inventory Created....");

		Assert.assertEquals(numOfOrders * numOfOrderLines, invnCreatedEventList.size());
		List<CustomerOrderCreationRequestDTO> orderCreationReqList = CustomerOrderCreator
				.createNewCustomerOrders(invnCreatedEventList, numOfOrders, numOfOrderLines);
		for (CustomerOrderCreationRequestDTO orderCreationReq : orderCreationReqList) {
			CustomerOrderDownloadEvent orderDloadEvent = new CustomerOrderDownloadEvent(orderCreationReq,SERVICE_NAME);
			EventPublisher.send(wmsStreams.inboundCustomerOrders(), orderDloadEvent, orderDloadEvent.getHeaderMap());
		}
		customerOrderCreatedEventList.addAll(custOrderEventReceiver.getEvent(CustomerOrderCreatedEvent.class));
		Assert.assertEquals(numOfOrders, customerOrderCreatedEventList.size());
		System.out.println("CustomerOrders Created....");
		List<OrderCreatedEvent> orderEventList = orderEventReceiver.getEvent(OrderCreatedEvent.class);
		Assert.assertEquals(numOfOrders, orderEventList.size());


		// plan order fulfillment
		List<String> batchNbrList = invokeOrderFulfillmentForWarehouse(busName, locnNbr, company, division, busUnit,
				RandomStringUtils.random(6, true, false), numOfOrders, orderEventList, numOfOrdersPerBatch);

		// this will be two events, order planned and order allocated.somehow, both orderplanned and order allocated are treated same as the json structure is same
		List<OrderPlannedEvent> orderPlannedEventList = orderEventReceiver.getEvent(OrderPlannedEvent.class);
		Assert.assertEquals(numOfOrders*2, orderPlannedEventList.size());

		List<InventoryAllocatedEvent> invnAllocatedEventList = inventoryEventReceiver.getEvent(InventoryAllocatedEvent.class);
		System.out.println("Inventory Allocated....");
		Assert.assertEquals(numOfOrders * numOfOrderLines, invnAllocatedEventList.size());

		// ensure picks are created
		List<PickCreatedEvent> pickCreatedEventList = pickEventReceiver.getEvent(PickCreatedEvent.class);
		Assert.assertEquals(numOfOrders * numOfOrderLines, pickCreatedEventList.size());

		// check order allocated events
/*		List<OrderPlannedEvent> orderAllocatedEventList = orderEventReceiver.getEvent(OrderAllocatedEvent.class);
		Assert.assertEquals(numOfOrders*2, orderAllocatedEventList.size());
*/
		// ensure ship is created
		/*
		 * List<ShipCreatedEvent> shipEventList =
		 * shipEventReceiver.getEvent(ShipCreatedEvent.class);
		 * Assert.assertEquals(numOfOrders, shipEventList.size());
		 */
		// ensure ship is routed
		List<ShipRoutingCompletedEvent> shipRoutingCompletedEventList = shipEventReceiver
				.getEvent(ShipRoutingCompletedEvent.class);
		Assert.assertEquals(numOfOrders, shipRoutingCompletedEventList.size());

		List<String> toteList = this.pickAllOrders(busName, locnNbr, company, division, busUnit, numOfOrders,
				numOfOrderLines, numOfPickers, batchNbrList);
		List<PickConfirmationEvent> pickConfirmationEventList = pickEventReceiver.getEvent(PickConfirmationEvent.class);
		Assert.assertEquals(numOfOrders * numOfOrderLines, pickConfirmationEventList.size());

		// confirm order picked
		List<OrderPickedEvent> orderPickedEventList = orderEventReceiver.getEvent(OrderPickedEvent.class);
		Assert.assertEquals(numOfOrders, orderPickedEventList.size());
		
		// ensure packs are created
		List<PackCreatedEvent> packCreatedEventList = packEventReceiver.getEvent(PackCreatedEvent.class);
		Assert.assertEquals(numOfOrders * numOfOrderLines, packCreatedEventList.size());

		// pack the task
		packAllOrders(busName, locnNbr, company, division, busUnit, numOfOrders, numOfOrderLines,numOfPackers,toteList);

		// ensure packs are confirmed
		List<PackConfirmationEvent> packConfirmedEventList = packEventReceiver.getEvent(PackConfirmationEvent.class);
		Assert.assertEquals(numOfOrders * numOfOrderLines, packConfirmedEventList.size());

		List<OrderPackedEvent> orderPackedEventList = orderEventReceiver.getEvent(OrderPackedEvent.class);
		Assert.assertEquals(numOfOrders, orderPackedEventList.size());
	}
/*
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
*/
	public List<String> invokeOrderFulfillmentForWarehouse(String busName, Integer locnNbr, String company,
			String division, String busUnit, String userId, int numOfOrders,
			List<OrderCreatedEvent> orderCreatedEventList, int numOfOrdersPerBatch) throws JsonParseException, JsonMappingException, IOException {
		List<String> batchNbrList = new ArrayList();

		int currentOrderCount = 0;
		List<Long> orderIdList = new ArrayList();
		String batchNbr = "";
		RestTemplate restTemplate = new RestTemplate();
		for (int i = 0; i < orderCreatedEventList.size(); i++) {
			OrderCreatedEvent orderCreatedEvent = orderCreatedEventList.get(i);
			OrderResourceDTO orderDTOObj = (OrderResourceDTO) EventResourceConverter
					.getObject(orderCreatedEvent.getEventResource(), orderCreatedEvent.getEventResourceClassName());
			orderIdList.add(orderDTOObj.getId());
			
			currentOrderCount++;
			if (currentOrderCount >= numOfOrdersPerBatch || ((i + 1) == orderCreatedEventList.size())) {
				currentOrderCount = 0;
				OrderFulfillmentRequestDTO orderFulfillmentReq = new OrderFulfillmentRequestDTO();
				orderFulfillmentReq.setBusName(busName);
				orderFulfillmentReq.setLocnNbr(locnNbr);
				orderFulfillmentReq.setCompany(company);
				orderFulfillmentReq.setDivision(division);
				orderFulfillmentReq.setBusUnit(busUnit);
				orderFulfillmentReq.setWarehouseMode(true);
				orderFulfillmentReq.setOrderIdList(orderIdList);
				String orderPlannerPlanOrderURL = "https://"+orderPlannerServiceHost + ":" + orderPlannerPort + "/orderplanner/v1/" + busName + "/" + locnNbr;
				System.out.println("order planner plan order url:" + orderPlannerPlanOrderURL);
				OrderFulfillmentResourceDTO response = restTemplate.postForObject(
						orderPlannerPlanOrderURL,
						orderFulfillmentReq, OrderFulfillmentResourceDTO.class);
				batchNbr = response.getBatchNbr();
				System.out.println("batch nbr from start order fulfillment:" + batchNbr);
				batchNbrList.add(batchNbr);
				System.out.println("order fulfillment response for warehouse:" + response);
				orderIdList = new ArrayList();
			}
		}
		return batchNbrList;
	}

	public List<String> pickAllOrders(String busName, Integer locnNbr, String company, String division, String busUnit,
			int numOfOrders, int numOfOrderLines, int numOfPickers, List<String> batchNbrList) {
		Random rand = new Random();
		RestTemplate restTemplate = new RestTemplate();
		int numOfPicks = numOfOrders * numOfOrderLines;
		List<String> pickerList = new ArrayList();
		for(int i=0;i<numOfPickers;i++) {
			pickerList.add(RandomStringUtils.random(6, true, false));
		}		
		List<String> toteList = new ArrayList();
		
		for (int j = 0; j < batchNbrList.size(); j++) {
			String batchNbr = batchNbrList.get(j);
			String userId = pickerList.get(rand.nextInt(pickerList.size()));
			System.out.println("Batch Nbr:" + batchNbr);
			String containerNbr = 5 + RandomStringUtils.random(19, false, true);
			String pickAssignURL = "https://" + pickingServiceHost + ":" + pickingPort + "/picking/v1/" + busName + "/" + locnNbr
					+ "/picks/next/" + batchNbr + "/" + userId;
			System.out.println("pick assign url:" + pickAssignURL);
			PickResourceDTO pickDTO = restTemplate.postForObject(pickAssignURL, null, PickResourceDTO.class);
			System.out.println("next pick:" + pickDTO);
			toteList.add(containerNbr); // create one tote per container to start with, later we can add tote capacity
			while (pickDTO != null) {
				// assign next pick
				// pick confirm the pick
				PickConfirmRequestDTO pickConfirmObj = new PickConfirmRequestDTO(pickDTO.getId(), pickDTO.getOrderId(),
						pickDTO.getBatchNbr(), pickDTO.getBusName(), pickDTO.getLocnNbr(), pickDTO.getBusUnit(),
						pickDTO.getCompany(), pickDTO.getDivision(), pickDTO.getOrderNbr(), pickDTO.getLocnBrcd(),
						pickDTO.getItemBrcd(), pickDTO.getQty(), containerNbr, userId);
				String pickConfirmURL = "https://" + pickingServiceHost + ":" + pickingPort + "/picking/v1/" + busName + "/" + locnNbr
						+ "/picks/" + pickDTO.getId();
				System.out.println("pick confirm url:" + pickConfirmURL);
				PickResourceDTO pickConfirmDTO = restTemplate.postForObject(pickConfirmURL, pickConfirmObj, PickResourceDTO.class);
				System.out.println("Pick Confirm Reponse:" + pickConfirmDTO);
				try {
					pickDTO = restTemplate.postForObject(pickAssignURL, null, PickResourceDTO.class);
					System.out.println("next pick:" + pickDTO);
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("No more picks available");
					pickDTO = null;
					break;
				}
			}
		}
		return toteList;
	}

	public void packAllOrders(String busName, Integer locnNbr, String company, String division, String busUnit,
			int numOfOrders, int numOfOrderLines, int numOfPackers, List<String> toteNbrList) {
		Random rand = new Random();
		RestTemplate restTemplate = new RestTemplate();
		List<String> packerList = new ArrayList();
		for(int i=0;i<numOfPackers;i++) {
			packerList.add(RandomStringUtils.random(6, true, false));
		}
		
		for (int j = 0; j < toteNbrList.size(); j++) {
			String toteNbr = toteNbrList.get(j);
			String packsGETURL = "https://" + packingServiceHost + ":" + packingPort + "/packing/v1/" + busName + "/" + locnNbr
					+ "/packs/container/" + toteNbr;
			System.out.println("pack getpacks url:" + packsGETURL);
			// List<PackDTO> packDTOList = restTemplate.getForObject(packsGETURL,
			// List.class);
			ResponseEntity<List<PackResourceDTO>> response = restTemplate.exchange(packsGETURL, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<PackResourceDTO>>() {
					});
			List<PackResourceDTO> packDTOList = response.getBody();

			String userId = packerList.get(rand.nextInt(packerList.size()));
			System.out.println("pack getpacks response:" + packDTOList);
			for (int i = 0; i < packDTOList.size(); i++) {
				PackResourceDTO packDTO = packDTOList.get(i);
				// pack confirm
				PackConfirmRequestDTO packConfirmObj = new PackConfirmRequestDTO(packDTO.getId(), packDTO.getOrderId(),
						packDTO.getBatchNbr(), packDTO.getBusName(), packDTO.getLocnNbr(), packDTO.getBusUnit(),
						packDTO.getCompany(), packDTO.getDivision(), packDTO.getOrderNbr(), packDTO.getItemBrcd(),
						packDTO.getQty(), toteNbr, "", userId);
				String packConfirmURL = "https://" + packingServiceHost + ":" + packingPort + "/packing/v1/" + busName + "/" + locnNbr
						+ "/packs/" + packDTO.getId();
				System.out.println("pack confirm url:" + packConfirmURL);
				PackResourceDTO pickConfirmDTO = restTemplate.postForObject(packConfirmURL, packConfirmObj, PackResourceDTO.class);
				System.out.println("Pack Confirm Reponse:" + pickConfirmDTO);
			}
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
		List<String> batchNbrList = new ArrayList();
		batchNbrList.add(batchNbr);
		int numOfPickers = 10;

		this.pickAllOrders(busName, locnNbr, company, division, busUnit, numOfOrders, numOfOrderLines, numOfPickers, batchNbrList);
	}

}
