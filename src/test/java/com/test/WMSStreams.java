package com.test;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface WMSStreams {
	String INVENTORY_INPUT = "inventory-in";
	String ORDERS_INPUT = "orders-in";

	@Output(INVENTORY_INPUT)
    public MessageChannel inboundInventory();
	
	@Output(ORDERS_INPUT)
    public MessageChannel inboundOrders();

}
