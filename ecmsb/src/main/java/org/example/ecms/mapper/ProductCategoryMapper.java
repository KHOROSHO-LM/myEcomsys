package org.example.ecms.mapper;

import org.example.ecms.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryMapper {
    List<ProductCategory> selectAll();
}
