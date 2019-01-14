package com.threedsoft.customerorder;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface CustomerOrderStreams {
	String CUSTOMER_ORDERS_INPUT = "customer-orders-in-dev";
	String CUSTOMER_ORDERS_OUTPUT = "customer-orders-out-dev";

	@Output(CUSTOMER_ORDERS_INPUT)
    public MessageChannel inboundCustomerOrders();

	@Input(CUSTOMER_ORDERS_OUTPUT)
    public SubscribableChannel outboundCustomerOrders();

}
