/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function (this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let lastTime = 0
  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

/**
 * 深拷贝
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as unknown as T
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as unknown as T
  if (obj instanceof Object) {
    const copy = {} as T
    Object.keys(obj).forEach(key => {
      (copy as any)[key] = deepClone((obj as any)[key])
    })
    return copy
  }
  return obj
}

/**
 * 生成唯一ID
 */
export function generateId(): string {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

/**
 * 获取URL参数
 */
export function getUrlParams(url?: string): Record<string, string> {
  const search = url ? new URL(url).search : window.location.search
  const params = new URLSearchParams(search)
  const result: Record<string, string> = {}
  params.forEach((value, key) => {
    result[key] = value
  })
  return result
}

/**
 * 将对象转换为URL查询字符串
 */
export function objectToQuery(obj: Record<string, any>): string {
  const params = new URLSearchParams()
  Object.keys(obj).forEach(key => {
    const value = obj[key]
    if (value !== null && value !== undefined && value !== '') {
      params.append(key, String(value))
    }
  })
  return params.toString()
}

/**
 * 下载文件
 */
export function downloadFile(data: Blob, filename: string): void {
  const url = window.URL.createObjectURL(data)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 复制文本到剪贴板
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    const result = document.execCommand('copy')
    document.body.removeChild(textarea)
    return result
  }
}

/**
 * 睡眠函数
 */
export function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 树形数据扁平化
 */
export function flattenTree<T extends { children?: T[] }>(
  tree: T[],
  childrenKey = 'children'
): T[] {
  const result: T[] = []
  const traverse = (nodes: T[]) => {
    nodes.forEach(node => {
      result.push(node)
      if ((node as any)[childrenKey]) {
        traverse((node as any)[childrenKey])
      }
    })
  }
  traverse(tree)
  return result
}

/**
 * 列表转树形结构
 */
export function listToTree<T extends { id: string | number; parentId?: string | number | null }>(
  list: T[],
  rootId: string | number | null = null
): (T & { children?: T[] })[] {
  const map = new Map<string | number, T & { children?: T[] }>()
  const result: (T & { children?: T[] })[] = []
  
  list.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })
  
  list.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === rootId) {
      result.push(node)
    } else if (item.parentId && map.has(item.parentId)) {
      const parent = map.get(item.parentId)!
      parent.children = parent.children || []
      parent.children.push(node)
    }
  })
  
  return result
}
