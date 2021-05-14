package com.singh.rupesh.productCompositeService;

import com.singh.rupesh.productCompositeService.services.ProductCompositeIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@SpringBootApplication
@ComponentScan("com.singh.rupesh")
public class ProductCompositeServiceApplication {

//	@Autowired
//	HealthIndicator healthIndicator;
//
//	@Autowired
//	ProductCompositeIntegration integration;
//
//	@Bean
//	ReactiveHealthIndicator coreServices() {
//		ReactiveHealthContributorRegistry registry = new DefaultReactiveHealthContributorRegistry(new LinkedHashMap<>());
//		registry.registerContributor("product", () -> integration.getProductHealth());
//		registry.registerContributor("recommendation", () -> integration.getRecommendationHealth());
//		registry.registerContributor("review", () -> integration.getReviewHealth());
//		return new Compo
//	}


	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}


}
