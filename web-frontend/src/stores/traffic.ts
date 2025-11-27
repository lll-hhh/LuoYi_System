import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { trafficApi } from '@/api/traffic'

export interface Road {
  id: number
  name: string
  code: string
  startPoint: string
  endPoint: string
  length: number
  lanes: number
  speedLimit: number
  status: 'normal' | 'congested' | 'blocked' | 'maintenance'
  congestionIndex: number
  avgSpeed: number
  flow: number
  createdAt: string
  updatedAt: string
}

export interface Junction {
  id: number
  name: string
  code: string
  type: 'crossroad' | 'T-junction' | 'roundabout' | 'overpass'
  latitude: number
  longitude: number
  status: 'normal' | 'congested' | 'accident' | 'maintenance'
  signalMode: 'fixed' | 'adaptive' | 'manual'
  greenTime: number
  redTime: number
  connectedRoads: number[]
  createdAt: string
  updatedAt: string
}

export interface Camera {
  id: number
  name: string
  code: string
  type: 'traffic' | 'surveillance' | 'recognition'
  roadId?: number
  junctionId?: number
  latitude: number
  longitude: number
  status: 'online' | 'offline' | 'maintenance'
  streamUrl: string
  resolution: string
  fps: number
  createdAt: string
  updatedAt: string
}

export const useTrafficStore = defineStore('traffic', () => {
  // 道路数据
  const roads = ref<Road[]>([])
  const roadLoading = ref(false)
  const roadTotal = ref(0)
  
  // 路口数据
  const junctions = ref<Junction[]>([])
  const junctionLoading = ref(false)
  const junctionTotal = ref(0)
  
  // 摄像头数据
  const cameras = ref<Camera[]>([])
  const cameraLoading = ref(false)
  const cameraTotal = ref(0)
  
  // 当前选中
  const currentRoad = ref<Road | null>(null)
  const currentJunction = ref<Junction | null>(null)
  const currentCamera = ref<Camera | null>(null)

  // 计算属性
  const normalRoads = computed(() => roads.value.filter(r => r.status === 'normal'))
  const congestedRoads = computed(() => roads.value.filter(r => r.status === 'congested'))
  const onlineCameras = computed(() => cameras.value.filter(c => c.status === 'online'))
  const offlineCameras = computed(() => cameras.value.filter(c => c.status === 'offline'))

  // 获取道路列表
  async function fetchRoads(params: { page?: number; pageSize?: number; status?: string } = {}) {
    roadLoading.value = true
    try {
      const res = await trafficApi.getRoadList(params)
      roads.value = res.data.list
      roadTotal.value = res.data.total
    } finally {
      roadLoading.value = false
    }
  }

  // 获取道路详情
  async function fetchRoadDetail(id: number) {
    const res = await trafficApi.getRoadDetail(id)
    currentRoad.value = res.data
    return res.data
  }

  // 创建道路
  async function createRoad(data: Partial<Road>) {
    const res = await trafficApi.createRoad(data)
    await fetchRoads()
    return res.data
  }

  // 更新道路
  async function updateRoad(id: number, data: Partial<Road>) {
    const res = await trafficApi.updateRoad(id, data)
    await fetchRoads()
    return res.data
  }

  // 删除道路
  async function deleteRoad(id: number) {
    await trafficApi.deleteRoad(id)
    await fetchRoads()
  }

  // 获取路口列表
  async function fetchJunctions(params: { page?: number; pageSize?: number; status?: string } = {}) {
    junctionLoading.value = true
    try {
      const res = await trafficApi.getJunctionList(params)
      junctions.value = res.data.list
      junctionTotal.value = res.data.total
    } finally {
      junctionLoading.value = false
    }
  }

  // 获取路口详情
  async function fetchJunctionDetail(id: number) {
    const res = await trafficApi.getJunctionDetail(id)
    currentJunction.value = res.data
    return res.data
  }

  // 创建路口
  async function createJunction(data: Partial<Junction>) {
    const res = await trafficApi.createJunction(data)
    await fetchJunctions()
    return res.data
  }

  // 更新路口
  async function updateJunction(id: number, data: Partial<Junction>) {
    const res = await trafficApi.updateJunction(id, data)
    await fetchJunctions()
    return res.data
  }

  // 删除路口
  async function deleteJunction(id: number) {
    await trafficApi.deleteJunction(id)
    await fetchJunctions()
  }

  // 更新信号灯配时
  async function updateSignalTiming(id: number, timing: { greenTime: number; redTime: number }) {
    const res = await trafficApi.updateSignalTiming(id, timing)
    await fetchJunctionDetail(id)
    return res.data
  }

  // 获取摄像头列表
  async function fetchCameras(params: { page?: number; pageSize?: number; status?: string } = {}) {
    cameraLoading.value = true
    try {
      const res = await trafficApi.getCameraList(params)
      cameras.value = res.data.list
      cameraTotal.value = res.data.total
    } finally {
      cameraLoading.value = false
    }
  }

  // 获取摄像头详情
  async function fetchCameraDetail(id: number) {
    const res = await trafficApi.getCameraDetail(id)
    currentCamera.value = res.data
    return res.data
  }

  // 创建摄像头
  async function createCamera(data: Partial<Camera>) {
    const res = await trafficApi.createCamera(data)
    await fetchCameras()
    return res.data
  }

  // 更新摄像头
  async function updateCamera(id: number, data: Partial<Camera>) {
    const res = await trafficApi.updateCamera(id, data)
    await fetchCameras()
    return res.data
  }

  // 删除摄像头
  async function deleteCamera(id: number) {
    await trafficApi.deleteCamera(id)
    await fetchCameras()
  }

  // 重置状态
  function reset() {
    roads.value = []
    junctions.value = []
    cameras.value = []
    currentRoad.value = null
    currentJunction.value = null
    currentCamera.value = null
  }

  return {
    roads,
    roadLoading,
    roadTotal,
    junctions,
    junctionLoading,
    junctionTotal,
    cameras,
    cameraLoading,
    cameraTotal,
    currentRoad,
    currentJunction,
    currentCamera,
    normalRoads,
    congestedRoads,
    onlineCameras,
    offlineCameras,
    fetchRoads,
    fetchRoadDetail,
    createRoad,
    updateRoad,
    deleteRoad,
    fetchJunctions,
    fetchJunctionDetail,
    createJunction,
    updateJunction,
    deleteJunction,
    updateSignalTiming,
    fetchCameras,
    fetchCameraDetail,
    createCamera,
    updateCamera,
    deleteCamera,
    reset
  }
})
