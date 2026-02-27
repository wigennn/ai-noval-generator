import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let wsClient = null

/**
 * 获取或创建 WebSocket 客户端
 */
/**
 * 获取后端服务器地址
 */
function getBackendUrl() {
  // 开发环境：直接连接到后端服务器
  // 生产环境：可以使用相对路径或环境变量
  if (import.meta.env.DEV) {
    return 'http://localhost:8080'
  }
  // 生产环境可以使用 window.location.origin 或环境变量
  return import.meta.env.VITE_API_BASE_URL || window.location.origin
}

export function getWebSocketClient() {
  if (!wsClient) {
    wsClient = new Client({
      webSocketFactory: () => {
        // SockJS 需要使用 http/https 协议，直接连接到后端服务器
        const backendUrl = getBackendUrl()
        return new SockJS(`${backendUrl}/ws`)
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onStompError: (frame) => {
        console.error('WebSocket STOMP error:', frame)
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed')
      }
    })
  }
  return wsClient
}

/**
 * 连接 WebSocket
 */
export function connectWebSocket() {
  const client = getWebSocketClient()
  if (!client.active) {
    client.activate()
  }
  return new Promise((resolve, reject) => {
    if (client.connected) {
      resolve(client)
      return
    }
    client.onConnect = () => {
      resolve(client)
    }
    client.onStompError = (frame) => {
      reject(new Error(frame.headers['message'] || 'WebSocket connection failed'))
    }
  })
}

/**
 * 断开 WebSocket 连接
 */
export function disconnectWebSocket() {
  if (wsClient && wsClient.active) {
    wsClient.deactivate()
  }
}
