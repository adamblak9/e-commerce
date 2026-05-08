package com.ecommerce.servlet;

import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/api/orders")
public class OrderServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    /**
     * POST /api/orders
     * Receives JSON from cart.html: { "items": [...], "total": 99.99, "address": "..." }
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        // 1. Get User ID from Request Attributes
        // (This was set previously by your JwtFilter/Authentication logic)
        Object userIdAttr = req.getAttribute("userId");

        if (userIdAttr == null) {
            sendError(res, 401, "Authentication required. Please log in.");
            return;
        }

        Long userId = Long.valueOf(userIdAttr.toString());

        try {
            // 2. Read complex JSON body (since it contains a List of items)
            // We use Map<String, Object> because "items" is a List, not a String
            Map<String, Object> body = mapper.readValue(req.getReader(), new TypeReference<Map<String, Object>>() {});

            if (body == null || !body.containsKey("items") || !body.containsKey("total")) {
                sendError(res, 400, "Invalid order data: items and total are required.");
                return;
            }

            // 3. Extract Data
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) body.get("items");
            String address = (String) body.get("address");

            // Handle total amount safely
            BigDecimal totalAmount = new BigDecimal(body.get("total").toString());

            if (cartItems.isEmpty()) {
                sendError(res, 400, "Cannot create an empty order.");
                return;
            }

            // 4. Create the Order in the Database via Service
            // This creates the 'orders' entry AND the 'order_items' entries
            Order order = orderService.createOrder(userId, cartItems, totalAmount, address);

            // 5. Send Success Response
            // Return the orderId so the frontend can redirect to: payment.html?orderId=XX
            sendJson(res, 201, Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "message", "Order created successfully"
            ));

        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid price format in order.");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Server error while processing order: " + e.getMessage());
        }
    }
}