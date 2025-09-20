package com.knusrae.cook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = {"com.knusrae.cook", "com.knusrae.common"})
@EnableJpaAuditing
public class CookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CookServiceApplication.class, args);
	}
}
