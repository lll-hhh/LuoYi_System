import { ref, reactive, Ref } from 'vue'
import { ElMessage } from 'element-plus'

interface UsePaginationOptions<T> {
  fetchApi: (params: any) => Promise<{ list: T[]; total: number }>
  defaultParams?: Record<string, any>
  immediate?: boolean
}

export function usePagination<T>(options: UsePaginationOptions<T>) {
  const { fetchApi, defaultParams = {}, immediate = true } = options

  const data = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const total = ref(0)
  const pagination = reactive({
    page: 1,
    limit: 10
  })
  const searchParams = reactive<Record<string, any>>({ ...defaultParams })

  const fetchData = async () => {
    loading.value = true
    try {
      const params = {
        ...searchParams,
        page: pagination.page,
        pageSize: pagination.limit
      }
      const res = await fetchApi(params)
      data.value = res.list
      total.value = res.total
    } catch (error) {
      console.error('Fetch error:', error)
      ElMessage.error('获取数据失败')
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pagination.page = 1
    fetchData()
  }

  const handleReset = () => {
    Object.keys(searchParams).forEach(key => {
      if (key in defaultParams) {
        searchParams[key] = defaultParams[key]
      } else {
        delete searchParams[key]
      }
    })
    pagination.page = 1
    fetchData()
  }

  const handlePagination = ({ page, limit }: { page: number; limit: number }) => {
    pagination.page = page
    pagination.limit = limit
    fetchData()
  }

  const refresh = () => {
    fetchData()
  }

  if (immediate) {
    fetchData()
  }

  return {
    data,
    loading,
    total,
    pagination,
    searchParams,
    fetchData,
    handleSearch,
    handleReset,
    handlePagination,
    refresh
  }
}
