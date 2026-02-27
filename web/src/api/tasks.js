import { api } from './client'

/**
 * 获取所有进行中的任务
 */
export function getActiveTasks() {
  return api.get('/tasks/active').then((r) => r.data)
}

/**
 * 根据关联ID获取任务列表
 */
export function getTasksByRelationId(relationId) {
  return api.get(`/tasks/relation/${relationId}`).then((r) => r.data)
}

/**
 * 查询任务（可选参数：taskType, taskStatus）
 */
export function getTasks(params = {}) {
  return api.get('/tasks', { params }).then((r) => r.data)
}
