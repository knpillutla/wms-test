package com.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.responses.InventoryDTO;
import com.example.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.example.order.dto.requests.CustomerOrderLineCreationRequestDTO;
public class CustomerOrderCreator {
	public static List<CustomerOrderCreationRequestDTO> createNewCustomerOrders(List<InventoryCreatedEvent> invnEventCreatedList ) throws Exception {
		//EventReceiver receiver = new EventReceiver("wmsinventorycreator-consumer", "orders-out");
		String externalBatchNbr = RandomStringUtils.random(10, false, true);
		List<CustomerOrderCreationRequestDTO> orderCreationReqList = new ArrayList();
		for (InventoryCreatedEvent invnEventCreated : invnEventCreatedList) {
			List<CustomerOrderLineCreationRequestDTO> orderLines = new ArrayList();
			for (int line = 1; line <= 1; line++) {
				Random rand = new Random();
				InventoryDTO invnResponseResource = invnEventCreated.getInventoryDTO();
				String upc = invnResponseResource.getItemBrcd();//RandomStringUtils.random(20, false, true);
				Integer qty = invnResponseResource.getQty();//rand.nextInt(9);
				CustomerOrderLineCreationRequestDTO orderLine = new CustomerOrderLineCreationRequestDTO(line, upc, qty, qty, "", "");
				orderLines.add(orderLine);
			}
			Date orderDttm = DateUtils.addDays(new java.util.Date(), 0);
			Date shipDttm = DateUtils.addDays(orderDttm, 5);
			Date deliveryDttm = DateUtils.addDays(shipDttm, 7);
			String deliveryType=  RandomStringUtils.randomAlphabetic(2, 2);
			CustomerOrderCreationRequestDTO orderReq = new CustomerOrderCreationRequestDTO("XYZ", 3456, "", "", "71", externalBatchNbr,
					"F"+RandomStringUtils.random(9, false, true), orderDttm, shipDttm, deliveryDttm,deliveryType , false, "",
					"Website", "CustomerOrderDownload", "", "", "Krishna", orderLines);
			orderCreationReqList.add(orderReq);
		}
		System.out.println("Total CustomerOrder Requests Created:" + orderCreationReqList.size());
		System.out.println("End");
		return orderCreationReqList;
	}
}
