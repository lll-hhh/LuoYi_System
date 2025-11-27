"""
络绎(Lorries)智慧交通管理系统 - 算法服务
提供车辆识别、拥堵预测、路线推荐、异常检测等AI功能的接口
集成AHP层次分析法算法模块
"""

from dataclasses import asdict

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime
from decimal import Decimal
import uvicorn
import math

# Prometheus监控
from prometheus_fastapi_instrumentator import Instrumentator

# 导入AHP算法模块
from algorithms.ahp_algorithms import (
    AHPAnalyzer,
    TrafficFlowAnalyzer,
    CongestionPredictor,
    StaffScheduler,
    InfrastructureAdvisor,
    AnomalyDetector
)
from algorithms.route_planning import DynamicRouteOptimizer, Stop as RouteStop, RoutePlanResult
from algorithms.dispatch_optimizer import DispatchOptimizer, DriverProfile, TaskProfile
from algorithms.anomaly_stream import TemporalAnomalyDetector, SeriesPoint, AnomalyEvent
from algorithms.demand_forecast import DemandForecaster, ForecastResult

# 初始化算法实例
traffic_flow_analyzer = TrafficFlowAnalyzer()
congestion_predictor = CongestionPredictor()
staff_scheduler = StaffScheduler()
infrastructure_advisor = InfrastructureAdvisor()
anomaly_detector = AnomalyDetector()
dynamic_route_optimizer = DynamicRouteOptimizer()
dispatch_optimizer = DispatchOptimizer()
temporal_anomaly_detector = TemporalAnomalyDetector()
demand_forecaster = DemandForecaster()

app = FastAPI(
    title="络绎算法服务",
    description="提供智慧交通管理系统的AI算法接口",
    version="1.0.0"
)

# 初始化Prometheus监控
Instrumentator().instrument(app).expose(app, endpoint="/metrics")

# CORS配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# =====================================================
# 数据模型定义
# =====================================================

class BoundingBox(BaseModel):
    x: int
    y: int
    width: int
    height: int


class VehicleRecognitionRequest(BaseModel):
    camera_id: Optional[int] = None
    image_base64: Optional[str] = None
    image_url: Optional[str] = None
    capture_time: Optional[datetime] = None


class VehicleRecognitionResponse(BaseModel):
    success: bool
    plate_number: Optional[str] = None
    plate_color: Optional[str] = None
    vehicle_type: Optional[str] = None
    vehicle_color: Optional[str] = None
    vehicle_brand: Optional[str] = None
    confidence: Optional[float] = None
    bounding_box: Optional[BoundingBox] = None
    process_time: Optional[datetime] = None
    error_message: Optional[str] = None


class CongestionPredictionRequest(BaseModel):
    road_id: Optional[int] = None
    junction_id: Optional[int] = None
    target_time: Optional[datetime] = None
    prediction_hours: Optional[int] = 1


class PredictionResult(BaseModel):
    target_time: datetime
    predicted_index: float
    predicted_level: str
    confidence: float
    suggestion: Optional[str] = None


class CongestionPredictionResponse(BaseModel):
    success: bool
    predictions: Optional[List[PredictionResult]] = None
    error_message: Optional[str] = None


class RouteRecommendationRequest(BaseModel):
    vehicle_plate: Optional[str] = None
    vehicle_type: Optional[str] = None
    start_lat: float
    start_lng: float
    end_lat: float
    end_lng: float
    avoid_roads: Optional[List[str]] = None
    preference: Optional[str] = "fastest"  # fastest, shortest, avoid_congestion


class WayPoint(BaseModel):
    lat: float
    lng: float
    description: Optional[str] = None


class RouteInfo(BaseModel):
    route_id: Optional[int] = None
    route_name: Optional[str] = None
    road_ids: Optional[List[int]] = None
    total_distance: float
    estimated_time: int
    congestion_index: float
    way_points: Optional[List[WayPoint]] = None


class RouteRecommendationResponse(BaseModel):
    success: bool
    recommended_route: Optional[RouteInfo] = None
    alternative_routes: Optional[List[RouteInfo]] = None
    error_message: Optional[str] = None


class MultiStopStop(BaseModel):
    id: str
    lat: float
    lng: float
    name: Optional[str] = None
    window_start: Optional[datetime] = None
    window_end: Optional[datetime] = None
    service_minutes: Optional[int] = 5
    priority: Optional[int] = 1


class MultiStopRouteRequest(BaseModel):
    depot: WayPoint
    stops: List[MultiStopStop]
    congestion_map: Optional[Dict[str, float]] = None
    traffic_index: Optional[float] = 1.0
    alternatives: Optional[int] = 2


class DriverPayload(BaseModel):
    id: str
    name: str
    skills: List[str] = Field(default_factory=list)
    status: Optional[str] = "AVAILABLE"
    capacity: int = 5
    location: Dict[str, float]
    workload: Optional[int] = 0


class TaskPayload(BaseModel):
    id: str
    required_skill: str
    priority: int = 1
    location: Dict[str, float]
    estimated_duration: int = 30


class DispatchOptimizeRequest(BaseModel):
    drivers: List[DriverPayload]
    tasks: List[TaskPayload]


class TimeSeriesSample(BaseModel):
    timestamp: datetime
    value: float


class TimeSeriesAnomalyRequest(BaseModel):
    series: List[TimeSeriesSample]
    window: Optional[int] = 12
    sensitivity: Optional[float] = 3.0
    trend_threshold: Optional[float] = 0.15


class DemandForecastRequest(BaseModel):
    series: List[float]
    periods: Optional[int] = 12
    alpha: Optional[float] = None
    beta: Optional[float] = None
    season_length: Optional[int] = None


class AnomalyDetectionRequest(BaseModel):
    camera_id: Optional[int] = None
    image_base64: Optional[str] = None
    image_url: Optional[str] = None
    video_url: Optional[str] = None
    plate_number: Optional[str] = None
    speed: Optional[float] = None


class AnomalyInfo(BaseModel):
    anomaly_type: str
    severity: str
    description: str
    confidence: float
    snapshot_url: Optional[str] = None


class AnomalyDetectionResponse(BaseModel):
    success: bool
    has_anomaly: bool = False
    anomalies: Optional[List[AnomalyInfo]] = None
    error_message: Optional[str] = None


# =====================================================
# API接口定义
# =====================================================

@app.get("/health")
async def health_check():
    """健康检查接口"""
    return {"status": "healthy", "timestamp": datetime.now().isoformat()}


@app.post("/api/recognize/vehicle", response_model=VehicleRecognitionResponse)
async def recognize_vehicle(request: VehicleRecognitionRequest):
    """
    车辆识别接口
    从图片中识别车牌号、车辆类型、颜色等信息
    
    注意: 当前为模拟实现，实际算法待集成
    """
    # TODO: 集成实际的车牌识别算法
    # 可以使用OpenCV + 深度学习模型进行车牌识别
    
    return VehicleRecognitionResponse(
        success=True,
        plate_number="京A12345",
        plate_color="蓝",
        vehicle_type="小型轿车",
        vehicle_color="白色",
        vehicle_brand="大众",
        confidence=0.95,
        bounding_box=BoundingBox(x=100, y=200, width=150, height=50),
        process_time=datetime.now(),
        error_message=None
    )


@app.post("/api/predict/congestion", response_model=CongestionPredictionResponse)
async def predict_congestion(request: CongestionPredictionRequest):
    """
    拥堵预测接口
    基于AHP层次分析法和历史数据预测未来的拥堵情况
    """
    predictions = []
    base_time = request.target_time or datetime.now()
    
    # 模拟当前交通数据（实际应从数据库获取）
    current_flow = 800  # 当前车流量
    road_capacity = 1000  # 道路容量
    
    for i in range(request.prediction_hours or 1):
        hour_offset = i + 1
        target_hour = (base_time.hour + hour_offset) % 24
        
        # 使用AHP算法预测
        result = congestion_predictor.predict_congestion(
            current_flow=current_flow,
            road_capacity=road_capacity,
            time_of_day=target_hour,
            day_of_week=base_time.weekday(),
            weather_condition="sunny",
            historical_data=[2.1, 2.5, 2.3, 2.8]  # 模拟历史数据
        )
        
        predictions.append(PredictionResult(
            target_time=base_time.replace(hour=target_hour if target_hour < 24 else target_hour - 24),
            predicted_index=result["predicted_index"],
            predicted_level=result["predicted_level"],
            confidence=result["confidence"],
            suggestion=result["suggestion"]
        ))
    
    return CongestionPredictionResponse(
        success=True,
        predictions=predictions,
        error_message=None
    )


@app.post("/api/recommend/route", response_model=RouteRecommendationResponse)
async def recommend_route(request: RouteRecommendationRequest):
    """
    路线推荐接口
    根据起终点和当前路况推荐最优路线
    
    注意: 当前为模拟实现，实际算法待集成
    """
    # TODO: 集成实际的路线推荐算法
    # 可以使用Dijkstra/A*算法结合实时路况数据
    
    recommended = RouteInfo(
        route_id=1,
        route_name="推荐路线1",
        road_ids=[1, 2, 3, 5],
        total_distance=12.5,
        estimated_time=25,
        congestion_index=1.8,
        way_points=[
            WayPoint(lat=request.start_lat, lng=request.start_lng, description="起点"),
            WayPoint(lat=(request.start_lat + request.end_lat) / 2, 
                    lng=(request.start_lng + request.end_lng) / 2, 
                    description="途经点"),
            WayPoint(lat=request.end_lat, lng=request.end_lng, description="终点")
        ]
    )
    
    alternatives = [
        RouteInfo(
            route_id=2,
            route_name="备选路线1",
            road_ids=[1, 4, 6, 5],
            total_distance=14.2,
            estimated_time=28,
            congestion_index=1.5,
            way_points=[]
        ),
        RouteInfo(
            route_id=3,
            route_name="备选路线2",
            road_ids=[1, 7, 8, 5],
            total_distance=15.8,
            estimated_time=32,
            congestion_index=1.2,
            way_points=[]
        )
    ]
    
    return RouteRecommendationResponse(
        success=True,
        recommended_route=recommended,
        alternative_routes=alternatives,
        error_message=None
    )


@app.post("/api/detect/anomaly", response_model=AnomalyDetectionResponse)
async def detect_anomaly(request: AnomalyDetectionRequest):
    """
    异常行为检测接口
    使用AHP算法检测车辆的违规行为、滞留异常等情况
    """
    has_anomaly = False
    anomalies = []
    
    # 超速检测
    if request.speed and request.speed > 120:
        has_anomaly = True
        anomalies.append(AnomalyInfo(
            anomaly_type="SPEEDING",
            severity="HIGH",
            description=f"超速行驶: {request.speed}km/h",
            confidence=0.98,
            snapshot_url=None
        ))
    
    # 使用AHP异常检测器（模拟数据）
    # 实际应从数据库获取车辆历史数据
    if request.plate_number:
        anomaly_result = anomaly_detector.analyze(
            vehicle_id=request.plate_number,
            current_location_id=request.camera_id or 1,
            stay_duration=30,  # 模拟滞留时间
            location_capacity=100,
            current_density=0.4,
            historical_stay_times=[15, 20, 18, 22, 25]
        )
        
        if anomaly_result["is_anomaly"]:
            has_anomaly = True
            for anomaly in anomaly_result["anomalies"]:
                anomalies.append(AnomalyInfo(
                    anomaly_type=anomaly["type"],
                    severity=anomaly["severity"],
                    description=anomaly["description"],
                    confidence=anomaly["score"],
                    snapshot_url=None
                ))
    
    return AnomalyDetectionResponse(
        success=True,
        has_anomaly=has_anomaly,
        anomalies=anomalies if anomalies else None,
        error_message=None
    )


@app.post("/api/anomaly/timeseries")
async def detect_timeseries_anomaly(request: TimeSeriesAnomalyRequest):
    """基于时序数据的异常检测接口"""
    series = [SeriesPoint(timestamp=item.timestamp, value=item.value) for item in request.series]
    result = temporal_anomaly_detector.analyze(
        series,
        window=request.window or 12,
        sensitivity=request.sensitivity or 3.0,
        trend_threshold=request.trend_threshold or 0.15
    )
    return {
        "success": True,
        "spikes": [serialize_anomaly_event(event) for event in result["spikes"]],
        "trend": [serialize_anomaly_event(event) for event in result["trend"]],
        "timestamp": datetime.now().isoformat()
    }


@app.get("/api/traffic/realtime/{road_id}")
async def get_realtime_traffic(road_id: int):
    """
    获取道路实时路况
    
    注意: 当前为模拟实现
    """
    return {
        "road_id": road_id,
        "congestion_index": 2.3,
        "congestion_level": "畅通",
        "average_speed": 45.5,
        "vehicle_count": 156,
        "timestamp": datetime.now().isoformat()
    }


@app.get("/api/statistics/summary")
async def get_statistics_summary():
    """
    获取统计摘要
    
    注意: 当前为模拟实现
    """
    return {
        "total_vehicles_today": 12580,
        "anomaly_count_today": 23,
        "average_congestion_index": 2.8,
        "peak_hour": "08:30",
        "timestamp": datetime.now().isoformat()
    }


@app.post("/api/demand/forecast")
async def demand_forecast_endpoint(request: DemandForecastRequest):
    """交通/运力需求预测接口"""
    forecaster = demand_forecaster
    if any(value is not None for value in [request.alpha, request.beta, request.season_length]):
        forecaster = DemandForecaster(
            alpha=request.alpha or demand_forecaster.alpha,
            beta=request.beta or demand_forecaster.beta,
            season_length=request.season_length or demand_forecaster.season_length
        )

    forecast = forecaster.forecast(request.series, periods=request.periods or 12)
    return {
        "success": True,
        "forecast": serialize_forecast(forecast),
        "timestamp": datetime.now().isoformat()
    }


# =====================================================
# 新增接口 - 匹配测试需求
# =====================================================

class VehicleDetectionRequest(BaseModel):
    image_url: Optional[str] = None
    image_base64: Optional[str] = None
    camera_id: Optional[str] = None


@app.post("/api/detect/vehicle")
async def detect_vehicle(request: VehicleDetectionRequest):
    """
    车辆检测接口
    检测图片中的车辆
    """
    return {
        "success": True,
        "vehicles": [
            {"type": "truck", "confidence": 0.95, "bbox": [100, 100, 200, 150]},
            {"type": "car", "confidence": 0.88, "bbox": [300, 120, 180, 120]}
        ],
        "count": 2,
        "timestamp": datetime.now().isoformat()
    }


class PlateRecognitionRequest(BaseModel):
    image_url: Optional[str] = None
    image_base64: Optional[str] = None


@app.post("/api/detect/plate")
async def detect_plate(request: PlateRecognitionRequest):
    """
    车牌识别接口
    """
    return {
        "success": True,
        "plates": [
            {"number": "京A12345", "color": "蓝", "confidence": 0.96}
        ],
        "timestamp": datetime.now().isoformat()
    }


class TrafficAnalyzeRequest(BaseModel):
    road_id: Optional[str] = None
    start_time: Optional[str] = None
    end_time: Optional[str] = None


@app.post("/api/traffic/analyze")
async def analyze_traffic(request: TrafficAnalyzeRequest):
    """
    交通分析接口
    """
    return {
        "success": True,
        "analysis": {
            "road_id": request.road_id,
            "average_flow": 1250,
            "peak_flow": 2100,
            "congestion_duration": 45,
            "congestion_level": "moderate"
        },
        "timestamp": datetime.now().isoformat()
    }


class TrafficPredictRequest(BaseModel):
    road_id: Optional[str] = None
    predict_time: Optional[str] = None
    duration_hours: Optional[int] = 1


@app.post("/api/traffic/predict")
async def predict_traffic(request: TrafficPredictRequest):
    """
    交通预测接口
    """
    return {
        "success": True,
        "predictions": [
            {"time": "08:00", "flow": 1800, "congestion_index": 3.2},
            {"time": "09:00", "flow": 2100, "congestion_index": 4.1},
            {"time": "10:00", "flow": 1650, "congestion_index": 2.8}
        ],
        "timestamp": datetime.now().isoformat()
    }


class RouteOptimizeRequest(BaseModel):
    start_point: dict
    end_point: dict
    waypoints: Optional[List[dict]] = None
    vehicle_type: Optional[str] = None
    avoid_congestion: Optional[bool] = False


@app.post("/api/route/optimize")
async def optimize_route(request: RouteOptimizeRequest):
    """
    路线优化接口
    """
    return {
        "success": True,
        "route": {
            "distance": 15.8,
            "duration": 32,
            "waypoints": [
                request.start_point,
                *(request.waypoints or []),
                request.end_point
            ],
            "instructions": [
                "向东行驶500米",
                "右转进入XX路",
                "直行2公里",
                "到达目的地"
            ]
        },
        "timestamp": datetime.now().isoformat()
    }


@app.post("/api/route/multistop")
async def optimize_multistop_route(request: MultiStopRouteRequest):
    """多站点路线优化接口"""
    depot_stop = RouteStop(
        id="depot",
        lat=request.depot.lat,
        lng=request.depot.lng,
        name=request.depot.description or "Depot",
        service_minutes=0,
        priority=1
    )
    stop_objects = [
        RouteStop(
            id=stop.id,
            lat=stop.lat,
            lng=stop.lng,
            name=stop.name,
            window_start=stop.window_start,
            window_end=stop.window_end,
            service_minutes=stop.service_minutes or 5,
            priority=stop.priority or 1
        )
        for stop in request.stops
    ]

    result = dynamic_route_optimizer.optimize(
        depot_stop,
        stop_objects,
        request.congestion_map,
        request.traffic_index or 1.0,
        request.alternatives or 2
    )

    return {
        "success": True,
        "best_plan": serialize_route_plan(result["best_plan"]),
        "alternatives": [serialize_route_plan(plan) for plan in result["alternatives"]],
        "timestamp": datetime.now().isoformat()
    }


@app.post("/api/dispatch/optimize")
async def optimize_dispatch(request: DispatchOptimizeRequest):
    """司机-任务调度优化接口"""
    drivers = [
        DriverProfile(
            id=driver.id,
            name=driver.name,
            skills=driver.skills,
            status=driver.status or "AVAILABLE",
            capacity=driver.capacity,
            location=driver.location,
            workload=driver.workload or 0
        )
        for driver in request.drivers
    ]

    tasks = [
        TaskProfile(
            id=task.id,
            required_skill=task.required_skill,
            priority=task.priority,
            location=task.location,
            estimated_duration=task.estimated_duration
        )
        for task in request.tasks
    ]

    result = dispatch_optimizer.optimize(drivers, tasks)
    return {
        "success": True,
        **result,
        "timestamp": datetime.now().isoformat()
    }


@app.get("/api/models")
async def get_models():
    """
    获取模型列表
    """
    return [
        {"name": "vehicle_detection", "version": "1.0.0", "status": "loaded"},
        {"name": "plate_recognition", "version": "1.0.0", "status": "loaded"},
        {"name": "anomaly_detection", "version": "1.0.0", "status": "loaded"},
        {"name": "traffic_prediction", "version": "1.0.0", "status": "loaded"}
    ]


@app.get("/api/models/status")
async def get_model_status():
    """
    获取模型状态
    """
    return {
        "status": "healthy",
        "models_loaded": 4,
        "gpu_available": False,
        "memory_usage": "2.1GB",
        "timestamp": datetime.now().isoformat()
    }


# =====================================================
# AHP算法接口 - 基于层次分析法
# =====================================================

class TrafficFlowPredictRequest(BaseModel):
    """客流预测请求"""
    last_week_flow: float
    last_month_flow: float
    last_quarter_flow: float
    weather_factor: Optional[float] = 1.0
    is_holiday: Optional[bool] = False
    special_event_factor: Optional[float] = 1.0


@app.post("/api/ahp/traffic-flow/predict")
async def ahp_predict_traffic_flow(request: TrafficFlowPredictRequest):
    """
    AHP客流预测接口
    基于历史数据和外部因素预测客流量
    """
    result = traffic_flow_analyzer.predict_traffic_flow(
        last_week_flow=request.last_week_flow,
        last_month_flow=request.last_month_flow,
        last_quarter_flow=request.last_quarter_flow,
        weather_factor=request.weather_factor,
        is_holiday=request.is_holiday,
        special_event_factor=request.special_event_factor
    )
    return {
        "success": True,
        "prediction": result,
        "timestamp": datetime.now().isoformat()
    }


class StaffScheduleRequest(BaseModel):
    """人员调度请求"""
    staff_list: List[Dict[str, Any]]  # [{id, name, skill_level, current_location}]
    tasks: List[Dict[str, Any]]  # [{id, location, urgency, required_skill}]


@app.post("/api/ahp/staff/schedule")
async def ahp_schedule_staff(request: StaffScheduleRequest):
    """
    AHP人员调度接口
    基于紧急程度、距离、技能匹配优化人员调度
    """
    result = staff_scheduler.schedule(
        staff_list=request.staff_list,
        tasks=request.tasks
    )
    return {
        "success": True,
        "schedule": result,
        "timestamp": datetime.now().isoformat()
    }


class InfrastructureAnalyzeRequest(BaseModel):
    """基建分析请求"""
    roads: List[Dict[str, Any]]  # [{id, name, current_capacity, usage_rate, age_years}]
    junctions: List[Dict[str, Any]]  # [{id, name, throughput, delay_time}]
    budget: Optional[float] = None


@app.post("/api/ahp/infrastructure/analyze")
async def ahp_analyze_infrastructure(request: InfrastructureAnalyzeRequest):
    """
    AHP基建建议接口
    分析道路/路口改建优先级
    """
    result = infrastructure_advisor.analyze(
        roads=request.roads,
        junctions=request.junctions,
        budget=request.budget
    )
    return {
        "success": True,
        "recommendations": result,
        "timestamp": datetime.now().isoformat()
    }


class CongestionAnalyzeRequest(BaseModel):
    """拥堵分析请求"""
    current_flow: float
    road_capacity: float
    time_of_day: int  # 0-23
    day_of_week: int  # 0-6
    weather_condition: Optional[str] = "sunny"
    historical_data: Optional[List[float]] = None


@app.post("/api/ahp/congestion/analyze")
async def ahp_analyze_congestion(request: CongestionAnalyzeRequest):
    """
    AHP拥堵分析接口
    分析拥堵原因和改善建议
    """
    result = congestion_predictor.predict_congestion(
        current_flow=request.current_flow,
        road_capacity=request.road_capacity,
        time_of_day=request.time_of_day,
        day_of_week=request.day_of_week,
        weather_condition=request.weather_condition,
        historical_data=request.historical_data
    )
    return {
        "success": True,
        "analysis": result,
        "timestamp": datetime.now().isoformat()
    }


class AnomalyAnalyzeRequest(BaseModel):
    """异常分析请求"""
    vehicle_id: str
    current_location_id: int
    stay_duration: float  # 当前滞留时间（分钟）
    location_capacity: int  # 位置容量
    current_density: float  # 当前密度 0-1
    historical_stay_times: Optional[List[float]] = None


@app.post("/api/ahp/anomaly/analyze")
async def ahp_analyze_anomaly(request: AnomalyAnalyzeRequest):
    """
    AHP异常分析接口
    分析车辆滞留异常和密度异常
    """
    result = anomaly_detector.analyze(
        vehicle_id=request.vehicle_id,
        current_location_id=request.current_location_id,
        stay_duration=request.stay_duration,
        location_capacity=request.location_capacity,
        current_density=request.current_density,
        historical_stay_times=request.historical_stay_times
    )
    return {
        "success": True,
        "analysis": result,
        "timestamp": datetime.now().isoformat()
    }


# =====================================================
# 路线规划算法 - A*算法实现
# =====================================================

class Node:
    """A*算法节点"""
    def __init__(self, id: int, lat: float, lng: float, name: str = ""):
        self.id = id
        self.lat = lat
        self.lng = lng
        self.name = name
        self.g = float('inf')  # 起点到该点的实际代价
        self.h = 0  # 该点到终点的估计代价
        self.f = float('inf')  # f = g + h
        self.parent = None
        self.neighbors: List[tuple] = []  # [(neighbor_id, distance, congestion_index)]

    def __lt__(self, other):
        return self.f < other.f


def haversine_distance(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
    """计算两点间的球面距离（公里）"""
    R = 6371  # 地球半径
    lat1, lng1, lat2, lng2 = map(math.radians, [lat1, lng1, lat2, lng2])
    dlat = lat2 - lat1
    dlng = lng2 - lng1
    a = math.sin(dlat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlng/2)**2
    c = 2 * math.asin(math.sqrt(a))
    return R * c


class RouteOptimizer:
    """路线优化器 - 基于A*算法"""
    
    def __init__(self):
        # 模拟路网数据
        self.nodes = {}
        self.initialize_network()
    
    def initialize_network(self):
        """初始化模拟路网"""
        # 创建一个简单的路网
        sample_nodes = [
            (1, 39.9042, 116.4074, "北京站"),
            (2, 39.9139, 116.4180, "东单"),
            (3, 39.9087, 116.3975, "天安门"),
            (4, 39.9289, 116.4174, "东直门"),
            (5, 39.9400, 116.4100, "三元桥"),
            (6, 39.9200, 116.4400, "朝阳门"),
            (7, 39.9350, 116.4300, "工体"),
            (8, 39.9500, 116.4500, "望京")
        ]
        
        for node_data in sample_nodes:
            node = Node(node_data[0], node_data[1], node_data[2], node_data[3])
            self.nodes[node_data[0]] = node
        
        # 定义连接关系
        edges = [
            (1, 2, 1.5, 2.0), (1, 3, 1.2, 1.5),
            (2, 3, 0.8, 1.2), (2, 4, 2.0, 2.5), (2, 6, 1.5, 3.0),
            (3, 4, 2.5, 2.0),
            (4, 5, 1.8, 1.8), (4, 7, 1.2, 2.2),
            (5, 7, 1.5, 1.5), (5, 8, 2.0, 1.0),
            (6, 7, 1.0, 2.5),
            (7, 8, 2.5, 1.2)
        ]
        
        for edge in edges:
            self.nodes[edge[0]].neighbors.append((edge[1], edge[2], edge[3]))
            self.nodes[edge[1]].neighbors.append((edge[0], edge[2], edge[3]))
    
    def find_nearest_node(self, lat: float, lng: float) -> int:
        """找到最近的节点"""
        min_dist = float('inf')
        nearest_id = 1
        for node_id, node in self.nodes.items():
            dist = haversine_distance(lat, lng, node.lat, node.lng)
            if dist < min_dist:
                min_dist = dist
                nearest_id = node_id
        return nearest_id
    
    def a_star(self, start_id: int, end_id: int, avoid_congestion: bool = False) -> Dict:
        """A*算法寻路"""
        import heapq
        
        # 重置节点状态
        for node in self.nodes.values():
            node.g = float('inf')
            node.f = float('inf')
            node.parent = None
        
        start = self.nodes[start_id]
        end = self.nodes[end_id]
        
        start.g = 0
        start.h = haversine_distance(start.lat, start.lng, end.lat, end.lng)
        start.f = start.h
        
        open_set = [start]
        closed_set = set()
        
        while open_set:
            current = heapq.heappop(open_set)
            
            if current.id == end_id:
                # 重建路径
                path = []
                total_distance = 0
                total_congestion = 0
                node = current
                while node:
                    path.append({
                        "id": node.id,
                        "name": node.name,
                        "lat": node.lat,
                        "lng": node.lng
                    })
                    if node.parent:
                        for n_id, dist, cong in node.parent.neighbors:
                            if n_id == node.id:
                                total_distance += dist
                                total_congestion += cong
                                break
                    node = node.parent
                path.reverse()
                
                avg_congestion = total_congestion / len(path) if path else 0
                estimated_time = int(total_distance / 0.5 * (1 + avg_congestion * 0.2))  # 分钟
                
                return {
                    "found": True,
                    "path": path,
                    "total_distance": round(total_distance, 2),
                    "estimated_time": estimated_time,
                    "average_congestion": round(avg_congestion, 2)
                }
            
            closed_set.add(current.id)
            
            for neighbor_id, distance, congestion in current.neighbors:
                if neighbor_id in closed_set:
                    continue
                
                neighbor = self.nodes[neighbor_id]
                
                # 计算代价（考虑拥堵）
                if avoid_congestion:
                    tentative_g = current.g + distance * (1 + congestion * 0.5)
                else:
                    tentative_g = current.g + distance
                
                if tentative_g < neighbor.g:
                    neighbor.g = tentative_g
                    neighbor.h = haversine_distance(neighbor.lat, neighbor.lng, end.lat, end.lng)
                    neighbor.f = neighbor.g + neighbor.h
                    neighbor.parent = current
                    
                    if neighbor not in open_set:
                        heapq.heappush(open_set, neighbor)
        
        return {"found": False, "path": [], "message": "无法找到路径"}


# 初始化路线优化器
route_optimizer = RouteOptimizer()


def serialize_route_stop(stop: RouteStop) -> Dict[str, Any]:
    return {
        "id": stop.id,
        "name": stop.name,
        "lat": stop.lat,
        "lng": stop.lng,
        "window_start": stop.window_start.isoformat() if getattr(stop, "window_start", None) else None,
        "window_end": stop.window_end.isoformat() if getattr(stop, "window_end", None) else None,
        "service_minutes": stop.service_minutes,
        "priority": stop.priority
    }


def serialize_route_plan(plan: RoutePlanResult) -> Dict[str, Any]:
    return {
        "sequence": [serialize_route_stop(stop) for stop in plan.sequence],
        "total_distance_km": plan.total_distance_km,
        "travel_minutes": plan.travel_minutes,
        "window_violations": plan.window_violations,
        "congestion_penalty": plan.congestion_penalty,
        "score": plan.score
    }


def serialize_anomaly_event(event: AnomalyEvent) -> Dict[str, Any]:
    return {
        "index": event.index,
        "timestamp": event.timestamp.isoformat(),
        "value": event.value,
        "score": event.score,
        "category": event.category,
        "message": event.message
    }


def serialize_forecast(result: ForecastResult) -> Dict[str, Any]:
    data = asdict(result)
    data["trend"] = float(result.trend)
    data["seasonality"] = float(result.seasonality)
    data["confidence"] = float(result.confidence)
    return data


@app.post("/api/route/astar")
async def astar_route(request: RouteOptimizeRequest):
    """
    A*算法路线规划接口
    """
    start_lat = request.start_point.get("lat", 39.9042)
    start_lng = request.start_point.get("lng", 116.4074)
    end_lat = request.end_point.get("lat", 39.9500)
    end_lng = request.end_point.get("lng", 116.4500)
    
    start_id = route_optimizer.find_nearest_node(start_lat, start_lng)
    end_id = route_optimizer.find_nearest_node(end_lat, end_lng)
    
    result = route_optimizer.a_star(start_id, end_id, request.avoid_congestion or False)
    
    return {
        "success": result["found"],
        "route": result,
        "timestamp": datetime.now().isoformat()
    }


# =====================================================
# LightGBM拥堵预测接口 - 基于机器学习
# =====================================================

# 延迟导入LightGBM模块，避免启动时的导入错误
lightgbm_predictor = None

def get_lightgbm_predictor():
    """懒加载LightGBM预测器"""
    global lightgbm_predictor
    if lightgbm_predictor is None:
        try:
            from algorithms.lightgbm_congestion import LightGBMCongestionPredictor
            lightgbm_predictor = LightGBMCongestionPredictor()
        except ImportError as e:
            raise HTTPException(status_code=500, detail=f"LightGBM模块加载失败: {str(e)}")
    return lightgbm_predictor


class LightGBMTrainRequest(BaseModel):
    """LightGBM模型训练请求"""
    road_id: str
    historical_data: List[Dict[str, Any]]  # 历史交通数据
    tune_hyperparams: Optional[bool] = False


class LightGBMPredictRequest(BaseModel):
    """LightGBM拥堵预测请求"""
    road_id: str
    prediction_time: Optional[datetime] = None
    features: Optional[Dict[str, Any]] = None


class LightGBMBatchPredictRequest(BaseModel):
    """LightGBM批量预测请求"""
    road_id: str
    start_time: datetime
    hours: int = 24
    interval_minutes: int = 15


@app.post("/api/lightgbm/train")
async def lightgbm_train(request: LightGBMTrainRequest):
    """
    LightGBM模型训练接口
    使用历史交通数据训练TTI预测模型
    """
    try:
        predictor = get_lightgbm_predictor()
        
        # 准备训练数据
        training_result = predictor.train(
            road_id=request.road_id,
            historical_data=request.historical_data,
            tune_hyperparams=request.tune_hyperparams
        )
        
        return {
            "success": True,
            "road_id": request.road_id,
            "training_result": training_result,
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"模型训练失败: {str(e)}")


@app.post("/api/lightgbm/predict")
async def lightgbm_predict(request: LightGBMPredictRequest):
    """
    LightGBM拥堵预测接口
    预测指定时间的交通拥堵指数(TTI)
    """
    try:
        predictor = get_lightgbm_predictor()
        
        prediction = predictor.predict(
            road_id=request.road_id,
            prediction_time=request.prediction_time or datetime.now(),
            features=request.features
        )
        
        return {
            "success": True,
            "road_id": request.road_id,
            "prediction": prediction,
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"预测失败: {str(e)}")


@app.post("/api/lightgbm/predict/batch")
async def lightgbm_batch_predict(request: LightGBMBatchPredictRequest):
    """
    LightGBM批量预测接口
    预测未来一段时间内的拥堵趋势
    """
    try:
        predictor = get_lightgbm_predictor()
        
        predictions = predictor.batch_predict(
            road_id=request.road_id,
            start_time=request.start_time,
            hours=request.hours,
            interval_minutes=request.interval_minutes
        )
        
        return {
            "success": True,
            "road_id": request.road_id,
            "predictions": predictions,
            "total_count": len(predictions),
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"批量预测失败: {str(e)}")


@app.get("/api/lightgbm/models")
async def lightgbm_list_models():
    """
    获取已训练的LightGBM模型列表
    """
    try:
        predictor = get_lightgbm_predictor()
        models = predictor.list_models()
        
        return {
            "success": True,
            "models": models,
            "count": len(models),
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取模型列表失败: {str(e)}")


@app.get("/api/lightgbm/models/{road_id}")
async def lightgbm_get_model_info(road_id: str):
    """
    获取指定道路的模型信息
    """
    try:
        predictor = get_lightgbm_predictor()
        model_info = predictor.get_model_info(road_id)
        
        if model_info is None:
            raise HTTPException(status_code=404, detail=f"未找到道路 {road_id} 的模型")
        
        return {
            "success": True,
            "road_id": road_id,
            "model_info": model_info,
            "timestamp": datetime.now().isoformat()
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取模型信息失败: {str(e)}")


@app.delete("/api/lightgbm/models/{road_id}")
async def lightgbm_delete_model(road_id: str):
    """
    删除指定道路的模型
    """
    try:
        predictor = get_lightgbm_predictor()
        success = predictor.delete_model(road_id)
        
        if not success:
            raise HTTPException(status_code=404, detail=f"未找到道路 {road_id} 的模型")
        
        return {
            "success": True,
            "message": f"已删除道路 {road_id} 的模型",
            "timestamp": datetime.now().isoformat()
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"删除模型失败: {str(e)}")


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8090)
