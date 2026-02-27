<template>
  <div v-if="tasks.length > 0" class="task-progress" ref="taskProgressRef">
    <button 
      type="button" 
      class="task-progress-btn"
      @click.stop="showTasks = !showTasks"
      :class="{ 'has-tasks': tasks.length > 0, 'active': showTasks }"
    >
      <span class="task-icon">📋</span>
      <span class="task-count">{{ tasks.length }}</span>
      <span class="task-arrow" :class="{ 'expanded': showTasks }">▼</span>
    </button>
    
    <Transition name="fade">
      <div v-if="showTasks" class="task-list" @click.stop>
        <div class="task-list-header">
          <span class="task-list-title">进行中的任务</span>
          <button type="button" class="task-close-btn" @click="showTasks = false">×</button>
        </div>
        <div class="task-list-content">
          <div v-for="task in tasks" :key="task.id" class="task-item">
            <div class="task-header">
              <span class="task-name">{{ task.taskName }}</span>
              <span class="task-status" :class="getStatusClass(task.taskStatus)">
                {{ getStatusText(task.taskStatus) }}
              </span>
            </div>
            <div class="task-progress-bar">
              <div 
                class="task-progress-fill" 
                :class="getStatusClass(task.taskStatus)"
                :style="{ width: getProgressWidth(task.taskStatus) + '%' }"
              ></div>
            </div>
            <div class="task-time">
              {{ formatTime(task.createdAt) }}
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as tasksApi from '../api/tasks'

const tasks = ref([])
const showTasks = ref(false)
const taskProgressRef = ref(null)
let pollInterval = null

// 点击外部关闭下拉
function handleClickOutside(event) {
  if (taskProgressRef.value && !taskProgressRef.value.contains(event.target)) {
    showTasks.value = false
  }
}

function getStatusText(status) {
  const map = { 0: '待处理', 1: '处理中', 2: '完成' }
  return map[status] || '未知'
}

function getStatusClass(status) {
  const map = { 0: 'status-pending', 1: 'status-processing', 2: 'status-completed' }
  return map[status] || ''
}

function getProgressWidth(status) {
  const map = { 0: 20, 1: 60, 2: 100 }
  return map[status] || 0
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (seconds < 60) return `${seconds}秒前`
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return date.toLocaleDateString()
}

async function loadTasks() {
  try {
    const activeTasks = await tasksApi.getActiveTasks()
    tasks.value = activeTasks || []
    
    // 如果没有进行中的任务，自动关闭下拉
    if (tasks.value.length === 0) {
      showTasks.value = false
    }
  } catch (err) {
    console.error('Failed to load tasks:', err)
  }
}

function startPolling() {
  // 立即加载一次
  loadTasks()
  
  // 每3秒轮询一次
  pollInterval = setInterval(() => {
    loadTasks()
  }, 3000)
}

function stopPolling() {
  if (pollInterval) {
    clearInterval(pollInterval)
    pollInterval = null
  }
}

onMounted(() => {
  startPolling()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  stopPolling()
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.task-progress {
  position: relative;
  margin-right: 8px;
}

.task-progress-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.task-progress-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
  border-color: var(--accent);
}

.task-progress-btn.has-tasks {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-light);
}

.task-progress-btn.active {
  background: var(--accent);
  color: white;
}

.task-icon {
  font-size: 16px;
}

.task-count {
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

.task-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.task-arrow.expanded {
  transform: rotate(180deg);
}

.task-list {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 320px;
  max-width: 400px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  overflow: hidden;
}

.task-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-secondary);
}

.task-list-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.task-close-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 20px;
  line-height: 1;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.task-close-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.task-list-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}

.task-item {
  padding: 12px;
  border-bottom: 1px solid var(--border);
}

.task-item:last-child {
  border-bottom: none;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
}

.task-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-pending {
  color: #f59e0b;
  background: #fef3c7;
}

.status-processing {
  color: #3b82f6;
  background: #dbeafe;
}

.status-completed {
  color: #10b981;
  background: #d1fae5;
}

.task-progress-bar {
  width: 100%;
  height: 4px;
  background: var(--bg-secondary);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 6px;
}

.task-progress-fill {
  height: 100%;
  transition: width 0.3s;
  border-radius: 2px;
}

.task-progress-fill.status-pending {
  background: #f59e0b;
}

.task-progress-fill.status-processing {
  background: #3b82f6;
}

.task-progress-fill.status-completed {
  background: #10b981;
}

.task-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
