package org.example.ecms.service;

import org.example.ecms.entity.Coupon;
import org.example.ecms.entity.CouponIssue;
import org.example.ecms.entity.Seckill;
import org.example.ecms.mapper.CouponIssueMapper;
import org.example.ecms.mapper.CouponMapper;
import org.example.ecms.mapper.SeckillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketingService {

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private CouponIssueMapper couponIssueMapper;
    @Autowired
    private SeckillMapper seckillMapper;

    public List<Coupon> listCoupons() {
        return couponMapper.selectAll();
    }

    public List<CouponIssue> listCouponIssues(Long couponId) {
        return couponIssueMapper.selectByCouponId(couponId);
    }

    public List<Seckill> listSeckills() {
        return seckillMapper.selectAll();
    }
}
