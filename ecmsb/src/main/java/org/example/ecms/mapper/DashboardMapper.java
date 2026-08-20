package org.example.ecms.mapper;

import org.example.ecms.entity.CategorySalesVO;
import org.example.ecms.entity.DashboardStats;
import org.example.ecms.entity.RecentOrderVO;

import java.util.List;

public interface DashboardMapper {
    DashboardStats selectStats();

    List<RecentOrderVO> selectRecentOrders();

    List<CategorySalesVO> selectCategorySales();
}
