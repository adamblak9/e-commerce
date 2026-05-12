package com.ecommerce.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class JwtFilter implements Filter {

    private static final String[] PUBLIC_ROUTES = {
            "/api/auth/login",
            "/api/auth/signup"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // CORS
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = req.getServletPath();
        String method = req.getMethod();

        // Routes totalement publiques (login, signup)
        for (String pub : PUBLIC_ROUTES) {
            if (path.equals(pub)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Rendre public le GET /api/products
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/products")) {
            // On laisse passer, mais on peut quand même extraire le token s'il est présent
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    DecodedJWT jwt = JwtUtil.verify(token);
                    req.setAttribute("userId", jwt.getClaim("userId").asLong());
                    req.setAttribute("email", jwt.getSubject());
                    req.setAttribute("role", jwt.getClaim("role").asString());
                } catch (JWTVerificationException e) {
                    // Token invalide : on ignore, on laisse passer sans attributs
                }
            }
            chain.doFilter(request, response);
            return;
        }

        // Toutes les autres routes nécessitent un token valide
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(res, 401, "Missing or invalid Authorization header.");
            return;
        }

        String token = authHeader.substring(7);
        try {
            DecodedJWT jwt = JwtUtil.verify(token);
            req.setAttribute("userId", jwt.getClaim("userId").asLong());
            req.setAttribute("email", jwt.getSubject());
            req.setAttribute("role", jwt.getClaim("role").asString());
            chain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            sendError(res, 401, "Invalid or expired token.");
        }
    }

    private void sendError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}