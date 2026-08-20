package org.example.ecms.mapper;

import org.example.ecms.entity.Customer;

import java.util.List;

public interface CustomerMapper {
    List<Customer> selectAll();
}
