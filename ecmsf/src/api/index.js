import axios from 'axios'

// localStorage key
const TOKEN_KEY = 'ecms_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

// 创建 axios 实例
const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动附带 Authorization 头
http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截器：统一解包 Result { code, message, data }，401 全局跳登录
http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data
      }
      // 业务层返回 401 → 触发全局未登录事件
      if (body.code === 401) {
        setToken('')
        window.dispatchEvent(new CustomEvent('unauthorized'))
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    // HTTP 401 → token 失效，触发跳登录
    if (error && error.response && error.response.status === 401) {
      setToken('')
      window.dispatchEvent(new CustomEvent('unauthorized'))
    }
    return Promise.reject(error)
  }
)

export default http

// ============ 鉴权 ============
export const authApi = {
  login: (body) => http.post('/auth/login', body),
  info: () => http.get('/auth/info'),
  logout: () => http.post('/auth/logout'),
}

// ============ 仪表盘 ============
export const dashboardApi = {
  getStats: () => http.get('/dashboard/stats'),
  getRecentOrders: () => http.get('/dashboard/recent-orders'),
  getCategorySales: () => http.get('/dashboard/category-sales'),
}

// ============ 商品 ============
export const productApi = {
  getCategories: () => http.get('/product/categories'),
  getList: () => http.get('/product/list'),
  getSkus: (productId) => http.get(`/product/${productId}/skus`),
  getImages: (productId) => http.get(`/product/${productId}/images`),
}

// ============ 订单 ============
export const orderApi = {
  getList: (status = 0) => http.get('/order/list', { params: { status } }),
  getItems: (orderId) => http.get(`/order/${orderId}/items`),
  getLogs: (orderId) => http.get(`/order/${orderId}/logs`),
}

// ============ 客户 ============
export const customerApi = {
  getList: () => http.get('/customer/list'),
  getLevels: () => http.get('/customer/levels'),
  getAddresses: (customerId) => http.get(`/customer/${customerId}/addresses`),
}

// ============ 营销 ============
export const marketingApi = {
  getCoupons: () => http.get('/marketing/coupons'),
  getCouponIssues: (couponId) => http.get(`/marketing/coupons/${couponId}/issues`),
  getSeckills: () => http.get('/marketing/seckills'),
}

// ============ 内容 ============
export const contentApi = {
  getBanners: () => http.get('/content/banners'),
  getNotices: () => http.get('/content/notices'),
}

// ============ 日志 ============
export const logApi = {
  getOperation: () => http.get('/log/operation'),
  getLogin: () => http.get('/log/login'),
}
