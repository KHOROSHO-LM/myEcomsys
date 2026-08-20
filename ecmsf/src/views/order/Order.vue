<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { orderApi } from '../../api'

// 订单状态（对应 order 表 status 注释：1待付款，2待发货，3待收货，4已完成，5已取消，6售后中）
const statusMap = {
  1: { text: '待付款', class: 'warn' },
  2: { text: '待发货', class: 'primary' },
  3: { text: '待收货', class: 'info' },
  4: { text: '已完成', class: 'success' },
  5: { text: '已取消', class: 'muted' },
  6: { text: '售后中', class: 'danger' },
}

// 支付方式：1微信，2支付宝
const payMethodMap = { 1: '微信', 2: '支付宝' }

// 订单主表（order 全字段，由后端 /api/order/list 返回并做驼峰→snake_case 映射）
const orders = ref([])
// 订单明细与操作日志（按选中订单动态加载）
const items = ref([])
const logs = ref([])

const selectedOrderId = ref(null)
const selectedOrder = computed(() => orders.value.find((o) => o.id === selectedOrderId.value))

const loading = ref(false)
const errorMsg = ref('')

// 状态筛选
const filterStatus = ref(0) // 0 全部
const filteredOrders = computed(() =>
  filterStatus.value === 0 ? orders.value : orders.value.filter((o) => o.status === filterStatus.value)
)

// 状态筛选按钮列表（避免在模板中内联构造数组）
const statusFilters = [
  { k: 0, t: '全部' },
  ...Object.entries(statusMap).map(([k, v]) => ({ k: +k, t: v.text })),
]

// 安全获取订单状态信息
function getStatus(status) {
  return statusMap[status] || { text: '-', class: '' }
}

function selectOrder(id) {
  selectedOrderId.value = id
}

// 后端驼峰 → 模板期望 snake_case；同时统一时间格式（去掉 LocalDateTime 序列化中的 'T'）
function mapOrder(o) {
  return {
    id: o.id,
    order_no: o.orderNo,
    customer_id: o.customerId,
    total_amount: o.totalAmount,
    pay_amount: o.payAmount,
    pay_method: o.payMethod,
    status: o.status,
    receiver_name: o.receiverName,
    receiver_phone: o.receiverPhone,
    receiver_address: o.receiverAddress,
    logistics_company: o.logisticsCompany,
    logistics_no: o.logisticsNo,
    pay_time: (o.payTime || '').replace('T', ' '),
    delivery_time: (o.deliveryTime || '').replace('T', ' '),
    finish_time: (o.finishTime || '').replace('T', ' '),
    cancel_time: (o.cancelTime || '').replace('T', ' '),
    created_at: (o.createdAt || '').replace('T', ' '),
  }
}

function mapItem(it) {
  return {
    id: it.id,
    order_id: it.orderId,
    product_id: it.productId,
    product_name: it.productName,
    sku_specs: it.skuSpecs,
    price: it.price,
    quantity: it.quantity,
    total_price: it.totalPrice,
  }
}

function mapLog(log) {
  return {
    id: log.id,
    order_id: log.orderId,
    operator: log.operator,
    action: log.action,
    remark: log.remark,
    created_at: (log.createdAt || '').replace('T', ' '),
  }
}

// 加载订单列表；列表非空时自动选中第一个订单，触发 watch 加载明细与日志
async function loadOrders(status = filterStatus.value) {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await orderApi.getList(status)
    orders.value = (data || []).map(mapOrder)
    if (orders.value.length) {
      selectedOrderId.value = orders.value[0].id
    } else {
      selectedOrderId.value = null
      items.value = []
      logs.value = []
    }
  } catch (e) {
    errorMsg.value = e.message || '订单加载失败'
    orders.value = []
    selectedOrderId.value = null
    items.value = []
    logs.value = []
  } finally {
    loading.value = false
  }
}

// 加载选中订单的明细与日志
async function loadItemsAndLogs(orderId) {
  if (!orderId) {
    items.value = []
    logs.value = []
    return
  }
  try {
    const [itemsData, logsData] = await Promise.all([
      orderApi.getItems(orderId),
      orderApi.getLogs(orderId),
    ])
    items.value = (itemsData || []).map(mapItem)
    logs.value = (logsData || []).map(mapLog)
  } catch (e) {
    errorMsg.value = e.message || '订单明细加载失败'
    items.value = []
    logs.value = []
  }
}

// 选中订单变化时加载明细与日志
watch(selectedOrderId, (v) => {
  loadItemsAndLogs(v)
})

// 状态筛选变化时重新加载订单列表（loadOrders 内部会自动选中第一项）
watch(filterStatus, (v) => {
  loadOrders(v)
})

onMounted(loadOrders)
</script>

<template>
  <div class="order-page">
    <div v-if="errorMsg" class="error-banner">{{ errorMsg }}</div>
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 订单主表（order） -->
    <section class="card">
      <div class="card-header">
        <h3>订单列表</h3>
        <span class="card-sub">表：order · 点击行查看明细与日志</span>
      </div>

      <div class="filter-bar">
        <button
          v-for="f in statusFilters"
          :key="f.k"
          class="filter-btn"
          :class="{ active: filterStatus === f.k }"
          @click="filterStatus = f.k"
        >
          {{ f.t }}
        </button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>order_no</th>
            <th>customer_id</th>
            <th>total_amount</th>
            <th>pay_amount</th>
            <th>pay_method</th>
            <th>status</th>
            <th>receiver</th>
            <th>created_at</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="o in filteredOrders"
            :key="o.id"
            :class="{ selected: selectedOrderId === o.id }"
            @click="selectOrder(o.id)"
          >
            <td>{{ o.id }}</td>
            <td class="mono">{{ o.order_no }}</td>
            <td>{{ o.customer_id }}</td>
            <td>¥{{ o.total_amount.toFixed(2) }}</td>
            <td class="price">¥{{ o.pay_amount.toFixed(2) }}</td>
            <td>{{ payMethodMap[o.pay_method] || '-' }}</td>
            <td>
              <span class="tag" :class="getStatus(o.status).class">{{ getStatus(o.status).text }}</span>
            </td>
            <td>
              <div>{{ o.receiver_name }}</div>
              <div class="mono small">{{ o.receiver_phone }}</div>
            </td>
            <td class="mono small">{{ o.created_at }}</td>
          </tr>
          <tr v-if="!filteredOrders.length">
            <td colspan="9" class="empty">该状态下暂无订单</td>
          </tr>
        </tbody>
      </table>
    </section>

    <div class="row">
      <!-- 订单明细（order_item） -->
      <section class="card col-2">
        <div class="card-header">
          <h3>订单明细</h3>
          <span class="card-sub">表：order_item · order_id={{ selectedOrderId }}</span>
        </div>
        <table class="table">
          <thead>
            <tr>
              <th>id</th>
              <th>product_id</th>
              <th>product_name</th>
              <th>sku_specs</th>
              <th>price</th>
              <th>quantity</th>
              <th>total_price</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="it in items" :key="it.id">
              <td>{{ it.id }}</td>
              <td>{{ it.product_id }}</td>
              <td>{{ it.product_name }}</td>
              <td>{{ it.sku_specs }}</td>
              <td>¥{{ it.price.toFixed(2) }}</td>
              <td>×{{ it.quantity }}</td>
              <td class="price">¥{{ it.total_price.toFixed(2) }}</td>
            </tr>
            <tr v-if="!items.length">
              <td colspan="7" class="empty">暂无明细</td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 订单详情 + 操作日志 -->
      <section class="card col-1">
        <div class="card-header">
          <h3>订单详情</h3>
          <span class="card-sub">表：order</span>
        </div>
        <div v-if="selectedOrder" class="detail">
          <div class="detail-row"><span>订单号</span><span class="mono">{{ selectedOrder.order_no }}</span></div>
          <div class="detail-row"><span>物流</span><span>{{ (selectedOrder.logistics_company || selectedOrder.logistics_no) ? `${selectedOrder.logistics_company || '-'} ${selectedOrder.logistics_no || ''}`.trim() : '-' }}</span></div>
          <div class="detail-row"><span>收货地址</span><span>{{ selectedOrder.receiver_address }}</span></div>
          <div class="detail-row"><span>支付时间</span><span class="mono small">{{ selectedOrder.pay_time || '-' }}</span></div>
          <div class="detail-row"><span>发货时间</span><span class="mono small">{{ selectedOrder.delivery_time || '-' }}</span></div>
          <div class="detail-row"><span>完成时间</span><span class="mono small">{{ selectedOrder.finish_time || '-' }}</span></div>
        </div>

        <div class="card-header" style="margin-top: 18px">
          <h3>操作日志</h3>
          <span class="card-sub">表：order_log</span>
        </div>
        <ul class="log-list">
          <li v-for="log in logs" :key="log.id">
            <div class="log-dot" :class="log.action"></div>
            <div class="log-content">
              <div class="log-title">
                <strong>{{ log.action }}</strong>
                <span class="log-operator">· {{ log.operator }}</span>
              </div>
              <div class="log-remark">{{ log.remark }}</div>
              <div class="log-time mono small">{{ log.created_at }}</div>
            </div>
          </li>
          <li v-if="!logs.length" class="empty">暂无日志</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
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

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 5px 14px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 16px;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.filter-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: #fff;
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
  cursor: pointer;
}

.table tbody tr:hover {
  background: #f9fafb;
}

.table tbody tr.selected {
  background: #eef2ff;
}

.mono {
  font-family: ui-monospace, Consolas, monospace;
}

.small {
  font-size: 12px;
  color: #9ca3af;
}

.price {
  color: #ef4444;
  font-weight: 500;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tag.primary { background: #dbeafe; color: #1d4ed8; }
.tag.warn { background: #fef3c7; color: #b45309; }
.tag.info { background: #e0e7ff; color: #4338ca; }
.tag.success { background: #d1fae5; color: #047857; }
.tag.muted { background: #f3f4f6; color: #6b7280; }
.tag.danger { background: #fee2e2; color: #b91c1c; }

.empty {
  text-align: center;
  color: #9ca3af;
  padding: 24px;
}

.row {
  display: flex;
  gap: 16px;
}

.col-2 { flex: 2; }
.col-1 { flex: 1; }

.detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  font-size: 13px;
  gap: 12px;
}

.detail-row span:first-child {
  width: 70px;
  color: #9ca3af;
  flex-shrink: 0;
}

.detail-row span:last-child {
  color: #374151;
  word-break: break-all;
}

.log-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
}

.log-list li {
  display: flex;
  gap: 10px;
  padding-bottom: 14px;
  position: relative;
}

.log-list li:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 14px;
  bottom: 0;
  width: 1px;
  background: #e5e7eb;
}

.log-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #667eea;
  margin-top: 4px;
  flex-shrink: 0;
  z-index: 1;
}

.log-dot.取消 { background: #ef4444; }
.log-dot.完成 { background: #10b981; }
.log-dot.发货 { background: #3b82f6; }
.log-dot.支付 { background: #f59e0b; }

.log-content {
  flex: 1;
}

.log-title {
  font-size: 13px;
  color: #1f2937;
}

.log-operator {
  color: #9ca3af;
  font-weight: normal;
}

.log-remark {
  font-size: 12px;
  color: #6b7280;
  margin: 2px 0;
}

.log-time {
  color: #9ca3af;
}

@media (max-width: 1024px) {
  .row { flex-direction: column; }
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
</style>
