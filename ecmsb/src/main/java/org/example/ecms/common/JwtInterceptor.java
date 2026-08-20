package org.example.ecms.common;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * JWT 鉴权拦截器。
 * 从 Authorization: Bearer xxx 取 token，解析成功则把 uid/uname 塞进 request 供 Controller 用，
 * 解析失败或缺失返回 401 + Result JSON。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USERNAME = "currentUsername";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            return writeUnauthorized(response, "未登录或缺少 token");
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = jwtUtil.parse(token);
            request.setAttribute(ATTR_USER_ID, claims.get("uid", Long.class));
            request.setAttribute(ATTR_USERNAME, claims.get("uname", String.class));
            return true;
        } catch (Exception e) {
            return writeUnauthorized(response, "token 已失效，请重新登录");
        }
    }

    private boolean writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter w = response.getWriter()) {
            w.write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
            w.flush();
        }
        return false;
    }

    /** 从请求头获取客户端 IP，处理反向代理 */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
