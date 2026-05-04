package com.ecommerce.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Helper your friend (or anyone) can call at the top of any servlet
 * to restrict access by role.
 *
 * Usage:
 *   if (!RoleGuard.require(req, res, "ADMIN")) return;
 *   if (!RoleGuard.require(req, res, "CUSTOMER", "ADMIN")) return;
 */
public class RoleGuard {

    /**
     * Returns true if the current user has one of the allowed roles.
     * Returns false and writes a 403 response if they don't.
     */
    public static boolean require(HttpServletRequest req,
                                  HttpServletResponse res,
                                  String... allowedRoles) throws IOException {

        String role = (String) req.getAttribute("role");

        for (String allowed : allowedRoles) {
            if (allowed.equalsIgnoreCase(role)) {
                return true;
            }
        }

        //  access denied
        res.setStatus(403);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(
                "{\"error\": \"Forbidden. Required role: "
                        + String.join(" or ", allowedRoles)
                        + "\", \"status\": 403}"
        );
        return false;
    }

    /**
     * Convenience — get the current user's ID from the request.
     */
    public static Long getUserId(HttpServletRequest req) {
        return (Long) req.getAttribute("userId");
    }

    /**
     * Convenience — get the current user's role from the request.
     */
    public static String getRole(HttpServletRequest req) {
        return (String) req.getAttribute("role");
    }

    /**
     * Convenience — get the current user's email from the request.
     */
    public static String getEmail(HttpServletRequest req) {
        return (String) req.getAttribute("email");
    }
}