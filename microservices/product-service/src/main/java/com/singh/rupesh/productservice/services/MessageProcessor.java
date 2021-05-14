package com.singh.rupesh.productservice.services;

import com.singh.rupesh.api.core.Product;
import com.singh.rupesh.api.core.ProductService;
import com.singh.rupesh.api.event.Event;
import com.singh.rupesh.util.customExceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final ProductService productService;

    @Autowired
    public MessageProcessor(ProductService productService) {
        this.productService = productService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Product> event) {

        LOG.info("Process message created at {}", event.getEventCreateAt());
        switch (event.getEventType()) {
            case CREATE:
                Product product = event.getData();
                LOG.info("Create product with productId: {}", product.getProductId());
                productService.createProduct(product);
                break;
            case DELETE:
                int productId = event.getKey();
                LOG.info("Delete product with productId: {}", productId);
                productService.deleteProduct(productId);
                break;
            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.error(errorMessage);
                throw new EventProcessingException(errorMessage);

        }
        LOG.info("Message processing done!");
    }
}
