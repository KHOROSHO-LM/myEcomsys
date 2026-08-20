package org.example.ecms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Seckill {
    private Long id;
    private Long productId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer seckillLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getSeckillPrice() { return seckillPrice; }
    public void setSeckillPrice(BigDecimal seckillPrice) { this.seckillPrice = seckillPrice; }
    public Integer getSeckillStock() { return seckillStock; }
    public void setSeckillStock(Integer seckillStock) { this.seckillStock = seckillStock; }
    public Integer getSeckillLimit() { return seckillLimit; }
    public void setSeckillLimit(Integer seckillLimit) { this.seckillLimit = seckillLimit; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
