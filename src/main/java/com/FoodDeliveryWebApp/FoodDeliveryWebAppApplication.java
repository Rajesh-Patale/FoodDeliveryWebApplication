package com.FoodDeliveryWebApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FoodDeliveryWebAppApplication {

	public static void main(String[] args) {
		SpringApplication app= new SpringApplication(FoodDeliveryWebAppApplication.class);

		ConfigurableApplicationContext context = app.run(args);
		String localhostLink = "http://localhost:8080";
		System.out.println("Application is running at : "+localhostLink);
		System.out.println("Hi");

	}

}
