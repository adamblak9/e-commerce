package com.ecommerce.servlet;

import com.ecommerce.service.ProductService;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
        String pathInfo = req.getPathInfo(); // e.g. "/5" or null

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                // GET /api/products/{id}
                Long id = parseId(pathInfo);
                Map<String, Object> product = productService.getProductById(id);
                sendJson(res, 200, product);

            } else {
                String category = req.getParameter("category");
                String search   = req.getParameter("search");
                String seller   = req.getParameter("seller");

                List<Map<String, Object>> products;

                if (search != null && !search.isBlank()) {
                    products = productService.search(search);
                } else if (category != null && !category.isBlank()) {
                    products = productService.getByCategory(category);
                } else if (seller != null && !seller.isBlank()) {
                    products = productService.getBySeller(Long.parseLong(seller));
                } else {
                    products = productService.getAllProducts();
                }

                sendJson(res, 200, products);
            }

        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/products  → create (SELLER or ADMIN only)
    // -------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, String> claims = extractClaims(req, res);
        if (claims == null) return;

        String role = claims.get("role");
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            sendError(res, 403, "Only sellers and admins can create products");
            return;
        }

        Map<String, String> body = readBody(req);

        String name        = body.get("name");
        String description = body.get("description");
        String priceStr    = body.get("price");
        String stockStr    = body.get("stockQuantity");
        String category    = body.get("category");
        String imageUrl    = body.get("imageUrl");

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
        Map<String, String> claims = extractClaims(req, res);
        if (claims == null) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Product ID is required");
            return;
        }

        try {
            Long id            = parseId(pathInfo);
            Map<String, String> body = readBody(req);

            String name        = body.get("name");
            String description = body.get("description");
            String priceStr    = body.get("price");
            String stockStr    = body.get("stockQuantity");
            String category    = body.get("category");
            String imageUrl    = body.get("imageUrl");
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
        Map<String, String> claims = extractClaims(req, res);
        if (claims == null) return;

        String pathInfo = req.getPathInfo();
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

    /**
     * Extracts JWT claims from cookie or Authorization header.
     * Returns null and sends 401 if token is missing/invalid.
     */
    private Map<String, String> extractClaims(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String token = null;

        // 1. Try Authorization header
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. Fallback to cookie
        if (token == null && req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            sendError(res, 401, "Authentication required");
            return null;
        }

        try {
            com.auth0.jwt.interfaces.DecodedJWT jwt = JwtUtil.verify(token);
            Map<String, String> claims = new java.util.HashMap<>();
            claims.put("userId", String.valueOf(jwt.getClaim("userId").asLong()));
            claims.put("role",   jwt.getClaim("role").asString());
            claims.put("email",  jwt.getSubject());
            return claims;
        } catch (Exception e) {
            sendError(res, 401, "Invalid or expired token");
            return null;
        }
    }}