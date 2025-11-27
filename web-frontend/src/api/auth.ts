import { post, get } from '@/utils/request'
import type { LoginRequest, LoginResponse, EmployeeInfo } from '@/types/auth'

export function login(data: LoginRequest): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', data)
}

export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

export function refreshToken(token: string): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/refresh', { refreshToken: token })
}

export function getUserInfo(): Promise<EmployeeInfo> {
  return get<EmployeeInfo>('/auth/info')
}
