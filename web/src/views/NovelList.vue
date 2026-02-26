<template>
  <div class="novel-list-page">
    <div class="page-header">
      <h1>我的小说</h1>
      <router-link to="/novels/new" class="primary">创建小说</router-link>
    </div>
    <div v-if="loading" class="loading">加载中…</div>
    <div v-else-if="!list.length" class="empty">暂无小说，去创建一本吧。</div>
    <ul v-else class="novel-list">
      <li v-for="n in list" :key="n.id" class="card novel-item">
        <router-link :to="`/novels/${n.id}`" class="title">{{ n.title }}</router-link>
        <p class="meta">{{ n.genre || '未分类' }} · {{ statusText(n.status) }}</p>
        <div class="actions">
          <router-link :to="`/novels/${n.id}/edit`" class="secondary">编辑</router-link>
          <button type="button" class="danger" @click="onDelete(n)">删除</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as novels from '../api/novels'

const props = defineProps({ userId: { type: Number, default: 1 } })
const router = useRouter()
const list = ref([])
const loading = ref(false)

function load() {
  if (!props.userId) return
  loading.value = true
  novels.getNovelsByUser(props.userId).then((data) => {
    list.value = data || []
  }).finally(() => { loading.value = false })
}

watch(() => props.userId, load, { immediate: true })

function statusText(s) {
  const map = { 0: '草稿', 1: '发布中', 2: '已完成' }
  return map[s] ?? '未知'
}

function onDelete(n) {
  if (!confirm(`确定删除《${n.title}》？`)) return
  novels.deleteNovel(n.id).then(() => {
    list.value = list.value.filter((x) => x.id !== n.id)
  })
}
</script>

<style scoped>
.novel-list-page .page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.novel-list-page .page-header a.primary {
  text-decoration: none;
}
.loading,
.empty {
  color: var(--text-secondary);
  padding: 32px;
  text-align: center;
}
.novel-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.novel-item {
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto auto;
  gap: 8px 16px;
  align-items: center;
}
.novel-item .title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  grid-column: 1;
  grid-row: 1;
}
.novel-item .title:hover {
  color: var(--accent);
}
.novel-item .meta {
  font-size: 13px;
  color: var(--text-secondary);
  grid-column: 1;
  grid-row: 2;
}
.novel-item .actions {
  grid-column: 2;
  grid-row: 1 / -1;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
