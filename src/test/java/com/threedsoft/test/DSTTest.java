package com.threedsoft.test;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class DSTTest {

	@Test
    public void dstTest() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse("2018-11-04 01:50:00", formatter);
        for (int i = 0; i < 15; i++) {
            localDateTime = localDateTime.plusMinutes(1);
            System.out.println("Time:" + localDateTime);
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.parse("2018-11-04 01:50:00", formatter), ZoneId.systemDefault());
        for (int i = 0; i < 15; i++) {
            zonedDateTime = zonedDateTime.plusMinutes(1);
            System.out.println("Time:" + zonedDateTime);
        }
    }
}
