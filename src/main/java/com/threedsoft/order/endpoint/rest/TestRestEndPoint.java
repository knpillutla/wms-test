package com.threedsoft.order.endpoint.rest;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.threedsoft.customer.order.dto.requests.CustomerOrderCreationRequestDTO;
import com.threedsoft.test.service.EventPublisher;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/test/v1")
@Api(value="CustomerOrder Service", description="Operations pertaining to CustomerOrders")
@RefreshScope
@Slf4j
public class TestRestEndPoint {

	@Autowired
	EventPublisher sender;
	

	@Value("${message: CustomerOrder Service - Config Server is not working..please check}")
    private String msg;
    
	@GetMapping("/hello")
	public ResponseEntity hello() throws Exception {
		return ResponseEntity.ok(msg);
	}
	@GetMapping("/order")
	public ResponseEntity testCustomerOrder() throws Exception {
		//EventPublisher.send(wmsStreams.inboundCustomerOrders(), createNewCustomerOrder());
		return ResponseEntity.ok(msg);
	}

	public CustomerOrderCreationRequestDTO createNewCustomerOrder() {
		LocalDateTime currentDate = LocalDateTime.now();
		LocalDateTime shipDttm = currentDate.plusDays(5);
		LocalDateTime expectedDeliveryDttm = currentDate.plusDays(7);
		Random rand = new Random();
		int orderNbr = rand.nextInt(10000);
		CustomerOrderCreationRequestDTO orderCreationReq = new CustomerOrderCreationRequestDTO("AMZ", 1000, "", "", "", "TEST"+orderNbr,
				"FIR0" +orderNbr , currentDate, shipDttm, expectedDeliveryDttm, "Express", "N", "", "TestService",
				"TestCreateCustomerOrder", "", "", "Krishna", null);
		return orderCreationReq;
	}
}
