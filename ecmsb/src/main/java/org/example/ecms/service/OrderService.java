package org.example.ecms.service;

import org.example.ecms.entity.Order;
import org.example.ecms.entity.OrderItem;
import org.example.ecms.entity.OrderLog;
import org.example.ecms.mapper.OrderItemMapper;
import org.example.ecms.mapper.OrderLogMapper;
import org.example.ecms.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderLogMapper orderLogMapper;

    public List<Order> listOrders() {
        return orderMapper.selectAll();
    }

    public List<Order> listOrdersByStatus(Integer status) {
        return orderMapper.selectByStatus(status);
    }

    public List<OrderItem> listItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    public List<OrderLog> listLogs(Long orderId) {
        return orderLogMapper.selectByOrderId(orderId);
    }
}
