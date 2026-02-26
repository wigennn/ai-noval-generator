<template>
  <div class="app">
    <header class="header">
      <router-link to="/" class="brand">
        <span class="logo-icon">🔥</span>
        <div class="brand-text">
          <span class="brand-title">AI 小说生成器</span>
          <span class="brand-subtitle">Novel Generator</span>
        </div>
      </router-link>

      <div class="header-center">
        <div v-if="toastMessage" :class="['toast-banner', toastType]">
          <span v-if="toastType === 'success'">✓</span>
          <span v-else>⚠</span>
          {{ toastMessage }}
        </div>
      </div>

      <div class="header-right">
        <span v-if="showApiWarning" class="api-warning">
          <span class="api-warning-icon">⚠</span>
          请配置 API Key
        </span>
        <router-link to="/library" class="header-nav-link">资料库</router-link>
        <button type="button" class="icon-btn" title="主题" @click="toggleDark">
          <span>🌙</span>
        </button>
        <button type="button" class="icon-btn" title="通知">🔔</button>
        <button type="button" class="icon-btn" title="设置" @click="openApiSettings">⚙</button>
        <div class="user-id-wrap">
          <span class="user-id-label">用户</span>
          <input v-model.number="userId" type="number" min="1" class="user-id-input" placeholder="ID" />
        </div>
      </div>
    </header>

    <main class="main">
      <router-view :userId="userId" @show-toast="showToast" @created-project="onProjectCreated" />
    </main>

    <CreateProjectModal
      v-if="showCreateModal"
      :userId="userId"
      @close="showCreateModal = false"
      @created="onProjectCreatedFromModal"
    />
    <ApiSettingsModal v-if="showApiModal" @close="showApiModal = false" @saved="onApiSaved" />
  </div>
</template>

<script setup>
import { ref, watch, computed, provide } from 'vue'
import CreateProjectModal from './components/CreateProjectModal.vue'
import ApiSettingsModal from './components/ApiSettingsModal.vue'

const userId = ref(Number(localStorage.getItem('novel_user_id')) || 1)
watch(userId, (v) => {
  if (v) localStorage.setItem('novel_user_id', String(v))
})

const toastMessage = ref('')
const toastType = ref('success')
const toastTimer = ref(null)

function showToast(msg, type = 'success') {
  toastMessage.value = msg
  toastType.value = type
  if (toastTimer.value) clearTimeout(toastTimer.value)
  toastTimer.value = setTimeout(() => {
    toastMessage.value = ''
    toastTimer.value = null
  }, 3000)
}

const showCreateModal = ref(false)
const showApiModal = ref(false)
const showApiWarning = computed(() => !localStorage.getItem('novel_api_key'))

provide('openCreateModal', () => { showCreateModal.value = true })
provide('showToast', showToast)
provide('userId', userId)

function openApiSettings() {
  showApiModal.value = true
}

function toggleDark() {
  document.documentElement.classList.toggle('dark')
}

function onProjectCreated() {
  showToast('项目创建成功', 'success')
}

function onProjectCreatedFromModal() {
  showCreateModal.value = false
  showToast('项目创建成功', 'success')
}

function onApiSaved() {
  showApiModal.value = false
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 12px 24px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
}
.brand:hover {
  color: var(--text-primary);
}
.logo-icon {
  font-size: 28px;
  line-height: 1;
}
.brand-text {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.brand-subtitle {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-secondary);
}
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.api-warning {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--warning);
  margin-right: 8px;
}
.api-warning-icon {
  font-size: 16px;
}
.header-nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  text-decoration: none;
}
.header-nav-link:hover,
.header-nav-link.router-link-active {
  color: var(--accent);
  background: var(--accent-light);
}
.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  background: transparent;
  font-size: 18px;
}
.icon-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}
.user-id-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 8px;
}
.user-id-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.user-id-input {
  width: 64px;
  padding: 6px 8px;
  font-size: 13px;
}
.main {
  flex: 1;
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}
</style>
