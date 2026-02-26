import axios from 'axios'

export const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    console.error('API Error', err.response?.data ?? err.message)
    return Promise.reject(err)
  }
)
