<template>
  <div class="novel-detail-page">
    <div class="top-bar">
      <router-link to="/" class="back-link">
        <span class="back-arrow">←</span>
        返回
      </router-link>
    </div>

    <div v-if="loading" class="loading">加载中…</div>
    <template v-else-if="novel">
      <h1 class="novel-title">{{ novel.title }}</h1>
      <p class="novel-stats">总计 {{ projectMeta.estimatedChapters }} 章 · 每章 {{ projectMeta.wordsPerChapter }} 字</p>

      <div class="tabs">
        <button type="button" :class="['tab', { active: activeTab === 'structure' }]" @click="activeTab = 'structure'">
          <span class="tab-icon">▦</span>
          小说架构
        </button>
        <button type="button" :class="['tab', { active: activeTab === 'outline' }]" @click="activeTab = 'outline'">
          <span class="tab-icon">☰</span>
          章节大纲
        </button>
        <button type="button" :class="['tab', { active: activeTab === 'writing' }]" @click="activeTab = 'writing'">
          <span class="tab-icon">✎</span>
          章节写作
        </button>
        <button type="button" :class="['tab', { active: activeTab === 'export' }]" @click="activeTab = 'export'">
          导出
        </button>
      </div>

      <!-- 小说架构 Tab -->
      <section v-show="activeTab === 'structure'" class="content-panel card">
        <div v-if="!novel.structure" class="generate-block">
          <div class="generate-icon">✨</div>
          <h2 class="generate-title">开始生成小说架构</h2>
          <p class="generate-desc">AI 将基于作品创作法，为你生成核心种子、角色体系、世界观和情节架构</p>
          <button type="button" class="btn-generate" @click="startGenerateStructure">
            <span class="btn-play">▷</span>
            开始生成架构
          </button>
        </div>
        <div v-else class="structure-content">
          <h2 class="section-title">小说结构</h2>
          <pre class="structure-text">{{ novel.structure }}</pre>
        </div>
      </section>

      <!-- 章节大纲 Tab -->
      <section v-show="activeTab === 'outline'" class="content-panel card">
        <p class="hint">章节大纲内容（待实现）</p>
      </section>

      <!-- 章节写作 Tab -->
      <section v-show="activeTab === 'writing'" class="content-panel card">
        <h2 class="section-title">章节列表</h2>
        <div v-if="!chapters.length" class="hint">暂无章节</div>
        <ul v-else class="chapter-list">
          <li v-for="ch in chapters" :key="ch.id" class="chapter-item">
            <span class="num">第 {{ ch.chapterNumber }} 章</span>
            <span class="title">{{ ch.title || '（无标题）' }}</span>
            <span class="status">{{ chapterStatusText(ch.status) }}</span>
          </li>
        </ul>
      </section>

      <!-- 导出 Tab -->
      <section v-show="activeTab === 'export'" class="content-panel card">
        <p class="hint">导出功能（待实现）</p>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import * as novels from '../api/novels'
import * as chaptersApi from '../api/chapters'

const route = useRoute()
const id = computed(() => route.params.id)
const novel = ref(null)
const loading = ref(false)
const chapters = ref([])
const activeTab = ref('structure')

const projectMeta = computed(() => {
  if (!novel.value?.id) return { estimatedChapters: 100, wordsPerChapter: 3000 }
  try {
    const raw = localStorage.getItem(`novel_meta_${novel.value.id}`)
    const data = raw ? JSON.parse(raw) : {}
    return {
      estimatedChapters: data.estimatedChapters ?? 100,
      wordsPerChapter: data.wordsPerChapter ?? 3000
    }
  } catch (_) {
    return { estimatedChapters: 100, wordsPerChapter: 3000 }
  }
})

function load() {
  if (!id.value) return
  loading.value = true
  Promise.all([
    novels.getNovel(id.value),
    chaptersApi.getByNovel(id.value)
  ]).then(([n, chs]) => {
    novel.value = n
    chapters.value = chs || []
  }).finally(() => { loading.value = false })
}

watch(id, load, { immediate: true })

function chapterStatusText(s) {
  const map = { 0: '待处理', 1: '处理中', 2: '完成' }
  return map[s] ?? '未知'
}

function startGenerateStructure() {
  // 后端已有异步生成结构逻辑，这里仅提示或轮询
  // 实际可调用接口或轮询 novel 的 structure 字段
}
</script>

<style scoped>
.novel-detail-page {
  padding: 0 8px;
}
.top-bar {
  margin-bottom: 16px;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
}
.back-link:hover {
  color: var(--accent);
}
.back-arrow {
  font-size: 18px;
}
.novel-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.novel-stats {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}
.tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--border);
}
.tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  font-size: 14px;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  border-radius: 0;
}
.tab:hover {
  color: var(--text-primary);
}
.tab.active {
  color: var(--accent);
  font-weight: 500;
  border-bottom-color: var(--accent);
}
.tab-icon {
  font-size: 16px;
}
.content-panel {
  padding: 32px;
  min-height: 320px;
}
.generate-block {
  text-align: center;
  padding: 24px 0;
}
.generate-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: var(--accent-light);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}
.generate-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.generate-desc {
  font-size: 14px;
  color: var(--text-secondary);
  max-width: 400px;
  margin: 0 auto 24px;
  line-height: 1.6;
}
.btn-generate {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  font-size: 16px;
  border-radius: var(--radius-sm);
}
.btn-play {
  font-size: 14px;
}
.structure-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  max-height: 400px;
  overflow: auto;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}
.hint {
  color: var(--text-secondary);
  font-size: 14px;
}
.chapter-list {
  list-style: none;
}
.chapter-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}
.chapter-item .num {
  color: var(--text-secondary);
  min-width: 80px;
}
.chapter-item .title {
  flex: 1;
}
.chapter-item .status {
  font-size: 12px;
  color: var(--text-secondary);
}
.loading {
  color: var(--text-secondary);
  padding: 48px;
  text-align: center;
}
</style>
