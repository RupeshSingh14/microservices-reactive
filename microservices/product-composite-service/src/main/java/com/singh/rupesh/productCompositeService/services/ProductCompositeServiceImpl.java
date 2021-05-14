package com.singh.rupesh.productCompositeService.services;

import com.singh.rupesh.api.compositeProduct.*;
import com.singh.rupesh.api.core.Product;
import com.singh.rupesh.api.core.Recommendation;
import com.singh.rupesh.api.core.Review;
import com.singh.rupesh.util.customExceptions.NotFoundException;
import com.singh.rupesh.util.exceptions.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration){
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
        return Mono.zip(
                values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex -> LOG.error("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {
        try {
            LOG.info("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    integration.createReview(review);
                });
            }
            LOG.info("createCompositeProduct: composite entries created for productId: {}", body.getProductId());
        }catch(RuntimeException re) {
            LOG.error("createCompositeProduct failed due to {}", re.getMessage());
            throw re;
        }
    }

    @Override
    public void deleteCompositeProduct(int productId) {
        LOG.info("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
        integration.deleteProduct(productId);
        integration.deleteRecommendations(productId);
        integration.deleteReviews(productId);
        LOG.info("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        //setting product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        //copy recommendation summary info from recommendations(List), if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .collect(Collectors.toList());

        //copy review summary info from reviews(List), if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null  :
                reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .collect(Collectors.toList());

        //create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        //final data of product composite
        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}