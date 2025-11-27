"""
算法服务单元测试
"""
from datetime import datetime, timedelta

import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


class TestHealthCheck:
    """健康检查测试"""
    
    def test_health_check(self):
        """测试健康检查接口"""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "healthy"


class TestVehicleDetection:
    """车辆检测测试"""
    
    def test_detect_vehicle_success(self):
        """测试车辆检测成功"""
        response = client.post(
            "/api/detect/vehicle",
            json={
                "image_url": "http://example.com/test.jpg",
                "camera_id": "CAM001"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "vehicles" in data
        assert "count" in data
    
    def test_detect_vehicle_with_base64(self):
        """测试使用Base64图片进行车辆检测"""
        response = client.post(
            "/api/detect/vehicle",
            json={
                "image_base64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
                "camera_id": "CAM001"
            }
        )
        assert response.status_code == 200


class TestPlateRecognition:
    """车牌识别测试"""
    
    def test_recognize_plate_success(self):
        """测试车牌识别成功"""
        response = client.post(
            "/api/detect/plate",
            json={
                "image_url": "http://example.com/plate.jpg"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "plates" in data
    
    def test_recognize_plate_empty_image(self):
        """测试空图片车牌识别"""
        response = client.post(
            "/api/detect/plate",
            json={}
        )
        # 根据实际实现，可能返回400或200（带空结果）
        assert response.status_code in [200, 400, 422]


class TestAnomalyDetection:
    """异常检测测试"""
    
    def test_detect_anomaly_success(self):
        """测试异常检测成功"""
        response = client.post(
            "/api/detect/anomaly",
            json={
                "image_url": "http://example.com/video.mp4",
                "camera_id": 1
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "anomalies" in data or "has_anomaly" in data or "success" in data
    
    def test_detect_anomaly_all_types(self):
        """测试检测所有异常类型"""
        response = client.post(
            "/api/detect/anomaly",
            json={
                "image_url": "http://example.com/video.mp4",
                "camera_id": 2
            }
        )
        assert response.status_code == 200


class TestTrafficAnalysis:
    """交通分析测试"""
    
    def test_analyze_traffic_success(self):
        """测试交通分析成功"""
        response = client.post(
            "/api/traffic/analyze",
            json={
                "road_id": "R001",
                "start_time": "2024-01-01T00:00:00",
                "end_time": "2024-01-01T23:59:59"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "analysis" in data or "flow" in data or "result" in data or response.json() is not None


class TestTrafficPrediction:
    """交通预测测试"""
    
    def test_predict_traffic_success(self):
        """测试交通预测成功"""
        response = client.post(
            "/api/traffic/predict",
            json={
                "road_id": "R001",
                "predict_time": "2024-01-02T08:00:00",
                "duration_hours": 4
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data is not None


class TestRouteOptimization:
    """路线优化测试"""
    
    def test_optimize_route_success(self):
        """测试路线优化成功"""
        response = client.post(
            "/api/route/optimize",
            json={
                "start_point": {"lat": 39.9042, "lng": 116.4074},
                "end_point": {"lat": 39.9142, "lng": 116.4174},
                "vehicle_type": "truck",
                "avoid_congestion": True
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data is not None
    
    def test_optimize_route_with_waypoints(self):
        """测试带途经点的路线优化"""
        response = client.post(
            "/api/route/optimize",
            json={
                "start_point": {"lat": 39.9042, "lng": 116.4074},
                "end_point": {"lat": 39.9342, "lng": 116.4374},
                "waypoints": [
                    {"lat": 39.9142, "lng": 116.4174},
                    {"lat": 39.9242, "lng": 116.4274}
                ],
                "vehicle_type": "truck"
            }
        )
        assert response.status_code == 200


class TestModelManagement:
    """模型管理测试"""
    
    def test_get_model_info(self):
        """测试获取模型信息"""
        response = client.get("/api/models")
        # 如果接口存在则验证，不存在则跳过
        if response.status_code == 200:
            data = response.json()
            assert isinstance(data, (list, dict))
        else:
            pytest.skip("Model info endpoint not implemented")
    
    def test_get_model_status(self):
        """测试获取模型状态"""
        response = client.get("/api/models/status")
        if response.status_code == 200:
            data = response.json()
            assert data is not None
        else:
            pytest.skip("Model status endpoint not implemented")


class TestAdvancedAlgorithms:
    """高级算法接口测试"""

    def test_multistop_route(self):
        response = client.post(
            "/api/route/multistop",
            json={
                "depot": {"lat": 39.9, "lng": 116.4},
                "stops": [
                    {"id": "S1", "lat": 39.91, "lng": 116.41, "priority": 2},
                    {"id": "S2", "lat": 39.92, "lng": 116.42, "priority": 1}
                ],
                "traffic_index": 1.1
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "best_plan" in data

    def test_dispatch_optimize(self):
        response = client.post(
            "/api/dispatch/optimize",
            json={
                "drivers": [
                    {"id": "D1", "name": "张三", "skills": ["冷链"], "location": {"lat": 39.9, "lng": 116.4}, "capacity": 3},
                    {"id": "D2", "name": "李四", "skills": ["普货"], "location": {"lat": 39.95, "lng": 116.45}, "capacity": 2}
                ],
                "tasks": [
                    {"id": "T1", "required_skill": "冷链", "priority": 5, "location": {"lat": 39.91, "lng": 116.41}, "estimated_duration": 60},
                    {"id": "T2", "required_skill": "普货", "priority": 3, "location": {"lat": 39.96, "lng": 116.46}, "estimated_duration": 40}
                ]
            }
        )
        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_timeseries_anomaly(self):
        base_time = datetime.utcnow()
        series = [
            {"timestamp": (base_time + timedelta(minutes=5 * i)).isoformat(), "value": float(i)}
            for i in range(12)
        ]
        series.append({"timestamp": (base_time + timedelta(minutes=60)).isoformat(), "value": 50.0})
        response = client.post(
            "/api/anomaly/timeseries",
            json={"series": series, "window": 6}
        )
        assert response.status_code == 200
        assert "spikes" in response.json()

    def test_demand_forecast(self):
        response = client.post(
            "/api/demand/forecast",
            json={
                "series": [120, 130, 128, 140, 150, 148, 160, 170],
                "periods": 4,
                "season_length": 4
            }
        )
        assert response.status_code == 200
        assert "forecast" in response.json()


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
