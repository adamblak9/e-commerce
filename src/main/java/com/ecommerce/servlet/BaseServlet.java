package com.ecommerce.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseServlet extends HttpServlet {

    protected static final ObjectMapper mapper = new ObjectMapper();


    @SuppressWarnings("unchecked")
    protected Map<String, Object> readBodyRaw(HttpServletRequest req) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        return mapper.readValue(body, Map.class);
    }

    protected Map<String, String> readBody(HttpServletRequest req) throws IOException {
        Map<String, Object> raw = readBodyRaw(req);
        Map<String, String> result = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            result.put(entry.getKey(), entry.getValue() != null ? String.valueOf(entry.getValue()) : null);
        }
        return result;
    }

    protected void sendJson(HttpServletResponse res, int status, Object data) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), data);
    }

    protected void sendError(HttpServletResponse res, int status, String message) throws IOException {
        sendJson(res, status, Map.of("error", message, "status", status));
    }
}