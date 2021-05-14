package com.singh.rupesh.productCompositeService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singh.rupesh.api.core.*;
import com.singh.rupesh.api.event.Event;
import com.singh.rupesh.api.event.EventType;
import com.singh.rupesh.productCompositeService.messages.MessageSources;
import com.singh.rupesh.util.customExceptions.InvalidInputException;
import com.singh.rupesh.util.customExceptions.NotFoundException;
import com.singh.rupesh.util.exceptions.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.singh.rupesh.api.event.EventType.CREATE;
import static com.singh.rupesh.api.event.EventType.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static reactor.core.publisher.Flux.empty;

@EnableBinding(MessageSources.class)
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger("ProductCompositeIntegration.class");

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private MessageSources messageSources;
    @Value("${app.product-service.host}")
    private String productServiceHost;
    @Value("${app.product-service.port}")
    private int productServicePort;
    @Value("${app.recommendation-service.host}")
    private String recommendationServiceHost;
    @Value("${app.recommendation-service.port}")
    private int recommendationServicePort;
    @Value("${app.review-service.host}")
    private String reviewServiceHost;
    @Value("${app.review-service.port}")
    private int reviewServicePort;

    public ProductCompositeIntegration(
            WebClient.Builder webClient,
            ObjectMapper mapper,
            MessageSources messageSources,
            String productServiceHost,
            int productServicePort,
            String recommendationServiceHost,
            int recommendationServicePort,
            String reviewServiceHost,
            int reviewServicePort
) {
        this.webClient = webClient.build();
        this.mapper = mapper;
        this.messageSources = messageSources;
        productServiceUrl = "http://" + productServiceHost + ':' + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ':' + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ':' + reviewServicePort + "/review";
    }

    @Override
    public Mono<Product> getProduct(Integer productId) {
        String url = productServiceUrl + "/" + productId;
        LOG.info("Will call the getProduct API on URL: {}", url);
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Product createProduct(Product body) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(),body)).build());
        return body;
    }

    @Override
    public void deleteProduct(int productId) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;
        LOG.info("Will call getRecommendations API on URL: {}", url);
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> empty());
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteRecommendations(int productId) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + "?productId=" + productId;
        LOG.info("Will call getReviews API on URL: {}",url);
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log()
                .onErrorResume(error -> empty());
    }

    @Override
    public Review createReview(Review body) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteReviews(int productId) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    private Throwable handleException(Throwable ex) {
        if(!(ex instanceof WebClientResponseException)) {
            LOG.error("Got an unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;
        switch (wcre.getStatusCode()) {
            case NOT_FOUND:
                throw new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                throw new InvalidInputException(getErrorMessage(wcre));
            default:
                LOG.error("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.error("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch(IOException ioex){
            return ex.getMessage();
        }
    }

}