package com.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.h2.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.inventory.dto.events.InventoryCreatedEvent;
import com.example.inventory.dto.requests.InventoryCreationRequestDTO;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderLineCreationRequestDTO;
import com.example.test.service.EventPublisher;

import junit.framework.Assert;
public class InventoryCreator {
	public static List<InventoryCreationRequestDTO> createNewInventoryRecords(int numOfUPCS) throws Exception {
		List<InventoryCreationRequestDTO> invnCreationReqList = new ArrayList();
		for (int i = 0; i < numOfUPCS; i++) {
			Random rand = new Random();
			String upc = RandomStringUtils.random(20, false, true);
			Integer qty = rand.nextInt(26);
			String aisle = new Integer(rand.nextInt(20)).toString();
			aisle = StringUtils.pad(aisle, 2,"0", false);
			String position = new Integer(rand.nextInt(50)).toString();
			aisle = StringUtils.pad(aisle, 2,"0", false);
			position = StringUtils.pad(aisle, 2,"0", true);
			String areaZone = RandomStringUtils.random(2, 0, 4, true, false, 'A','B','C','D','E');
			String level = RandomStringUtils.random(1, 0, 5, true, false, 'A','B','C','D','E','F');
			InventoryCreationRequestDTO inventoryReq = new InventoryCreationRequestDTO("InventoryCreationRequest","XYZ", 3456, "71", "", "", areaZone+aisle+level+position,
					RandomStringUtils.random(20, false, true), qty, "", false, "Krishna", false);
			invnCreationReqList.add(inventoryReq);
		}
		System.out.println("Created Inventory for :" + invnCreationReqList.size() +" UPCs");
		System.out.println("End");
		return invnCreationReqList;
	}
}
