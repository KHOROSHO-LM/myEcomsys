<script setup>
import { ref, computed } from 'vue'
import { authApi, setToken } from '../../api'

const emit = defineEmits(['login-success'])

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

// 简单的密码可见性切换
const showPassword = ref(false)

// 表单是否可提交（基本非空校验）
const canSubmit = computed(
  () => username.value.trim() && password.value.trim() && !loading.value
)

async function handleLogin() {
  errorMsg.value = ''
  if (!username.value.trim() || !password.value.trim()) {
    errorMsg.value = '请输入账号和密码'
    return
  }
  loading.value = true
  try {
    // 真实调用后端 /api/auth/login，返回 {token, username, nickname, expireMinutes}
    const data = await authApi.login({
      username: username.value.trim(),
      password: password.value,
    })
    setToken(data.token)
    emit('login-success', data.username || username.value.trim())
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <!-- 登录表单 -->
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0 0 20 4H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z" />
          </svg>
        </div>
        <h1>电商管理系统</h1>
        <p class="subtitle">ECMS Admin · 后台登录</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-item">
          <label>账号</label>
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
            <input
              v-model.trim="username"
              type="text"
              placeholder="请输入账号"
              autocomplete="username"
            />
          </div>
        </div>

        <div class="form-item">
          <label>密码</label>
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z" />
            </svg>
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
            <span
              class="toggle-eye"
              :title="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <svg v-if="showPassword" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-8a3 3 0 1 0 0 6 3 3 0 0 0 0-6z" />
              </svg>
              <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46A11.804 11.804 0 0 0 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65a3 3 0 0 0 3 3c.22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53a5 5 0 0 1-5-5c0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16a3 3 0 0 0-3-3l-.17.01z" />
              </svg>
            </span>
          </div>
        </div>

        <div v-if="errorMsg" class="error-msg">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          {{ errorMsg }}
        </div>

        <button type="submit" class="btn-login" :disabled="!canSubmit">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <div class="tip">
          测试账号：<strong>admin</strong>　密码：<strong>admin123</strong>
        </div>
      </form>
    </div>

    <p class="copyright">© 2026 ECMS 电商管理系统</p>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Microsoft YaHei', sans-serif;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: fade-in 0.4s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-header h1 {
  margin: 0;
  font-size: 22px;
  color: #1f2937;
  font-weight: 600;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #9ca3af;
  letter-spacing: 1px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #4b5563;
  font-weight: 500;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  color: #9ca3af;
  pointer-events: none;
}

.input-wrap input {
  width: 100%;
  height: 44px;
  padding: 0 42px 0 40px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2937;
  background: #f9fafb;
  transition: all 0.2s;
  box-sizing: border-box;
}

.input-wrap input:focus {
  outline: none;
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.input-wrap input::placeholder {
  color: #c0c4cc;
}

.toggle-eye {
  position: absolute;
  right: 10px;
  cursor: pointer;
  color: #9ca3af;
  display: flex;
  align-items: center;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s;
}

.toggle-eye:hover {
  color: #667eea;
}

.error-msg {
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

.btn-login {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  color: #fff;
  font-weight: 500;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 4px;
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.tip {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  padding: 8px;
  background: #f3f4f6;
  border-radius: 6px;
}

.tip strong {
  color: #667eea;
  font-weight: 600;
}

.copyright {
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
