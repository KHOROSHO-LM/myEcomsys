package org.example.ecms.mapper;

import org.example.ecms.entity.ProductImage;

import java.util.List;

public interface ProductImageMapper {
    List<ProductImage> selectByProductId(Long productId);
}
