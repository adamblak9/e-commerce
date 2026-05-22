package com.ecommerce.servlet;

import com.ecommerce.filter.RoleGuard;
import com.ecommerce.service.AdminService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * GET    /api/admin/stats                       → dashboard statistics
 * GET    /api/admin/users                       → list all users
 * GET    /api/admin/users/{id}                  → get one user
 * PUT    /api/admin/users/{id}/role             → change role
 * PUT    /api/admin/users/{id}/status           → change status
 * PUT    /api/admin/users/{id}/reset-password   → reset password
 * DELETE /api/admin/users/{id}                  → soft-delete user
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends BaseServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        String pathInfo = req.getPathInfo(); // e.g. /stats  or /users  or /users/5

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(res, 400, "Endpoint required: /stats or /users");
                return;
            }

            if (pathInfo.equals("/stats")) {
                sendJson(res, 200, adminService.getDashboardStats());
                return;
            }

            if (pathInfo.equals("/users") || pathInfo.equals("/users/")) {
                sendJson(res, 200, adminService.getAllUsers());
                return;
            }

            if (pathInfo.startsWith("/users/")) {
                String[] parts = pathInfo.split("/"); // ["", "users", "5"]
                if (parts.length >= 3) {
                    Long userId = Long.parseLong(parts[2]);
                    sendJson(res, 200, adminService.getUserById(userId));
                    return;
                }
            }

            sendError(res, 404, "Unknown admin endpoint");
        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid user ID");
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        Long adminId = RoleGuard.getUserId(req);
        String pathInfo = req.getPathInfo();

        // Expected patterns: /users/{id}/role | /users/{id}/status | /users/{id}/reset-password
        if (pathInfo == null || !pathInfo.startsWith("/users/")) {
            sendError(res, 400, "Invalid endpoint");
            return;
        }

        try {
            String[] parts = pathInfo.split("/"); // ["","users","5","role"]
            if (parts.length < 4) {
                sendError(res, 400, "Missing action: /users/{id}/role|status|reset-password");
                return;
            }

            Long targetId = Long.parseLong(parts[2]);
            String action = parts[3];
            Map<String, String> body = readBody(req);

            switch (action) {
                case "role" -> {
                    String role = body.get("role");
                    if (role == null) { sendError(res, 400, "role is required"); return; }
                    sendJson(res, 200, adminService.updateUserRole(adminId, targetId, role));
                }
                case "status" -> {
                    String status = body.get("status");
                    if (status == null) { sendError(res, 400, "status is required"); return; }
                    sendJson(res, 200, adminService.updateUserStatus(adminId, targetId, status));
                }
                case "reset-password" -> {
                    String password = body.get("password");
                    if (password == null) { sendError(res, 400, "password is required"); return; }
                    sendJson(res, 200, adminService.resetUserPassword(adminId, targetId, password));
                }
                default -> sendError(res, 404, "Unknown action: " + action);
            }
        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid user ID");
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        Long adminId  = RoleGuard.getUserId(req);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || !pathInfo.startsWith("/users/")) {
            sendError(res, 400, "Invalid endpoint");
            return;
        }

        try {
            String[] parts = pathInfo.split("/");
            Long targetId = Long.parseLong(parts[2]);
            adminService.deleteUser(adminId, targetId);
            sendJson(res, 200, Map.of("message", "User deactivated successfully"));
        } catch (NumberFormatException e) {
            sendError(res, 400, "Invalid user ID");
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Internal server error");
        }
    }
}
