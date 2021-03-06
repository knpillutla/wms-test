package com.threedsoft.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.threedsoft.customer.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.threedsoft.customer.order.dto.requests.CustomerOrderLineCreationRequestDTO;
import com.threedsoft.inventory.dto.events.InventoryCreatedEvent;
import com.threedsoft.inventory.dto.responses.InventoryResourceDTO;
import com.threedsoft.order.dto.responses.OrderResourceDTO;
public class CustomerOrderCreator {
	public static List<CustomerOrderCreationRequestDTO> createNewCustomerOrders(List<InventoryCreatedEvent> invnEventCreatedList, int numOfOrders, int numOfOrderLines ) throws Exception {
		//EventReceiver receiver = new EventReceiver("wmsinventorycreator-consumer", "orders-out");
		String externalBatchNbr = RandomStringUtils.random(10, false, true);
		List<CustomerOrderCreationRequestDTO> orderCreationReqList = new ArrayList();
		Iterator<InventoryCreatedEvent> invnIterator = invnEventCreatedList.iterator();
		for (int i=0;i<numOfOrders;i++) {
			List<CustomerOrderLineCreationRequestDTO> orderLines = new ArrayList();
			for (int line = 1; line <= numOfOrderLines; line++) {
				Random rand = new Random();
				InventoryCreatedEvent invnCreatedEvent = invnIterator.next();
//				OrderResourceDTO orderDTOObj = (OrderResourceDTO) orderEventReceiver.getMapper().
//						convertValue(orderCreatedEvent.getEventResource(), OrderResourceDTO.class);

				//InventoryResourceDTO invnResponseResource = (InventoryResourceDTO) invnCreatedEvent.getEventResource();
				InventoryResourceDTO invnResponseResource = EventResourceConverter.getObject(invnCreatedEvent.getEventResource(), InventoryResourceDTO.class);
				String upc = invnResponseResource.getItemBrcd();//RandomStringUtils.random(20, false, true);
				Integer qty = invnResponseResource.getQty();//rand.nextInt(9);
				CustomerOrderLineCreationRequestDTO orderLine = new CustomerOrderLineCreationRequestDTO();
				orderLine.setBusName(invnResponseResource.getBusName());
				orderLine.setLocnNbr(invnResponseResource.getLocnNbr());
				orderLine.setOrderQty(qty);
				orderLine.setOrigOrderQty(qty);
				orderLine.setItemBrcd(upc);
				orderLine.setOrderLineNbr(line);
				
				orderLines.add(orderLine);
			}
			LocalDateTime orderDttm = LocalDateTime.now();
			LocalDateTime shipDttm = orderDttm.plusDays(5);
			LocalDateTime deliveryDttm = orderDttm.plusDays(7);
			String deliveryType=  RandomStringUtils.randomAlphabetic(2, 2);
			CustomerOrderCreationRequestDTO orderReq = new CustomerOrderCreationRequestDTO("XYZ", 3456, "", "", "71", externalBatchNbr,
					"F"+RandomStringUtils.random(9, false, true), orderDttm, shipDttm, deliveryDttm,deliveryType , "N", "",
					"Website", "CustomerOrderDownload", "", "", "Krishna", orderLines);
			orderCreationReqList.add(orderReq);
		}
		System.out.println("Total CustomerOrder Requests Created:" + orderCreationReqList.size());
		System.out.println("End");
		return orderCreationReqList;
	}
}
