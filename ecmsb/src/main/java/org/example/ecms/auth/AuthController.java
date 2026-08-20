package org.example.ecms.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.example.ecms.common.JwtInterceptor;
import org.example.ecms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口：登录 / 当前用户信息 / 登出。
 * /api/auth/login 与 /api/auth/logout 不进 JwtInterceptor；
 * /api/auth/info 必须带 token 才能访问。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<AuthService.LoginResult> login(@RequestBody AuthService.LoginRequest body,
                                                HttpServletRequest request) {
        String ip = JwtInterceptor.getClientIp(request);
        return Result.success(authService.login(body, ip));
    }

    @GetMapping("/info")
    public Result<AuthService.LoginResult> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        String username = (String) request.getAttribute(JwtInterceptor.ATTR_USERNAME);
        return Result.success(authService.buildInfoFromToken(userId, username));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String username = (String) request.getAttribute(JwtInterceptor.ATTR_USERNAME);
        authService.logout(username, JwtInterceptor.getClientIp(request));
        return Result.success();
    }
}
