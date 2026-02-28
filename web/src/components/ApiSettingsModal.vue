<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal modal-api">
      <div class="modal-header">
        <h2 class="modal-title">AI 设置</h2>
        <button type="button" class="modal-close" aria-label="关闭" @click="$emit('close')">×</button>
      </div>
      <form @submit.prevent="onSave" class="modal-body">
        <div class="section">
          <h3 class="section-title">对话模型配置</h3>
          <div class="field">
            <label class="label">AI渠道</label>
            <select v-model="form.channel" @change="onChannelChange">
              <option value="Ali">阿里</option>
              <option value="DeepSeek">DeepSeek</option>
              <option value="OpenAI">OpenAI</option>
              <option value="Custom">自定义</option>
            </select>
          </div>
          <div class="field">
            <label class="label">API Base URL</label>
            <input v-model="form.baseUrl" placeholder="https://api.deepseek.com" />
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
            <input v-model="form.defaultModel" placeholder="例如: deepseek-chat" />
          </div>
        </div>

        <div class="section">
          <h3 class="section-title">嵌入模型配置</h3>
          <div class="field">
            <label class="label">嵌入模型 API Base URL</label>
            <input v-model="form.embeddingBaseUrl" placeholder="https://api.openai.com/v1" />
          </div>
          <div class="field">
            <label class="label">嵌入模型 API Key</label>
            <div class="input-with-icon">
              <input :type="showEmbeddingKey ? 'text' : 'password'" v-model="form.embeddingApiKey" placeholder="请输入嵌入模型 API Key（留空则使用对话模型 Key）" />
              <button type="button" class="input-icon-btn" @click="showEmbeddingKey = !showEmbeddingKey">{{ showEmbeddingKey ? '🙈' : '👁' }}</button>
            </div>
          </div>
          <div class="field">
            <label class="label">嵌入模型名称</label>
            <input v-model="form.embeddingModel" placeholder="例如: text-embedding-3-small" />
          </div>
        </div>
      </form>
      <div class="modal-footer">
        <button type="button" class="secondary" @click="$emit('close')">取消</button>
        <button type="button" class="primary" @click="onSave" :disabled="saving">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuth } from '../store/auth'
import * as userModelsApi from '../api/userModels'

const emit = defineEmits(['close', 'saved'])

const { userId } = useAuth()
const showKey = ref(false)
const showEmbeddingKey = ref(false)
const saving = ref(false)

const form = ref({
  channel: 'DeepSeek',
  baseUrl: 'https://api.deepseek.com',
  apiKey: '',
  defaultModel: 'deepseek-chat',
  embeddingBaseUrl: 'https://api.openai.com/v1',
  embeddingApiKey: '',
  embeddingModel: 'text-embedding-3-small'
})

// 渠道配置映射
const channelConfigs = {
  DeepSeek: {
    baseUrl: 'https://api.deepseek.com',
    defaultModel: 'deepseek-chat',
    embeddingBaseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    embeddingModel: 'text-embedding-v4'
  },
  OpenAI: {
    baseUrl: 'https://api.openai.com/v1',
    defaultModel: 'gpt-4',
    embeddingBaseUrl: 'https://api.openai.com/v1',
    embeddingModel: 'text-embedding-3-small'
  },
  Ali: {
    baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    defaultModel: 'deepseek-v3.2',
    embeddingBaseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    embeddingModel: 'text-embedding-v4'
  },
  Custom: {
    baseUrl: '',
    defaultModel: '',
    embeddingBaseUrl: '',
    embeddingModel: ''
  }
}

function onChannelChange() {
  const config = channelConfigs[form.value.channel]
  if (config) {
    form.value.baseUrl = config.baseUrl
    form.value.defaultModel = config.defaultModel
    form.value.embeddingBaseUrl = config.embeddingBaseUrl
    form.value.embeddingModel = config.embeddingModel
  }
}

onMounted(async () => {
  // 优先从后端加载配置
  if (userId.value) {
    try {
      const settings = await userModelsApi.getApiSettings(userId.value)
      if (settings.chatModel) {
        form.value.channel = settings.chatModel.modelType
        form.value.baseUrl = settings.chatModel.modelUrl
        form.value.apiKey = settings.chatModel.apiKey
        form.value.defaultModel = settings.chatModel.modelName
      }
      if (settings.embeddingModel) {
        form.value.embeddingBaseUrl = settings.embeddingModel.modelUrl
        form.value.embeddingApiKey = settings.embeddingModel.apiKey
        form.value.embeddingModel = settings.embeddingModel.modelName
      }
    } catch (err) {
      console.warn('Failed to load API settings from server:', err)
      // 如果后端加载失败，从 localStorage 加载
      loadFromLocalStorage()
    }
  } else {
    // 如果没有 userId，从 localStorage 加载
    loadFromLocalStorage()
  }
})

function loadFromLocalStorage() {
  try {
    const saved = JSON.parse(localStorage.getItem('novel_api_settings') || '{}')
    if (saved.baseUrl) form.value.baseUrl = saved.baseUrl
    if (saved.apiKey) form.value.apiKey = saved.apiKey
    if (saved.defaultModel) form.value.defaultModel = saved.defaultModel
    if (saved.channel) form.value.channel = saved.channel
    if (saved.embeddingBaseUrl) form.value.embeddingBaseUrl = saved.embeddingBaseUrl
    if (saved.embeddingApiKey) form.value.embeddingApiKey = saved.embeddingApiKey
    if (saved.embeddingModel) form.value.embeddingModel = saved.embeddingModel
  } catch (_) {}
}

async function onSave() {
  if (saving.value) return
  
  saving.value = true
  try {
    // 保存到后端
    if (userId.value) {
      await userModelsApi.saveApiSettings(userId.value, {
        channel: form.value.channel,
        baseUrl: form.value.baseUrl,
        apiKey: form.value.apiKey,
        defaultModel: form.value.defaultModel,
        embeddingBaseUrl: form.value.embeddingBaseUrl,
        embeddingApiKey: form.value.embeddingApiKey || '',
        embeddingModel: form.value.embeddingModel
      })
    }
    
    // 同时保存到 localStorage（作为备用）
    localStorage.setItem('novel_api_key', form.value.apiKey)
    localStorage.setItem('novel_api_settings', JSON.stringify({
      channel: form.value.channel,
      baseUrl: form.value.baseUrl,
      defaultModel: form.value.defaultModel,
      embeddingBaseUrl: form.value.embeddingBaseUrl,
      embeddingApiKey: form.value.embeddingApiKey,
      embeddingModel: form.value.embeddingModel
    }))
    
    emit('saved')
  } catch (err) {
    console.error('Failed to save API settings:', err)
    alert('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.modal-api {
  max-width: 520px;
}

.section {
  margin-bottom: 24px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
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
  cursor: pointer;
}

.input-icon-btn:hover {
  background: var(--bg-hover);
}
</style>
