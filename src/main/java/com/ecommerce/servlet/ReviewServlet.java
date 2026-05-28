package com.ecommerce.servlet;

import com.ecommerce.service.ReviewService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * REST API for product reviews (comments + ratings).
 *
 * Routes:
 *   GET    /api/products/{productId}/reviews         → list reviews + stats for a product
 *   POST   /api/products/{productId}/reviews         → add a review  (CUSTOMER / ADMIN)
 *   GET    /api/products/{productId}/reviews/{id}    → get one review
 *   PUT    /api/products/{productId}/reviews/{id}    → edit a review (author / ADMIN)
 *   DELETE /api/products/{productId}/reviews/{id}    → delete a review (author / ADMIN)
 *
 *   GET    /api/reviews/me                           → all reviews by the current user
 *
 * Business rule: at least one of `rating` or `comment` must be provided.
 *                Both are optional individually — a user can rate without writing text,
 *                or write a comment without selecting stars.
 */
@WebServlet({"/api/products/*/reviews", "/api/products/*/reviews/*", "/api/reviews/me"})
public class ReviewServlet extends BaseServlet {

    private final ReviewService reviewService = new ReviewService();

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();

        // GET /api/reviews/me
        if (uri.endsWith("/api/reviews/me")) {
            Long userId = (Long) req.getAttribute("userId");
            if (userId == null) { sendError(res, 401, "Authentication required"); return; }
            sendJson(res, 200, reviewService.getReviewsByUser(userId));
            return;
        }

        try {
            long[] ids = parseProductAndReviewId(req);
            long productId = ids[0];
            long reviewId  = ids[1];  // -1 if not present

            if (reviewId == -1) {
                // List all reviews for the product (public endpoint)
                sendJson(res, 200, reviewService.getProductReviews(productId));
            } else {
                sendJson(res, 200, reviewService.getReviewById(reviewId));
            }

        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error");
        }
    }

    // -------------------------------------------------------------------------
    // POST – add a review
    // -------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = (Long) req.getAttribute("userId");
        if (userId == null) { sendError(res, 401, "Authentication required"); return; }

        try {
            long[] ids = parseProductAndReviewId(req);
            long productId = ids[0];

            Map<String, String> body = readBody(req);
            String ratingStr = body.get("rating");
            String comment   = body.get("comment");

            // Parse rating — null is allowed (comment-only review)
            Integer rating = null;
            if (ratingStr != null && !ratingStr.isBlank() && !ratingStr.equals("null")) {
                rating = Integer.parseInt(ratingStr);
            }

            // Validate: at least one of rating or comment must be provided
            boolean hasRating  = rating != null;
            boolean hasComment = comment != null && !comment.trim().isEmpty();
            if (!hasRating && !hasComment) {
                sendError(res, 400, "Please provide at least a rating or a comment.");
                return;
            }

            Map<String, Object> review = reviewService.addReview(productId, userId, rating, comment);
            sendJson(res, 201, review);

        } catch (NumberFormatException e) {
            sendError(res, 400, "rating must be an integer between 1 and 5");
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error");
        }
    }

    // -------------------------------------------------------------------------
    // PUT – update a review
    // -------------------------------------------------------------------------

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = (Long) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) { sendError(res, 401, "Authentication required"); return; }

        try {
            long[] ids = parseProductAndReviewId(req);
            long reviewId = ids[1];
            if (reviewId == -1) { sendError(res, 400, "Review ID is required"); return; }

            Map<String, String> body = readBody(req);
            String ratingStr = body.get("rating");
            String comment   = body.get("comment");

            Integer rating = null;
            if (ratingStr != null && !ratingStr.isBlank() && !ratingStr.equals("null")) {
                rating = Integer.parseInt(ratingStr);
            }

            Map<String, Object> updated = reviewService.updateReview(
                    reviewId, userId, role, rating, comment);
            sendJson(res, 200, updated);

        } catch (NumberFormatException e) {
            sendError(res, 400, "rating must be an integer between 1 and 5");
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error");
        }
    }

    // -------------------------------------------------------------------------
    // DELETE – remove a review
    // -------------------------------------------------------------------------

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = (Long) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) { sendError(res, 401, "Authentication required"); return; }

        try {
            long[] ids = parseProductAndReviewId(req);
            long reviewId = ids[1];
            if (reviewId == -1) { sendError(res, 400, "Review ID is required"); return; }

            reviewService.deleteReview(reviewId, userId, role);
            sendJson(res, 200, Map.of("message", "Review deleted successfully"));

        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private long[] parseProductAndReviewId(HttpServletRequest req) {
        String uri = req.getRequestURI().replaceFirst(req.getContextPath(), "");
        String[] parts = uri.split("/");
        // parts: ["", "api", "products", "42", "reviews", "7?"]
        try {
            long productId = Long.parseLong(parts[3]);
            long reviewId  = parts.length >= 6 ? Long.parseLong(parts[5]) : -1L;
            return new long[]{productId, reviewId};
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid product or review ID in URL");
        }
    }
}
