package org.example.ecms.service;

import org.example.ecms.entity.CategorySalesVO;
import org.example.ecms.entity.DashboardStats;
import org.example.ecms.entity.RecentOrderVO;
import org.example.ecms.mapper.DashboardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    public DashboardStats getStats() {
        return dashboardMapper.selectStats();
    }

    public List<RecentOrderVO> getRecentOrders() {
        return dashboardMapper.selectRecentOrders();
    }

    public List<CategorySalesVO> getCategorySales() {
        return dashboardMapper.selectCategorySales();
    }
}
