import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/default.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'traffic',
        name: 'Traffic',
        meta: { title: '交通管理', icon: 'Van' },
        children: [
          {
            path: 'road',
            name: 'Road',
            component: () => import('@/views/traffic/road/index.vue'),
            meta: { title: '道路管理' }
          },
          {
            path: 'junction',
            name: 'Junction',
            component: () => import('@/views/traffic/junction/index.vue'),
            meta: { title: '路口管理' }
          },
          {
            path: 'camera',
            name: 'Camera',
            component: () => import('@/views/traffic/camera/index.vue'),
            meta: { title: '摄像头管理' }
          }
        ]
      },
      {
        path: 'monitoring',
        name: 'Monitoring',
        meta: { title: '监控中心', icon: 'Monitor' },
        children: [
          {
            path: 'realtime',
            name: 'Realtime',
            component: () => import('@/views/monitoring/realtime/index.vue'),
            meta: { title: '实时监控' }
          },
          {
            path: 'anomaly',
            name: 'Anomaly',
            component: () => import('@/views/monitoring/anomaly/index.vue'),
            meta: { title: '异常处理' }
          }
        ]
      },
      {
        path: 'warehouse',
        name: 'Warehouse',
        meta: { title: '仓储管理', icon: 'House' },
        children: [
          {
            path: 'list',
            name: 'WarehouseList',
            component: () => import('@/views/warehouse/list/index.vue'),
            meta: { title: '仓库列表' }
          },
          {
            path: 'cargo',
            name: 'Cargo',
            component: () => import('@/views/warehouse/cargo/index.vue'),
            meta: { title: '货物管理' }
          }
        ]
      },
      {
        path: 'parking',
        name: 'Parking',
        component: () => import('@/views/parking/index.vue'),
        meta: { title: '停车场管理', icon: 'Place' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        meta: { title: '数据统计', icon: 'DataAnalysis' },
        children: [
          {
            path: 'traffic',
            name: 'TrafficStats',
            component: () => import('@/views/statistics/traffic/index.vue'),
            meta: { title: '交通统计' }
          },
          {
            path: 'report',
            name: 'Report',
            component: () => import('@/views/statistics/report/index.vue'),
            meta: { title: '报表中心' }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'employee',
            name: 'Employee',
            component: () => import('@/views/system/employee/index.vue'),
            meta: { title: '员工管理' }
          },
          {
            path: 'department',
            name: 'Department',
            component: () => import('@/views/system/department/index.vue'),
            meta: { title: '部门管理' }
          },
          {
            path: 'role',
            name: 'Role',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理' }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
