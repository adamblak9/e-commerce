package com.ecommerce.servlet;

import com.ecommerce.filter.RoleGuard;
import com.ecommerce.service.ProfileService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * /api/profile          GET  — fetch own profile
 * /api/profile          PUT  — update personal info
 * /api/profile/password PUT  — change password
 * /api/profile          DELETE — deactivate account
 */
@WebServlet("/api/profile/*")
public class ProfileServlet extends BaseServlet {

    private final ProfileService profileService = new ProfileService();

    // ── GET /api/profile ─────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Unauthorized"); return; }

        try {
            Map<String, Object> profile = profileService.getProfile(userId);
            sendJson(res, 200, profile);
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    // ── PUT /api/profile  or  PUT /api/profile/password ──────────────────────
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Unauthorized"); return; }

        String pathInfo = req.getPathInfo(); // null or "/password"

        if ("/password".equals(pathInfo)) {
            handleChangePassword(req, res, userId);
        } else {
            handleUpdateInfo(req, res, userId);
        }
    }

    // ── DELETE /api/profile ───────────────────────────────────────────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Long userId = RoleGuard.getUserId(req);
        if (userId == null) { sendError(res, 401, "Unauthorized"); return; }

        try {
            Map<String, String> body = readBody(req);
            String password = body.get("password");

            if (isBlank(password)) {
                sendError(res, 400, "password is required to delete your account");
                return;
            }

            profileService.deleteAccount(userId, password);
            sendJson(res, 200, Map.of("message", "Account deactivated successfully"));

        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void handleUpdateInfo(HttpServletRequest req, HttpServletResponse res,
                                  Long userId) throws IOException {
        try {
            Map<String, String> body = readBody(req);

            Map<String, Object> updated = profileService.updateInfo(
                    userId,
                    body.get("firstName"),
                    body.get("lastName"),
                    body.get("username"),
                    body.get("email"),
                    body.get("phoneNumber")
            );
            sendJson(res, 200, updated);

        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse res,
                                      Long userId) throws IOException {
        try {
            Map<String, String> body = readBody(req);

            String currentPassword  = body.get("currentPassword");
            String newPassword      = body.get("newPassword");
            String confirmPassword  = body.get("confirmPassword");

            if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
                sendError(res, 400, "currentPassword, newPassword and confirmPassword are required");
                return;
            }

            profileService.changePassword(userId, currentPassword, newPassword, confirmPassword);
            sendJson(res, 200, Map.of("message", "Password changed successfully"));

        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
