import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { warehouseApi } from '@/api/warehouse'

export interface Warehouse {
  id: number
  name: string
  code: string
  type: 'transit' | 'storage' | 'distribution'
  address: string
  latitude: number
  longitude: number
  area: number
  capacity: number
  usedCapacity: number
  manager: string
  managerPhone: string
  status: 'active' | 'inactive' | 'maintenance'
  createdAt: string
  updatedAt: string
}

export interface Cargo {
  id: number
  trackingNumber: string
  name: string
  type: 'general' | 'fragile' | 'perishable' | 'dangerous' | 'valuable'
  weight: number
  volume: number
  quantity: number
  warehouseId: number
  warehouseName: string
  location: string
  status: 'inbound' | 'stored' | 'outbound' | 'transit' | 'delivered'
  sender: string
  senderPhone: string
  senderAddress: string
  receiver: string
  receiverPhone: string
  receiverAddress: string
  estimatedArrival?: string
  actualArrival?: string
  createdAt: string
  updatedAt: string
}

export interface CargoMovement {
  id: number
  cargoId: number
  type: 'inbound' | 'outbound' | 'transfer'
  fromWarehouseId?: number
  toWarehouseId?: number
  operator: string
  quantity: number
  remark: string
  createdAt: string
}

export const useWarehouseStore = defineStore('warehouse', () => {
  // 仓库数据
  const warehouses = ref<Warehouse[]>([])
  const warehouseLoading = ref(false)
  const warehouseTotal = ref(0)
  const currentWarehouse = ref<Warehouse | null>(null)
  
  // 货物数据
  const cargos = ref<Cargo[]>([])
  const cargoLoading = ref(false)
  const cargoTotal = ref(0)
  const currentCargo = ref<Cargo | null>(null)
  
  // 货物流转记录
  const movements = ref<CargoMovement[]>([])
  const movementLoading = ref(false)

  // 计算属性
  const activeWarehouses = computed(() => 
    warehouses.value.filter(w => w.status === 'active')
  )
  const totalCapacity = computed(() => 
    warehouses.value.reduce((sum, w) => sum + w.capacity, 0)
  )
  const usedCapacity = computed(() => 
    warehouses.value.reduce((sum, w) => sum + w.usedCapacity, 0)
  )
  const capacityRate = computed(() => {
    if (totalCapacity.value === 0) return 0
    return (usedCapacity.value / totalCapacity.value * 100).toFixed(2)
  })
  const storedCargos = computed(() => 
    cargos.value.filter(c => c.status === 'stored')
  )
  const transitCargos = computed(() => 
    cargos.value.filter(c => c.status === 'transit')
  )

  // 获取仓库列表
  async function fetchWarehouses(params: { 
    page?: number
    pageSize?: number
    status?: string
    type?: string
    keyword?: string
  } = {}) {
    warehouseLoading.value = true
    try {
      const res = await warehouseApi.getWarehouseList(params)
      warehouses.value = res.data.list
      warehouseTotal.value = res.data.total
    } finally {
      warehouseLoading.value = false
    }
  }

  // 获取仓库详情
  async function fetchWarehouseDetail(id: number) {
    const res = await warehouseApi.getWarehouseDetail(id)
    currentWarehouse.value = res.data
    return res.data
  }

  // 创建仓库
  async function createWarehouse(data: Partial<Warehouse>) {
    const res = await warehouseApi.createWarehouse(data)
    await fetchWarehouses()
    return res.data
  }

  // 更新仓库
  async function updateWarehouse(id: number, data: Partial<Warehouse>) {
    const res = await warehouseApi.updateWarehouse(id, data)
    await fetchWarehouses()
    return res.data
  }

  // 删除仓库
  async function deleteWarehouse(id: number) {
    await warehouseApi.deleteWarehouse(id)
    await fetchWarehouses()
  }

  // 获取货物列表
  async function fetchCargos(params: { 
    page?: number
    pageSize?: number
    status?: string
    type?: string
    warehouseId?: number
    keyword?: string
  } = {}) {
    cargoLoading.value = true
    try {
      const res = await warehouseApi.getCargoList(params)
      cargos.value = res.data.list
      cargoTotal.value = res.data.total
    } finally {
      cargoLoading.value = false
    }
  }

  // 获取货物详情
  async function fetchCargoDetail(id: number) {
    const res = await warehouseApi.getCargoDetail(id)
    currentCargo.value = res.data
    return res.data
  }

  // 创建货物
  async function createCargo(data: Partial<Cargo>) {
    const res = await warehouseApi.createCargo(data)
    await fetchCargos()
    return res.data
  }

  // 更新货物
  async function updateCargo(id: number, data: Partial<Cargo>) {
    const res = await warehouseApi.updateCargo(id, data)
    await fetchCargos()
    return res.data
  }

  // 删除货物
  async function deleteCargo(id: number) {
    await warehouseApi.deleteCargo(id)
    await fetchCargos()
  }

  // 货物入库
  async function inboundCargo(id: number, data: { warehouseId: number; location: string; quantity: number }) {
    const res = await warehouseApi.inboundCargo(id, data)
    await fetchCargos()
    return res.data
  }

  // 货物出库
  async function outboundCargo(id: number, data: { quantity: number; remark: string }) {
    const res = await warehouseApi.outboundCargo(id, data)
    await fetchCargos()
    return res.data
  }

  // 货物调拨
  async function transferCargo(id: number, data: { 
    fromWarehouseId: number
    toWarehouseId: number
    quantity: number
    remark: string
  }) {
    const res = await warehouseApi.transferCargo(id, data)
    await fetchCargos()
    return res.data
  }

  // 获取货物流转记录
  async function fetchCargoMovements(cargoId: number) {
    movementLoading.value = true
    try {
      const res = await warehouseApi.getCargoMovements(cargoId)
      movements.value = res.data.list
    } finally {
      movementLoading.value = false
    }
  }

  // 追踪货物
  async function trackCargo(trackingNumber: string) {
    const res = await warehouseApi.trackCargo(trackingNumber)
    return res.data
  }

  // 重置状态
  function reset() {
    warehouses.value = []
    cargos.value = []
    movements.value = []
    currentWarehouse.value = null
    currentCargo.value = null
  }

  return {
    warehouses,
    warehouseLoading,
    warehouseTotal,
    currentWarehouse,
    cargos,
    cargoLoading,
    cargoTotal,
    currentCargo,
    movements,
    movementLoading,
    activeWarehouses,
    totalCapacity,
    usedCapacity,
    capacityRate,
    storedCargos,
    transitCargos,
    fetchWarehouses,
    fetchWarehouseDetail,
    createWarehouse,
    updateWarehouse,
    deleteWarehouse,
    fetchCargos,
    fetchCargoDetail,
    createCargo,
    updateCargo,
    deleteCargo,
    inboundCargo,
    outboundCargo,
    transferCargo,
    fetchCargoMovements,
    trackCargo,
    reset
  }
})
