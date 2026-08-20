package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.Product;
import org.example.ecms.entity.ProductCategory;
import org.example.ecms.entity.ProductImage;
import org.example.ecms.entity.ProductSku;
import org.example.ecms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/categories")
    public Result<List<ProductCategory>> categories() {
        return Result.success(productService.listCategories());
    }

    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.listProducts());
    }

    @GetMapping("/{id}/skus")
    public Result<List<ProductSku>> skus(@PathVariable("id") Long id) {
        return Result.success(productService.listSkus(id));
    }

    @GetMapping("/{id}/images")
    public Result<List<ProductImage>> images(@PathVariable("id") Long id) {
        return Result.success(productService.listImages(id));
    }
}
