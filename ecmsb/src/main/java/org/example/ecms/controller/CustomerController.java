package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.Customer;
import org.example.ecms.entity.CustomerAddress;
import org.example.ecms.entity.CustomerLevel;
import org.example.ecms.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public Result<List<Customer>> list() {
        return Result.success(customerService.listCustomers());
    }

    @GetMapping("/levels")
    public Result<List<CustomerLevel>> levels() {
        return Result.success(customerService.listLevels());
    }

    @GetMapping("/{id}/addresses")
    public Result<List<CustomerAddress>> addresses(@PathVariable("id") Long id) {
        return Result.success(customerService.listAddresses(id));
    }
}
