package org.example.ecms.mapper;

import org.example.ecms.entity.LoginLog;

import java.util.List;

/**
 * 登录日志 Mapper。
 */
public interface LoginLogMapper {

    /** 插入一条登录日志（成功/失败均记录） */
    int insert(LoginLog loginLog);

    /** 查询全部登录日志（系统日志页用） */
    List<LoginLog> selectAll();
}
