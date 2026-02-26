import { api } from './client'

export function getByNovel(novelId) {
  return api.get(`/chapters/novel/${novelId}`).then((r) => r.data)
}

export function getById(id) {
  return api.get(`/chapters/${id}`).then((r) => r.data)
}

/**
 * 生成章节（async: true=异步 MQ 生成，false=实时生成）
 */
export function generateChapter(data) {
  return api.post('/chapters', {
    novelId: data.novelId,
    chapterNumber: data.chapterNumber,
    title: data.title,
    abstractContent: data.abstractContent || undefined,
    async: data.async !== false
  }).then((r) => r.data)
}
