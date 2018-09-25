package com.test;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface WMSStreams {
	String INVENTORY_INPUT = "inventory-in";
	String INVENTORY_OUTPUT = "inventory-out";
	String CUSTOMER_ORDERS_INPUT = "customer-orders-in";
	String CUSTOMER_ORDERS_OUTPUT = "customer-orders-out";

	@Output(INVENTORY_INPUT)
    public MessageChannel inboundInventory();
	
	@Output(CUSTOMER_ORDERS_INPUT)
    public MessageChannel inboundCustomerOrders();

}
