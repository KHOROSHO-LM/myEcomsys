package org.example.ecms.mapper;

import org.example.ecms.entity.ProductSku;

import java.util.List;

public interface ProductSkuMapper {
    List<ProductSku> selectByProductId(Long productId);
}
