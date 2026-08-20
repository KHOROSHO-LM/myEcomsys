<script setup>
import { ref, computed, onMounted } from 'vue'
import { logApi } from '../../api'

// 操作日志（operation_log：id, user_id, username, module, action, request_url, request_method, ip, cost_time, created_at）
const operationLogs = ref([])

// 登录日志（login_log：id, user_id, username, login_type, ip, status, message, created_at）
// login_type：1后台管理员，2买家；status：1成功，0失败
const loginLogs = ref([])

const methodMap = {
  GET: 'info',
  POST: 'success',
  PUT: 'warn',
  DELETE: 'danger',
}

// 筛选
const logType = ref('operation') // operation / login
const currentData = computed(() =>
  logType.value === 'operation' ? operationLogs.value : loginLogs.value
)

const loading = ref(false)
const errorMsg = ref('')

const formatTime = (x) => (x || '').replace('T', ' ')

const loadAll = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const [opList, loginList] = await Promise.all([
      logApi.getOperation(),
      logApi.getLogin(),
    ])
    operationLogs.value = (opList || []).map((x) => ({
      id: x.id,
      user_id: x.userId,
      username: x.username,
      module: x.module,
      action: x.action,
      request_url: x.requestUrl,
      request_method: x.requestMethod,
      ip: x.ip,
      cost_time: x.costTime,
      created_at: formatTime(x.createdAt),
    }))
    loginLogs.value = (loginList || []).map((x) => ({
      id: x.id,
      user_id: x.userId,
      username: x.username,
      login_type: x.loginType,
      ip: x.ip,
      status: x.status,
      message: x.message,
      created_at: formatTime(x.createdAt),
    }))
  } catch (e) {
    errorMsg.value = (e && e.message) || '加载日志失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="log-page">
    <div v-if="errorMsg" class="error-banner">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
      </svg>
      {{ errorMsg }}
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <section class="card">
      <div class="card-header">
        <h3>系统日志</h3>
        <div class="tabs">
          <button
            class="tab"
            :class="{ active: logType === 'operation' }"
            @click="logType = 'operation'"
          >
            操作日志 · operation_log
          </button>
          <button
            class="tab"
            :class="{ active: logType === 'login' }"
            @click="logType = 'login'"
          >
            登录日志 · login_log
          </button>
        </div>
      </div>

      <!-- 操作日志 -->
      <table v-if="logType === 'operation'" class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>user_id</th>
            <th>username</th>
            <th>module</th>
            <th>action</th>
            <th>request_url</th>
            <th>method</th>
            <th>ip</th>
            <th>cost_time</th>
            <th>created_at</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in currentData" :key="log.id">
            <td>{{ log.id }}</td>
            <td>{{ log.user_id || '-' }}</td>
            <td>{{ log.username }}</td>
            <td>{{ log.module }}</td>
            <td>
              <span class="tag" :class="methodMap[log.request_method] || 'muted'">{{ log.action }}</span>
            </td>
            <td class="mono small">{{ log.request_url }}</td>
            <td>
              <span class="tag" :class="methodMap[log.request_method] || 'muted'">{{ log.request_method }}</span>
            </td>
            <td class="mono small">{{ log.ip }}</td>
            <td :class="{ slow: log.cost_time > 100 }">{{ log.cost_time }}ms</td>
            <td class="mono small">{{ log.created_at }}</td>
          </tr>
        </tbody>
      </table>

      <!-- 登录日志 -->
      <table v-else class="table">
        <thead>
          <tr>
            <th>id</th>
            <th>user_id</th>
            <th>username</th>
            <th>login_type</th>
            <th>ip</th>
            <th>status</th>
            <th>message</th>
            <th>created_at</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in currentData" :key="log.id">
            <td>{{ log.id }}</td>
            <td>{{ log.user_id || '-' }}</td>
            <td>{{ log.username }}</td>
            <td>
              <span class="tag" :class="log.login_type === 1 ? 'primary' : 'info'">
                {{ log.login_type === 1 ? '后台管理员' : '买家' }}
              </span>
            </td>
            <td class="mono small">{{ log.ip }}</td>
            <td>
              <span class="tag" :class="log.status ? 'success' : 'danger'">
                {{ log.status ? '成功' : '失败' }}
              </span>
            </td>
            <td>{{ log.message }}</td>
            <td class="mono small">{{ log.created_at }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.log-page {
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
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.tabs {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
}

.tab {
  padding: 6px 14px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  font-family: ui-monospace, Consolas, monospace;
}

.tab.active {
  background: #fff;
  color: #667eea;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
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

.table tbody tr:hover {
  background: #f9fafb;
}

.mono {
  font-family: ui-monospace, Consolas, monospace;
}

.small {
  font-size: 12px;
  color: #9ca3af;
}

.slow {
  color: #f59e0b;
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
.tag.warn { background: #fef3c7; color: #b45309; }
.tag.danger { background: #fee2e2; color: #b91c1c; }

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
