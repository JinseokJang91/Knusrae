package com.knusrae.cook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.knusrae.cook", "com.knusrae.common"})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "com.knusrae.common.domain.repository",
        "com.knusrae.cook.api.recipe.domain.repository",
        "com.knusrae.cook.api.ingredient.domain.repository",
        "com.knusrae.cook.api.theme.domain.repository"
})
@EntityScan(basePackages = {
        "com.knusrae.common.domain.entity",
        "com.knusrae.cook.api.recipe.domain.entity",
        "com.knusrae.cook.api.ingredient.domain.entity",
        "com.knusrae.cook.api.theme.domain.entity"
})
public class CookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CookServiceApplication.class, args);
	}
}
