package com.phenikaa.tourService.specification;

import com.phenikaa.tourService.dto.request.SearchTourCriteria;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.Review;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TourSpecification {
    public static Specification<Tour> withDynamicFilters(SearchTourCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text search fields (LIKE %text%)
            if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("title")),
                        "%" + criteria.getTitle().toUpperCase() + "%"));
            }

            if (criteria.getDeparture() != null && !criteria.getDeparture().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("departure")),
                        "%" + criteria.getDeparture().toUpperCase() + "%"));
            }

            if (criteria.getDestination() != null && !criteria.getDestination().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("destination")),
                        "%" + criteria.getDestination().toUpperCase() + "%"));
            }

            // Status filter (exact match)
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // Category name filter (join với Category) - support multiple categories
            if (criteria.getCategoryNames() != null && !criteria.getCategoryNames().isEmpty()) {
                // Use IN clause for multiple category names
                List<String> upperCaseNames = criteria.getCategoryNames().stream()
                        .map(String::toUpperCase)
                        .toList();
                predicates.add(criteriaBuilder.upper(root.get("category").get("name")).in(upperCaseNames));
            } else if (criteria.getCategoryName() != null && !criteria.getCategoryName().trim().isEmpty()) {
                // Backward compatibility for single category name
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("category").get("name")),
                        "%" + criteria.getCategoryName().toUpperCase() + "%"));
            }

            // Price range filter
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("adultPrice"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("adultPrice"), criteria.getMaxPrice()));
            }

            // Duration range filter
            if (criteria.getMinDuration() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("duration"), criteria.getMinDuration()));
            }

            if (criteria.getMaxDuration() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("duration"), criteria.getMaxDuration()));
            }

            // Boolean flags filter
            if (criteria.getFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("featured"), criteria.getFeatured()));
            }

            if (criteria.getIsHot() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isHot"), criteria.getIsHot()));
            }

            if (criteria.getHasPromotion() != null) {
                predicates.add(criteriaBuilder.equal(root.get("hasPromotion"), criteria.getHasPromotion()));
            }

            // Rating filter - calculate average rating from reviews
            if (criteria.getMinRating() != null || criteria.getMaxRating() != null) {
                // Create subquery to calculate average rating
                var subquery = query.subquery(Double.class);
                Root<Review> reviewRoot = subquery.from(Review.class);
                subquery.select(criteriaBuilder.avg(reviewRoot.get("rating")))
                        .where(criteriaBuilder.equal(reviewRoot.get("tour"), root));

                if (criteria.getMinRating() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            subquery, criteria.getMinRating().doubleValue()));
                }

                if (criteria.getMaxRating() != null) {
                    predicates.add(criteriaBuilder.lessThan(
                            subquery, criteria.getMaxRating().doubleValue()));
                }
            }

            // Schedule date range filters (only for AVAILABLE schedules)
            if (criteria.getDepartureDate() != null && !criteria.getDepartureDate().trim().isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(criteria.getDepartureDate(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    Instant instant = dateTime.toInstant(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.join("schedules").get("departureDate"), instant),
                            criteriaBuilder.equal(
                                    root.join("schedules").get("status"), "AVAILABLE")));
                } catch (Exception e) {
                    // Log error but don't fail
                }
            }

            if (criteria.getReturnDate() != null && !criteria.getReturnDate().trim().isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(criteria.getReturnDate(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    Instant instant = dateTime.toInstant(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.join("schedules").get("returnDate"), instant),
                            criteriaBuilder.equal(
                                    root.join("schedules").get("status"), "AVAILABLE")));
                } catch (Exception e) {
                    // Log error but don't fail
                }
            }

            // Date range filter (optional)
            if (criteria.getCreatedAfter() != null && !criteria.getCreatedAfter().trim().isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(criteria.getCreatedAfter(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    Instant instant = dateTime.toInstant(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), instant));
                } catch (Exception e) {
                    // Log error but don't fail
                }
            }

            if (criteria.getCreatedBefore() != null && !criteria.getCreatedBefore().trim().isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(criteria.getCreatedBefore(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    Instant instant = dateTime.toInstant(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), instant));
                } catch (Exception e) {
                    // Log error but don't fail
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
