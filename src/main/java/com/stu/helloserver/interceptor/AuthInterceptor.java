package com.stu.helloserver.interceptor;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求方法和路径
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 2. 细粒度放行规则
        // 规则A：POST /api/users → 允许注册（无需Token）
        boolean isCreateUser = "POST".equalsIgnoreCase(method)
                && "/api/users".equals(uri);

        // 规则B：GET /api/users/* → 允许查看用户信息（无需Token）
        boolean isGetUser = "GET".equalsIgnoreCase(method)
                && uri.startsWith("/api/users/");

        // 满足公开规则 → 直接放行
        if (isCreateUser || isGetUser) {
            return true;
        }

        // 3. 敏感操作（DELETE/PUT等）必须校验Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            // 返回统一401错误
            Result<?> errorResult = Result.error(ResultCode.TOKEN_INVALID);
            ObjectMapper objectMapper = new ObjectMapper();
            String errorJson = objectMapper.writeValueAsString(errorResult);
            response.getWriter().write(errorJson);
            return false; // 拦截
        }

        // Token存在 → 放行
        return true;
    }
}