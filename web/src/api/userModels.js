import { api } from './client'

/**
 * 保存 API 设置
 */
export function saveApiSettings(userId, data) {
  return api.post(`/user-models/api-settings?userId=${userId}`, data).then((r) => r.data)
}

/**
 * 获取 API 设置
 */
export function getApiSettings(userId) {
  return api.get(`/user-models/api-settings?userId=${userId}`).then((r) => r.data)
}
