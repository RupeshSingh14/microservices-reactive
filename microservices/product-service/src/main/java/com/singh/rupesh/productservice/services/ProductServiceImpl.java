package com.singh.rupesh.productservice.services;

import com.singh.rupesh.api.core.Product;
import com.singh.rupesh.api.core.ProductService;
import com.singh.rupesh.productservice.persistence.ProductEntity;
import com.singh.rupesh.productservice.persistence.ProductRepository;
import com.singh.rupesh.util.customExceptions.InvalidInputException;
import com.singh.rupesh.util.customExceptions.NotFoundException;
import com.singh.rupesh.util.exceptions.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger("ProductServiceImpl.class");

    private final ProductMapper mapper;
    private final ProductRepository repository;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ProductMapper mapper, ProductRepository repository, ServiceUtil serviceUtil) {
        this.mapper = mapper;
        this.repository = repository;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Product> getProduct(Integer productId) {
        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        return repository.findByProductId(productId)
                .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public Product createProduct(Product body) {
        if(body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, productId: " + body.getProductId()))
                .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public void deleteProduct(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        LOG.info("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();
    }
}