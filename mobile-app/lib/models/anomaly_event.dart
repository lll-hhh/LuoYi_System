/// 络绎智慧交通管理系统 - 异常事件模型

class AnomalyEvent {
  final int? id;
  final String? remoteId;
  final int? taskId;
  final AnomalyType eventType;
  final Severity severity;
  final String description;
  final double? locationLat;
  final double? locationLng;
  final String? snapshotPath;
  final bool resolved;
  final DateTime? resolvedAt;
  final String? resolvedBy;
  final String? resolution;
  final DateTime createdAt;
  final bool synced;

  AnomalyEvent({
    this.id,
    this.remoteId,
    this.taskId,
    required this.eventType,
    required this.severity,
    required this.description,
    this.locationLat,
    this.locationLng,
    this.snapshotPath,
    this.resolved = false,
    this.resolvedAt,
    this.resolvedBy,
    this.resolution,
    required this.createdAt,
    this.synced = false,
  });

  factory AnomalyEvent.fromJson(Map<String, dynamic> json) {
    return AnomalyEvent(
      id: json['id'] as int?,
      remoteId: json['remoteId'] as String?,
      taskId: json['taskId'] as int?,
      eventType: AnomalyType.fromString(json['eventType'] as String? ?? 'other'),
      severity: Severity.fromString(json['severity'] as String? ?? 'low'),
      description: json['description'] as String? ?? '',
      locationLat: (json['locationLat'] as num?)?.toDouble(),
      locationLng: (json['locationLng'] as num?)?.toDouble(),
      snapshotPath: json['snapshotPath'] as String?,
      resolved: json['resolved'] == 1 || json['resolved'] == true,
      resolvedAt: json['resolvedAt'] != null ? DateTime.parse(json['resolvedAt']) : null,
      resolvedBy: json['resolvedBy'] as String?,
      resolution: json['resolution'] as String?,
      createdAt: json['createdAt'] != null 
          ? DateTime.parse(json['createdAt']) 
          : DateTime.now(),
      synced: json['synced'] == 1 || json['synced'] == true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'remoteId': remoteId,
      'taskId': taskId,
      'eventType': eventType.value,
      'severity': severity.value,
      'description': description,
      'locationLat': locationLat,
      'locationLng': locationLng,
      'snapshotPath': snapshotPath,
      'resolved': resolved ? 1 : 0,
      'resolvedAt': resolvedAt?.toIso8601String(),
      'resolvedBy': resolvedBy,
      'resolution': resolution,
      'createdAt': createdAt.toIso8601String(),
      'synced': synced ? 1 : 0,
    };
  }

  AnomalyEvent copyWith({
    int? id,
    String? remoteId,
    int? taskId,
    AnomalyType? eventType,
    Severity? severity,
    String? description,
    double? locationLat,
    double? locationLng,
    String? snapshotPath,
    bool? resolved,
    DateTime? resolvedAt,
    String? resolvedBy,
    String? resolution,
    DateTime? createdAt,
    bool? synced,
  }) {
    return AnomalyEvent(
      id: id ?? this.id,
      remoteId: remoteId ?? this.remoteId,
      taskId: taskId ?? this.taskId,
      eventType: eventType ?? this.eventType,
      severity: severity ?? this.severity,
      description: description ?? this.description,
      locationLat: locationLat ?? this.locationLat,
      locationLng: locationLng ?? this.locationLng,
      snapshotPath: snapshotPath ?? this.snapshotPath,
      resolved: resolved ?? this.resolved,
      resolvedAt: resolvedAt ?? this.resolvedAt,
      resolvedBy: resolvedBy ?? this.resolvedBy,
      resolution: resolution ?? this.resolution,
      createdAt: createdAt ?? this.createdAt,
      synced: synced ?? this.synced,
    );
  }

  /// 获取事件类型显示名称
  String get eventTypeName => eventType.displayName;

  /// 获取严重程度显示名称
  String get severityName => severity.displayName;

  /// 获取严重程度颜色
  int get severityColor => severity.color;

  /// 获取事件类型图标
  int get eventTypeIcon => eventType.iconCodePoint;
}

/// 异常类型枚举
enum AnomalyType {
  speeding('speeding', '超速', 0xe559),
  illegalParking('illegal_parking', '违规停车', 0xe54f),
  routeDeviation('route_deviation', '偏离路线', 0xe55b),
  longStay('long_stay', '长时间滞留', 0xe8b5),
  accident('accident', '事故', 0xe002),
  breakdown('breakdown', '故障', 0xe1a4),
  weatherAlert('weather_alert', '天气预警', 0xf15c),
  trafficJam('traffic_jam', '交通拥堵', 0xe531),
  other('other', '其他', 0xe8fd);

  final String value;
  final String displayName;
  final int iconCodePoint;

  const AnomalyType(this.value, this.displayName, this.iconCodePoint);

  static AnomalyType fromString(String value) {
    return AnomalyType.values.firstWhere(
      (e) => e.value == value.toLowerCase(),
      orElse: () => AnomalyType.other,
    );
  }
}

/// 严重程度枚举
enum Severity {
  low('low', '低', 0xFF4CAF50),
  medium('medium', '中', 0xFFFFC107),
  high('high', '高', 0xFFFF9800),
  critical('critical', '紧急', 0xFFF44336);

  final String value;
  final String displayName;
  final int color;

  const Severity(this.value, this.displayName, this.color);

  static Severity fromString(String value) {
    return Severity.values.firstWhere(
      (e) => e.value == value.toLowerCase(),
      orElse: () => Severity.low,
    );
  }
}
