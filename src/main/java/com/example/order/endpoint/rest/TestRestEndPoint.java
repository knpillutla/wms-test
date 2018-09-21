package com.example.order.endpoint.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.test.service.EventPublisher;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/test/v1")
@Api(value="Order Service", description="Operations pertaining to Orders")
@RefreshScope
@Slf4j
public class TestRestEndPoint {

	@Autowired
	EventPublisher sender;
	

	@Value("${message: Order Service - Config Server is not working..please check}")
    private String msg;
    
	@GetMapping("/hello")
	public ResponseEntity hello() throws Exception {
		return ResponseEntity.ok(msg);
	}
	@GetMapping("/order")
	public ResponseEntity testOrder() throws Exception {
		//EventPublisher.send(wmsStreams.inboundOrders(), createNewOrder());
		return ResponseEntity.ok(msg);
	}

	public OrderCreationRequestDTO createNewOrder() {
		Date currentDate = new java.util.Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.DATE, 5);
		Date shipDttm = cal.getTime();
		cal.add(Calendar.DATE, 10);
		Date expectedDeliveryDttm = cal.getTime();
		Random rand = new Random();
		int orderNbr = rand.nextInt(10000);
		OrderCreationRequestDTO orderCreationReq = new OrderCreationRequestDTO("AMZ", 1000, "", "", "", "TEST"+orderNbr,
				"FIR0" +orderNbr , currentDate, shipDttm, expectedDeliveryDttm, "Express", false, "", "TestService",
				"TestCreateOrder", "", "", "Krishna");
		return orderCreationReq;
	}
}
