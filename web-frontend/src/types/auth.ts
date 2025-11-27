export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  employeeInfo: EmployeeInfo
}

export interface EmployeeInfo {
  employeeId: number
  username: string
  realName: string
  avatar: string
  roleName: string
  roleCode: string
  departmentName: string
}
