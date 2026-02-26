<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">AI 小说生成器</h1>
      <p class="login-subtitle">基于 RAG 的 AI 创作工具 · 邮箱验证码登录</p>

      <form class="form" @submit.prevent="handleLoginByCode">
        <div class="field">
          <label for="login-email">邮箱</label>
          <input
            id="login-email"
            v-model="email"
            type="email"
            required
            placeholder="your@email.com"
            autocomplete="email"
            :disabled="loading"
          />
        </div>
        <div class="field row">
          <div class="field-code">
            <label for="login-code">验证码</label>
            <input
              id="login-code"
              v-model="code"
              type="text"
              inputmode="numeric"
              maxlength="8"
              placeholder="请输入验证码"
              autocomplete="one-time-code"
              :disabled="loading"
            />
          </div>
          <button
            type="button"
            class="btn-send-code"
            :disabled="sendCooldown > 0 || loading || !email"
            @click="handleSendCode"
          >
            {{ sendCooldown > 0 ? `${sendCooldown}s 后重发` : '获取验证码' }}
          </button>
        </div>
        <p v-if="sendError" class="error-msg">{{ sendError }}</p>
        <p v-if="loginError" class="error-msg">{{ loginError }}</p>
        <button type="submit" class="btn-submit" :disabled="loading || !email || !code">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="hint">
        未注册的邮箱将自动创建账号。验证码 5 分钟内有效。
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../store/auth'
import * as authApi from '../api/auth'

const router = useRouter()
const route = useRoute()
const { loginByCode: doLoginByCode } = useAuth()

const email = ref('')
const code = ref('')
const loading = ref(false)
const sendCooldown = ref(0)
const sendError = ref('')
const loginError = ref('')
let cooldownTimer = null

async function handleSendCode() {
  if (!email.value || sendCooldown.value > 0) return
  sendError.value = ''
  try {
    await authApi.sendCode(email.value)
    sendCooldown.value = 60
    cooldownTimer = setInterval(() => {
      sendCooldown.value--
      if (sendCooldown.value <= 0 && cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }, 1000)
  } catch (e) {
    const data = e.response?.data
    sendError.value = (data && typeof data === 'object' && data.message) ? data.message : (e.message || '发送失败')
  }
}

async function handleLoginByCode() {
  if (!email.value || !code.value || loading.value) return
  loginError.value = ''
  loading.value = true
  try {
    await doLoginByCode(email.value, code.value)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    loginError.value = e.response?.status === 401 ? '验证码错误或已过期' : (e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--bg-page, #f5f5f5);
}
.dark .login-page {
  background: var(--bg-page);
}
.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
  text-align: center;
}
.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px;
  text-align: center;
}
.form .field {
  margin-bottom: 16px;
}
.form .field.row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.form .field-code {
  flex: 1;
  min-width: 0;
}
.form label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.form input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input, #fff);
  color: var(--text-primary);
  box-sizing: border-box;
}
.form input:focus {
  outline: none;
  border-color: var(--accent);
}
.form input:disabled {
  opacity: 0.8;
  cursor: not-allowed;
}
.btn-send-code {
  flex-shrink: 0;
  padding: 10px 16px;
  font-size: 14px;
  color: var(--accent);
  background: var(--accent-light, rgba(0, 120, 212, 0.1));
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  cursor: pointer;
  white-space: nowrap;
}
.btn-send-code:hover:not(:disabled) {
  opacity: 0.9;
}
.btn-send-code:disabled {
  color: var(--text-secondary);
  background: var(--bg-hover);
  border-color: var(--border);
  cursor: not-allowed;
}
.error-msg {
  font-size: 13px;
  color: var(--warning, #c00);
  margin: 0 0 12px;
}
.btn-submit {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  font-weight: 500;
  color: #fff;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  margin-top: 8px;
}
.btn-submit:hover:not(:disabled) {
  opacity: 0.9;
}
.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 16px 0 0;
  text-align: center;
}
</style>
