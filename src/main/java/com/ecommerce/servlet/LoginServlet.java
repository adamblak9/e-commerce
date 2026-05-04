package com.ecommerce.servlet;

import com.ecommerce.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/login")
public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        Map<String, String> body = readBody(req);

        String email = body.get("email");
        String password = body.get("password");

        if (isBlank(email) || isBlank(password)) {
            sendError(res, 400, "email and password are required");
            return;
        }

        try {
            // LOGIN
            Map<String, Object> response = authService.login(email, password);

            String token = (String) response.get("token");

            // 🔐 CREATE HTTP ONLY COOKIE
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60); // 1 hour
            jwtCookie.setSecure(false); // set TRUE in HTTPS production

            res.addCookie(jwtCookie);

            // ❌ Do NOT expose token to frontend anymore
            response.remove("token");

            sendJson(res, 200, response);

        } catch (SecurityException e) {
            sendError(res, 401, e.getMessage());

        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}