package com.threedsoft.test;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface WMSStreamsDev {
	String INVENTORY_INPUT = "inventory-in-dev";
	String INVENTORY_OUTPUT = "inventory-out-dev";
	String CUSTOMER_ORDERS_INPUT = "customer-orders-in-dev";
	String CUSTOMER_ORDERS_OUTPUT = "customer-orders-out-dev";
	String ORDERS_OUTPUT = "orders-out-dev";
	String PICK_OUTPUT = "pick-out-dev";
	String PACK_OUTPUT = "pack-out-dev";
	String SHIP_OUTPUT = "ship-out-dev";

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
