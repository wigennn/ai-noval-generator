<template>
  <div class="library-page">
    <div class="page-header">
      <h1>资料库</h1>
      <p class="sub">上传设定或参考文档，写小说时可选择关联到某本小说。</p>
    </div>

    <section class="card form-section">
      <h2 class="section-title">上传新资料库</h2>
      <form @submit.prevent="onUpload" class="upload-form">
        <div class="field">
          <label class="label">资料库名称</label>
          <input v-model="form.name" required placeholder="例如：角色设定、世界观参考" />
        </div>
        <div class="field">
          <label class="label">资料内容（文本）</label>
          <textarea v-model="form.content" required placeholder="粘贴或输入要入库的文本，将自动向量化存储" rows="6"></textarea>
        </div>
        <button type="submit" class="primary" :disabled="uploading">
          {{ uploading ? '上传中…' : '上传并入库' }}
        </button>
      </form>
    </section>

    <section class="list-section">
      <h2 class="section-title">我的资料库</h2>
      <div v-if="loading" class="loading">加载中…</div>
      <div v-else-if="!list.length" class="empty">暂无资料库，请先上传。</div>
      <ul v-else class="vector-list">
        <li v-for="item in list" :key="item.id" class="card vector-item">
          <div class="info">
            <span class="name">{{ item.vectorName }}</span>
            <span class="meta">ID: {{ item.vectorId.slice(0, 8) }}… · {{ formatDate(item.createdAt) }}</span>
          </div>
          <button type="button" class="danger" @click="onDelete(item)">删除</button>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import * as userVectors from '../api/userVectors'

const props = defineProps({ userId: { type: Number, default: 1 } })

const list = ref([])
const loading = ref(false)
const uploading = ref(false)
const form = ref({ name: '', content: '' })

function load() {
  if (!props.userId) return
  loading.value = true
  userVectors.listByUser(props.userId).then((data) => {
    list.value = data || []
  }).finally(() => { loading.value = false })
}

watch(() => props.userId, load, { immediate: true })

function formatDate(v) {
  if (!v) return '-'
  const d = new Date(v)
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function onUpload() {
  if (!props.userId || uploading.value) return
  uploading.value = true
  try {
    await userVectors.upload({
      userId: props.userId,
      vectorName: form.value.name.trim(),
      content: form.value.content.trim()
    })
    form.value = { name: '', content: '' }
    load()
  } finally {
    uploading.value = false
  }
}

function onDelete(item) {
  if (!confirm(`确定删除资料库「${item.vectorName}」？`)) return
  userVectors.remove(item.id).then(load)
}
</script>

<style scoped>
.library-page .sub {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 4px;
}
.form-section {
  margin-bottom: 32px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}
.upload-form .field {
  margin-bottom: 16px;
}
.upload-form .field:last-of-type {
  margin-bottom: 20px;
}
.list-section .loading,
.list-section .empty {
  color: var(--text-secondary);
  padding: 24px;
  text-align: center;
}
.vector-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.vector-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.vector-item .info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.vector-item .name {
  font-weight: 500;
  color: var(--text-primary);
}
.vector-item .meta {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
