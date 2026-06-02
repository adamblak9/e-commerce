package com.ecommerce.servlet;

import com.ecommerce.servlet.MetricsServlet;
import com.ecommerce.filter.RoleGuard;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GET    /api/cart        → get current user's cart
 * POST   /api/cart        → add item  { productId, productName, price, quantity, imageUrl }
 * PUT    /api/cart        → update qty { productId, quantity }
 * DELETE /api/cart        → remove item { productId } OR clear cart if no body
 * POST   /api/cart/merge  → merge guest cart on login { items: [...] }
 */
@WebServlet("/api/cart/*")
public class CartServlet extends BaseServlet {

    private final CartService cartService = new CartService();

    // ── Read body as Map<String, Object> ─────────────
    // BaseServlet.readBody() returns Map<String,String> which is too narrow.
    // We use Jackson directly here to get Map<String, Object>.
    private Map<String, Object> readObjectBody(HttpServletRequest req) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        return mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
    }

    // ── GET — load cart ───────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Login required"); return; }
        try {
            sendJson(res, 200, cartService.getCart(userId));
        } catch (Exception e) {
            sendError(res, 500, "Could not load cart: " + e.getMessage());
        }
    }

    // ── POST — add item or merge guest cart ───────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Login required"); return; }

        String path = req.getPathInfo(); // "/merge" or null

        // ── Merge guest cart on login ─────────────────
        if ("/merge".equals(path)) {
            try {
                Map<String, Object> body = readObjectBody(req);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items =
                        (List<Map<String, Object>>) body.get("items");

                if (items == null || items.isEmpty()) {
                    sendJson(res, 200, cartService.getCart(userId));
                    return;
                }
                sendJson(res, 200, cartService.mergeGuestCart(userId, items));
            } catch (Exception e) {
                sendError(res, 500, "Merge failed: " + e.getMessage());
            }
            return;
        }

        // ── Add item ──────────────────────────────────
        try {
            Map<String, Object> body = readObjectBody(req);

            Long       productId   = Long.valueOf(body.get("productId").toString());
            String     productName = body.get("productName").toString();
            BigDecimal price       = new BigDecimal(body.get("price").toString());
            int        quantity    = body.containsKey("quantity")
                    ? Integer.parseInt(body.get("quantity").toString()) : 1;
            String     imageUrl    = body.getOrDefault("imageUrl", "").toString();

            sendJson(res, 200,
                    cartService.addItem(userId, productId, productName, price, quantity, imageUrl));
            MetricsServlet.cartAddedTotal.incrementAndGet();
        } catch (IllegalStateException e) {
            sendError(res, 409, e.getMessage()); // out of stock / limit exceeded
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage()); // product not found
        } catch (Exception e) {
            sendError(res, 500, "Could not add item: " + e.getMessage());
        }
    }

    // ── PUT — update quantity ─────────────────────────
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Login required"); return; }
        try {
            Map<String, Object> body = readObjectBody(req);
            Long productId = Long.valueOf(body.get("productId").toString());
            int  quantity  = Integer.parseInt(body.get("quantity").toString());
            sendJson(res, 200, cartService.updateQuantity(userId, productId, quantity));
        } catch (Exception e) {
            sendError(res, 500, "Could not update item: " + e.getMessage());
        }
    }

    // ── DELETE — remove item or clear cart ────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Login required"); return; }
        try {
            String rawBody = req.getReader().lines().collect(Collectors.joining()).trim();

            if (rawBody.isEmpty() || rawBody.equals("{}")) {
                // Nobody → clear entire cart
                sendJson(res, 200, cartService.clearCart(userId));
            } else {
                Map<String, Object> body = mapper.readValue(rawBody,
                        new TypeReference<Map<String, Object>>() {});
                Long productId = Long.valueOf(body.get("productId").toString());
                sendJson(res, 200, cartService.removeItem(userId, productId));
            }
        } catch (Exception e) {
            sendError(res, 500, "Could not remove item: " + e.getMessage());
        }
    }
}