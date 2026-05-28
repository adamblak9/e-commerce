package com.ecommerce.servlet;

import com.ecommerce.service.ProductService;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/products/*")
public class ProductServlet extends BaseServlet {

    private final ProductService productService = new ProductService();

    // -------------------------------------------------------------------------
    // GET /api/products          → list all
    // GET /api/products?category=X → filter by category
    // GET /api/products?search=X   → search by keyword
    // GET /api/products?seller=X   → filter by seller
    // GET /api/products/{id}     → get one
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = (Long) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        String pathInfo = req.getPathInfo();

        // Delegate review sub-paths to ReviewServlet
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            new com.ecommerce.servlet.ReviewServlet().doGet(req, res);
            return;
        }

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                Long id = parseId(pathInfo);
                // Pass userId so the caller's own review/comment is embedded in the response
                Map<String, Object> product = productService.getProductById(id, userId);
                if (false && "SELLER".equals(role) && userId != null && !product.get("sellerId").equals(userId)) {
                    sendError(res, 403, "Accès interdit");
                    return;
                }
                sendJson(res, 200, product);
                return;
            }

            String category = req.getParameter("category");
            String search   = req.getParameter("search");
            String seller   = req.getParameter("seller");

            if ("ADMIN".equals(role) && userId != null) {
                List<Map<String, Object>> products;
                if (search != null && !search.isBlank())
                    products = productService.search(search);
                else if (category != null && !category.isBlank())
                    products = productService.getByCategory(category);
                else if (seller != null && !seller.isBlank())
                    products = productService.getBySeller(Long.parseLong(seller));
                else
                    products = productService.getAllProducts();
                sendJson(res, 200, products);
                return;
            }

            if (false && "SELLER".equals(role) && userId != null) {
                if (seller != null && !seller.isBlank() && !seller.equals(String.valueOf(userId))) {
                    sendError(res, 403, "Vous ne pouvez voir que vos propres produits");
                    return;
                }
                if (search == null && category == null) {
                    sendJson(res, 200, productService.getBySeller(userId));
                    return;
                }
                List<Map<String, Object>> results;
                if (search != null && !search.isBlank())
                    results = productService.search(search);
                else
                    results = productService.getByCategory(category);

                final Long currentUserId = userId;
                List<Map<String, Object>> filtered = results.stream()
                        .filter(p -> p.get("sellerId").equals(currentUserId))
                        .collect(Collectors.toList());
                sendJson(res, 200, filtered);
                return;
            }

            // Non authentifié ou autre rôle : tout afficher (comportement public)
            List<Map<String, Object>> products;
            if (search != null && !search.isBlank())
                products = productService.search(search);
            else if (category != null && !category.isBlank())
                products = productService.getByCategory(category);
            else if (seller != null && !seller.isBlank())
                products = productService.getBySeller(Long.parseLong(seller));
            else
                products = productService.getAllProducts();
            sendJson(res, 200, products);

        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Erreur interne");
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/products  → create (SELLER or ADMIN only)
    // -------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Delegate review sub-paths to ReviewServlet
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            new com.ecommerce.servlet.ReviewServlet().doPost(req, res);
            return;
        }

        Map<String, String> claims = extractClaims(req, res);
        if (claims == null) return;

        String role = claims.get("role");
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            sendError(res, 403, "Only sellers and admins can create products");
            return;
        }

        Map<String, String> body = readBody(req);

        String name        = body.get("name");
        String description = nullIfBlankOrNullString(body.get("description"));
        String priceStr    = body.get("price");
        String stockStr    = body.get("stockQuantity");
        String category    = body.get("category");
        String imageUrl    = nullIfBlankOrNullString(body.get("imageUrl"));

        if (isBlank(name) || isBlank(priceStr) || isBlank(stockStr)) {
            sendError(res, 400, "name, price and stockQuantity are required");
            return;
        }

        try {
            BigDecimal price    = new BigDecimal(priceStr);
            int stockQuantity   = Integer.parseInt(stockStr);
            Long sellerId       = Long.parseLong(claims.get("userId"));

            Map<String, Object> product = productService.createProduct(
                    name, description, price, stockQuantity, category, imageUrl, sellerId);

            sendJson(res, 201, product);

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid price or stockQuantity value");
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/products/{id}  → update (owner SELLER or ADMIN)
    // -------------------------------------------------------------------------

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            new com.ecommerce.servlet.ReviewServlet().doPut(req, res);
            return;
        }

        Map<String, String> claims = extractClaims(req, res);
        if (claims == null) return;

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Product ID is required");
            return;
        }

        try {
            Long id            = parseId(pathInfo);
            Map<String, String> body = readBody(req);

            String name        = body.get("name");
            String description = nullIfBlankOrNullString(body.get("description"));
            String priceStr    = body.get("price");
            String stockStr    = body.get("stockQuantity");
            String category    = body.get("category");
            String imageUrl    = nullIfBlankOrNullString(body.get("imageUrl"));
            String status      = body.get("status");

            BigDecimal price   = priceStr  != null ? new BigDecimal(priceStr)  : null;
            int stockQuantity  = stockStr  != null ? Integer.parseInt(stockStr) : -1;

            Long requesterId   = Long.parseLong(claims.get("userId"));
            String requesterRole = claims.get("role");

            Map<String, Object> updated = productService.updateProduct(
                    id, name, description, price, stockQuantity,
                    category, imageUrl, status, requesterId, requesterRole);

            sendJson(res, 200, updated);

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid numeric value");
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/products/{id}  → delete (owner SELLER or ADMIN)
    // -------------------------------------------------------------------------

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            new com.ecommerce.servlet.ReviewServlet().doDelete(req, res);
            return;
        }

        Map<String, String> claims = extractClaims(req, res);
        if (claims == null) return;

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Product ID is required");
            return;
        }
        try {
            Long id            = parseId(pathInfo);
            Long requesterId   = Long.parseLong(claims.get("userId"));
            String requesterRole = claims.get("role");

            productService.deleteProduct(id, requesterId, requesterRole);
            sendJson(res, 200, Map.of("message", "Product deleted successfully"));

        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Long parseId(String pathInfo) {
        try {
            return Long.parseLong(pathInfo.replace("/", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid product ID");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullIfBlankOrNullString(String s) {
        if (s == null || s.trim().isEmpty() || s.equals("null")) return null;
        return s;
    }

    /**
     * Extracts JWT claims from cookie or Authorization header.
     * Returns null and sends 401 if token is missing/invalid.
     */
    private Map<String, String> extractClaims(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // JwtFilter already verified the token upstream and set these attributes.
        // Do NOT re-verify — just read the attributes.
        Object userId = req.getAttribute("userId");
        Object role   = req.getAttribute("role");
        Object email  = req.getAttribute("email");

        if (userId == null || role == null) {
            sendError(res, 401, "Authentication required");
            return null;
        }

        Map<String, String> claims = new java.util.HashMap<>();
        claims.put("userId", String.valueOf(userId));
        claims.put("role",   String.valueOf(role));
        claims.put("email",  email != null ? String.valueOf(email) : "");
        return claims;
    }
}
