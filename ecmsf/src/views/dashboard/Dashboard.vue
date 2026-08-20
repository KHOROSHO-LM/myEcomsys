<script setup>
import { ref, onMounted } from 'vue'
import { dashboardApi } from '../../api'

// 数据来自后端 /api/dashboard/* 接口（对应 sql.txt 各表统计）
const stats = ref([])
const recentOrders = ref([])
const hotCategories = ref([])
const loading = ref(false)
const errorMsg = ref('')

const statusMap = {
  1: { text: '待付款', class: 'warn' },
  2: { text: '待发货', class: 'primary' },
  3: { text: '待收货', class: 'info' },
  4: { text: '已完成', class: 'success' },
  5: { text: '已取消', class: 'muted' },
  6: { text: '售后中', class: 'danger' },
}

function buildStats(s) {
  if (!s) return []
  return [
    { label: '商品总数', table: 'product', value: s.productTotal ?? 0, trend: '上架 ' + (s.productOnline ?? 0) + ' 件', type: 'up' },
    { label: '订单总数', table: 'order', value: s.orderTotal ?? 0, trend: '待发货 ' + (s.orderPendingShip ?? 0) + ' 单', type: 'up' },
    { label: '注册客户', table: 'customer', value: s.customerTotal ?? 0, trend: '禁用 ' + (s.customerDisabled ?? 0) + ' 人', type: 'up' },
    { label: '待发货订单', table: 'order(status=2)', value: s.orderPendingShip ?? 0, trend: '需及时处理', type: 'warn' },
    { label: '上架商品', table: 'product(status=1)', value: s.productOnline ?? 0, trend: '在线商品', type: 'up' },
    { label: '优惠券活动', table: 'coupon(status=1)', value: s.couponActive ?? 0, trend: '启用中', type: 'up' },
    { label: '秒杀活动', table: 'seckill(status=1)', value: s.seckillActive ?? 0, trend: '进行中', type: 'up' },
    { label: '禁用客户', table: 'customer(status=0)', value: s.customerDisabled ?? 0, trend: '已禁用', type: 'warn' },
  ]
}

async function loadData() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [statsData, ordersData, categoriesData] = await Promise.all([
      dashboardApi.getStats(),
      dashboardApi.getRecentOrders(),
      dashboardApi.getCategorySales(),
    ])
    stats.value = buildStats(statsData)
    recentOrders.value = (ordersData || []).map((o) => ({
      order_no: o.orderNo,
      customer: o.customerName || ('客户#' + o.customerId),
      amount: '¥' + Number(o.payAmount || 0).toFixed(2),
      status: o.status,
      time: (o.createdAt || '').replace('T', ' ').slice(0, 16),
    }))
    // 按销量最大值归一化为百分比
    const max = Math.max(1, ...(categoriesData || []).map((c) => c.sales || 0))
    hotCategories.value = (categoriesData || []).map((c) => ({
      name: c.name,
      sales: c.sales || 0,
      percent: Math.round(((c.sales || 0) / max) * 100),
    }))
  } catch (e) {
    errorMsg.value = e.message || '数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard">
    <div v-if="errorMsg" class="error-banner">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
      </svg>
      {{ errorMsg }}
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-label">{{ s.label }}</div>
        <div class="stat-value">{{ s.value.toLocaleString() }}</div>
        <div class="stat-table">表：{{ s.table }}</div>
        <div class="stat-trend" :class="s.type">{{ s.trend }}</div>
      </div>
    </div>

    <div class="row">
      <div class="card col-2">
        <div class="card-header">
          <h3>最近订单</h3>
          <span class="card-sub">来源：order 表</span>
        </div>
        <table class="table">
          <thead>
            <tr>
              <th>订单号 order_no</th>
              <th>买家 customer_id</th>
              <th>实付 pay_amount</th>
              <th>状态 status</th>
              <th>下单时间 created_at</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in recentOrders" :key="o.order_no">
              <td>{{ o.order_no }}</td>
              <td>{{ o.customer }}</td>
              <td>{{ o.amount }}</td>
              <td>
                <span class="tag" :class="(statusMap[o.status] || {}).class || 'muted'">
                  {{ (statusMap[o.status] || {}).text || '-' }}
                </span>
              </td>
              <td>{{ o.time }}</td>
            </tr>
            <tr v-if="!recentOrders.length && !loading">
              <td colspan="5" class="empty">暂无订单数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card col-1">
        <div class="card-header">
          <h3>分类销量 TOP</h3>
          <span class="card-sub">来源：product_category</span>
        </div>
        <div class="bar-list">
          <div v-for="c in hotCategories" :key="c.name" class="bar-item">
            <div class="bar-label">
              <span>{{ c.name }}</span>
              <span class="bar-value">{{ c.sales }}</span>
            </div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: c.percent + '%' }"></div>
            </div>
          </div>
          <div v-if="!hotCategories.length && !loading" class="empty">暂无分类销量</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.error-banner {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ef4444;
  font-size: 13px;
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #fee2e2;
}

.loading {
  color: #6b7280;
  font-size: 13px;
  padding: 8px 0;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #1f2937;
  margin: 6px 0 4px;
  line-height: 1;
}

.stat-table {
  font-size: 11px;
  color: #9ca3af;
  font-family: ui-monospace, Consolas, monospace;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
}

.stat-trend.up {
  color: #10b981;
}

.stat-trend.warn {
  color: #f59e0b;
}

.row {
  display: flex;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.col-2 {
  flex: 2;
}

.col-1 {
  flex: 1;
}

.card-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.card-sub {
  font-size: 12px;
  color: #9ca3af;
  font-family: ui-monospace, Consolas, monospace;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.table th {
  text-align: left;
  padding: 10px 8px;
  font-weight: 500;
  color: #6b7280;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.table td {
  padding: 11px 8px;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tag.primary {
  background: #dbeafe;
  color: #1d4ed8;
}

.tag.warn {
  background: #fef3c7;
  color: #b45309;
}

.tag.info {
  background: #e0e7ff;
  color: #4338ca;
}

.tag.success {
  background: #d1fae5;
  color: #047857;
}

.tag.muted {
  background: #f3f4f6;
  color: #6b7280;
}

.tag.danger {
  background: #fee2e2;
  color: #b91c1c;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #374151;
  margin-bottom: 5px;
}

.bar-value {
  color: #6b7280;
  font-size: 12px;
}

.bar-track {
  height: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
}

.empty {
  text-align: center;
  color: #9ca3af;
  padding: 24px;
}

@media (max-width: 1024px) {
  .row {
    flex-direction: column;
  }
}
</style>
