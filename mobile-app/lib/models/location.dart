/// 络绎智慧交通管理系统 - 位置记录模型

class LocationRecord {
  final int? id;
  final int? taskId;
  final double latitude;
  final double longitude;
  final double? speed;
  final double? heading;
  final double? accuracy;
  final double? altitude;
  final DateTime recordedAt;
  final bool synced;

  LocationRecord({
    this.id,
    this.taskId,
    required this.latitude,
    required this.longitude,
    this.speed,
    this.heading,
    this.accuracy,
    this.altitude,
    required this.recordedAt,
    this.synced = false,
  });

  factory LocationRecord.fromJson(Map<String, dynamic> json) {
    return LocationRecord(
      id: json['id'] as int?,
      taskId: json['taskId'] as int?,
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      speed: (json['speed'] as num?)?.toDouble(),
      heading: (json['heading'] as num?)?.toDouble(),
      accuracy: (json['accuracy'] as num?)?.toDouble(),
      altitude: (json['altitude'] as num?)?.toDouble(),
      recordedAt: json['recordedAt'] != null 
          ? DateTime.parse(json['recordedAt']) 
          : DateTime.now(),
      synced: json['synced'] == 1 || json['synced'] == true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'taskId': taskId,
      'latitude': latitude,
      'longitude': longitude,
      'speed': speed,
      'heading': heading,
      'accuracy': accuracy,
      'altitude': altitude,
      'recordedAt': recordedAt.toIso8601String(),
      'synced': synced ? 1 : 0,
    };
  }

  LocationRecord copyWith({
    int? id,
    int? taskId,
    double? latitude,
    double? longitude,
    double? speed,
    double? heading,
    double? accuracy,
    double? altitude,
    DateTime? recordedAt,
    bool? synced,
  }) {
    return LocationRecord(
      id: id ?? this.id,
      taskId: taskId ?? this.taskId,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      speed: speed ?? this.speed,
      heading: heading ?? this.heading,
      accuracy: accuracy ?? this.accuracy,
      altitude: altitude ?? this.altitude,
      recordedAt: recordedAt ?? this.recordedAt,
      synced: synced ?? this.synced,
    );
  }

  /// 格式化速度显示 (km/h)
  String get formattedSpeed {
    if (speed == null) return '--';
    return '${(speed! * 3.6).toStringAsFixed(1)} km/h';
  }

  /// 格式化坐标显示
  String get formattedCoordinates {
    return '${latitude.toStringAsFixed(6)}, ${longitude.toStringAsFixed(6)}';
  }
}

/// 路线点
class RoutePoint {
  final double latitude;
  final double longitude;
  final String? description;

  RoutePoint({
    required this.latitude,
    required this.longitude,
    this.description,
  });

  factory RoutePoint.fromJson(Map<String, dynamic> json) {
    return RoutePoint(
      latitude: (json['lat'] as num).toDouble(),
      longitude: (json['lng'] as num).toDouble(),
      description: json['description'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'lat': latitude,
      'lng': longitude,
      'description': description,
    };
  }
}

/// 路线信息
class RouteInfo {
  final int? routeId;
  final String? routeName;
  final List<int>? roadIds;
  final double totalDistance;
  final int estimatedTime;
  final double congestionIndex;
  final List<RoutePoint> wayPoints;

  RouteInfo({
    this.routeId,
    this.routeName,
    this.roadIds,
    required this.totalDistance,
    required this.estimatedTime,
    required this.congestionIndex,
    this.wayPoints = const [],
  });

  factory RouteInfo.fromJson(Map<String, dynamic> json) {
    return RouteInfo(
      routeId: json['routeId'] as int?,
      routeName: json['routeName'] as String?,
      roadIds: (json['roadIds'] as List?)?.map((e) => e as int).toList(),
      totalDistance: (json['totalDistance'] as num).toDouble(),
      estimatedTime: json['estimatedTime'] as int? ?? 0,
      congestionIndex: (json['congestionIndex'] as num?)?.toDouble() ?? 0.0,
      wayPoints: (json['wayPoints'] as List?)
          ?.map((e) => RoutePoint.fromJson(e))
          .toList() ?? [],
    );
  }

  /// 格式化距离
  String get formattedDistance {
    if (totalDistance < 1) {
      return '${(totalDistance * 1000).toStringAsFixed(0)} 米';
    }
    return '${totalDistance.toStringAsFixed(1)} 公里';
  }

  /// 格式化时间
  String get formattedTime {
    if (estimatedTime < 60) {
      return '$estimatedTime 分钟';
    }
    int hours = estimatedTime ~/ 60;
    int minutes = estimatedTime % 60;
    return '$hours 小时 $minutes 分钟';
  }

  /// 拥堵程度描述
  String get congestionLevel {
    if (congestionIndex < 1.5) return '畅通';
    if (congestionIndex < 2.0) return '轻度拥堵';
    if (congestionIndex < 2.5) return '中度拥堵';
    return '严重拥堵';
  }

  /// 拥堵程度颜色
  int get congestionColor {
    if (congestionIndex < 1.5) return 0xFF4CAF50;
    if (congestionIndex < 2.0) return 0xFFFFC107;
    if (congestionIndex < 2.5) return 0xFFFF9800;
    return 0xFFF44336;
  }
}
