import { api } from './client'

export function getNovelsByUser(userId) {
  return api.get(`/novels/user/${userId}`).then((r) => r.data)
}

export function getNovel(id) {
  return api.get(`/novels/${id}`).then((r) => r.data)
}

export function createNovel(data) {
  return api.post('/novels', data).then((r) => r.data)
}

export function updateNovel(id, data) {
  return api.put(`/novels/${id}`, data).then((r) => r.data)
}

export function deleteNovel(id) {
  return api.delete(`/novels/${id}`)
}
