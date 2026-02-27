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
        <div v-if="!novel.structure && !streamingStructure" class="generate-block">
          <div class="generate-icon">✨</div>
          <h2 class="generate-title">开始生成小说架构</h2>
          <p class="generate-desc">AI 将基于作品创作法，为你生成核心种子、角色体系、世界观和情节架构</p>
          <button type="button" class="btn-generate" :disabled="generatingStructure" @click="startStreamStructure">
            <span class="btn-play">▷</span>
            {{ generatingStructure ? '生成中...' : '开始生成架构' }}
          </button>
        </div>
        <div v-else-if="streamingStructure" class="structure-content">
          <div class="structure-header">
            <h2 class="section-title">正在生成小说架构...</h2>
            <button type="button" class="btn-stop" @click="stopStreamStructure">
              <span>⏹</span>
              停止生成
            </button>
          </div>
          <div class="streaming-content">
            <div class="structure-text rich-text" v-html="asHtml(streamingStructure)"></div>
            <div class="streaming-indicator">
              <span class="typing-cursor">▊</span>
            </div>
          </div>
        </div>
        <div v-else class="structure-content">
          <div class="structure-header">
            <h2 class="section-title">小说结构</h2>
            <button type="button" class="btn-regenerate" :disabled="generatingStructure" @click="startStreamStructure">
              {{ generatingStructure ? '生成中...' : '重新生成' }}
            </button>
          </div>
          <div class="structure-text rich-text" v-html="asHtml(novel.structure)"></div>
        </div>
      </section>

      <!-- 章节大纲 Tab -->
      <section v-show="activeTab === 'outline'" class="content-panel card">
        <div v-if="!novel.chapterOutline && !streamingOutline" class="generate-block">
          <div class="generate-icon">📋</div>
          <h2 class="generate-title">开始生成章节大纲</h2>
          <p class="generate-desc">AI 将基于小说架构，为你生成详细的章节大纲，包含每章的核心事件、出场角色和关键场景</p>
          <div v-if="!novel.structure" class="warning-box">
            <p>⚠️ 请先完成「小说架构」的生成，才能生成章节大纲</p>
          </div>
          <button 
            type="button" 
            class="btn-generate" 
            :disabled="generatingOutline || !novel.structure" 
            @click="startStreamOutline">
            <span class="btn-play">▷</span>
            {{ generatingOutline ? '生成中...' : '开始生成大纲' }}
          </button>
        </div>
        <div v-else-if="streamingOutline" class="outline-content">
          <div class="outline-header">
            <h2 class="section-title">正在生成章节大纲...</h2>
            <button type="button" class="btn-stop" @click="stopStreamOutline">
              <span>⏹</span>
              停止生成
            </button>
          </div>
          <div class="streaming-content">
            <div class="outline-text rich-text" v-html="asHtml(streamingOutline)"></div>
            <div class="streaming-indicator">
              <span class="typing-cursor">▊</span>
            </div>
          </div>
        </div>
        <div v-else class="outline-content">
          <div class="outline-header">
            <h2 class="section-title">章节大纲</h2>
            <div class="outline-actions">
              <button type="button" class="btn-continue" :disabled="generatingOutline" @click="startStreamOutline(true)">
                {{ generatingOutline ? '生成中...' : '继续生成' }}
              </button>
              <button type="button" class="btn-regenerate" :disabled="generatingOutline" @click="startStreamOutline(false)">
                {{ generatingOutline ? '生成中...' : '重新生成' }}
              </button>
            </div>
          </div>
          <div class="outline-text rich-text" v-html="asHtml(novel.chapterOutline)"></div>
        </div>
      </section>

      <!-- 章节写作 Tab -->
      <section v-show="activeTab === 'writing'" class="content-panel card">
        <div class="writing-header">
          <h2 class="section-title">章节列表</h2>
          <button 
            type="button" 
            class="btn-add-chapter" 
            @click="showAddChapterModal = true"
            :disabled="!novel.structure">
            <span>+</span>
            添加章节
          </button>
        </div>
        
        <div v-if="!chapters.length" class="hint">暂无章节，点击「添加章节」开始创作</div>
        <ul v-else class="chapter-list">
          <li v-for="ch in chapters" :key="ch.id" class="chapter-item" @click="openChapterDetail(ch)">
            <span class="num">第 {{ ch.chapterNumber }} 章</span>
            <span class="title">{{ ch.title || '（无标题）' }}</span>
            <span class="status" :class="getStatusClass(ch.status)">
              {{ chapterStatusText(ch.status) }}
            </span>
            <button 
              v-if="ch.status === 0 || ch.status === 1"
              type="button" 
              class="btn-generate-chapter"
              @click.stop="openChapterDetail(ch)"
              :disabled="streamingChapters.has(ch.id)">
              {{ streamingChapters.has(ch.id) ? '生成中...' : '生成内容' }}
            </button>
          </li>
        </ul>


        <!-- 章节详情/编辑 Modal -->
        <div v-if="selectedChapter" class="chapter-detail-modal" @click.self="closeChapterDetail">
          <div class="chapter-detail-content">
            <div class="chapter-detail-header">
              <h3>第 {{ selectedChapter.chapterNumber }} 章 {{ selectedChapter.title || '（无标题）' }}</h3>
              <div class="chapter-detail-actions">
                <button 
                  v-if="chapterDetailExportableText"
                  type="button" 
                  class="btn-export-txt"
                  @click="exportChapterTxt">
                  📄 导出 TXT
                </button>
                <button type="button" class="btn-close" @click="closeChapterDetail">×</button>
              </div>
            </div>
            <div class="chapter-detail-body">
              <!-- 正在生成中（状态为0或1，且有流式内容） -->
              <div v-if="(selectedChapter.status === 0 || selectedChapter.status === 1) && streamingContent[selectedChapter.id]" class="chapter-generating">
                <div class="chapter-detail-header">
                  <h3>正在生成章节内容...</h3>
                  <button type="button" class="btn-stop" @click="stopStreamChapter(selectedChapter)">
                    <span>⏹</span>
                    停止生成
                  </button>
                </div>
                <div class="streaming-content">
                  <div class="chapter-text rich-text" v-html="asHtml(streamingContent[selectedChapter.id])"></div>
                  <div class="streaming-indicator">
                    <span class="typing-cursor">▊</span>
                  </div>
                </div>
              </div>
              <!-- 待生成或生成中但无流式内容 -->
              <div v-else-if="selectedChapter.status === 0 || selectedChapter.status === 1" class="generate-prompt">
                <p>章节内容尚未生成</p>
                <button 
                  type="button" 
                  class="btn-generate-in-modal"
                  @click="startStreamChapter(selectedChapter)"
                  :disabled="streamingChapters.has(selectedChapter.id)">
                  {{ streamingChapters.has(selectedChapter.id) ? '生成中...' : '开始生成' }}
                </button>
              </div>
              <!-- 已完成 -->
              <div v-else class="chapter-content">
                <div class="chapter-text rich-text" v-html="asHtml(selectedChapter.content || '（无内容）')"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 添加章节 Modal -->
        <div v-if="showAddChapterModal" class="chapter-detail-modal" @click.self="showAddChapterModal = false">
          <div class="chapter-detail-content">
            <div class="chapter-detail-header">
              <h3>添加新章节</h3>
              <button type="button" class="btn-close" @click="showAddChapterModal = false">×</button>
            </div>
            <div class="chapter-detail-body">
              <div class="field">
                <label class="label">章节序号</label>
                <input 
                  v-model.number="newChapter.chapterNumber" 
                  type="number" 
                  min="1" 
                  placeholder="例如：1" />
              </div>
              <div class="field">
                <label class="label">章节标题</label>
                <input 
                  v-model="newChapter.title" 
                  placeholder="例如：初入江湖" />
              </div>
              <div class="field">
                <label class="label">章节摘要（可选）</label>
                <textarea 
                  v-model="newChapter.abstractContent" 
                  rows="3"
                  placeholder="简要描述本章的核心事件..."></textarea>
              </div>
              <div class="field">
                <button 
                  type="button" 
                  class="btn-primary"
                  @click="createChapter"
                  :disabled="!newChapter.chapterNumber || !newChapter.title || creatingChapter">
                  {{ creatingChapter ? '创建中...' : '创建章节' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 导出 Tab -->
      <section v-show="activeTab === 'export'" class="content-panel card">
        <h2 class="section-title">导出章节</h2>
        <p class="export-desc">将当前小说的全部章节导出为文件，支持以下格式：</p>
        <div class="export-buttons">
          <button type="button" class="btn-export" :disabled="exporting || !chapters.length" @click="doExport('txt')">
            <span class="btn-export-icon">📄</span>
            导出为 TXT
          </button>
          <button type="button" class="btn-export" :disabled="exporting || !chapters.length" @click="doExport('md')">
            <span class="btn-export-icon">📝</span>
            导出为 Markdown
          </button>
          <button type="button" class="btn-export" :disabled="exporting || !chapters.length" @click="doExport('docx')">
            <span class="btn-export-icon">📘</span>
            导出为 Word
          </button>
        </div>
        <p v-if="!chapters.length" class="hint">暂无章节，请先在「章节写作」中生成章节后再导出。</p>
        <p v-else class="hint">共 {{ chapters.length }} 章，导出后将包含所有章节标题与正文。</p>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as novels from '../api/novels'
import * as chaptersApi from '../api/chapters'
import { connectWebSocket, disconnectWebSocket, getWebSocketClient } from '../utils/websocket'
import { renderMarkdown } from '../utils/markdown'

const route = useRoute()
const id = computed(() => route.params.id)
const novel = ref(null)
const loading = ref(false)
const chapters = ref([])
const activeTab = ref('structure')
const exporting = ref(false)
const generatingStructure = ref(false)
const generatingOutline = ref(false)
const streamingStructure = ref('')
const streamingOutline = ref('')

// 章节写作相关
const showAddChapterModal = ref(false)
const selectedChapter = ref(null)
const streamingChapters = ref(new Set())
const streamingContent = ref({})
const creatingChapter = ref(false)
const newChapter = ref({
  chapterNumber: null,
  title: '',
  abstractContent: ''
})

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

// 富文本渲染辅助
function asHtml(text) {
  return renderMarkdown(text || '')
}

// 根据章节大纲解析章节标题
const outlineChapters = computed(() => {
  if (!novel.value?.chapterOutline) return []
  const lines = novel.value.chapterOutline.split('\n')
  const result = []
  const chapterRegex = /^##\s*第(\d+)章\s*(.*)$/
  for (const line of lines) {
    const m = line.match(chapterRegex)
    if (m) {
      const num = parseInt(m[1], 10)
      const title = m[2]?.trim().replace(/^\[|\]$/g, '') || ''
      result.push({ chapterNumber: num, title })
    }
  }
  return result
})

// 章节详情中可用于导出 TXT 的正文（流式内容或已保存内容）
const chapterDetailExportableText = computed(() => {
  if (!selectedChapter.value) return ''
  const ch = selectedChapter.value
  const streamed = streamingContent.value[ch.id]
  if (streamed && streamed.trim()) return streamed
  if (ch.content && ch.content.trim()) return ch.content
  return ''
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

// 监听 activeTab，切换到需要 WebSocket 的 tab 时初始化
watch(activeTab, (newTab) => {
  if (newTab === 'writing' || newTab === 'structure' || newTab === 'outline') {
    initWebSocket()
  }
})

onMounted(() => {
  if (activeTab.value === 'writing' || activeTab.value === 'structure' || activeTab.value === 'outline') {
    initWebSocket()
  }
})

onUnmounted(() => {
  if (wsSubscription) {
    wsSubscription.unsubscribe()
  }
  disconnectWebSocket()
})

function chapterStatusText(s) {
  const map = { 0: '待处理', 1: '处理中', 2: '完成' }
  return map[s] ?? '未知'
}

function getStatusClass(s) {
  const map = { 0: 'status-pending', 1: 'status-processing', 2: 'status-completed' }
  return map[s] || ''
}

function findChapterByNumber(chapterNumber) {
  return chapters.value.find((c) => c.chapterNumber === chapterNumber)
}

// WebSocket 相关
let wsSubscription = null

async function initWebSocket() {
  try {
    await connectWebSocket()
  } catch (err) {
    console.error('Failed to connect WebSocket:', err)
  }
}

async function subscribeChapterStream(novelId, chapterNumber, chapterId) {
  const client = getWebSocketClient()
  if (!client || !client.connected) {
    console.warn('WebSocket not connected')
    return
  }

  const topic = `/topic/chapters/${novelId}/${chapterNumber}`
  
  // 取消之前的订阅
  if (wsSubscription) {
    wsSubscription.unsubscribe()
  }

  wsSubscription = client.subscribe(topic, async (message) => {
    const payload = JSON.parse(message.body)
    const { type, content } = payload

    if (type === 'delta') {
      // 增量内容
      if (!streamingContent.value[chapterId]) {
        streamingContent.value[chapterId] = ''
      }
      streamingContent.value[chapterId] += content
      
      // 如果章节详情已打开，自动滚动到底部
      if (selectedChapter.value && selectedChapter.value.id === chapterId) {
        nextTick(() => {
          const textEl = document.querySelector('.streaming-content .chapter-text')
          if (textEl) {
            textEl.scrollTop = textEl.scrollHeight
          }
        })
      }
    } else if (type === 'complete') {
      // 生成完成
      streamingChapters.value.delete(chapterId)
      // 清除流式内容，等待加载最终内容
      delete streamingContent.value[chapterId]
      // 重新加载章节列表和详情
      await loadChapters()
      if (selectedChapter.value && selectedChapter.value.id === chapterId) {
        await loadChapterDetail(chapterId)
      }
    } else if (type === 'stopped') {
      // 生成已停止
      streamingChapters.value.delete(chapterId)
      // 保留已生成的内容，不清空
      if (selectedChapter.value && selectedChapter.value.id === chapterId) {
        // 更新章节状态
        await loadChapterDetail(chapterId)
      }
    } else if (type === 'error') {
      // 生成错误
      streamingChapters.value.delete(chapterId)
      delete streamingContent.value[chapterId]
      alert('生成失败：' + (content || '未知错误'))
    }
  })
}

function startStreamChapter(chapter) {
  if (!novel.value || !chapter || streamingChapters.value.has(chapter.id)) return
  
  const client = getWebSocketClient()
  if (!client || !client.connected) {
    alert('WebSocket 未连接，请刷新页面重试')
    return
  }

  streamingChapters.value.add(chapter.id)
  streamingContent.value[chapter.id] = ''
  
  // 订阅流
  subscribeChapterStream(novel.value.id, chapter.chapterNumber, chapter.id)
  
  // 打开章节详情
  if (!selectedChapter.value || selectedChapter.value.id !== chapter.id) {
    selectedChapter.value = { ...chapter }
  }

  // 发送生成请求
  client.publish({
    destination: '/app/chapters/stream',
    body: JSON.stringify({
      novelId: novel.value.id,
      chapterNumber: chapter.chapterNumber,
      title: chapter.title,
      abstractContent: chapter.abstractContent
    })
  })
}

async function createChapter() {
  if (!id.value || !newChapter.value.chapterNumber || !newChapter.value.title) return
  if (creatingChapter.value) return

  creatingChapter.value = true
  try {
    // 先创建章节（通过API，但不生成内容）
    // 注意：后端会创建章节并触发MQ生成，但我们可以通过WebSocket覆盖这个行为
    const chapter = await chaptersApi.generateChapter({
      novelId: id.value,
      chapterNumber: newChapter.value.chapterNumber,
      title: newChapter.value.title,
      abstractContent: newChapter.value.abstractContent || undefined,
      async: true // 使用异步MQ，但我们会立即通过WebSocket流式生成
    })
    
    showAddChapterModal.value = false
    newChapter.value = { chapterNumber: null, title: '', abstractContent: '' }
    await loadChapters()
    
    // 找到刚创建的章节，自动开始流式生成
    const createdChapter = chapters.value.find(c => c.id === chapter.id)
    if (createdChapter) {
      startStreamChapter(createdChapter)
    }
  } catch (err) {
    console.error('Failed to create chapter:', err)
    // 如果章节已存在，直接通过WebSocket生成
    if (err.response?.status === 500 || err.message?.includes('already exists')) {
      // 章节可能已存在，直接通过WebSocket触发流式生成
      const chapter = {
        id: null, // 后端会自动创建
        novelId: id.value,
        chapterNumber: newChapter.value.chapterNumber,
        title: newChapter.value.title,
        abstractContent: newChapter.value.abstractContent
      }
      showAddChapterModal.value = false
      newChapter.value = { chapterNumber: null, title: '', abstractContent: '' }
      await loadChapters()
      startStreamChapter(chapter)
    } else {
      alert('创建章节失败，请稍后重试')
    }
  } finally {
    creatingChapter.value = false
  }
}

async function loadChapters() {
  if (!id.value) return
  try {
    const chs = await chaptersApi.getByNovel(id.value)
    chapters.value = chs || []
  } catch (err) {
    console.error('Failed to load chapters:', err)
  }
}

async function loadChapterDetail(chapterId) {
  try {
    const chapter = await chaptersApi.getById(chapterId)
    if (selectedChapter.value && selectedChapter.value.id === chapterId) {
      selectedChapter.value = chapter
    }
  } catch (err) {
    console.error('Failed to load chapter detail:', err)
  }
}

function openChapterDetail(chapter) {
  selectedChapter.value = { ...chapter }
  // 如果正在生成，订阅流式内容
  if (streamingChapters.value.has(chapter.id)) {
    subscribeChapterStream(novel.value.id, chapter.chapterNumber, chapter.id)
  } else {
    // 加载章节详情（含 content），支持内容查看
    loadChapterDetail(chapter.id)
  }
}

function closeChapterDetail() {
  selectedChapter.value = null
  if (wsSubscription) {
    wsSubscription.unsubscribe()
    wsSubscription = null
  }
}

function exportChapterTxt() {
  const ch = selectedChapter.value
  const text = chapterDetailExportableText.value
  if (!ch || !text) return
  const title = `第${ch.chapterNumber}章 ${(ch.title || '无标题').trim()}`
  const full = title + '\n\n' + text
  const blob = new Blob(['\uFEFF' + full], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const safeName = (novel.value?.title || '章节') + '-' + title.replace(/[\s\\/:*?"<>|]+/g, '-').replace(/-+/g, '-').replace(/^-|-$/g, '') + '.txt'
  a.download = safeName || 'chapter.txt'
  a.click()
  URL.revokeObjectURL(url)
}

function openCreateFromOutline(oc) {
  showAddChapterModal.value = true
  newChapter.value.chapterNumber = oc.chapterNumber
  newChapter.value.title = oc.title || ''
  newChapter.value.abstractContent = ''
}

function stopStreamStructure() {
  const client = getWebSocketClient()
  if (!client || !client.connected) return
  
  client.publish({
    destination: '/app/novels/stop',
    body: JSON.stringify({
      novelId: id.value,
      streamType: 'structure'
    })
  })
}

function stopStreamOutline() {
  const client = getWebSocketClient()
  if (!client || !client.connected) return
  
  client.publish({
    destination: '/app/novels/stop',
    body: JSON.stringify({
      novelId: id.value,
      streamType: 'outline'
    })
  })
}

function stopStreamChapter(chapter) {
  if (!chapter || !novel.value) return
  
  const client = getWebSocketClient()
  if (!client || !client.connected) return
  
  client.publish({
    destination: '/app/chapters/stop',
    body: JSON.stringify({
      novelId: novel.value.id,
      chapterNumber: chapter.chapterNumber,
      streamType: 'chapter'
    })
  })
}

function startStreamStructure() {
  if (!id.value || generatingStructure.value) return
  
  const client = getWebSocketClient()
  if (!client || !client.connected) {
    alert('WebSocket 未连接，请刷新页面重试')
    return
  }

  generatingStructure.value = true
  streamingStructure.value = ''

  const topic = `/topic/novels/${id.value}/structure`
  let subscription = null

  subscription = client.subscribe(topic, async (message) => {
    const payload = JSON.parse(message.body)
    const { type, content } = payload

    if (type === 'delta') {
      streamingStructure.value += content
      // 自动滚动
      nextTick(() => {
        const textEl = document.querySelector('.structure-content .structure-text')
        if (textEl) {
          textEl.scrollTop = textEl.scrollHeight
        }
      })
    } else if (type === 'complete') {
      generatingStructure.value = false
      streamingStructure.value = ''
      await load()
      if (subscription) {
        subscription.unsubscribe()
      }
    } else if (type === 'stopped') {
      generatingStructure.value = false
      // 保留已生成的内容，不清空
      if (subscription) {
        subscription.unsubscribe()
      }
    } else if (type === 'error') {
      generatingStructure.value = false
      streamingStructure.value = ''
      alert('生成失败：' + (content || '未知错误'))
      if (subscription) {
        subscription.unsubscribe()
      }
    }
  })

  // 发送生成请求
  client.publish({
    destination: '/app/novels/stream',
    body: JSON.stringify({
      novelId: id.value,
      streamType: 'structure'
    })
  })
}

function startStreamOutline(continueOutline = false) {
  if (!id.value || generatingOutline.value || !novel.value?.structure) return
  
  const client = getWebSocketClient()
  if (!client || !client.connected) {
    alert('WebSocket 未连接，请刷新页面重试')
    return
  }

  generatingOutline.value = true
  // 继续生成时先显示已有大纲，后续 delta 会追加
  streamingOutline.value = continueOutline && novel.value?.chapterOutline ? novel.value.chapterOutline : ''

  const topic = `/topic/novels/${id.value}/outline`
  let subscription = null

  subscription = client.subscribe(topic, async (message) => {
    const payload = JSON.parse(message.body)
    const { type, content } = payload

    if (type === 'delta') {
      streamingOutline.value += content
      // 自动滚动
      nextTick(() => {
        const textEl = document.querySelector('.outline-content .outline-text')
        if (textEl) {
          textEl.scrollTop = textEl.scrollHeight
        }
      })
    } else if (type === 'complete') {
      generatingOutline.value = false
      streamingOutline.value = ''
      await load()
      if (subscription) {
        subscription.unsubscribe()
      }
    } else if (type === 'stopped') {
      generatingOutline.value = false
      // 保留已生成的内容，不清空
      if (subscription) {
        subscription.unsubscribe()
      }
    } else if (type === 'error') {
      generatingOutline.value = false
      streamingOutline.value = ''
      alert('生成失败：' + (content || '未知错误'))
      if (subscription) {
        subscription.unsubscribe()
      }
    }
  })

  // 发送生成请求（continueOutline: true 时带已有大纲继续生成）
  client.publish({
    destination: '/app/novels/stream',
    body: JSON.stringify({
      novelId: id.value,
      streamType: 'outline',
      continueOutline: continueOutline
    })
  })
}

function doExport(format) {
  if (!id.value || exporting.value) return
  exporting.value = true
  novels.exportNovel(id.value, format)
    .then(({ blob, filename }) => {
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      a.click()
      URL.revokeObjectURL(url)
    })
    .catch(() => {})
    .finally(() => { exporting.value = false })
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
.structure-header,
.outline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.outline-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.btn-continue {
  padding: 8px 16px;
  font-size: 14px;
  color: #fff;
  background: var(--accent);
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.btn-continue:hover:not(:disabled) {
  background: var(--accent-dark, #2563eb);
  border-color: var(--accent-dark, #2563eb);
}
.btn-continue:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-regenerate {
  padding: 8px 16px;
  font-size: 14px;
  color: var(--accent);
  background: var(--bg-card);
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-stop {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  color: #dc2626;
  background: var(--bg-card);
  border: 1px solid #dc2626;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-stop:hover {
  background: #fee2e2;
  color: #991b1b;
}
.btn-regenerate:hover:not(:disabled) {
  background: var(--accent-light);
}
.btn-regenerate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.structure-text,
.outline-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  max-height: 500px;
  overflow: auto;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
}
.warning-box {
  padding: 12px 16px;
  margin: 16px auto 24px;
  max-width: 400px;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: #856404;
}
.btn-generate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
.export-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
.export-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.btn-export {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.btn-export:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}
.btn-export:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-export-icon {
  font-size: 18px;
}
.loading {
  color: var(--text-secondary);
  padding: 48px;
  text-align: center;
}

.writing-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.btn-add-chapter {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  color: white;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-add-chapter:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-add-chapter:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chapter-item {
  cursor: pointer;
  position: relative;
}

.chapter-item:hover {
  background: var(--bg-hover);
}

.btn-generate-chapter {
  padding: 6px 12px;
  font-size: 12px;
  color: var(--accent);
  background: var(--accent-light);
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-generate-chapter:hover:not(:disabled) {
  background: var(--accent);
  color: white;
}

.btn-generate-chapter:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status-pending {
  color: #f59e0b;
}

.status-processing {
  color: #3b82f6;
}

.status-completed {
  color: #10b981;
}

.chapter-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.chapter-detail-content {
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.chapter-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
}

.chapter-detail-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.chapter-detail-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-export-txt {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--accent);
  background: var(--bg-card);
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-export-txt:hover {
  background: var(--accent-light);
}

.btn-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  line-height: 1;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-close:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.chapter-detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.chapter-generating {
  position: relative;
}

.streaming-content {
  position: relative;
}

.chapter-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-primary);
  max-height: 60vh;
  overflow-y: auto;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  margin: 0;
}

.streaming-indicator {
  display: inline-block;
  margin-left: 4px;
}

.typing-cursor {
  color: var(--accent);
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.generate-prompt {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-secondary);
}

.btn-generate-in-modal {
  margin-top: 16px;
  padding: 12px 24px;
  font-size: 14px;
  color: white;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-generate-in-modal:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-generate-in-modal:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chapter-content {
  margin-top: 16px;
}

.field {
  margin-bottom: 16px;
}

.field .label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.field input,
.field textarea {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-family: inherit;
}

.field input:focus,
.field textarea:focus {
  outline: none;
  border-color: var(--accent);
}

.btn-primary {
  padding: 10px 20px;
  font-size: 14px;
  color: white;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
