package com.example.test;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class WMSTestApplication {
	private Random random = new Random();
	public static void main(String[] args) {
		SpringApplication.run(WMSTestApplication.class, args);
	}
}
