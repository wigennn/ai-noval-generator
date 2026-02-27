<template>
  <div class="home">
    <p class="hero-tagline">基于RAG的AI创作工具</p>
    <h1 class="hero-title">AI 小说生成器</h1>
<!--    <p class="hero-desc">AI生成小说章节大纲，让创作更高效</p>-->
    <button type="button" class="btn-create" @click="openCreateModal">
      <span class="btn-create-icon">+</span>
      创建新项目
    </button>

    <div class="project-card">
      <template v-if="loading">
        <div class="empty-state">加载中…</div>
      </template>
      <template v-else-if="!list.length">
        <div class="empty-icon">📄</div>
        <p class="empty-title">还没有项目</p>
        <p class="empty-hint">点击上方按钮创建你的第一个小说项目</p>
      </template>
      <template v-else>
        <ul class="project-list">
          <li v-for="n in list" :key="n.id" class="project-item">
            <router-link :to="`/novels/${n.id}`" class="project-link">
              <span class="project-name">{{ n.title }}</span>
              <span class="project-meta">{{ n.genre || '未分类' }} · {{ statusText(n.status) }}</span>
            </router-link>
            <div class="project-actions">
              <router-link :to="`/novels/${n.id}/edit`" class="btn-edit">编辑</router-link>
              <button type="button" class="btn-delete" @click.stop="onDelete(n)">删除</button>
            </div>
          </li>
        </ul>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, inject } from 'vue'
import { useRouter } from 'vue-router'
import * as novels from '../api/novels'

const userId = inject('userId', ref(1))
const openCreateModal = inject('openCreateModal', () => {})
const router = useRouter()

const list = ref([])
const loading = ref(false)

function load() {
  if (!userId?.value) return
  loading.value = true
  novels.getNovelsByUser(userId.value).then((data) => {
    list.value = data || []
  }).finally(() => { loading.value = false })
}

watch(userId, load, { immediate: true })

function statusText(s) {
  const map = { 0: '草稿', 1: '发布中', 2: '已完成' }
  return map[s] ?? '未知'
}

function onDelete(n) {
  if (!confirm(`确定删除《${n.title}》？此操作将删除所有相关数据，且无法恢复。`)) return
  novels.deleteNovel(n.id).then(() => {
    list.value = list.value.filter((x) => x.id !== n.id)
  }).catch((err) => {
    console.error('删除失败:', err)
    alert('删除失败，请稍后重试')
  })
}
</script>

<style scoped>
.home {
  text-align: center;
  padding: 0 16px;
}
.hero-tagline {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.hero-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.hero-desc {
  font-size: 15px;
  color: var(--text-secondary);
  max-width: 480px;
  margin: 0 auto 32px;
  line-height: 1.6;
}
.btn-create {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  font-size: 16px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  margin-bottom: 32px;
}
.btn-create-icon {
  font-size: 20px;
  line-height: 1;
}
.project-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 48px 24px;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}
.empty-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.empty-hint {
  font-size: 14px;
  color: var(--text-secondary);
}
.project-list {
  list-style: none;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  text-align: left;
}
.project-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
  padding: 16px 0;
}
.project-item:last-child {
  border-bottom: none;
}
.project-link {
  flex: 1;
  text-decoration: none;
  color: var(--text-primary);
}
.project-link:hover {
  color: var(--accent);
}
.project-name {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
}
.project-meta {
  font-size: 13px;
  color: var(--text-secondary);
}
.project-actions {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}
.btn-edit,
.btn-delete {
  padding: 6px 12px;
  font-size: 13px;
  border-radius: var(--radius-sm);
  border: none;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
}
.btn-edit {
  background: var(--bg-secondary);
  color: var(--text-primary);
}
.btn-edit:hover {
  background: var(--bg-hover);
}
.btn-delete {
  background: transparent;
  color: var(--danger);
  border: 1px solid var(--danger);
}
.btn-delete:hover {
  background: var(--danger);
  color: white;
}
</style>
