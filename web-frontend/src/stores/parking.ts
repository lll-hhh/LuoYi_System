import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { parkingApi } from '@/api/parking'

export interface ParkingLot {
  id: number
  name: string
  code: string
  type: 'surface' | 'underground' | 'multi-level'
  address: string
  latitude: number
  longitude: number
  totalSpaces: number
  availableSpaces: number
  hourlyRate: number
  dailyMaxRate: number
  openTime: string
  closeTime: string
  status: 'open' | 'closed' | 'full' | 'maintenance'
  manager: string
  managerPhone: string
  features: string[]
  createdAt: string
  updatedAt: string
}

export interface ParkingSpace {
  id: number
  lotId: number
  code: string
  floor: number
  zone: string
  type: 'regular' | 'handicap' | 'ev-charging' | 'vip' | 'reserved'
  status: 'available' | 'occupied' | 'reserved' | 'maintenance'
  vehiclePlate?: string
  entryTime?: string
  reservedBy?: string
  reservedUntil?: string
}

export interface ParkingRecord {
  id: number
  lotId: number
  lotName: string
  spaceId: number
  spaceCode: string
  vehiclePlate: string
  vehicleType: 'car' | 'motorcycle' | 'truck'
  entryTime: string
  exitTime?: string
  duration?: number
  fee?: number
  paymentStatus: 'pending' | 'paid' | 'waived'
  paymentMethod?: 'cash' | 'card' | 'wechat' | 'alipay'
  createdAt: string
}

export const useParkingStore = defineStore('parking', () => {
  // 停车场数据
  const parkingLots = ref<ParkingLot[]>([])
  const parkingLoading = ref(false)
  const parkingTotal = ref(0)
  const currentLot = ref<ParkingLot | null>(null)
  
  // 车位数据
  const spaces = ref<ParkingSpace[]>([])
  const spaceLoading = ref(false)
  
  // 停车记录
  const records = ref<ParkingRecord[]>([])
  const recordLoading = ref(false)
  const recordTotal = ref(0)

  // 计算属性
  const openLots = computed(() => 
    parkingLots.value.filter(l => l.status === 'open')
  )
  const totalSpaces = computed(() => 
    parkingLots.value.reduce((sum, l) => sum + l.totalSpaces, 0)
  )
  const availableSpaces = computed(() => 
    parkingLots.value.reduce((sum, l) => sum + l.availableSpaces, 0)
  )
  const occupancyRate = computed(() => {
    if (totalSpaces.value === 0) return 0
    return ((totalSpaces.value - availableSpaces.value) / totalSpaces.value * 100).toFixed(1)
  })
  const availableSpacesByLot = computed(() => 
    spaces.value.filter(s => s.status === 'available')
  )
  const occupiedSpacesByLot = computed(() => 
    spaces.value.filter(s => s.status === 'occupied')
  )

  // 获取停车场列表
  async function fetchParkingLots(params: {
    page?: number
    pageSize?: number
    status?: string
    type?: string
    keyword?: string
  } = {}) {
    parkingLoading.value = true
    try {
      const res = await parkingApi.getParkingLotList(params)
      parkingLots.value = res.data.list
      parkingTotal.value = res.data.total
    } finally {
      parkingLoading.value = false
    }
  }

  // 获取停车场详情
  async function fetchParkingLotDetail(id: number) {
    const res = await parkingApi.getParkingLotDetail(id)
    currentLot.value = res.data
    return res.data
  }

  // 创建停车场
  async function createParkingLot(data: Partial<ParkingLot>) {
    const res = await parkingApi.createParkingLot(data)
    await fetchParkingLots()
    return res.data
  }

  // 更新停车场
  async function updateParkingLot(id: number, data: Partial<ParkingLot>) {
    const res = await parkingApi.updateParkingLot(id, data)
    await fetchParkingLots()
    return res.data
  }

  // 删除停车场
  async function deleteParkingLot(id: number) {
    await parkingApi.deleteParkingLot(id)
    await fetchParkingLots()
  }

  // 获取车位列表
  async function fetchSpaces(lotId: number, params: {
    floor?: number
    zone?: string
    type?: string
    status?: string
  } = {}) {
    spaceLoading.value = true
    try {
      const res = await parkingApi.getSpaceList(lotId, params)
      spaces.value = res.data.list
    } finally {
      spaceLoading.value = false
    }
  }

  // 获取车位详情
  async function fetchSpaceDetail(lotId: number, spaceId: number) {
    const res = await parkingApi.getSpaceDetail(lotId, spaceId)
    return res.data
  }

  // 更新车位状态
  async function updateSpaceStatus(lotId: number, spaceId: number, status: ParkingSpace['status']) {
    const res = await parkingApi.updateSpaceStatus(lotId, spaceId, status)
    await fetchSpaces(lotId)
    return res.data
  }

  // 预约车位
  async function reserveSpace(lotId: number, spaceId: number, data: {
    reservedBy: string
    reservedUntil: string
    vehiclePlate: string
  }) {
    const res = await parkingApi.reserveSpace(lotId, spaceId, data)
    await fetchSpaces(lotId)
    return res.data
  }

  // 取消预约
  async function cancelReservation(lotId: number, spaceId: number) {
    const res = await parkingApi.cancelReservation(lotId, spaceId)
    await fetchSpaces(lotId)
    return res.data
  }

  // 获取停车记录
  async function fetchRecords(params: {
    page?: number
    pageSize?: number
    lotId?: number
    vehiclePlate?: string
    paymentStatus?: string
    startDate?: string
    endDate?: string
  } = {}) {
    recordLoading.value = true
    try {
      const res = await parkingApi.getRecordList(params)
      records.value = res.data.list
      recordTotal.value = res.data.total
    } finally {
      recordLoading.value = false
    }
  }

  // 车辆入场
  async function vehicleEntry(data: {
    lotId: number
    vehiclePlate: string
    vehicleType: 'car' | 'motorcycle' | 'truck'
    spaceId?: number
  }) {
    const res = await parkingApi.vehicleEntry(data)
    await fetchParkingLots()
    if (currentLot.value?.id === data.lotId) {
      await fetchSpaces(data.lotId)
    }
    return res.data
  }

  // 车辆出场
  async function vehicleExit(recordId: number, paymentMethod?: string) {
    const res = await parkingApi.vehicleExit(recordId, { paymentMethod })
    await fetchRecords()
    await fetchParkingLots()
    return res.data
  }

  // 计算停车费
  async function calculateFee(recordId: number) {
    const res = await parkingApi.calculateFee(recordId)
    return res.data
  }

  // 获取实时统计
  async function fetchRealtimeStats() {
    const res = await parkingApi.getRealtimeStats()
    return res.data
  }

  // 获取收入统计
  async function fetchRevenueStats(params: {
    startDate: string
    endDate: string
    lotId?: number
  }) {
    const res = await parkingApi.getRevenueStats(params)
    return res.data
  }

  // 重置状态
  function reset() {
    parkingLots.value = []
    spaces.value = []
    records.value = []
    currentLot.value = null
  }

  return {
    parkingLots,
    parkingLoading,
    parkingTotal,
    currentLot,
    spaces,
    spaceLoading,
    records,
    recordLoading,
    recordTotal,
    openLots,
    totalSpaces,
    availableSpaces,
    occupancyRate,
    availableSpacesByLot,
    occupiedSpacesByLot,
    fetchParkingLots,
    fetchParkingLotDetail,
    createParkingLot,
    updateParkingLot,
    deleteParkingLot,
    fetchSpaces,
    fetchSpaceDetail,
    updateSpaceStatus,
    reserveSpace,
    cancelReservation,
    fetchRecords,
    vehicleEntry,
    vehicleExit,
    calculateFee,
    fetchRealtimeStats,
    fetchRevenueStats,
    reset
  }
})
