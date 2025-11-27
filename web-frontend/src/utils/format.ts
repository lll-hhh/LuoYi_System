import dayjs from 'dayjs'

/**
 * 格式化日期
 */
export function formatDate(date: string | Date | number, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return '-'
  return dayjs(date).format(format)
}

/**
 * 格式化相对时间
 */
export function formatRelativeTime(date: string | Date | number): string {
  if (!date) return '-'
  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'minute')
  
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  if (diff < 43200) return `${Math.floor(diff / 1440)}天前`
  return target.format('YYYY-MM-DD')
}

/**
 * 格式化数字
 */
export function formatNumber(num: number, precision = 2): string {
  if (num === null || num === undefined) return '-'
  if (num >= 100000000) {
    return (num / 100000000).toFixed(precision) + '亿'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(precision) + '万'
  }
  return num.toLocaleString()
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化时长（秒）
 */
export function formatDuration(seconds: number): string {
  if (!seconds || seconds < 0) return '-'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  
  if (h > 0) {
    return `${h}小时${m}分钟`
  }
  if (m > 0) {
    return `${m}分钟${s}秒`
  }
  return `${s}秒`
}

/**
 * 格式化金额
 */
export function formatMoney(amount: number, symbol = '¥'): string {
  if (amount === null || amount === undefined) return '-'
  return `${symbol}${amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`
}

/**
 * 格式化百分比
 */
export function formatPercent(value: number, precision = 2): string {
  if (value === null || value === undefined) return '-'
  return `${(value * 100).toFixed(precision)}%`
}
