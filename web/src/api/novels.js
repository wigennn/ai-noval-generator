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

/**
 * 导出小说章节（format: txt | md | docx），返回 Blob，需自行触发下载
 */
export function exportNovel(novelId, format = 'txt') {
  return api.get(`/novels/${novelId}/export`, {
    params: { format },
    responseType: 'blob'
  }).then((res) => {
    const disp = res.headers['content-disposition']
    let filename = `novel.${format}`
    if (disp) {
      const m = disp.match(/filename\*?=(?:UTF-8'')?([^;]+)/)
      if (m && m[1]) filename = decodeURIComponent(m[1].trim().replace(/^"|"$/g, ''))
    }
    return { blob: res.data, filename }
  })
}
