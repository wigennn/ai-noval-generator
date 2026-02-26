import { ref, computed } from 'vue'
import * as authApi from '../api/auth'

const currentUser = ref(null)

export async function loadMe() {
  try {
    const data = await authApi.me()
    currentUser.value = data
    return data
  } catch {
    currentUser.value = null
    return null
  }
}

export function useAuth() {
  const userId = computed(() => currentUser.value?.id ?? null)
  const isLoggedIn = computed(() => !!currentUser.value)

  async function login(email, password) {
    const user = await authApi.login(email, password)
    currentUser.value = user
    return user
  }

  async function loginByCode(email, code) {
    const user = await authApi.loginByCode(email, code)
    currentUser.value = user
    return user
  }

  async function register(data) {
    const user = await authApi.register(data)
    currentUser.value = user
    return user
  }

  async function logout() {
    await authApi.logout()
    currentUser.value = null
  }

  return {
    currentUser,
    userId,
    isLoggedIn,
    loadMe,
    login,
    loginByCode,
    register,
    logout
  }
}

export { currentUser }
