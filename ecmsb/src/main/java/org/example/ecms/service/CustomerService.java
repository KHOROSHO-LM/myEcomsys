package org.example.ecms.service;

import org.example.ecms.entity.Customer;
import org.example.ecms.entity.CustomerAddress;
import org.example.ecms.entity.CustomerLevel;
import org.example.ecms.mapper.CustomerAddressMapper;
import org.example.ecms.mapper.CustomerLevelMapper;
import org.example.ecms.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private CustomerAddressMapper customerAddressMapper;
    @Autowired
    private CustomerLevelMapper customerLevelMapper;

    public List<Customer> listCustomers() {
        return customerMapper.selectAll();
    }

    public List<CustomerAddress> listAddresses(Long customerId) {
        return customerAddressMapper.selectByCustomerId(customerId);
    }

    public List<CustomerLevel> listLevels() {
        return customerLevelMapper.selectAll();
    }
}
