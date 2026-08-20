package org.example.ecms.service;

import org.example.ecms.entity.Product;
import org.example.ecms.entity.ProductCategory;
import org.example.ecms.entity.ProductImage;
import org.example.ecms.entity.ProductSku;
import org.example.ecms.mapper.ProductCategoryMapper;
import org.example.ecms.mapper.ProductImageMapper;
import org.example.ecms.mapper.ProductMapper;
import org.example.ecms.mapper.ProductSkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    @Autowired
    private ProductSkuMapper productSkuMapper;
    @Autowired
    private ProductImageMapper productImageMapper;

    public List<ProductCategory> listCategories() {
        return productCategoryMapper.selectAll();
    }

    public List<Product> listProducts() {
        return productMapper.selectAll();
    }

    public List<ProductSku> listSkus(Long productId) {
        return productSkuMapper.selectByProductId(productId);
    }

    public List<ProductImage> listImages(Long productId) {
        return productImageMapper.selectByProductId(productId);
    }
}
