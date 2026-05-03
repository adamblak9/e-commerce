package com.ecommerce.servlet;

import com.ecommerce.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
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

        String email    = body.get("email");
        String password = body.get("password");

        if (isBlank(email) || isBlank(password)) {
            sendError(res, 400, "email and password are required");
            return;
        }

        try {
            Map<String, Object> response = authService.login(email, password);
            sendJson(res, 200, response);

        } catch (SecurityException e) {
            // wrong credentials or inactive account
            sendError(res, 401, e.getMessage());

        } catch (Exception e) {
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}