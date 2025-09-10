package com.fixi.fixi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fixi.fixi")
public class FixiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FixiApplication.class, args);
	}

}
