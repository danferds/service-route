package com.sr.serviceroute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceRouteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRouteApplication.class, args);
	}

}
