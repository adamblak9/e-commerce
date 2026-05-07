package com.ecommerce.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")  // intercepts every request to /api/...
public class JwtFilter implements Filter {

    private static final String[] PUBLIC_ROUTES = {
            "/api/auth/login",
            "/api/auth/signup"
    };

    private static final String[] PUBLIC_PREFIXES = {
            "/api/products"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        // ── Allow CORS (needed if your HTML pages are on a different port) ──
        res.setHeader("Access-Control-Allow-Origin",  "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // ── Let preflight OPTIONS requests through immediately ──
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // ── Check if route is public ──
        String path = req.getServletPath();
        for (String pub : PUBLIC_ROUTES) {
            if (path.equals(pub)) {
                chain.doFilter(request, response);
                return;
            }
        }
// Allow GET requests to /api/products publicly
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            for (String prefix : PUBLIC_PREFIXES) {
                if (path.startsWith(prefix)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // ── Extract the Authorization header ──
        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(res, 401, "Missing or invalid Authorization header. Expected: Bearer <token>");
            return;
        }

        String token = authHeader.substring(7); // remove "Bearer " prefix

        // ── Verify the token ──
        try {
            DecodedJWT jwt = JwtUtil.verify(token);

            // Attach user info to the request so servlets can read it
            req.setAttribute("userId", jwt.getClaim("userId").asLong());
            req.setAttribute("email",  jwt.getSubject());
            req.setAttribute("role",   jwt.getClaim("role").asString());

            chain.doFilter(request, response); // token is valid, continue

        } catch (JWTVerificationException e) {
            sendError(res, 401, "Token is invalid or expired. Please log in again.");
        }
    }

    private void sendError(HttpServletResponse res, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(
                "{\"error\": \"" + message + "\", \"status\": " + status + "}"
        );
    }

    @Override public void init(FilterConfig config) {}
    @Override public void destroy() {}
}