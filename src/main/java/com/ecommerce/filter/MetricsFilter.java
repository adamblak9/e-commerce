package com.ecommerce.filter;

import com.ecommerce.servlet.MetricsServlet;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Automatically increments shopease_http_requests_total
 * for every request that passes through the app.
 *
 * Register this BEFORE JwtFilter in web.xml, or use @WebFilter("/*").
 */
@WebFilter("/*")
public class MetricsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Count every incoming HTTP request
        MetricsServlet.httpRequestsTotal.incrementAndGet();

        chain.doFilter(request, response);
    }
}
