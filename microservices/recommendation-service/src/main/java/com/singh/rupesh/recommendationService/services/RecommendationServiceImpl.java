package com.singh.rupesh.recommendationService.services;

import com.mongodb.DuplicateKeyException;
import com.singh.rupesh.api.core.Recommendation;
import com.singh.rupesh.api.core.RecommendationService;
import com.singh.rupesh.recommendationService.persistence.RecommendationEntity;
import com.singh.rupesh.recommendationService.persistence.RecommendationRepository;
import com.singh.rupesh.util.customExceptions.InvalidInputException;
import com.singh.rupesh.util.exceptions.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if(productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        return repository.findByProductId(productId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, productId: " + body.getProductId() + ", recommendationId: " + body.getRecommendationId()))
                .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public void deleteRecommendations(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        LOG.info("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
    }
}