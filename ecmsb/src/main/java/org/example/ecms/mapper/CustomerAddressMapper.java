package org.example.ecms.mapper;

import org.example.ecms.entity.CustomerAddress;

import java.util.List;

public interface CustomerAddressMapper {
    List<CustomerAddress> selectByCustomerId(Long customerId);
}
