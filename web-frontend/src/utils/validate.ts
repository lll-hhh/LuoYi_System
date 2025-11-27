/**
 * 表单验证规则
 */

// 手机号验证
export const phoneRule = {
  pattern: /^1[3-9]\d{9}$/,
  message: '请输入正确的手机号码',
  trigger: 'blur'
}

// 邮箱验证
export const emailRule = {
  type: 'email' as const,
  message: '请输入正确的邮箱地址',
  trigger: 'blur'
}

// 必填验证
export const requiredRule = (message = '此项为必填项') => ({
  required: true,
  message,
  trigger: 'blur'
})

// 长度验证
export const lengthRule = (min: number, max: number, message?: string) => ({
  min,
  max,
  message: message || `长度应在 ${min} 到 ${max} 个字符之间`,
  trigger: 'blur'
})

// 数字范围验证
export const rangeRule = (min: number, max: number, message?: string) => ({
  type: 'number' as const,
  min,
  max,
  message: message || `数值应在 ${min} 到 ${max} 之间`,
  trigger: 'blur'
})

// 用户名验证
export const usernameRule = {
  pattern: /^[a-zA-Z][a-zA-Z0-9_]{3,15}$/,
  message: '用户名需以字母开头，4-16位字母、数字或下划线',
  trigger: 'blur'
}

// 密码验证
export const passwordRule = {
  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,20}$/,
  message: '密码需8-20位，包含大小写字母和数字',
  trigger: 'blur'
}

// 身份证验证
export const idCardRule = {
  pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
  message: '请输入正确的身份证号码',
  trigger: 'blur'
}

// 车牌号验证
export const plateNumberRule = {
  pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5,6}$/,
  message: '请输入正确的车牌号',
  trigger: 'blur'
}

// URL验证
export const urlRule = {
  type: 'url' as const,
  message: '请输入正确的URL地址',
  trigger: 'blur'
}

// IP地址验证
export const ipRule = {
  pattern: /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
  message: '请输入正确的IP地址',
  trigger: 'blur'
}

// 自定义验证器
export const createValidator = (
  validator: (value: any) => boolean | string,
  message = '验证失败'
) => ({
  validator: (_rule: any, value: any, callback: (error?: Error) => void) => {
    const result = validator(value)
    if (result === true) {
      callback()
    } else {
      callback(new Error(typeof result === 'string' ? result : message))
    }
  },
  trigger: 'blur'
})

// 确认密码验证
export const confirmPasswordRule = (getPassword: () => string) => ({
  validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
    if (value !== getPassword()) {
      callback(new Error('两次输入的密码不一致'))
    } else {
      callback()
    }
  },
  trigger: 'blur'
})
