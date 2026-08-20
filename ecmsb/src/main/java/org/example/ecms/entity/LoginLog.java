package org.example.ecms.entity;

import java.time.LocalDateTime;

/**
 * 登录日志实体，对应 login_log 表。
 * 登录成功/失败都会落一条记录，便于审计。
 */
public class LoginLog {
    private Long id;
    private Long userId;
    private String username;
    private Integer loginType;
    private String ip;
    private Integer status;
    private String message;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getLoginType() { return loginType; }
    public void setLoginType(Integer loginType) { this.loginType = loginType; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
