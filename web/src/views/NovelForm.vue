<template>
  <div class="novel-form-page">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑小说' : '创建小说' }}</h1>
      <router-link to="/novels" class="secondary">返回列表</router-link>
    </div>

    <form @submit.prevent="onSubmit" class="card form">
      <div class="field">
        <label class="label">标题</label>
        <input v-model="form.title" required placeholder="小说标题" />
      </div>
      <div class="field">
        <label class="label">题材</label>
        <input v-model="form.genre" placeholder="如：玄幻、都市、言情" />
      </div>
      <div class="field">
        <label class="label">世界观设定</label>
        <textarea v-model="form.settingText" placeholder="用于 RAG 的世界观与设定说明" rows="5"></textarea>
      </div>

      <div class="field">
        <label class="label">关联资料库</label>
        <p class="hint">选择已上传的资料库，生成章节时可参考其内容。可多选。</p>
        <div v-if="libraryLoading" class="hint">加载资料库中…</div>
        <div v-else class="library-options">
          <label v-for="lib in libraryList" :key="lib.id" class="library-check">
            <input type="checkbox" :value="lib.vectorId" v-model="selectedVectorIds" />
            <span>{{ lib.vectorName }}</span>
          </label>
          <p v-if="!libraryList.length" class="hint">暂无资料库，请先在<a href="#" @click.prevent="goLibrary">资料库</a>页面上传。</p>
        </div>
      </div>
      <div v-if="!isEdit" class="field">
        <label class="label">生成方式</label>
        <div class="generate-mode">
          <label class="radio-label">
            <input v-model="form.async" type="radio" :value="true" />
            <span>异步生成</span>
          </label>
          <label class="radio-label">
            <input v-model="form.async" type="radio" :value="false" />
            <span>实时生成</span>
          </label>
        </div>
      </div>

      <div class="actions">
        <button type="submit" class="primary" :disabled="saving">
          {{ saving ? '保存中…' : (isEdit ? '保存' : '创建') }}
        </button>
        <router-link to="/novels" class="secondary">取消</router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as novels from '../api/novels'
import * as userVectors from '../api/userVectors'
import * as novelVectors from '../api/novelVectors'

const props = defineProps({ userId: { type: Number, default: 1 } })
const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const form = ref({ title: '', genre: '', settingText: '', async: true })
const saving = ref(false)
const libraryList = ref([])
const libraryLoading = ref(false)
const selectedVectorIds = ref([])
const existingNovelVectors = ref([])

function loadNovel() {
  if (!isEdit.value || !route.params.id) return
  novels.getNovel(route.params.id).then((data) => {
    form.value = {
      title: data.title,
      genre: data.genre || '',
      settingText: data.settingText || '',
      async: true
    }
  })
  novelVectors.listByNovel(Number(route.params.id)).then((data) => {
    existingNovelVectors.value = data || []
    selectedVectorIds.value = (data || []).map((v) => v.vectorId)
  })
}

function loadLibrary() {
  if (!props.userId) return
  libraryLoading.value = true
  userVectors.listByUser(props.userId).then((data) => {
    libraryList.value = data || []
  }).finally(() => { libraryLoading.value = false })
}

watch(() => props.userId, () => { loadLibrary(); loadNovel() }, { immediate: true })
onMounted(() => { if (isEdit.value) loadNovel() })

function goLibrary() {
  router.push('/library')
}

async function onSubmit() {
  if (!props.userId || saving.value) return
  saving.value = true
  try {
    if (isEdit.value) {
      await novels.updateNovel(route.params.id, {
        userId: props.userId,
        title: form.value.title,
        genre: form.value.genre,
        settingText: form.value.settingText
      })
      const novelId = Number(route.params.id)
      const toAdd = selectedVectorIds.value.filter((id) => !existingNovelVectors.value.some((v) => v.vectorId === id))
      const toRemove = existingNovelVectors.value.filter((v) => !selectedVectorIds.value.includes(v.vectorId))
      for (const v of toRemove) await novelVectors.remove(v.id)
      for (const vectorId of toAdd) await novelVectors.add(novelId, vectorId)
    } else {
      const novel = await novels.createNovel({
        userId: props.userId,
        title: form.value.title,
        genre: form.value.genre,
        settingText: form.value.settingText,
        async: form.value.async
      })
      for (const vectorId of selectedVectorIds.value) {
        await novelVectors.add(novel.id, vectorId)
      }
    }
    router.push('/novels')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.novel-form-page .page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.novel-form-page .page-header a.secondary {
  text-decoration: none;
}
.form .field {
  margin-bottom: 20px;
}
.hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.library-options {
  margin-top: 8px;
}
.library-check {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  cursor: pointer;
}
.library-check input {
  width: auto;
}
.generate-mode {
  display: flex;
  gap: 16px;
}
.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.radio-label input {
  width: auto;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
.actions a.secondary {
  text-decoration: none;
}
</style>
