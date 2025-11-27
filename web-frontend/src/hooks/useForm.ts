import { ref, Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface UseFormOptions<T> {
  initialData: T | (() => T)
  createApi?: (data: T) => Promise<any>
  updateApi?: (id: number | string, data: T) => Promise<any>
  deleteApi?: (id: number | string) => Promise<any>
  onSuccess?: () => void
}

export function useForm<T extends Record<string, any>>(options: UseFormOptions<T>) {
  const { initialData, createApi, updateApi, deleteApi, onSuccess } = options

  const visible = ref(false)
  const isEdit = ref(false)
  const loading = ref(false)
  const formData = ref(
    typeof initialData === 'function' ? initialData() : { ...initialData }
  ) as Ref<T>
  const currentId = ref<number | string | null>(null)

  const resetForm = () => {
    formData.value = typeof initialData === 'function' ? initialData() : { ...initialData }
    currentId.value = null
  }

  const openCreate = () => {
    resetForm()
    isEdit.value = false
    visible.value = true
  }

  const openEdit = (id: number | string, data: T) => {
    formData.value = { ...data }
    currentId.value = id
    isEdit.value = true
    visible.value = true
  }

  const handleSubmit = async () => {
    loading.value = true
    try {
      if (isEdit.value && updateApi && currentId.value !== null) {
        await updateApi(currentId.value, formData.value)
        ElMessage.success('更新成功')
      } else if (createApi) {
        await createApi(formData.value)
        ElMessage.success('创建成功')
      }
      visible.value = false
      onSuccess?.()
    } catch (error) {
      console.error('Submit error:', error)
    } finally {
      loading.value = false
    }
  }

  const handleDelete = async (id: number | string, message = '确定要删除这条数据吗？') => {
    if (!deleteApi) return
    
    try {
      await ElMessageBox.confirm(message, '删除确认', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
      
      await deleteApi(id)
      ElMessage.success('删除成功')
      onSuccess?.()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('Delete error:', error)
      }
    }
  }

  const handleCancel = () => {
    visible.value = false
    resetForm()
  }

  return {
    visible,
    isEdit,
    loading,
    formData,
    currentId,
    openCreate,
    openEdit,
    handleSubmit,
    handleDelete,
    handleCancel,
    resetForm
  }
}
