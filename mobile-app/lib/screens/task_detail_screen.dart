import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/task_provider.dart';
import '../models/transport_task.dart';
import '../widgets/widgets.dart';

/// 任务详情屏幕
class TaskDetailScreen extends StatefulWidget {
  final String taskId;

  const TaskDetailScreen({super.key, required this.taskId});

  @override
  State<TaskDetailScreen> createState() => _TaskDetailScreenState();
}

class _TaskDetailScreenState extends State<TaskDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TaskProvider>().loadTaskDetail(widget.taskId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('任务详情'),
        actions: [
          IconButton(
            icon: const Icon(Icons.map),
            onPressed: () => _navigateToMap(),
          ),
          PopupMenuButton<String>(
            onSelected: _handleMenuAction,
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'share',
                child: ListTile(
                  leading: Icon(Icons.share),
                  title: Text('分享'),
                  contentPadding: EdgeInsets.zero,
                ),
              ),
              const PopupMenuItem(
                value: 'report',
                child: ListTile(
                  leading: Icon(Icons.report_problem),
                  title: Text('上报异常'),
                  contentPadding: EdgeInsets.zero,
                ),
              ),
            ],
          ),
        ],
      ),
      body: Consumer<TaskProvider>(
        builder: (context, taskProvider, child) {
          final task = taskProvider.selectedTask;

          if (taskProvider.isLoading && task == null) {
            return const Center(child: CircularProgressIndicator());
          }

          if (task == null) {
            return ErrorState(
              message: taskProvider.error ?? '任务不存在',
              onRetry: () =>
                  taskProvider.loadTaskDetail(widget.taskId),
            );
          }

          return RefreshIndicator(
            onRefresh: () => taskProvider.loadTaskDetail(widget.taskId),
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildStatusHeader(task),
                  _buildBasicInfo(task),
                  _buildLocationInfo(task),
                  _buildTimeInfo(task),
                  _buildCargoInfo(task),
                  if (task.notes != null && task.notes!.isNotEmpty)
                    _buildNotes(task),
                  _buildTimeline(task),
                  const SizedBox(height: 100), // 为底部按钮留空间
                ],
              ),
            ),
          );
        },
      ),
      bottomNavigationBar: Consumer<TaskProvider>(
        builder: (context, taskProvider, child) {
          final task = taskProvider.selectedTask;
          if (task == null) return const SizedBox.shrink();
          return _buildBottomActions(task, taskProvider);
        },
      ),
    );
  }

  Widget _buildStatusHeader(TransportTask task) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: _getStatusColor(task.status).withOpacity(0.1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              TaskStatusBadge(
                status: task.status,
                fontSize: 14,
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              ),
              const SizedBox(width: 12),
              TaskPriorityBadge(priority: task.priority),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            '任务编号: ${task.taskNumber}',
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '创建时间: ${_formatDateTime(task.createdAt)}',
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBasicInfo(TransportTask task) {
    return InfoCard(
      title: '基本信息',
      titleIcon: Icons.info_outline,
      children: [
        InfoRow(
          icon: Icons.local_shipping,
          label: '车辆信息',
          value: task.vehiclePlate ?? '待分配',
        ),
        InfoRow(
          icon: Icons.person,
          label: '司机',
          value: task.driverName ?? '待分配',
        ),
        InfoRow(
          icon: Icons.phone,
          label: '联系电话',
          value: task.driverPhone ?? '-',
          trailing: task.driverPhone != null
              ? IconButton(
                  icon: const Icon(Icons.call, color: Colors.green),
                  onPressed: () => _makePhoneCall(task.driverPhone!),
                )
              : null,
        ),
      ],
    );
  }

  Widget _buildLocationInfo(TransportTask task) {
    return InfoCard(
      title: '位置信息',
      titleIcon: Icons.location_on_outlined,
      trailing: IconButton(
        icon: const Icon(Icons.navigation),
        onPressed: () => _startNavigation(task),
      ),
      children: [
        _buildLocationItem(
          icon: Icons.location_on,
          iconColor: Colors.green,
          label: '起点',
          address: task.originAddress,
          latitude: task.originLatitude,
          longitude: task.originLongitude,
        ),
        const SizedBox(height: 12),
        _buildLocationItem(
          icon: Icons.flag,
          iconColor: Colors.red,
          label: '终点',
          address: task.destinationAddress,
          latitude: task.destinationLatitude,
          longitude: task.destinationLongitude,
        ),
        if (task.estimatedDistance != null || task.estimatedDuration != null)
          Padding(
            padding: const EdgeInsets.only(top: 16),
            child: Row(
              children: [
                if (task.estimatedDistance != null)
                  Expanded(
                    child: _buildStatItem(
                      icon: Icons.straighten,
                      label: '预计里程',
                      value: '${task.estimatedDistance!.toStringAsFixed(1)} 公里',
                    ),
                  ),
                if (task.estimatedDuration != null)
                  Expanded(
                    child: _buildStatItem(
                      icon: Icons.timer,
                      label: '预计时长',
                      value: _formatDuration(task.estimatedDuration!),
                    ),
                  ),
              ],
            ),
          ),
      ],
    );
  }

  Widget _buildLocationItem({
    required IconData icon,
    required Color iconColor,
    required String label,
    required String address,
    required double latitude,
    required double longitude,
  }) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: iconColor.withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, color: iconColor, size: 20),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: TextStyle(
                  color: Colors.grey[500],
                  fontSize: 12,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                address,
                style: const TextStyle(fontSize: 14),
              ),
              Text(
                '${latitude.toStringAsFixed(6)}, ${longitude.toStringAsFixed(6)}',
                style: TextStyle(
                  color: Colors.grey[500],
                  fontSize: 11,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildStatItem({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(icon, size: 20, color: Colors.grey[600]),
          const SizedBox(width: 8),
          Column(
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
                value,
                style: const TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 13,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTimeInfo(TransportTask task) {
    return InfoCard(
      title: '时间安排',
      titleIcon: Icons.schedule,
      children: [
        InfoRow(
          icon: Icons.play_arrow,
          label: '计划开始',
          value: _formatDateTime(task.plannedStartTime),
        ),
        InfoRow(
          icon: Icons.stop,
          label: '计划结束',
          value: _formatDateTime(task.plannedEndTime),
        ),
        if (task.actualStartTime != null)
          InfoRow(
            icon: Icons.play_circle,
            iconColor: Colors.green,
            label: '实际开始',
            value: _formatDateTime(task.actualStartTime!),
          ),
        if (task.actualEndTime != null)
          InfoRow(
            icon: Icons.check_circle,
            iconColor: Colors.green,
            label: '实际完成',
            value: _formatDateTime(task.actualEndTime!),
          ),
      ],
    );
  }

  Widget _buildCargoInfo(TransportTask task) {
    return InfoCard(
      title: '货物信息',
      titleIcon: Icons.inventory_2_outlined,
      children: [
        InfoRow(
          icon: Icons.label,
          label: '货物名称',
          value: task.cargoName,
        ),
        InfoRow(
          icon: Icons.scale,
          label: '货物重量',
          value: '${task.cargoWeight.toStringAsFixed(1)} 吨',
        ),
        InfoRow(
          icon: Icons.view_in_ar,
          label: '货物体积',
          value: '${task.cargoVolume.toStringAsFixed(1)} 立方米',
        ),
        if (task.cargoType != null)
          InfoRow(
            icon: Icons.category,
            label: '货物类型',
            value: task.cargoType!,
          ),
      ],
    );
  }

  Widget _buildNotes(TransportTask task) {
    return InfoCard(
      title: '备注',
      titleIcon: Icons.note_outlined,
      children: [
        Text(
          task.notes!,
          style: TextStyle(
            color: Colors.grey[700],
            height: 1.5,
          ),
        ),
      ],
    );
  }

  Widget _buildTimeline(TransportTask task) {
    final items = <Map<String, dynamic>>[];

    items.add({
      'time': _formatTime(task.createdAt),
      'title': '任务创建',
      'icon': Icons.add_circle,
      'color': Colors.grey,
      'isActive': true,
    });

    if (task.status != TaskStatus.pending) {
      items.add({
        'time': _formatTime(task.createdAt.add(const Duration(minutes: 5))),
        'title': '任务已分配',
        'subtitle': '司机: ${task.driverName ?? "未知"}',
        'icon': Icons.assignment_ind,
        'color': Colors.blue,
        'isActive': true,
      });
    }

    if (task.actualStartTime != null) {
      items.add({
        'time': _formatTime(task.actualStartTime!),
        'title': '开始运输',
        'icon': Icons.local_shipping,
        'color': Colors.orange,
        'isActive': true,
      });
    }

    if (task.actualEndTime != null) {
      items.add({
        'time': _formatTime(task.actualEndTime!),
        'title': '运输完成',
        'icon': Icons.check_circle,
        'color': Colors.green,
        'isActive': true,
      });
    }

    if (task.status == TaskStatus.inProgress) {
      items.add({
        'time': '',
        'title': '运输中...',
        'icon': Icons.more_horiz,
        'color': Colors.orange,
        'isActive': false,
      });
    }

    return InfoCard(
      title: '任务进度',
      titleIcon: Icons.timeline,
      children: [
        ...items.asMap().entries.map((entry) {
          final index = entry.key;
          final item = entry.value;
          return TimelineItem(
            time: item['time'] as String,
            title: item['title'] as String,
            subtitle: item['subtitle'] as String?,
            icon: item['icon'] as IconData,
            color: item['color'] as Color,
            isFirst: index == 0,
            isLast: index == items.length - 1,
            isActive: item['isActive'] as bool,
          );
        }),
      ],
    );
  }

  Widget _buildBottomActions(TransportTask task, TaskProvider provider) {
    if (task.status == TaskStatus.completed ||
        task.status == TaskStatus.cancelled) {
      return const SizedBox.shrink();
    }

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: SafeArea(
        child: Row(
          children: [
            if (task.status == TaskStatus.assigned)
              Expanded(
                child: LoadingButton(
                  isLoading: provider.isLoading,
                  onPressed: () => _startTask(task, provider),
                  text: '开始任务',
                  icon: Icons.play_arrow,
                  backgroundColor: Colors.blue,
                ),
              ),
            if (task.status == TaskStatus.inProgress) ...[
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => _reportAnomaly(task),
                  icon: const Icon(Icons.report_problem),
                  label: const Text('上报异常'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.orange,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: LoadingButton(
                  isLoading: provider.isLoading,
                  onPressed: () => _completeTask(task, provider),
                  text: '完成任务',
                  icon: Icons.check,
                  backgroundColor: Colors.green,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Color _getStatusColor(TaskStatus status) {
    switch (status) {
      case TaskStatus.pending:
        return Colors.grey;
      case TaskStatus.assigned:
        return Colors.blue;
      case TaskStatus.inProgress:
        return Colors.orange;
      case TaskStatus.completed:
        return Colors.green;
      case TaskStatus.cancelled:
        return Colors.red;
      case TaskStatus.abnormal:
        return Colors.purple;
    }
  }

  String _formatDateTime(DateTime dateTime) {
    return '${dateTime.year}-${dateTime.month.toString().padLeft(2, '0')}-${dateTime.day.toString().padLeft(2, '0')} ${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }

  String _formatTime(DateTime dateTime) {
    return '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }

  String _formatDuration(int minutes) {
    if (minutes < 60) return '$minutes 分钟';
    final hours = minutes ~/ 60;
    final mins = minutes % 60;
    if (mins == 0) return '$hours 小时';
    return '$hours 小时 $mins 分钟';
  }

  void _navigateToMap() {
    final task = context.read<TaskProvider>().selectedTask;
    if (task != null) {
      Navigator.pushNamed(context, '/tracking-map', arguments: task.id);
    }
  }

  void _handleMenuAction(String action) {
    switch (action) {
      case 'share':
        _shareTask();
        break;
      case 'report':
        final task = context.read<TaskProvider>().selectedTask;
        if (task != null) _reportAnomaly(task);
        break;
    }
  }

  void _shareTask() {
    // 实现分享功能
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('分享功能开发中')),
    );
  }

  void _makePhoneCall(String phone) {
    // 实现拨打电话功能
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('拨打电话: $phone')),
    );
  }

  void _startNavigation(TransportTask task) {
    // 实现导航功能
    Navigator.pushNamed(context, '/navigation', arguments: {
      'origin': {
        'latitude': task.originLatitude,
        'longitude': task.originLongitude,
      },
      'destination': {
        'latitude': task.destinationLatitude,
        'longitude': task.destinationLongitude,
      },
    });
  }

  Future<void> _startTask(TransportTask task, TaskProvider provider) async {
    await provider.startTask(task.id);
    if (provider.error == null) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('任务已开始'),
            backgroundColor: Colors.green,
          ),
        );
      }
    }
  }

  Future<void> _completeTask(TransportTask task, TaskProvider provider) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认完成'),
        content: const Text('确定要完成此任务吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('确定'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await provider.completeTask(task.id);
      if (provider.error == null && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('任务已完成'),
            backgroundColor: Colors.green,
          ),
        );
      }
    }
  }

  void _reportAnomaly(TransportTask task) {
    Navigator.pushNamed(context, '/report-anomaly', arguments: task.id);
  }
}
