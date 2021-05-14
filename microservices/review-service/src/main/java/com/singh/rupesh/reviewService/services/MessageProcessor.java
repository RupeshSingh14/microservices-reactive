package com.singh.rupesh.reviewService.services;

import com.singh.rupesh.api.core.Recommendation;
import com.singh.rupesh.api.core.Review;
import com.singh.rupesh.api.core.ReviewService;
import com.singh.rupesh.api.event.Event;
import com.singh.rupesh.util.customExceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final ReviewService reviewService;

    public MessageProcessor(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Review> event) {

        LOG.info("Process message created at {}", event.getEventCreateAt());
        switch (event.getEventType()) {
            case CREATE:
                Review review = event.getData();
                LOG.info("Create reviews with ID: {}/{}", review.getProductId(), review.getReviewId());
                reviewService.createReview(review);
                break;
            case DELETE:
                int productId = event.getKey();
                LOG.info("Delete reviews with productId: {}", productId);
                reviewService.deleteReviews(productId);
                break;
            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.error(errorMessage);
                throw new EventProcessingException(errorMessage);

        }
        LOG.info("Message processing done!");
    }
}
