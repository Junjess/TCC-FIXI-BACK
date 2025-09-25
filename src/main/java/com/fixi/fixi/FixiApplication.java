package com.fixi.fixi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.fixi.fixi")
@EnableScheduling
public class FixiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FixiApplication.class, args);
	}

}
