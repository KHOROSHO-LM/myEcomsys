package org.example.ecms.mapper;

import org.example.ecms.entity.Order;

import java.util.List;

public interface OrderMapper {
    List<Order> selectAll();

    List<Order> selectByStatus(Integer status);
}
