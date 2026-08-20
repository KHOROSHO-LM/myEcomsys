<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { productApi } from '../../api'

// 商品分类（来自后端 /api/product/categories，对应 product_category 表）
const categories = ref([])

// 商品主表（来自后端 /api/product/list，对应 product 表）
const products = ref([])

// 选中商品查看 SKU 与附图
const selectedProductId = ref(null)

// SKU 规格（来自后端 /api/product/{id}/skus，对应 product_sku 表）
const skus = ref([])

// 商品附图（来自后端 /api/product/{id}/images，对应 product_image 表）
const images = ref([])

const loading = ref(false)
const errorMsg = ref('')

async function loadCategoriesAndProducts() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [cats, pros] = await Promise.all([
      productApi.getCategories(),
      productApi.getList(),
    ])
    categories.value = cats || []
    products.value = pros || []
    // 默认选中第一个商品
    if (products.value.length > 0) {
      selectedProductId.value = products.value[0].id
    }
  } catch (e) {
    errorMsg.value = e.message || '数据加载失败'
  } finally {
    loading.value = false
  }
}

async function loadSkusAndImages(productId) {
  if (!productId) {
    skus.value = []
    images.value = []
    return
  }
  try {
    const [s, i] = await Promise.all([
      productApi.getSkus(productId),
      productApi.getImages(productId),
    ])
    skus.value = s || []
    images.value = i || []
  } catch (e) {
    skus.value = []
    images.value = []
  }
}

watch(selectedProductId, (newId) => {
  loadSkusAndImages(newId)
})

function selectProduct(id) {
  selectedProductId.value = id
}

function getCategoryName(id) {
  return categories.value.find((c) => c.id === id)?.name || '-'
}

onMounted(loadCategoriesAndProducts)
</script>

<template>
  <div class="product-page">
    <div v-if="errorMsg" class="error-banner">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
      </svg>
      {{ errorMsg }}
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <!-- 商品分类（product_category） -->
    <section class="card">
      <div class="card-header">
        <h3>商品分类</h3>
        <span class="card-sub">表：product_category · 共 {{ categories.length }} 条</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>parent_id</th>
            <th>name</th>
            <th>level</th>
            <th>sort</th>
            <th>status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in categories" :key="c.id">
            <td>{{ c.id }}</td>
            <td>{{ c.parentId }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.level }}</td>
            <td>{{ c.sort }}</td>
            <td>
              <span class="tag" :class="c.status ? 'success' : 'muted'">
                {{ c.status ? '启用' : '禁用' }}
              </span>
            </td>
          </tr>
          <tr v-if="!categories.length && !loading">
            <td colspan="6" class="empty">暂无分类数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 商品主表（product） -->
    <section class="card">
      <div class="card-header">
        <h3>商品列表</h3>
        <span class="card-sub">表：product · 点击行查看 SKU 与附图</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>category_id</th>
            <th>name / subtitle</th>
            <th>main_image</th>
            <th>price</th>
            <th>stock</th>
            <th>sales</th>
            <th>status</th>
            <th>new/hot</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="p in products"
            :key="p.id"
            :class="{ selected: selectedProductId === p.id }"
            @click="selectProduct(p.id)"
          >
            <td>{{ p.id }}</td>
            <td>{{ getCategoryName(p.categoryId) }}</td>
            <td>
              <div class="product-name">{{ p.name }}</div>
              <div class="product-sub">{{ p.subtitle }}</div>
            </td>
            <td class="mono">{{ p.mainImage || '-' }}</td>
            <td class="price">¥{{ Number(p.price || 0).toFixed(2) }}</td>
            <td :class="{ 'stock-zero': p.stock === 0 }">{{ p.stock }}</td>
            <td>{{ p.sales }}</td>
            <td>
              <span class="tag" :class="p.status ? 'success' : 'muted'">
                {{ p.status ? '上架' : '下架' }}
              </span>
            </td>
            <td>
              <span v-if="p.isNew" class="tag info">新品</span>
              <span v-if="p.isHot" class="tag danger">热卖</span>
            </td>
          </tr>
          <tr v-if="!products.length && !loading">
            <td colspan="9" class="empty">暂无商品数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <div class="row">
      <!-- SKU 规格（product_sku） -->
      <section class="card col-2">
        <div class="card-header">
          <h3>SKU 规格</h3>
          <span class="card-sub">表：product_sku · 当前商品 id={{ selectedProductId || '-' }}</span>
        </div>
        <table class="table">
          <thead>
            <tr>
              <th>id</th>
              <th>product_id</th>
              <th>specs</th>
              <th>price</th>
              <th>stock</th>
              <th>image</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in skus" :key="s.id">
              <td>{{ s.id }}</td>
              <td>{{ s.productId }}</td>
              <td>{{ s.specs }}</td>
              <td class="price">¥{{ Number(s.price || 0).toFixed(2) }}</td>
              <td>{{ s.stock }}</td>
              <td class="mono">{{ s.image || '-' }}</td>
            </tr>
            <tr v-if="!skus.length">
              <td colspan="6" class="empty">暂无 SKU 数据</td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 商品附图（product_image） -->
      <section class="card col-1">
        <div class="card-header">
          <h3>商品附图</h3>
          <span class="card-sub">表：product_image</span>
        </div>
        <ul class="image-list">
          <li v-for="img in images" :key="img.id">
            <div class="img-placeholder">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
                <path d="M21 19V5c0-1.1-.9-2-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" />
              </svg>
            </div>
            <div class="img-info">
              <div class="mono">{{ img.imageUrl }}</div>
              <div class="img-sort">sort: {{ img.sort }}</div>
            </div>
          </li>
          <li v-if="!images.length" class="empty">暂无附图</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<style scoped>
.product-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.product-name {
  color: #1f2937;
  font-weight: 500;
}

.product-sub {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 2px;
}

.mono {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 12px;
  color: #6b7280;
}

.price {
  color: #ef4444;
  font-weight: 500;
}

.stock-zero {
  color: #ef4444;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 4px;
}

.tag.success { background: #d1fae5; color: #047857; }
.tag.muted { background: #f3f4f6; color: #6b7280; }
.tag.info { background: #e0e7ff; color: #4338ca; }
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

.image-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-list li {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 10px;
  background: #f9fafb;
  border-radius: 8px;
}

.img-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  background: #e5e7eb;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.img-info {
  min-width: 0;
}

.img-sort {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

@media (max-width: 1024px) {
  .row { flex-direction: column; }
}
</style>
