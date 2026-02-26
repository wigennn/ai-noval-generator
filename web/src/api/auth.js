import { api } from './client'

/**
 * 发送邮箱验证码
 */
export function sendCode(email) {
  return api.post('/auth/send-code', { email })
}

/**
 * 验证码登录
 */
export function loginByCode(email, code) {
  return api.post('/auth/login-by-code', { email, code }).then((r) => r.data)
}

/**
 * 邮箱 + 密码登录
 */
export function login(email, password) {
  return api.post('/auth/login', { email, password }).then((r) => r.data)
}

/**
 * 邮箱注册
 */
export function register(data) {
  return api.post('/auth/register', data).then((r) => r.data)
}

/**
 * 获取当前登录用户
 */
export function me() {
  return api.get('/auth/me').then((r) => r.data)
}

/**
 * 退出登录
 */
export function logout() {
  return api.post('/auth/logout')
}
