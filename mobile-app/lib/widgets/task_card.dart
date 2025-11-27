import 'package:flutter/material.dart';
import '../models/transport_task.dart';
import 'status_badge.dart';

/// 任务卡片组件
class TaskCard extends StatelessWidget {
  final TransportTask task;
  final VoidCallback? onTap;
  final VoidCallback? onLongPress;
  final bool showActions;
  final Function(TransportTask)? onStart;
  final Function(TransportTask)? onComplete;
  final Function(TransportTask)? onReport;

  const TaskCard({
    super.key,
    required this.task,
    this.onTap,
    this.onLongPress,
    this.showActions = false,
    this.onStart,
    this.onComplete,
    this.onReport,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: _getBorderSide(),
      ),
      child: InkWell(
        onTap: onTap,
        onLongPress: onLongPress,
        borderRadius: BorderRadius.circular(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeader(context),
            _buildContent(context),
            if (showActions && _shouldShowActions()) _buildActions(context),
          ],
        ),
      ),
    );
  }

  BorderSide _getBorderSide() {
    if (task.priority == TaskPriority.urgent) {
      return const BorderSide(color: Colors.red, width: 2);
    }
    if (task.priority == TaskPriority.high) {
      return BorderSide(color: Colors.orange[400]!, width: 1.5);
    }
    return BorderSide.none;
  }

  Widget _buildHeader(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: const BorderRadius.vertical(top: Radius.circular(12)),
      ),
      child: Row(
        children: [
          TaskStatusBadge(status: task.status),
          const SizedBox(width: 8),
          TaskPriorityBadge(priority: task.priority),
          const Spacer(),
          Text(
            '#${task.taskNumber}',
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildContent(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 货物信息
          Row(
            children: [
              Icon(Icons.inventory_2, size: 18, color: Colors.grey[600]),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  task.cargoName,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              Text(
                '${task.cargoWeight.toStringAsFixed(1)}吨',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontSize: 14,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          // 起点
          _buildLocationRow(
            icon: Icons.location_on,
            iconColor: Colors.green,
            label: '起点',
            address: task.originAddress,
          ),
          const SizedBox(height: 8),
          // 终点
          _buildLocationRow(
            icon: Icons.flag,
            iconColor: Colors.red,
            label: '终点',
            address: task.destinationAddress,
          ),
          const SizedBox(height: 12),
          // 时间信息
          Row(
            children: [
              Icon(Icons.schedule, size: 16, color: Colors.grey[500]),
              const SizedBox(width: 4),
              Text(
                '预计: ${_formatDateTime(task.plannedStartTime)} - ${_formatDateTime(task.plannedEndTime)}',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontSize: 12,
                ),
              ),
            ],
          ),
          if (task.estimatedDistance != null || task.estimatedDuration != null)
            Padding(
              padding: const EdgeInsets.only(top: 8),
              child: Row(
                children: [
                  if (task.estimatedDistance != null) ...[
                    Icon(Icons.straighten, size: 16, color: Colors.grey[500]),
                    const SizedBox(width: 4),
                    Text(
                      '${task.estimatedDistance!.toStringAsFixed(1)}公里',
                      style: TextStyle(color: Colors.grey[600], fontSize: 12),
                    ),
                    const SizedBox(width: 16),
                  ],
                  if (task.estimatedDuration != null) ...[
                    Icon(Icons.timer, size: 16, color: Colors.grey[500]),
                    const SizedBox(width: 4),
                    Text(
                      _formatDuration(task.estimatedDuration!),
                      style: TextStyle(color: Colors.grey[600], fontSize: 12),
                    ),
                  ],
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildLocationRow({
    required IconData icon,
    required Color iconColor,
    required String label,
    required String address,
  }) {
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.all(4),
          decoration: BoxDecoration(
            color: iconColor.withOpacity(0.1),
            borderRadius: BorderRadius.circular(4),
          ),
          child: Icon(icon, size: 16, color: iconColor),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: TextStyle(
                  color: Colors.grey[500],
                  fontSize: 11,
                ),
              ),
              Text(
                address,
                style: const TextStyle(fontSize: 13),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ],
    );
  }

  bool _shouldShowActions() {
    return task.status == TaskStatus.assigned ||
        task.status == TaskStatus.inProgress;
  }

  Widget _buildActions(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          if (task.status == TaskStatus.assigned)
            TextButton.icon(
              onPressed: () => onStart?.call(task),
              icon: const Icon(Icons.play_arrow, size: 18),
              label: const Text('开始'),
              style: TextButton.styleFrom(
                foregroundColor: Colors.blue,
              ),
            ),
          if (task.status == TaskStatus.inProgress) ...[
            TextButton.icon(
              onPressed: () => onReport?.call(task),
              icon: const Icon(Icons.report_problem, size: 18),
              label: const Text('上报'),
              style: TextButton.styleFrom(
                foregroundColor: Colors.orange,
              ),
            ),
            const SizedBox(width: 8),
            TextButton.icon(
              onPressed: () => onComplete?.call(task),
              icon: const Icon(Icons.check, size: 18),
              label: const Text('完成'),
              style: TextButton.styleFrom(
                foregroundColor: Colors.green,
              ),
            ),
          ],
        ],
      ),
    );
  }

  String _formatDateTime(DateTime dateTime) {
    return '${dateTime.month}/${dateTime.day} ${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }

  String _formatDuration(int minutes) {
    if (minutes < 60) {
      return '$minutes分钟';
    }
    final hours = minutes ~/ 60;
    final mins = minutes % 60;
    if (mins == 0) {
      return '$hours小时';
    }
    return '$hours小时$mins分钟';
  }
}

/// 简化版任务卡片
class TaskCardCompact extends StatelessWidget {
  final TransportTask task;
  final VoidCallback? onTap;

  const TaskCardCompact({
    super.key,
    required this.task,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      leading: _buildStatusIcon(),
      title: Text(
        task.cargoName,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: Text(
        '${task.originAddress} → ${task.destinationAddress}',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: TextStyle(
          color: Colors.grey[600],
          fontSize: 12,
        ),
      ),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          TaskStatusBadge(
            status: task.status,
            fontSize: 10,
            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
          ),
          const SizedBox(height: 4),
          Text(
            '#${task.taskNumber}',
            style: TextStyle(
              color: Colors.grey[500],
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusIcon() {
    Color color;
    IconData icon;

    switch (task.status) {
      case TaskStatus.pending:
        color = Colors.grey;
        icon = Icons.hourglass_empty;
        break;
      case TaskStatus.assigned:
        color = Colors.blue;
        icon = Icons.assignment;
        break;
      case TaskStatus.inProgress:
        color = Colors.orange;
        icon = Icons.local_shipping;
        break;
      case TaskStatus.completed:
        color = Colors.green;
        icon = Icons.check_circle;
        break;
      case TaskStatus.cancelled:
        color = Colors.red;
        icon = Icons.cancel;
        break;
      case TaskStatus.abnormal:
        color = Colors.purple;
        icon = Icons.error;
        break;
    }

    return CircleAvatar(
      backgroundColor: color.withOpacity(0.1),
      child: Icon(icon, color: color, size: 24),
    );
  }
}
