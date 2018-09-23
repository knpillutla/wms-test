package com.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.responses.InventoryDTO;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderLineCreationRequestDTO;
import com.example.test.service.EventPublisher;
public class OrderCreator {
	public static List<OrderCreationRequestDTO> createNewOrders(List<InventoryCreatedEvent> invnEventCreatedList ) throws Exception {
		//EventReceiver receiver = new EventReceiver("wmsinventorycreator-consumer", "orders-out");
		String externalBatchNbr = RandomStringUtils.random(10, false, true);
		List<OrderCreationRequestDTO> orderCreationReqList = new ArrayList();
		for (InventoryCreatedEvent invnEventCreated : invnEventCreatedList) {
			List<OrderLineCreationRequestDTO> orderLines = new ArrayList();
			for (int line = 0; line < 1; line++) {
				Random rand = new Random();
				InventoryDTO invnResponseResource = invnEventCreated.getInventoryDTO();
				String upc = invnResponseResource.getItemBrcd();//RandomStringUtils.random(20, false, true);
				Integer qty = invnResponseResource.getQty();//rand.nextInt(9);
				OrderLineCreationRequestDTO orderLine = new OrderLineCreationRequestDTO(upc, qty, qty, "", "");
				orderLines.add(orderLine);
			}
			Date orderDttm = DateUtils.addDays(new java.util.Date(), 0);
			Date shipDttm = DateUtils.addDays(orderDttm, 5);
			Date deliveryDttm = DateUtils.addDays(shipDttm, 7);
			String deliveryType=  RandomStringUtils.randomAlphabetic(2, 2);
			OrderCreationRequestDTO orderReq = new OrderCreationRequestDTO("XYZ", 3456, "", "", "71", externalBatchNbr,
					"F"+RandomStringUtils.random(9, false, true), orderDttm, shipDttm, deliveryDttm,deliveryType , false, "",
					"Website", "OrderDownload", "", "", "Krishna", orderLines);
			orderCreationReqList.add(orderReq);
		}
		System.out.println("Total Order Requests Created:" + orderCreationReqList.size());
		System.out.println("End");
		return orderCreationReqList;
	}
}
