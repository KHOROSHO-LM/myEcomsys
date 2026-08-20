<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { marketingApi } from '../../api'

// 类型：1满减，2折扣
const couponTypeMap = { 1: '满减', 2: '折扣' }

// 优惠券领取记录 status：0未使用，1已使用，2已过期
const issueStatusMap = { 0: { text: '未使用', class: 'info' }, 1: { text: '已使用', class: 'success' }, 2: { text: '已过期', class: 'muted' } }

// 秒杀活动 status：0未开始，1进行中，2已结束
const seckillStatusMap = { 0: { text: '未开始', class: 'muted' }, 1: { text: '进行中', class: 'danger' }, 2: { text: '已结束', class: 'muted' } }

// 表数据
const coupons = ref([])
const issues = ref([])
const seckills = ref([])

// 选中优惠券的领取记录
const couponIssues = ref([])
const selectedCouponId = ref(null)

const loading = ref(false)
const errorMsg = ref('')

// LocalDateTime 字符串可能含 'T'，统一替换为空格
function fmtTime(t) {
  return (t == null ? '' : String(t)).replace('T', ' ')
}

// 后端驼峰 → 模板蛇形
function mapCoupon(c) {
  return {
    id: c.id,
    name: c.name,
    type: c.type,
    threshold: c.threshold,
    discount_value: c.discountValue,
    stock: c.stock,
    used_count: c.usedCount,
    per_user_limit: c.perUserLimit,
    expire_start: fmtTime(c.expireStart),
    expire_end: fmtTime(c.expireEnd),
    status: c.status,
  }
}

function mapIssue(i) {
  return {
    id: i.id,
    coupon_id: i.couponId,
    customer_id: i.customerId,
    status: i.status,
    used_time: fmtTime(i.usedTime),
    order_id: i.orderId,
    expire_time: fmtTime(i.expireTime),
    created_at: fmtTime(i.createdAt),
  }
}

function mapSeckill(s) {
  return {
    id: s.id,
    product_id: s.productId,
    product_name: '-',
    seckill_price: s.seckillPrice,
    seckill_stock: s.seckillStock,
    seckill_limit: s.seckillLimit,
    start_time: fmtTime(s.startTime),
    end_time: fmtTime(s.endTime),
    status: s.status,
  }
}

async function loadIssues(couponId) {
  if (!couponId) {
    couponIssues.value = []
    return
  }
  try {
    const list = await marketingApi.getCouponIssues(couponId)
    couponIssues.value = (list || []).map(mapIssue)
  } catch (e) {
    errorMsg.value = e.message || '加载领取记录失败'
    couponIssues.value = []
  }
}

async function loadAll() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [couponList, seckillList] = await Promise.all([
      marketingApi.getCoupons(),
      marketingApi.getSeckills(),
    ])
    coupons.value = (couponList || []).map(mapCoupon)
    seckills.value = (seckillList || []).map(mapSeckill)
    if (coupons.value.length) {
      selectedCouponId.value = coupons.value[0].id
    }
  } catch (e) {
    errorMsg.value = e.message || '加载营销数据失败'
  } finally {
    loading.value = false
  }
}

watch(selectedCouponId, (newId) => {
  loadIssues(newId)
})

function selectCoupon(id) {
  selectedCouponId.value = id
}

function getProgress(c) {
  return c.stock ? Math.round((c.used_count / c.stock) * 100) : 0
}

onMounted(loadAll)
</script>

<template>
  <div class="marketing-page">
    <div v-if="errorMsg" class="error-banner">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
      </svg>
      {{ errorMsg }}
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <!-- 优惠券（coupon） -->
    <section class="card">
      <div class="card-header">
        <h3>优惠券列表</h3>
        <span class="card-sub">表：coupon · 点击卡片查看领取记录</span>
      </div>
      <div class="coupon-grid">
        <div
          v-for="c in coupons"
          :key="c.id"
          class="coupon-card"
          :class="{ selected: selectedCouponId === c.id, disabled: !c.status }"
          @click="selectCoupon(c.id)"
        >
          <div class="coupon-top">
            <div class="coupon-value">
              <template v-if="c.type === 1">¥{{ c.discount_value }}</template>
              <template v-else>{{ c.discount_value }}<span class="unit">折</span></template>
            </div>
            <div class="coupon-type">{{ couponTypeMap[c.type] }}</div>
          </div>
          <div class="coupon-name">{{ c.name }}</div>
          <div class="coupon-condition">满 ¥{{ c.threshold }} 可用</div>
          <div class="coupon-progress">
            <div class="progress-info">
              <span>已领 {{ c.used_count }} / {{ c.stock }}</span>
              <span>{{ getProgress(c) }}%</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: getProgress(c) + '%' }"></div>
            </div>
          </div>
          <div class="coupon-time mono">{{ c.expire_start.slice(0, 10) }} ~ {{ c.expire_end.slice(0, 10) }}</div>
          <div class="coupon-status">
            <span class="tag" :class="c.status ? 'success' : 'muted'">{{ c.status ? '启用' : '停用' }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 领取记录（coupon_issue） -->
    <section class="card">
      <div class="card-header">
        <h3>优惠券领取记录</h3>
        <span class="card-sub">表：coupon_issue · coupon_id={{ selectedCouponId }}</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>coupon_id</th>
            <th>customer_id</th>
            <th>status</th>
            <th>used_time</th>
            <th>order_id</th>
            <th>expire_time</th>
            <th>created_at</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="i in couponIssues" :key="i.id">
            <td>{{ i.id }}</td>
            <td>{{ i.coupon_id }}</td>
            <td>{{ i.customer_id }}</td>
            <td>
              <span class="tag" :class="issueStatusMap[i.status].class">{{ issueStatusMap[i.status].text }}</span>
            </td>
            <td class="mono small">{{ i.used_time || '-' }}</td>
            <td>{{ i.order_id || '-' }}</td>
            <td class="mono small">{{ i.expire_time }}</td>
            <td class="mono small">{{ i.created_at }}</td>
          </tr>
          <tr v-if="!couponIssues.length">
            <td colspan="8" class="empty">该优惠券暂无领取记录</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 秒杀活动（seckill） -->
    <section class="card">
      <div class="card-header">
        <h3>秒杀活动</h3>
        <span class="card-sub">表：seckill</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>product_id</th>
            <th>product_name</th>
            <th>seckill_price</th>
            <th>seckill_stock</th>
            <th>seckill_limit</th>
            <th>start_time</th>
            <th>end_time</th>
            <th>status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in seckills" :key="s.id">
            <td>{{ s.id }}</td>
            <td>{{ s.product_id }}</td>
            <td>{{ s.product_name }}</td>
            <td class="price">¥{{ s.seckill_price.toFixed(2) }}</td>
            <td>{{ s.seckill_stock }}</td>
            <td>限购 {{ s.seckill_limit }} 件</td>
            <td class="mono small">{{ s.start_time }}</td>
            <td class="mono small">{{ s.end_time }}</td>
            <td>
              <span class="tag" :class="seckillStatusMap[s.status].class">{{ seckillStatusMap[s.status].text }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.marketing-page {
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

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
}

.coupon-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  position: relative;
}

.coupon-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.coupon-card.selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.coupon-card.disabled {
  background: #f9fafb;
  opacity: 0.7;
}

.coupon-top {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}

.coupon-value {
  font-size: 24px;
  font-weight: 600;
  color: #ef4444;
  line-height: 1;
}

.coupon-value .unit {
  font-size: 14px;
  margin-left: 2px;
}

.coupon-type {
  font-size: 12px;
  color: #6b7280;
  background: #fef3c7;
  padding: 2px 8px;
  border-radius: 10px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.coupon-condition {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 10px;
}

.coupon-progress {
  margin-bottom: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.progress-track {
  height: 6px;
  background: #f3f4f6;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
}

.coupon-time {
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.coupon-status {
  position: absolute;
  top: 12px;
  right: 12px;
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
.tag.success { background: #d1fae5; color: #047857; }
.tag.muted { background: #f3f4f6; color: #6b7280; }
.tag.info { background: #e0e7ff; color: #4338ca; }
.tag.danger { background: #fee2e2; color: #b91c1c; }

.empty {
  text-align: center;
  color: #9ca3af;
  padding: 24px;
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
