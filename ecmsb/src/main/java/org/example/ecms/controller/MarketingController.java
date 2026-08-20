package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.Coupon;
import org.example.ecms.entity.CouponIssue;
import org.example.ecms.entity.Seckill;
import org.example.ecms.service.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/marketing")
public class MarketingController {

    @Autowired
    private MarketingService marketingService;

    @GetMapping("/coupons")
    public Result<List<Coupon>> coupons() {
        return Result.success(marketingService.listCoupons());
    }

    @GetMapping("/coupons/{id}/issues")
    public Result<List<CouponIssue>> couponIssues(@PathVariable("id") Long id) {
        return Result.success(marketingService.listCouponIssues(id));
    }

    @GetMapping("/seckills")
    public Result<List<Seckill>> seckills() {
        return Result.success(marketingService.listSeckills());
    }
}
