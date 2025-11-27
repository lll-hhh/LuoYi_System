import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { LoginRequest, LoginResponse } from '@/types/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<LoginResponse['employeeInfo'] | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function loginAction(loginData: LoginRequest) {
    const res = await login(loginData)
    token.value = res.accessToken
    userInfo.value = res.employeeInfo
    localStorage.setItem('token', res.accessToken)
    localStorage.setItem('refreshToken', res.refreshToken)
    return res
  }

  async function logoutAction() {
    try {
      await logout()
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    }
  }

  async function getUserInfoAction() {
    const res = await getUserInfo()
    userInfo.value = res
    return res
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    loginAction,
    logoutAction,
    getUserInfoAction
  }
})
