package com.ecommerce.servlet;

import com.ecommerce.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/signup")
public class SignupServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        Map<String, String> body = readBody(req);

        // ── Basic presence validation ────────────────────────────────────────
        String username  = body.get("username");
        String email     = body.get("email");
        String password  = body.get("password");
        String firstName = body.get("firstName");
        String lastName  = body.get("lastName");
        String phone     = body.get("phoneNumber"); // optional

        if (isBlank(username) || isBlank(email) || isBlank(password)
                || isBlank(firstName) || isBlank(lastName)) {
            sendError(res, 400, "username, email, password, firstName and lastName are required");
            return;
        }

        if (password.length() < 8) {
            sendError(res, 400, "Password must be at least 8 characters");
            return;
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            sendError(res, 400, "Email is not valid");
            return;
        }

        // ── Delegate to service ──────────────────────────────────────────────
        try {
            Map<String, Object> response = authService.signup(
                    username, email, password, firstName, lastName, phone);
            sendJson(res, 201, response);

        } catch (IllegalArgumentException e) {
            // duplicate email or username
            sendError(res, 409, e.getMessage());

        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}