package org.example.ecms.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.ecms.entity.SysUser;

import java.time.LocalDateTime;

/**
 * 管理员账号 Mapper。
 */
public interface SysUserMapper {

    /** 按用户名查询账号（登录校验用） */
    SysUser selectByUsername(@Param("username") String username);

    /** 更新最后登录时间与 IP */
    int updateLastLogin(@Param("id") Long id,
                        @Param("lastLoginTime") LocalDateTime lastLoginTime,
                        @Param("lastLoginIp") String lastLoginIp);
}
