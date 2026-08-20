<script setup>
import { ref, computed } from 'vue'
import Dashboard from '../dashboard/Dashboard.vue'
import Product from '../product/Product.vue'
import Order from '../order/Order.vue'
import Customer from '../customer/Customer.vue'
import Marketing from '../marketing/Marketing.vue'
import Content from '../content/Content.vue'
import Log from '../log/Log.vue'

defineProps({
  username: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['logout'])

// 侧边菜单（基于 sql.txt 中的模块）
const menus = [
  {
    key: 'dashboard',
    label: '仪表盘',
    icon: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z',
  },
  {
    key: 'product',
    label: '商品管理',
    icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z',
  },
  {
    key: 'order',
    label: '订单管理',
    icon: 'M8 2c-1.1 0-2 .9-2 2v3H4a2 2 0 0 0-2 2v9c0 1.1.9 2 2 2h16a2 2 0 0 0 2-2v-9a2 2 0 0 0-2-2h-2V4a2 2 0 0 0-2-2H8zm0 2h8v3H8V4zm12 13H4v-7h16v7z',
  },
  {
    key: 'customer',
    label: '客户管理',
    icon: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  },
  {
    key: 'marketing',
    label: '营销中心',
    icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17.93V18h-2v1.93A8.01 8.01 0 0 1 4.07 13H6v-2H4.07A8.01 8.01 0 0 1 11 4.07V6h2V4.07A8.01 8.01 0 0 1 19.93 11H18v2h1.93A8.01 8.01 0 0 1 13 19.93z',
  },
  {
    key: 'banner',
    label: '内容管理',
    icon: 'M21 19V5c0-1.1-.9-2-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z',
  },
  {
    key: 'log',
    label: '系统日志',
    icon: 'M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  },
]

const activeMenu = ref('dashboard')

// 菜单 key 与板块组件的映射
const componentMap = {
  dashboard: Dashboard,
  product: Product,
  order: Order,
  customer: Customer,
  marketing: Marketing,
  banner: Content,
  log: Log,
}

const currentComponent = computed(() => componentMap[activeMenu.value])

function selectMenu(key) {
  activeMenu.value = key
}

function handleLogout() {
  emit('logout')
}
</script>

<template>
  <div class="home-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="brand">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
          <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0 0 20 4H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z" />
        </svg>
        <span>ECMS</span>
      </div>
      <nav class="menu">
        <a
          v-for="m in menus"
          :key="m.key"
          class="menu-item"
          :class="{ active: activeMenu === m.key }"
          @click="selectMenu(m.key)"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path :d="m.icon" />
          </svg>
          <span>{{ m.label }}</span>
        </a>
      </nav>
    </aside>

    <!-- 主区域 -->
    <div class="main">
      <header class="topbar">
        <div class="page-title">首页 · {{ menus.find(m => m.key === activeMenu)?.label }}</div>
        <div class="user-info">
          <div class="user-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
          </div>
          <span class="username">{{ username }}</span>
          <button class="btn-logout" @click="handleLogout">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h8v-2H4V5z" />
            </svg>
            退出
          </button>
        </div>
      </header>

      <main class="content">
        <component :is="currentComponent" />
      </main>
    </div>
  </div>
</template>

<style scoped>
.home-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 220px;
  background: #1f2937;
  color: #d1d5db;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 22px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid #374151;
}

.menu {
  flex: 1;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 22px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background: #374151;
  color: #fff;
}

.menu-item.active {
  background: #111827;
  color: #fff;
  border-left-color: #667eea;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.username {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.btn-logout {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 10px;
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

.panel {
  background: #fff;
  border-radius: 10px;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.panel h2 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #1f2937;
}

.panel p {
  margin: 6px 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
}

.panel .hint {
  color: #9ca3af;
  font-size: 13px;
}

.panel strong {
  color: #667eea;
}

@media (max-width: 768px) {
  .sidebar {
    width: 60px;
  }
  .brand span,
  .menu-item span {
    display: none;
  }
  .menu-item {
    justify-content: center;
    padding: 14px 0;
  }
}
</style>
