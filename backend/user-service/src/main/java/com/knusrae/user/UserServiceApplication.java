package com.knusrae.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.knusrae.user", "com.knusrae.common"})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"com.knusrae.common.domain.repository", "com.knusrae.user.api.domain.repository"})
@EntityScan(basePackages = {"com.knusrae.common.domain.entity", "com.knusrae.user.api.domain.entity"})
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
