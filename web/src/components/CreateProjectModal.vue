<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal modal-create">
      <div class="modal-header">
        <h2 class="modal-title">创建新项目</h2>
        <button type="button" class="modal-close" aria-label="关闭" @click="$emit('close')">×</button>
      </div>
      <form @submit.prevent="onSubmit" class="modal-body">
        <div class="field">
          <label class="label">项目名称 <span class="required">*</span></label>
          <input v-model="form.title" maxlength="50" placeholder="例如: 星辰人海" />
          <span class="char-count">{{ (form.title || '').length }}/50</span>
        </div>
        <div class="field">
          <label class="label">小说主题/核心创意 <span class="required">*</span></label>
          <textarea v-model="form.theme" maxlength="500" placeholder="描述你的小说核心创意, 例如: 一个普通少年意外获得神秘传承, 在修仙世界中逐步成长..." rows="4"></textarea>
          <span class="char-count">{{ (form.theme || '').length }}/500</span>
        </div>
        <div class="field">
          <label class="label">小说类型 <span class="required">*</span></label>
          <select v-model="form.genre" required>
            <option value="">请选择</option>
            <option v-for="t in novelTypes" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
        <div class="field row">
          <div class="half">
            <label class="label">预计章节数 <span class="required">*</span></label>
            <div class="stepper">
              <button type="button" class="stepper-btn" @click="form.estimatedChapters = Math.max(1, form.estimatedChapters - 1)">−</button>
              <input v-model.number="form.estimatedChapters" type="number" min="1" max="999" class="stepper-input" />
              <button type="button" class="stepper-btn" @click="form.estimatedChapters = Math.min(999, form.estimatedChapters + 1)">+</button>
            </div>
          </div>
          <div class="half">
            <label class="label">每章字数 <span class="required">*</span></label>
            <div class="stepper">
              <button type="button" class="stepper-btn" @click="form.wordsPerChapter = Math.max(500, form.wordsPerChapter - 500)">−</button>
              <input v-model.number="form.wordsPerChapter" type="number" min="500" step="500" class="stepper-input" />
              <button type="button" class="stepper-btn" @click="form.wordsPerChapter = form.wordsPerChapter + 500">+</button>
            </div>
          </div>
        </div>
        <div class="field">
          <label class="label">创作指导 (可选)</label>
          <textarea v-model="form.guidance" maxlength="1000" placeholder="你可以在这里添加额外的创作要求, 如特定角色设定、情节走向、写作风格等..." rows="3"></textarea>
          <span class="char-count">{{ (form.guidance || '').length }}/1000</span>
        </div>
        <div class="field">
          <label class="label">生成方式</label>
          <div class="generate-mode">
            <label class="radio-label">
              <input v-model="form.async" type="radio" :value="true" />
              <span>异步生成</span>
              <span class="hint-inline">（推荐，通过消息队列后台生成，不阻塞页面）</span>
            </label>
            <label class="radio-label">
              <input v-model="form.async" type="radio" :value="false" />
              <span>实时生成</span>
              <span class="hint-inline">（等待 AI 完成后返回，需较长时间）</span>
            </label>
          </div>
        </div>
      </form>
      <div class="modal-footer">
        <button type="button" class="secondary" @click="$emit('close')">取消</button>
        <button type="button" class="primary" @click="onSubmit">
          <span class="btn-icon">✓</span>
          创建项目
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as novels from '../api/novels'

const props = defineProps({ userId: { type: Number, default: 1 } })
const emit = defineEmits(['close', 'created'])
const router = useRouter()

const novelTypes = ['玄幻', '都市', '言情', '科幻', '历史', '武侠', '游戏', '轻小说', '其他']

const form = ref({
  title: '',
  theme: '',
  genre: '',
  estimatedChapters: 100,
  wordsPerChapter: 3000,
  guidance: '',
  async: true
})

const saving = ref(false)

async function onSubmit() {
  if (!props.userId || saving.value) return
  if (!form.value.title?.trim()) return
  if (!form.value.theme?.trim()) return
  saving.value = true
  try {
    const settingText = [form.value.theme, form.value.guidance].filter(Boolean).join('\n\n')
    const novel = await novels.createNovel({
      userId: props.userId,
      title: form.value.title.trim(),
      genre: form.value.genre || undefined,
      settingText: settingText || undefined,
      async: form.value.async
    })
    const meta = { estimatedChapters: form.value.estimatedChapters, wordsPerChapter: form.value.wordsPerChapter }
    localStorage.setItem(`novel_meta_${novel.id}`, JSON.stringify(meta))
    emit('created')
    router.push(`/novels/${novel.id}`)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.modal-create {
  max-width: 560px;
}
.field {
  margin-bottom: 20px;
}
.field .char-count {
  display: block;
  text-align: right;
  margin-top: 4px;
}
.required {
  color: var(--danger);
}
.row {
  display: flex;
  gap: 16px;
}
.half {
  flex: 1;
}
.stepper {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.stepper-btn {
  width: 40px;
  height: 40px;
  padding: 0;
  background: var(--bg-hover);
  color: var(--text-primary);
  border: none;
  border-radius: 0;
  font-size: 18px;
  line-height: 1;
}
.stepper-btn:hover {
  background: var(--border);
}
.stepper-input {
  width: 80px;
  text-align: center;
  border: none;
  border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
  border-radius: 0;
}
.generate-mode {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}
.radio-label input {
  width: auto;
}
.hint-inline {
  font-size: 12px;
  color: var(--text-secondary);
}
.btn-icon {
  margin-right: 6px;
}
</style>
