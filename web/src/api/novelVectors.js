import { api } from './client'

export function listByNovel(novelId) {
  return api.get(`/novel-vectors/novel/${novelId}`).then((r) => r.data)
}

export function add(novelId, vectorId) {
  return api.post('/novel-vectors', { novelId, vectorId }).then((r) => r.data)
}

export function remove(id) {
  return api.delete(`/novel-vectors/${id}`)
}
