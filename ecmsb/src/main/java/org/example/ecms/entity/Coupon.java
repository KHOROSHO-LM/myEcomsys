package org.example.ecms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Coupon {
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal threshold;
    private BigDecimal discountValue;
    private Integer stock;
    private Integer usedCount;
    private Integer perUserLimit;
    private LocalDateTime expireStart;
    private LocalDateTime expireEnd;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }
    public LocalDateTime getExpireStart() { return expireStart; }
    public void setExpireStart(LocalDateTime expireStart) { this.expireStart = expireStart; }
    public LocalDateTime getExpireEnd() { return expireEnd; }
    public void setExpireEnd(LocalDateTime expireEnd) { this.expireEnd = expireEnd; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
