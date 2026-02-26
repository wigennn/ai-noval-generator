<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal modal-api">
      <div class="modal-header">
        <h2 class="modal-title">API 设置</h2>
        <button type="button" class="modal-close" aria-label="关闭" @click="$emit('close')">×</button>
      </div>
      <form @submit.prevent="onSave" class="modal-body">
        <div class="field">
          <label class="label">渠道</label>
          <select v-model="form.channel">
            <option value="Chatfire">Chatfire</option>
            <option value="OpenAI">OpenAI</option>
            <option value="Custom">自定义</option>
          </select>
        </div>
        <div class="field">
          <label class="label">API Base URL</label>
          <input v-model="form.baseUrl" placeholder="https://api.openai.com/v1" />
        </div>
        <div class="field">
          <label class="label">API Key</label>
          <div class="input-with-icon">
            <input :type="showKey ? 'text' : 'password'" v-model="form.apiKey" placeholder="请输入 API Key" />
            <button type="button" class="input-icon-btn" @click="showKey = !showKey">{{ showKey ? '🙈' : '👁' }}</button>
          </div>
        </div>
        <div class="field">
          <label class="label">默认模型</label>
          <input v-model="form.defaultModel" placeholder="例如: gpt-4" />
        </div>
        <div class="field">
          <label class="label">各环节模型配置</label>
          <div class="model-tabs">
            <button type="button" v-for="tab in modelTabs" :key="tab.key" :class="['model-tab', { active: currentModelTab === tab.key }]" @click="currentModelTab = tab.key">
              {{ tab.label }}
            </button>
          </div>
        </div>
        <div class="field checkbox-field">
          <label class="checkbox-label">
            <input type="checkbox" v-model="form.useDefaultModel" />
            <span>勾选使用默认模型</span>
          </label>
        </div>
      </form>
      <div class="modal-footer">
        <a href="https://api.chatfire.site" target="_blank" rel="noopener" class="link-btn">获取 Key</a>
        <button type="button" class="secondary" @click="$emit('close')">取消</button>
        <button type="button" class="primary" @click="onSave">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const emit = defineEmits(['close', 'saved'])

const showKey = ref(false)
const currentModelTab = ref('structure')

const modelTabs = [
  { key: 'structure', label: '架构生成' },
  { key: 'outline', label: '大纲生成' },
  { key: 'chapter', label: '章节生成' },
  { key: 'final', label: '定稿处理' },
  { key: 'expand', label: '章节扩写' }
]

const form = ref({
  channel: 'Chatfire',
  baseUrl: 'https://api.chatfire.site/v1',
  apiKey: '',
  defaultModel: 'gemini-3-flash-preview',
  useDefaultModel: false
})

onMounted(() => {
  try {
    const saved = JSON.parse(localStorage.getItem('novel_api_settings') || '{}')
    if (saved.baseUrl) form.value.baseUrl = saved.baseUrl
    if (saved.apiKey) form.value.apiKey = saved.apiKey
    if (saved.defaultModel) form.value.defaultModel = saved.defaultModel
    if (saved.channel) form.value.channel = saved.channel
  } catch (_) {}
})

function onSave() {
  localStorage.setItem('novel_api_key', form.value.apiKey)
  localStorage.setItem('novel_api_settings', JSON.stringify({
    channel: form.value.channel,
    baseUrl: form.value.baseUrl,
    defaultModel: form.value.defaultModel
  }))
  emit('saved')
}
</script>

<style scoped>
.modal-api {
  max-width: 520px;
}
.input-with-icon {
  position: relative;
  display: flex;
}
.input-with-icon input {
  padding-right: 44px;
}
.input-icon-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  padding: 0;
  background: transparent;
  font-size: 16px;
  border-radius: var(--radius-sm);
}
.input-icon-btn:hover {
  background: var(--bg-hover);
}
.model-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.model-tab {
  padding: 8px 14px;
  font-size: 13px;
  background: var(--bg-hover);
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
}
.model-tab.active {
  background: var(--accent-light);
  color: var(--accent);
  font-weight: 500;
}
.checkbox-field {
  margin-bottom: 0;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary);
}
.checkbox-label input {
  width: auto;
}
.link-btn {
  margin-right: auto;
  font-size: 14px;
}
</style>
