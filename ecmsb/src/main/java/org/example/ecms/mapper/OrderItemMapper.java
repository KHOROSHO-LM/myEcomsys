package org.example.ecms.mapper;

import org.example.ecms.entity.OrderItem;

import java.util.List;

public interface OrderItemMapper {
    List<OrderItem> selectByOrderId(Long orderId);
}
