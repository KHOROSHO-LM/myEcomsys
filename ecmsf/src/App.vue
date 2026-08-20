<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Login from './views/login/Login.vue'
import Home from './views/home/Home.vue'
import { authApi, getToken, setToken } from './api'

const loggedIn = ref(false)
const currentUser = ref('')
const booting = ref(true)

function handleLoginSuccess(username) {
  currentUser.value = username
  loggedIn.value = true
}

function handleLogout() {
  // 通知后端记录登出日志（失败不阻断前端退出）
  authApi.logout().catch(() => {})
  setToken('')
  loggedIn.value = false
  currentUser.value = ''
}

// 监听全局 401 事件（响应拦截器在 token 失效时派发）
function onUnauthorized() {
  setToken('')
  loggedIn.value = false
  currentUser.value = ''
}

onMounted(async () => {
  // 启动时若本地有 token，调 /api/auth/info 校验是否仍有效
  const token = getToken()
  if (token) {
    try {
      const info = await authApi.info()
      if (info && info.username) {
        currentUser.value = info.username
        loggedIn.value = true
      }
    } catch (e) {
      // token 已失效，清掉
      setToken('')
    }
  }
  booting.value = false
  window.addEventListener('unauthorized', onUnauthorized)
})

onUnmounted(() => {
  window.removeEventListener('unauthorized', onUnauthorized)
})
</script>

<template>
  <div v-if="booting" class="boot-loading">加载中...</div>
  <Home v-else-if="loggedIn" :username="currentUser" @logout="handleLogout" />
  <Login v-else @login-success="handleLoginSuccess" />
</template>

<style>
.boot-loading {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 14px;
}
</style>
