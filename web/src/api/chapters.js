import { api } from './client'

export function getByNovel(novelId) {
  return api.get(`/chapters/novel/${novelId}`).then((r) => r.data)
}

export function getById(id) {
  return api.get(`/chapters/${id}`).then((r) => r.data)
}
