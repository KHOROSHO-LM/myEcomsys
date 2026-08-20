package org.example.ecms.mapper;

import org.example.ecms.entity.CouponIssue;

import java.util.List;

public interface CouponIssueMapper {
    List<CouponIssue> selectByCouponId(Long couponId);
}
