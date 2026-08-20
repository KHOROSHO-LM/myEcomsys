<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { customerApi } from '../../api'

// 会员等级（表：customer_level）
const levels = ref([])

// 买家列表（表：customer）
const customers = ref([])

// 收货地址（表：customer_address，按选中客户加载）
const customerAddresses = ref([])

const selectedCustomerId = ref(null)
const loading = ref(false)
const errorMsg = ref('')

function getLevelName(id) {
  return levels.value.find((l) => l.id === id)?.level_name || '普通会员'
}

function getLevelClass(id) {
  const map = { 1: 'muted', 2: 'info', 3: 'warn', 4: 'danger' }
  return map[id] || 'muted'
}

function selectCustomer(id) {
  selectedCustomerId.value = id
}

// 后端驼峰 → 模板下划线字段映射
function mapLevel(l) {
  return {
    id: l.id,
    level_name: l.levelName,
    level_code: l.levelCode,
    min_points: l.minPoints,
    discount_rate: l.discountRate,
    benefits: l.benefits,
    sort: l.sort,
    status: l.status,
  }
}

function mapCustomer(c) {
  return {
    id: c.id,
    username: c.username,
    phone: c.phone,
    email: c.email,
    nickname: c.nickname,
    avatar: c.avatar,
    level_id: c.levelId,
    points: c.points,
    status: c.status,
    register_time: (c.registerTime || '').replace('T', ' '),
    last_login_time: (c.lastLoginTime || '').replace('T', ' '),
  }
}

function mapAddress(a) {
  return {
    id: a.id,
    customer_id: a.customerId,
    receiver_name: a.receiverName,
    receiver_phone: a.receiverPhone,
    province: a.province,
    city: a.city,
    district: a.district,
    detail: a.detail,
    is_default: a.isDefault,
  }
}

async function loadAll() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [levelList, customerList] = await Promise.all([
      customerApi.getLevels(),
      customerApi.getList(),
    ])
    levels.value = (levelList || []).map(mapLevel)
    customers.value = (customerList || []).map(mapCustomer)
    if (customers.value.length > 0) {
      selectedCustomerId.value = customers.value[0].id
    }
  } catch (e) {
    errorMsg.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function loadAddresses() {
  if (selectedCustomerId.value == null) {
    customerAddresses.value = []
    return
  }
  try {
    const list = await customerApi.getAddresses(selectedCustomerId.value)
    customerAddresses.value = (list || []).map(mapAddress)
  } catch (e) {
    errorMsg.value = e.message || '加载收货地址失败'
    customerAddresses.value = []
  }
}

watch(selectedCustomerId, () => {
  loadAddresses()
})

onMounted(loadAll)
</script>

<template>
  <div class="customer-page">
    <div v-if="errorMsg" class="error-banner">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
      </svg>
      {{ errorMsg }}
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <!-- 会员等级（customer_level） -->
    <section class="card">
      <div class="card-header">
        <h3>会员等级</h3>
        <span class="card-sub">表：customer_level · 共 {{ levels.length }} 条</span>
      </div>
      <div class="level-grid">
        <div v-for="l in levels" :key="l.id" class="level-card" :class="getLevelClass(l.id)">
          <div class="level-name">{{ l.level_name }}</div>
          <div class="level-code mono">{{ l.level_code }}</div>
          <div class="level-row"><span>最低积分</span><strong>{{ l.min_points }}</strong></div>
          <div class="level-row"><span>折扣率</span><strong>{{ (l.discount_rate * 10).toFixed(1) }} 折</strong></div>
          <div class="level-benefits">{{ l.benefits }}</div>
        </div>
      </div>
    </section>

    <!-- 买家列表（customer） -->
    <section class="card">
      <div class="card-header">
        <h3>买家列表</h3>
        <span class="card-sub">表：customer · 点击行查看收货地址</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>username</th>
            <th>nickname</th>
            <th>phone</th>
            <th>email</th>
            <th>level_id</th>
            <th>points</th>
            <th>status</th>
            <th>register_time</th>
            <th>last_login_time</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="c in customers"
            :key="c.id"
            :class="{ selected: selectedCustomerId === c.id }"
            @click="selectCustomer(c.id)"
          >
            <td>{{ c.id }}</td>
            <td class="mono">{{ c.username }}</td>
            <td>{{ c.nickname }}</td>
            <td>{{ c.phone }}</td>
            <td class="mono small">{{ c.email || '-' }}</td>
            <td>
              <span class="tag" :class="getLevelClass(c.level_id)">{{ getLevelName(c.level_id) }}</span>
            </td>
            <td>{{ c.points }}</td>
            <td>
              <span class="tag" :class="c.status ? 'success' : 'muted'">{{ c.status ? '正常' : '禁用' }}</span>
            </td>
            <td class="mono small">{{ c.register_time }}</td>
            <td class="mono small">{{ c.last_login_time }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 收货地址（customer_address） -->
    <section class="card">
      <div class="card-header">
        <h3>收货地址</h3>
        <span class="card-sub">表：customer_address · customer_id={{ selectedCustomerId }}</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>customer_id</th>
            <th>receiver_name</th>
            <th>receiver_phone</th>
            <th>province / city / district</th>
            <th>detail</th>
            <th>is_default</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in customerAddresses" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.customer_id }}</td>
            <td>{{ a.receiver_name }}</td>
            <td>{{ a.receiver_phone }}</td>
            <td>{{ a.province }} / {{ a.city }} / {{ a.district }}</td>
            <td>{{ a.detail }}</td>
            <td>
              <span v-if="a.is_default" class="tag primary">默认</span>
              <span v-else class="tag muted">-</span>
            </td>
          </tr>
          <tr v-if="!customerAddresses.length">
            <td colspan="7" class="empty">该客户暂无收货地址</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.customer-page {
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

.level-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.level-card {
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #e5e7eb;
  background: #f9fafb;
}

.level-card.muted { border-left-color: #9ca3af; }
.level-card.info { border-left-color: #4338ca; }
.level-card.warn { border-left-color: #b45309; }
.level-card.danger { border-left-color: #b91c1c; }

.level-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.level-code {
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 10px;
}

.level-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin: 4px 0;
}

.level-row span {
  color: #6b7280;
}

.level-row strong {
  color: #1f2937;
}

.level-benefits {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
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
.tag.warn { background: #fef3c7; color: #b45309; }
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
