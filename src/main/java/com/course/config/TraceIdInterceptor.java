package com.course.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 1️⃣ Read traceId from ThreadContext (set by filter)
        String traceId = ThreadContext.get(TRACE_ID);
        if (traceId == null) {
            traceId = "N/A"; // fallback, should not happen
        }

        // 2️⃣ Log interceptor preHandle
        System.out.println("TraceIdInterceptor preHandle: " + request.getRequestURI() + ", traceId=" + traceId);
        return true; // continue request
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 3️⃣ Log cleanup
        String traceId = ThreadContext.get(TRACE_ID);
        if (traceId == null) {
            traceId = "N/A";
        }
        System.out.println("TraceIdInterceptor afterCompletion cleanup: " + request.getRequestURI() + ", traceId=" + traceId);
    }
}