package com.ecommerce.servlet;

import com.ecommerce.filter.RoleGuard;
import com.ecommerce.service.CategoryService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GET    /api/categories          → list all (public)
 * GET    /api/categories/active   → list active only (public)
 * GET    /api/categories/{id}     → get one (public)
 * POST   /api/categories          → create (ADMIN only)
 * PUT    /api/categories/{id}     → update (ADMIN only)
 * DELETE /api/categories/{id}     → delete (ADMIN only)
 */
@WebServlet("/api/categories/*")
public class CategoryServlet extends BaseServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/active")) {
                sendJson(res, 200, categoryService.getActiveCategories());
                return;
            }

            if (pathInfo != null && !pathInfo.equals("/")) {
                Long id = parseId(pathInfo);
                sendJson(res, 200, categoryService.getCategoryById(id));
                return;
            }

            sendJson(res, 200, categoryService.getAllCategories());
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        Map<String, String> body = readBody(req);
        String name        = body.get("name");
        String description = body.get("description");
        String icon        = body.get("icon");

        try {
            Map<String, Object> category = categoryService.createCategory(name, description, icon);
            sendJson(res, 201, category);
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Category ID is required");
            return;
        }

        try {
            Long id            = parseId(pathInfo);
            Map<String, String> body = readBody(req);

            String  name        = body.get("name");
            String  description = body.get("description");
            String  icon        = body.get("icon");
            Boolean active      = body.containsKey("active") ? Boolean.parseBoolean(body.get("active")) : null;

            Map<String, Object> updated = categoryService.updateCategory(id, name, description, icon, active);
            sendJson(res, 200, updated);
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!RoleGuard.require(req, res, "ADMIN")) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(res, 400, "Category ID is required");
            return;
        }

        try {
            Long id = parseId(pathInfo);
            categoryService.deleteCategory(id);
            sendJson(res, 200, Map.of("message", "Category deleted successfully"));
        } catch (IllegalArgumentException e) {
            sendError(res, 404, e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Internal server error");
        }
    }

    private Long parseId(String pathInfo) {
        try {
            return Long.parseLong(pathInfo.replace("/", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid category ID");
        }
    }
}
