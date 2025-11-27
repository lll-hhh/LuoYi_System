/// 络绎智慧交通管理系统 - 运输任务模型

class TransportTask {
  final int? id;
  final String taskNumber;
  final int? vehicleId;
  final int? cargoId;
  final String? driverName;
  final String? driverPhone;
  final String startLocation;
  final String endLocation;
  final double? startLat;
  final double? startLng;
  final double? endLat;
  final double? endLng;
  final DateTime? startTime;
  final DateTime? endTime;
  final DateTime? estimatedArrival;
  final DateTime? actualArrival;
  final double? totalDistance;
  final TaskStatus status;
  final String? notes;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  TransportTask({
    this.id,
    required this.taskNumber,
    this.vehicleId,
    this.cargoId,
    this.driverName,
    this.driverPhone,
    required this.startLocation,
    required this.endLocation,
    this.startLat,
    this.startLng,
    this.endLat,
    this.endLng,
    this.startTime,
    this.endTime,
    this.estimatedArrival,
    this.actualArrival,
    this.totalDistance,
    this.status = TaskStatus.pending,
    this.notes,
    this.createdAt,
    this.updatedAt,
  });

  factory TransportTask.fromJson(Map<String, dynamic> json) {
    return TransportTask(
      id: json['id'] as int?,
      taskNumber: json['taskNumber'] as String? ?? '',
      vehicleId: json['vehicleId'] as int?,
      cargoId: json['cargoId'] as int?,
      driverName: json['driverName'] as String?,
      driverPhone: json['driverPhone'] as String?,
      startLocation: json['startLocation'] as String? ?? '',
      endLocation: json['endLocation'] as String? ?? '',
      startLat: (json['startLat'] as num?)?.toDouble(),
      startLng: (json['startLng'] as num?)?.toDouble(),
      endLat: (json['endLat'] as num?)?.toDouble(),
      endLng: (json['endLng'] as num?)?.toDouble(),
      startTime: json['startTime'] != null ? DateTime.parse(json['startTime']) : null,
      endTime: json['endTime'] != null ? DateTime.parse(json['endTime']) : null,
      estimatedArrival: json['estimatedArrival'] != null ? DateTime.parse(json['estimatedArrival']) : null,
      actualArrival: json['actualArrival'] != null ? DateTime.parse(json['actualArrival']) : null,
      totalDistance: (json['totalDistance'] as num?)?.toDouble(),
      status: TaskStatus.fromString(json['status'] as String? ?? 'pending'),
      notes: json['notes'] as String?,
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt']) : null,
      updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'taskNumber': taskNumber,
      'vehicleId': vehicleId,
      'cargoId': cargoId,
      'driverName': driverName,
      'driverPhone': driverPhone,
      'startLocation': startLocation,
      'endLocation': endLocation,
      'startLat': startLat,
      'startLng': startLng,
      'endLat': endLat,
      'endLng': endLng,
      'startTime': startTime?.toIso8601String(),
      'endTime': endTime?.toIso8601String(),
      'estimatedArrival': estimatedArrival?.toIso8601String(),
      'actualArrival': actualArrival?.toIso8601String(),
      'totalDistance': totalDistance,
      'status': status.value,
      'notes': notes,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  TransportTask copyWith({
    int? id,
    String? taskNumber,
    int? vehicleId,
    int? cargoId,
    String? driverName,
    String? driverPhone,
    String? startLocation,
    String? endLocation,
    double? startLat,
    double? startLng,
    double? endLat,
    double? endLng,
    DateTime? startTime,
    DateTime? endTime,
    DateTime? estimatedArrival,
    DateTime? actualArrival,
    double? totalDistance,
    TaskStatus? status,
    String? notes,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return TransportTask(
      id: id ?? this.id,
      taskNumber: taskNumber ?? this.taskNumber,
      vehicleId: vehicleId ?? this.vehicleId,
      cargoId: cargoId ?? this.cargoId,
      driverName: driverName ?? this.driverName,
      driverPhone: driverPhone ?? this.driverPhone,
      startLocation: startLocation ?? this.startLocation,
      endLocation: endLocation ?? this.endLocation,
      startLat: startLat ?? this.startLat,
      startLng: startLng ?? this.startLng,
      endLat: endLat ?? this.endLat,
      endLng: endLng ?? this.endLng,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      estimatedArrival: estimatedArrival ?? this.estimatedArrival,
      actualArrival: actualArrival ?? this.actualArrival,
      totalDistance: totalDistance ?? this.totalDistance,
      status: status ?? this.status,
      notes: notes ?? this.notes,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  /// 是否可以开始任务
  bool get canStart => status == TaskStatus.pending;

  /// 是否进行中
  bool get isInProgress => status == TaskStatus.inProgress;

  /// 是否已完成
  bool get isCompleted => status == TaskStatus.completed;

  /// 获取状态显示文本
  String get statusText {
    switch (status) {
      case TaskStatus.pending:
        return '待开始';
      case TaskStatus.inProgress:
        return '运输中';
      case TaskStatus.completed:
        return '已完成';
      case TaskStatus.cancelled:
        return '已取消';
    }
  }

  /// 获取状态颜色
  int get statusColor {
    switch (status) {
      case TaskStatus.pending:
        return 0xFFFFA000;
      case TaskStatus.inProgress:
        return 0xFF2196F3;
      case TaskStatus.completed:
        return 0xFF4CAF50;
      case TaskStatus.cancelled:
        return 0xFF9E9E9E;
    }
  }
}

/// 任务状态枚举
enum TaskStatus {
  pending('pending'),
  inProgress('in_progress'),
  completed('completed'),
  cancelled('cancelled');

  final String value;
  const TaskStatus(this.value);

  static TaskStatus fromString(String value) {
    switch (value.toLowerCase()) {
      case 'pending':
        return TaskStatus.pending;
      case 'in_progress':
      case 'inprogress':
        return TaskStatus.inProgress;
      case 'completed':
        return TaskStatus.completed;
      case 'cancelled':
      case 'canceled':
        return TaskStatus.cancelled;
      default:
        return TaskStatus.pending;
    }
  }
}
