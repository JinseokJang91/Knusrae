package com.knusrae.cook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.knusrae.cook", "com.knusrae.common"})
public class CookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CookServiceApplication.class, args);
	}

}
