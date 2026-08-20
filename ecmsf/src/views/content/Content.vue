<script setup>
import { ref, onMounted } from 'vue'
import { contentApi } from '../../api'

const banners = ref([])
const notices = ref([])
const activeNotice = ref(null)
const loading = ref(false)
const errorMsg = ref('')

const loadAll = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const [bannerList, noticeList] = await Promise.all([
      contentApi.getBanners(),
      contentApi.getNotices(),
    ])
    banners.value = (bannerList || []).map((b) => ({
      id: b.id,
      title: b.title,
      image_url: b.imageUrl,
      link_url: b.linkUrl,
      sort: b.sort,
      status: b.status,
      created_at: (b.createdAt || '').replace('T', ' '),
    }))
    notices.value = (noticeList || []).map((n) => ({
      id: n.id,
      title: n.title,
      content: n.content,
      status: n.status,
      sort: n.sort,
      created_at: (n.createdAt || '').replace('T', ' '),
    }))
    if (notices.value.length > 0) {
      activeNotice.value = notices.value[0].id
    }
  } catch (e) {
    errorMsg.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="content-page">
    <div v-if="errorMsg" class="error-banner">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
      </svg>
      {{ errorMsg }}
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <!-- 轮播图（banner） -->
    <section class="card">
      <div class="card-header">
        <h3>轮播图管理</h3>
        <span class="card-sub">表：banner · 共 {{ banners.length }} 条</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>image_url</th>
            <th>title</th>
            <th>link_url</th>
            <th>sort</th>
            <th>status</th>
            <th>created_at</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in banners" :key="b.id">
            <td>{{ b.id }}</td>
            <td>
              <div class="banner-thumb">
                <div class="thumb-placeholder">
                  <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
                    <path d="M21 19V5c0-1.1-.9-2-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" />
                  </svg>
                </div>
                <span class="mono small">{{ b.image_url }}</span>
              </div>
            </td>
            <td>{{ b.title }}</td>
            <td class="mono small">{{ b.link_url }}</td>
            <td>{{ b.sort }}</td>
            <td>
              <span class="tag" :class="b.status ? 'success' : 'muted'">{{ b.status ? '显示' : '隐藏' }}</span>
            </td>
            <td class="mono small">{{ b.created_at }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 公告（notice） -->
    <section class="card">
      <div class="card-header">
        <h3>公告管理</h3>
        <span class="card-sub">表：notice · 共 {{ notices.length }} 条</span>
      </div>
      <div class="notice-layout">
        <ul class="notice-list">
          <li
            v-for="n in notices"
            :key="n.id"
            :class="{ active: activeNotice === n.id }"
            @click="activeNotice = n.id"
          >
            <div class="notice-item-top">
              <span class="notice-title">{{ n.title }}</span>
              <span class="tag" :class="n.status ? 'success' : 'muted'">{{ n.status ? '显示' : '隐藏' }}</span>
            </div>
            <div class="notice-meta">
              <span class="mono small">sort: {{ n.sort }}</span>
              <span class="mono small">{{ n.created_at }}</span>
            </div>
          </li>
        </ul>
        <div class="notice-detail">
          <div class="card-sub" style="margin-bottom: 10px">表：notice · id={{ activeNotice }}</div>
          <h4>{{ notices.find((n) => n.id === activeNotice)?.title }}</h4>
          <p>{{ notices.find((n) => n.id === activeNotice)?.content }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.content-page {
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

.banner-thumb {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thumb-placeholder {
  width: 80px;
  height: 40px;
  border-radius: 4px;
  background: #e5e7eb;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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

.tag.success { background: #d1fae5; color: #047857; }
.tag.muted { background: #f3f4f6; color: #6b7280; }

.notice-layout {
  display: flex;
  gap: 16px;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 360px;
}

.notice-list li {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.notice-list li:hover {
  border-color: #667eea;
}

.notice-list li.active {
  border-color: #667eea;
  background: #eef2ff;
}

.notice-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.notice-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.notice-meta {
  display: flex;
  gap: 12px;
}

.notice-detail {
  flex: 2;
  padding: 18px;
  background: #f9fafb;
  border-radius: 8px;
  min-height: 160px;
}

.notice-detail h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #1f2937;
}

.notice-detail p {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .notice-layout {
    flex-direction: column;
  }
  .notice-list {
    max-width: 100%;
  }
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
