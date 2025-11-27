/**
 * 本地存储工具
 */

const PREFIX = 'luoyi_'

export const storage = {
  /**
   * 设置存储项
   */
  set<T>(key: string, value: T, expires?: number): void {
    const data = {
      value,
      expires: expires ? Date.now() + expires * 1000 : null
    }
    localStorage.setItem(PREFIX + key, JSON.stringify(data))
  },

  /**
   * 获取存储项
   */
  get<T>(key: string, defaultValue?: T): T | undefined {
    const item = localStorage.getItem(PREFIX + key)
    if (!item) return defaultValue
    
    try {
      const data = JSON.parse(item)
      if (data.expires && Date.now() > data.expires) {
        localStorage.removeItem(PREFIX + key)
        return defaultValue
      }
      return data.value as T
    } catch {
      return defaultValue
    }
  },

  /**
   * 移除存储项
   */
  remove(key: string): void {
    localStorage.removeItem(PREFIX + key)
  },

  /**
   * 清除所有存储
   */
  clear(): void {
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith(PREFIX)) {
        localStorage.removeItem(key)
      }
    })
  }
}

export const session = {
  /**
   * 设置会话存储项
   */
  set<T>(key: string, value: T): void {
    sessionStorage.setItem(PREFIX + key, JSON.stringify(value))
  },

  /**
   * 获取会话存储项
   */
  get<T>(key: string, defaultValue?: T): T | undefined {
    const item = sessionStorage.getItem(PREFIX + key)
    if (!item) return defaultValue
    
    try {
      return JSON.parse(item) as T
    } catch {
      return defaultValue
    }
  },

  /**
   * 移除会话存储项
   */
  remove(key: string): void {
    sessionStorage.removeItem(PREFIX + key)
  },

  /**
   * 清除所有会话存储
   */
  clear(): void {
    Object.keys(sessionStorage).forEach(key => {
      if (key.startsWith(PREFIX)) {
        sessionStorage.removeItem(key)
      }
    })
  }
}

// Token 专用操作
const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refresh_token'

export const tokenStorage = {
  getToken(): string | undefined {
    return storage.get<string>(TOKEN_KEY)
  },
  
  setToken(token: string, expires?: number): void {
    storage.set(TOKEN_KEY, token, expires)
  },
  
  removeToken(): void {
    storage.remove(TOKEN_KEY)
  },
  
  getRefreshToken(): string | undefined {
    return storage.get<string>(REFRESH_TOKEN_KEY)
  },
  
  setRefreshToken(token: string, expires?: number): void {
    storage.set(REFRESH_TOKEN_KEY, token, expires)
  },
  
  removeRefreshToken(): void {
    storage.remove(REFRESH_TOKEN_KEY)
  },
  
  clearTokens(): void {
    storage.remove(TOKEN_KEY)
    storage.remove(REFRESH_TOKEN_KEY)
  }
}
