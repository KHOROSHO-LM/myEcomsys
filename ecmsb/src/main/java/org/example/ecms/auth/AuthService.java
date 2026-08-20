package org.example.ecms.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.ecms.common.BizException;
import org.example.ecms.common.JwtUtil;
import org.example.ecms.entity.LoginLog;
import org.example.ecms.entity.SysUser;
import org.example.ecms.mapper.LoginLogMapper;
import org.example.ecms.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 鉴权业务：登录校验 / 登录日志 / JWT 签发。
 * 密码用 BCrypt 校验（数据库存的是 bcrypt 密文）。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    /** login_type：1=后台管理员登录 */
    private static final int LOGIN_TYPE_ADMIN = 1;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private JwtUtil jwtUtil;

    /** 登录请求体 */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** 登录返回体 */
    public static class LoginResult {
        private final String token;
        private final String username;
        private final String nickname;
        private final long expireMinutes;

        public LoginResult(String token, String username, String nickname, long expireMinutes) {
            this.token = token;
            this.username = username;
            this.nickname = nickname;
            this.expireMinutes = expireMinutes;
        }

        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getNickname() { return nickname; }
        public long getExpireMinutes() { return expireMinutes; }
    }

    /**
     * 登录：账号查 DB → BCrypt 校验 → 签 JWT → 记登录日志 → 更新最后登录信息。
     * 任何失败都抛 BizException(401)，并落一条 status=0 的登录日志。
     */
    public LoginResult login(LoginRequest body, String ip) {
        String username = body.getUsername() == null ? "" : body.getUsername().trim();
        String password = body.getPassword() == null ? "" : body.getPassword();

        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            recordLoginLog(null, username, ip, 0, "账号不存在");
            throw new BizException(401, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            recordLoginLog(user.getId(), username, ip, 0, "账号已禁用");
            throw new BizException(401, "账号已被禁用");
        }
        if (!verifyPassword(password, user.getPassword())) {
            recordLoginLog(user.getId(), username, ip, 0, "密码错误");
            throw new BizException(401, "账号或密码错误");
        }

        String token = jwtUtil.generate(user.getId(), user.getUsername());
        recordLoginLog(user.getId(), username, ip, 1, "登录成功");

        // 更新最后登录信息（失败不阻断登录流程）
        try {
            sysUserMapper.updateLastLogin(user.getId(), LocalDateTime.now(), ip);
        } catch (Exception e) {
            log.warn("更新最后登录信息失败 userId={}", user.getId(), e);
        }

        long expireMinutes = jwtUtil.getExpireMillis() / 60_000L;
        return new LoginResult(token, user.getUsername(), user.getNickname(), expireMinutes);
    }

    /** /api/auth/info：从 token 已解析出的 uid/username 重建登录信息 */
    public LoginResult buildInfoFromToken(Long userId, String username) {
        // token 已经过拦截器校验，直接返回；如需附带 nickname 可再查 DB
        return new LoginResult(null, username, null, jwtUtil.getExpireMillis() / 60_000L);
    }

    /** 登出：JWT 无状态，前端清 token 即可；后端记一条日志便于审计 */
    public void logout(String username, String ip) {
        if (username != null) {
            recordLoginLog(null, username, ip, 1, "用户主动登出");
        }
    }

    /** BCrypt 校验：明文与密文比对 */
    private boolean verifyPassword(String plaintext, String hash) {
        if (hash == null || hash.isEmpty() || plaintext.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(plaintext.toCharArray(), hash).verified;
        } catch (Exception e) {
            log.warn("BCrypt 校验异常", e);
            return false;
        }
    }

    /** 落登录日志，失败不影响主流程 */
    private void recordLoginLog(Long userId, String username, String ip, int status, String message) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginType(LOGIN_TYPE_ADMIN);
            loginLog.setIp(ip);
            loginLog.setStatus(status);
            loginLog.setMessage(message);
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.warn("落登录日志失败 user={}", username, e);
        }
    }
}
