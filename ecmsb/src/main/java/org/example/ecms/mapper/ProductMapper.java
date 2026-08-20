package org.example.ecms.mapper;

import org.example.ecms.entity.Product;

import java.util.List;

public interface ProductMapper {
    List<Product> selectAll();
}
