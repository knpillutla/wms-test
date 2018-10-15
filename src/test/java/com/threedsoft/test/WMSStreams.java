package com.threedsoft.test;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface WMSStreams {
	String INVENTORY_INPUT = "inventory-in";
	String INVENTORY_OUTPUT = "inventory-out";
	String CUSTOMER_ORDERS_INPUT = "customer-orders-in";
	String CUSTOMER_ORDERS_OUTPUT = "customer-orders-out";
	String ORDERS_OUTPUT = "orders-out";
	String PICK_OUTPUT = "pick-out";
	String PACK_OUTPUT = "pack-out";
	String SHIP_OUTPUT = "ship-out";

	@Output(INVENTORY_INPUT)
    public MessageChannel inboundInventory();
	
	@Output(CUSTOMER_ORDERS_INPUT)
    public MessageChannel inboundCustomerOrders();

	@Output(PICK_OUTPUT)
    public MessageChannel outboundPick();

	@Output(PACK_OUTPUT)
    public MessageChannel outboundPack();

	@Output(SHIP_OUTPUT)
    public MessageChannel outboundShip();
}
