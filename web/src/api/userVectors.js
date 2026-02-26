import { api } from './client'

export function listByUser(userId) {
  return api.get(`/user-vectors/user/${userId}`).then((r) => r.data)
}

export function getById(id) {
  return api.get(`/user-vectors/${id}`).then((r) => r.data)
}

/** 上传资料库：名称 + 文本内容，后端会向量化并入库 */
export function upload(data) {
  return api.post('/user-vectors/upload', data).then((r) => r.data)
}

export function remove(id) {
  return api.delete(`/user-vectors/${id}`)
}
