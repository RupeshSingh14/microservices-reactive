package com.singh.rupesh.reviewService.services;

import com.singh.rupesh.api.core.Review;
import com.singh.rupesh.api.core.ReviewService;
import com.singh.rupesh.reviewService.persistence.ReviewEntity;
import com.singh.rupesh.reviewService.persistence.ReviewRepository;
import com.singh.rupesh.util.customExceptions.InvalidInputException;
import com.singh.rupesh.util.exceptions.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository repository;
    private final ServiceUtil serviceUtil;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil){
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if(productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        return repository.findByProductId(productId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public Review createReview(Review body) {
        if(body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        ReviewEntity entity = mapper.apiToEntity(body);
        Mono<Review> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, productId: " + body.getProductId() + ", reviewId: " + body.getReviewId()))
                .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public void deleteReviews(int productId) {
        if(productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
    }
}