package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.CategorySalesVO;
import org.example.ecms.entity.DashboardStats;
import org.example.ecms.entity.RecentOrderVO;
import org.example.ecms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStats> stats() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/recent-orders")
    public Result<List<RecentOrderVO>> recentOrders() {
        return Result.success(dashboardService.getRecentOrders());
    }

    @GetMapping("/category-sales")
    public Result<List<CategorySalesVO>> categorySales() {
        return Result.success(dashboardService.getCategorySales());
    }
}
