import 'dart:convert';
import 'package:luoyi_mobile/services/http_service.dart';

/// 路线规划服务
class RouteService {
  final HttpService _httpService = HttpService();

  /// 规划路线
  Future<RouteResult> planRoute({
    required double startLat,
    required double startLng,
    required double endLat,
    required double endLng,
    String? vehicleType,
    bool avoidCongestion = false,
  }) async {
    final response = await _httpService.post('/api/route/astar', {
      'start_point': {'lat': startLat, 'lng': startLng},
      'end_point': {'lat': endLat, 'lng': endLng},
      'vehicle_type': vehicleType,
      'avoid_congestion': avoidCongestion,
    });

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return RouteResult.fromJson(data);
    }
    throw Exception('路线规划失败');
  }

  /// 获取路线推荐
  Future<List<RouteRecommendation>> getRecommendations({
    required double startLat,
    required double startLng,
    required double endLat,
    required double endLng,
  }) async {
    final response = await _httpService.post('/api/recommend/route', {
      'start_lat': startLat,
      'start_lng': startLng,
      'end_lat': endLat,
      'end_lng': endLng,
    });

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      if (data['success'] == true) {
        final recommended = RouteRecommendation.fromJson(data['recommended_route']);
        final alternatives = (data['alternative_routes'] as List?)
            ?.map((e) => RouteRecommendation.fromJson(e))
            .toList() ?? [];
        return [recommended, ...alternatives];
      }
    }
    return [];
  }

  /// 获取实时路况
  Future<TrafficInfo> getRealtimeTraffic(int roadId) async {
    final response = await _httpService.get('/api/traffic/realtime/$roadId');
    
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return TrafficInfo.fromJson(data);
    }
    throw Exception('获取路况失败');
  }

  /// 预测拥堵
  Future<CongestionPrediction> predictCongestion({
    int? roadId,
    int? junctionId,
    DateTime? targetTime,
    int hours = 1,
  }) async {
    final response = await _httpService.post('/api/predict/congestion', {
      'road_id': roadId,
      'junction_id': junctionId,
      'target_time': targetTime?.toIso8601String(),
      'prediction_hours': hours,
    });

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return CongestionPrediction.fromJson(data);
    }
    throw Exception('拥堵预测失败');
  }
}

/// 路线规划结果
class RouteResult {
  final bool success;
  final List<RoutePoint> path;
  final double totalDistance;
  final int estimatedTime;
  final double averageCongestion;

  RouteResult({
    required this.success,
    required this.path,
    required this.totalDistance,
    required this.estimatedTime,
    required this.averageCongestion,
  });

  factory RouteResult.fromJson(Map<String, dynamic> json) {
    final route = json['route'] ?? {};
    final pathData = route['path'] as List? ?? [];
    
    return RouteResult(
      success: json['success'] ?? false,
      path: pathData.map((p) => RoutePoint.fromJson(p)).toList(),
      totalDistance: (route['total_distance'] ?? 0).toDouble(),
      estimatedTime: route['estimated_time'] ?? 0,
      averageCongestion: (route['average_congestion'] ?? 0).toDouble(),
    );
  }
}

/// 路线点
class RoutePoint {
  final int id;
  final String name;
  final double lat;
  final double lng;

  RoutePoint({
    required this.id,
    required this.name,
    required this.lat,
    required this.lng,
  });

  factory RoutePoint.fromJson(Map<String, dynamic> json) {
    return RoutePoint(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      lat: (json['lat'] ?? 0).toDouble(),
      lng: (json['lng'] ?? 0).toDouble(),
    );
  }
}

/// 路线推荐
class RouteRecommendation {
  final int routeId;
  final String routeName;
  final List<int> roadIds;
  final double totalDistance;
  final int estimatedTime;
  final double congestionIndex;

  RouteRecommendation({
    required this.routeId,
    required this.routeName,
    required this.roadIds,
    required this.totalDistance,
    required this.estimatedTime,
    required this.congestionIndex,
  });

  factory RouteRecommendation.fromJson(Map<String, dynamic> json) {
    return RouteRecommendation(
      routeId: json['route_id'] ?? 0,
      routeName: json['route_name'] ?? '',
      roadIds: (json['road_ids'] as List?)?.cast<int>() ?? [],
      totalDistance: (json['total_distance'] ?? 0).toDouble(),
      estimatedTime: json['estimated_time'] ?? 0,
      congestionIndex: (json['congestion_index'] ?? 0).toDouble(),
    );
  }
}

/// 实时路况信息
class TrafficInfo {
  final int roadId;
  final double congestionIndex;
  final String congestionLevel;
  final double averageSpeed;
  final int vehicleCount;
  final DateTime timestamp;

  TrafficInfo({
    required this.roadId,
    required this.congestionIndex,
    required this.congestionLevel,
    required this.averageSpeed,
    required this.vehicleCount,
    required this.timestamp,
  });

  factory TrafficInfo.fromJson(Map<String, dynamic> json) {
    return TrafficInfo(
      roadId: json['road_id'] ?? 0,
      congestionIndex: (json['congestion_index'] ?? 0).toDouble(),
      congestionLevel: json['congestion_level'] ?? '未知',
      averageSpeed: (json['average_speed'] ?? 0).toDouble(),
      vehicleCount: json['vehicle_count'] ?? 0,
      timestamp: DateTime.tryParse(json['timestamp'] ?? '') ?? DateTime.now(),
    );
  }
}

/// 拥堵预测
class CongestionPrediction {
  final bool success;
  final List<PredictionItem> predictions;

  CongestionPrediction({
    required this.success,
    required this.predictions,
  });

  factory CongestionPrediction.fromJson(Map<String, dynamic> json) {
    final predList = json['predictions'] as List? ?? [];
    return CongestionPrediction(
      success: json['success'] ?? false,
      predictions: predList.map((p) => PredictionItem.fromJson(p)).toList(),
    );
  }
}

/// 预测项
class PredictionItem {
  final DateTime targetTime;
  final double predictedIndex;
  final String predictedLevel;
  final double confidence;
  final String? suggestion;

  PredictionItem({
    required this.targetTime,
    required this.predictedIndex,
    required this.predictedLevel,
    required this.confidence,
    this.suggestion,
  });

  factory PredictionItem.fromJson(Map<String, dynamic> json) {
    return PredictionItem(
      targetTime: DateTime.tryParse(json['target_time'] ?? '') ?? DateTime.now(),
      predictedIndex: (json['predicted_index'] ?? 0).toDouble(),
      predictedLevel: json['predicted_level'] ?? '未知',
      confidence: (json['confidence'] ?? 0).toDouble(),
      suggestion: json['suggestion'],
    );
  }
}
