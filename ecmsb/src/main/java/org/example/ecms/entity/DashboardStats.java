package org.example.ecms.entity;

public class DashboardStats {
    private long productTotal;
    private long orderTotal;
    private long customerTotal;
    private long orderPendingShip;
    private long productOnline;
    private long couponActive;
    private long seckillActive;
    private long customerDisabled;

    public long getProductTotal() { return productTotal; }
    public void setProductTotal(long productTotal) { this.productTotal = productTotal; }
    public long getOrderTotal() { return orderTotal; }
    public void setOrderTotal(long orderTotal) { this.orderTotal = orderTotal; }
    public long getCustomerTotal() { return customerTotal; }
    public void setCustomerTotal(long customerTotal) { this.customerTotal = customerTotal; }
    public long getOrderPendingShip() { return orderPendingShip; }
    public void setOrderPendingShip(long orderPendingShip) { this.orderPendingShip = orderPendingShip; }
    public long getProductOnline() { return productOnline; }
    public void setProductOnline(long productOnline) { this.productOnline = productOnline; }
    public long getCouponActive() { return couponActive; }
    public void setCouponActive(long couponActive) { this.couponActive = couponActive; }
    public long getSeckillActive() { return seckillActive; }
    public void setSeckillActive(long seckillActive) { this.seckillActive = seckillActive; }
    public long getCustomerDisabled() { return customerDisabled; }
    public void setCustomerDisabled(long customerDisabled) { this.customerDisabled = customerDisabled; }
}
