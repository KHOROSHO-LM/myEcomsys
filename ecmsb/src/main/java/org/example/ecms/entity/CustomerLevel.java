package org.example.ecms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerLevel {
    private Long id;
    private String levelName;
    private String levelCode;
    private Integer minPoints;
    private BigDecimal discountRate;
    private String benefits;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public Integer getMinPoints() { return minPoints; }
    public void setMinPoints(Integer minPoints) { this.minPoints = minPoints; }
    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
