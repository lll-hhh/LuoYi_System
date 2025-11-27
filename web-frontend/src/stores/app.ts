import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
}

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  
  // 当前主题
  const theme = ref<'light' | 'dark'>(
    (localStorage.getItem('theme') as 'light' | 'dark') || 'light'
  )
  
  // 面包屑
  const breadcrumbs = ref<{ title: string; path?: string }[]>([])
  
  // 标签页
  const tabs = ref<{ path: string; title: string }[]>([
    { path: '/dashboard', title: '仪表盘' }
  ])
  const activeTab = ref('/dashboard')
  
  // 全局加载状态
  const loading = ref(false)
  
  // 菜单数据
  const menuItems = ref<MenuItem[]>([
    {
      path: '/dashboard',
      title: '仪表盘',
      icon: 'Odometer'
    },
    {
      path: '/traffic',
      title: '交通管理',
      icon: 'Van',
      children: [
        { path: '/traffic/road', title: '道路管理' },
        { path: '/traffic/junction', title: '路口管理' },
        { path: '/traffic/camera', title: '摄像头管理' }
      ]
    },
    {
      path: '/monitoring',
      title: '监控中心',
      icon: 'Monitor',
      children: [
        { path: '/monitoring/realtime', title: '实时监控' },
        { path: '/monitoring/anomaly', title: '异常处理' }
      ]
    },
    {
      path: '/warehouse',
      title: '仓储管理',
      icon: 'House',
      children: [
        { path: '/warehouse/list', title: '仓库列表' },
        { path: '/warehouse/cargo', title: '货物管理' }
      ]
    },
    {
      path: '/parking',
      title: '停车场管理',
      icon: 'Place'
    },
    {
      path: '/statistics',
      title: '数据统计',
      icon: 'DataAnalysis',
      children: [
        { path: '/statistics/traffic', title: '交通统计' },
        { path: '/statistics/report', title: '报表中心' }
      ]
    },
    {
      path: '/system',
      title: '系统管理',
      icon: 'Setting',
      children: [
        { path: '/system/employee', title: '员工管理' },
        { path: '/system/department', title: '部门管理' },
        { path: '/system/role', title: '角色管理' },
        { path: '/system/task', title: '任务管理' }
      ]
    }
  ])

  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置主题
  function setTheme(newTheme: 'light' | 'dark') {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    document.documentElement.setAttribute('data-theme', newTheme)
  }

  // 设置面包屑
  function setBreadcrumbs(items: { title: string; path?: string }[]) {
    breadcrumbs.value = items
  }

  // 添加标签页
  function addTab(tab: { path: string; title: string }) {
    const exists = tabs.value.find(t => t.path === tab.path)
    if (!exists) {
      tabs.value.push(tab)
    }
    activeTab.value = tab.path
  }

  // 关闭标签页
  function closeTab(path: string) {
    const index = tabs.value.findIndex(t => t.path === path)
    if (index > -1) {
      tabs.value.splice(index, 1)
      // 如果关闭的是当前标签，切换到最后一个
      if (activeTab.value === path && tabs.value.length > 0) {
        activeTab.value = tabs.value[tabs.value.length - 1].path
      }
    }
  }

  // 关闭其他标签页
  function closeOtherTabs(path: string) {
    tabs.value = tabs.value.filter(t => t.path === path || t.path === '/dashboard')
    activeTab.value = path
  }

  // 关闭所有标签页
  function closeAllTabs() {
    tabs.value = [{ path: '/dashboard', title: '仪表盘' }]
    activeTab.value = '/dashboard'
  }

  // 设置全局加载状态
  function setLoading(status: boolean) {
    loading.value = status
  }

  return {
    sidebarCollapsed,
    theme,
    breadcrumbs,
    tabs,
    activeTab,
    loading,
    menuItems,
    toggleSidebar,
    setTheme,
    setBreadcrumbs,
    addTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs,
    setLoading
  }
})
