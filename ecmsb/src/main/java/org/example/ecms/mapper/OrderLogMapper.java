package org.example.ecms.mapper;

import org.example.ecms.entity.OrderLog;

import java.util.List;

public interface OrderLogMapper {
    List<OrderLog> selectByOrderId(Long orderId);
}
