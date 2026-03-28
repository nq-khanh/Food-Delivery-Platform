package com.hkt.fooddelivery.entity.listener;

import com.hkt.fooddelivery.entity.OrderReview;
import com.hkt.fooddelivery.entity.Restaurant;
import com.hkt.fooddelivery.entity.Shipper;
import jakarta.persistence.PostPersist;

public class OrderReviewListener {

    @PostPersist
    public void afterReviewSaved(OrderReview review) {
        Restaurant res = review.getRestaurant();
        if (res != null && review.getRestaurantRating() != null) {
            res.updateRating(review.getRestaurantRating());
        }

        Shipper shipper = review.getShipper();
        if (shipper != null && review.getShipperRating() != null) {
            shipper.updateRating(review.getShipperRating());
        }

        if (review.getItemReviews() != null) {
            review.getItemReviews().forEach(itemReview -> {
                if (itemReview.getProduct() != null) {
                    itemReview.getProduct().updateRating(itemReview.getRating());
                }
            });
        }
    }
}