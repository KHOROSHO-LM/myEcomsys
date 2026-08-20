package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.Order;
import org.example.ecms.entity.OrderItem;
import org.example.ecms.entity.OrderLog;
import org.example.ecms.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public Result<List<Order>> list(@RequestParam(value = "status", required = false) Integer status) {
        if (status == null || status == 0) {
            return Result.success(orderService.listOrders());
        }
        return Result.success(orderService.listOrdersByStatus(status));
    }

    @GetMapping("/{id}/items")
    public Result<List<OrderItem>> items(@PathVariable("id") Long id) {
        return Result.success(orderService.listItems(id));
    }

    @GetMapping("/{id}/logs")
    public Result<List<OrderLog>> logs(@PathVariable("id") Long id) {
        return Result.success(orderService.listLogs(id));
    }
}
