import 'package:flutter/material.dart';
import '../models/transport_task.dart';
import '../models/anomaly_event.dart';

/// 任务状态徽章组件
class TaskStatusBadge extends StatelessWidget {
  final TaskStatus status;
  final double fontSize;
  final EdgeInsets padding;

  const TaskStatusBadge({
    super.key,
    required this.status,
    this.fontSize = 12,
    this.padding = const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: _getBackgroundColor(),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        _getStatusText(),
        style: TextStyle(
          color: _getTextColor(),
          fontSize: fontSize,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  String _getStatusText() {
    switch (status) {
      case TaskStatus.pending:
        return '待分配';
      case TaskStatus.assigned:
        return '已分配';
      case TaskStatus.inProgress:
        return '进行中';
      case TaskStatus.completed:
        return '已完成';
      case TaskStatus.cancelled:
        return '已取消';
      case TaskStatus.abnormal:
        return '异常';
    }
  }

  Color _getBackgroundColor() {
    switch (status) {
      case TaskStatus.pending:
        return Colors.grey[200]!;
      case TaskStatus.assigned:
        return Colors.blue[100]!;
      case TaskStatus.inProgress:
        return Colors.orange[100]!;
      case TaskStatus.completed:
        return Colors.green[100]!;
      case TaskStatus.cancelled:
        return Colors.red[100]!;
      case TaskStatus.abnormal:
        return Colors.purple[100]!;
    }
  }

  Color _getTextColor() {
    switch (status) {
      case TaskStatus.pending:
        return Colors.grey[700]!;
      case TaskStatus.assigned:
        return Colors.blue[700]!;
      case TaskStatus.inProgress:
        return Colors.orange[700]!;
      case TaskStatus.completed:
        return Colors.green[700]!;
      case TaskStatus.cancelled:
        return Colors.red[700]!;
      case TaskStatus.abnormal:
        return Colors.purple[700]!;
    }
  }
}

/// 任务优先级徽章组件
class TaskPriorityBadge extends StatelessWidget {
  final TaskPriority priority;
  final double fontSize;
  final EdgeInsets padding;

  const TaskPriorityBadge({
    super.key,
    required this.priority,
    this.fontSize = 12,
    this.padding = const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: _getBackgroundColor(),
        borderRadius: BorderRadius.circular(4),
        border: Border.all(color: _getBorderColor(), width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            _getIcon(),
            size: fontSize + 2,
            color: _getTextColor(),
          ),
          const SizedBox(width: 4),
          Text(
            _getPriorityText(),
            style: TextStyle(
              color: _getTextColor(),
              fontSize: fontSize,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  String _getPriorityText() {
    switch (priority) {
      case TaskPriority.low:
        return '低';
      case TaskPriority.normal:
        return '普通';
      case TaskPriority.high:
        return '高';
      case TaskPriority.urgent:
        return '紧急';
    }
  }

  IconData _getIcon() {
    switch (priority) {
      case TaskPriority.low:
        return Icons.arrow_downward;
      case TaskPriority.normal:
        return Icons.remove;
      case TaskPriority.high:
        return Icons.arrow_upward;
      case TaskPriority.urgent:
        return Icons.priority_high;
    }
  }

  Color _getBackgroundColor() {
    switch (priority) {
      case TaskPriority.low:
        return Colors.green[50]!;
      case TaskPriority.normal:
        return Colors.blue[50]!;
      case TaskPriority.high:
        return Colors.orange[50]!;
      case TaskPriority.urgent:
        return Colors.red[50]!;
    }
  }

  Color _getBorderColor() {
    switch (priority) {
      case TaskPriority.low:
        return Colors.green[300]!;
      case TaskPriority.normal:
        return Colors.blue[300]!;
      case TaskPriority.high:
        return Colors.orange[300]!;
      case TaskPriority.urgent:
        return Colors.red[300]!;
    }
  }

  Color _getTextColor() {
    switch (priority) {
      case TaskPriority.low:
        return Colors.green[700]!;
      case TaskPriority.normal:
        return Colors.blue[700]!;
      case TaskPriority.high:
        return Colors.orange[700]!;
      case TaskPriority.urgent:
        return Colors.red[700]!;
    }
  }
}

/// 异常严重程度徽章组件
class SeverityBadge extends StatelessWidget {
  final Severity severity;
  final double fontSize;
  final EdgeInsets padding;

  const SeverityBadge({
    super.key,
    required this.severity,
    this.fontSize = 12,
    this.padding = const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: _getBackgroundColor(),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            _getIcon(),
            size: fontSize + 2,
            color: Colors.white,
          ),
          const SizedBox(width: 4),
          Text(
            _getSeverityText(),
            style: TextStyle(
              color: Colors.white,
              fontSize: fontSize,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  String _getSeverityText() {
    switch (severity) {
      case Severity.low:
        return '轻微';
      case Severity.medium:
        return '一般';
      case Severity.high:
        return '严重';
      case Severity.critical:
        return '紧急';
    }
  }

  IconData _getIcon() {
    switch (severity) {
      case Severity.low:
        return Icons.info_outline;
      case Severity.medium:
        return Icons.warning_amber_outlined;
      case Severity.high:
        return Icons.error_outline;
      case Severity.critical:
        return Icons.dangerous_outlined;
    }
  }

  Color _getBackgroundColor() {
    switch (severity) {
      case Severity.low:
        return Colors.blue;
      case Severity.medium:
        return Colors.orange;
      case Severity.high:
        return Colors.deepOrange;
      case Severity.critical:
        return Colors.red;
    }
  }
}

/// 异常类型徽章组件
class AnomalyTypeBadge extends StatelessWidget {
  final AnomalyType type;
  final double fontSize;
  final EdgeInsets padding;

  const AnomalyTypeBadge({
    super.key,
    required this.type,
    this.fontSize = 11,
    this.padding = const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: Colors.grey[200],
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            _getIcon(),
            size: fontSize + 2,
            color: Colors.grey[700],
          ),
          const SizedBox(width: 4),
          Text(
            _getTypeText(),
            style: TextStyle(
              color: Colors.grey[700],
              fontSize: fontSize,
            ),
          ),
        ],
      ),
    );
  }

  String _getTypeText() {
    switch (type) {
      case AnomalyType.vehicleBreakdown:
        return '车辆故障';
      case AnomalyType.trafficAccident:
        return '交通事故';
      case AnomalyType.roadBlock:
        return '道路阻塞';
      case AnomalyType.weatherDelay:
        return '天气延误';
      case AnomalyType.cargoDamage:
        return '货物损坏';
      case AnomalyType.cargoLoss:
        return '货物丢失';
      case AnomalyType.routeDeviation:
        return '路线偏离';
      case AnomalyType.illegalParking:
        return '违规停车';
      case AnomalyType.speeding:
        return '超速行驶';
      case AnomalyType.fatigueDriving:
        return '疲劳驾驶';
      case AnomalyType.equipmentFailure:
        return '设备故障';
      case AnomalyType.communication:
        return '通讯异常';
      case AnomalyType.customerComplaint:
        return '客户投诉';
      case AnomalyType.other:
        return '其他';
    }
  }

  IconData _getIcon() {
    switch (type) {
      case AnomalyType.vehicleBreakdown:
        return Icons.build;
      case AnomalyType.trafficAccident:
        return Icons.car_crash;
      case AnomalyType.roadBlock:
        return Icons.block;
      case AnomalyType.weatherDelay:
        return Icons.cloud;
      case AnomalyType.cargoDamage:
        return Icons.broken_image;
      case AnomalyType.cargoLoss:
        return Icons.search_off;
      case AnomalyType.routeDeviation:
        return Icons.alt_route;
      case AnomalyType.illegalParking:
        return Icons.local_parking;
      case AnomalyType.speeding:
        return Icons.speed;
      case AnomalyType.fatigueDriving:
        return Icons.airline_seat_flat;
      case AnomalyType.equipmentFailure:
        return Icons.settings;
      case AnomalyType.communication:
        return Icons.signal_cellular_off;
      case AnomalyType.customerComplaint:
        return Icons.feedback;
      case AnomalyType.other:
        return Icons.more_horiz;
    }
  }
}
