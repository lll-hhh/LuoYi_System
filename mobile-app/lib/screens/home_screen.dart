import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/task_provider.dart';
import '../providers/location_provider.dart';
import '../models/transport_task.dart';
import '../widgets/widgets.dart';

/// 主页屏幕
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TaskProvider>().loadTasks();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('络绎运输'),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_outlined),
            onPressed: () => Navigator.pushNamed(context, '/notifications'),
          ),
          IconButton(
            icon: const Icon(Icons.person_outline),
            onPressed: () => Navigator.pushNamed(context, '/profile'),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await context.read<TaskProvider>().refreshTasks();
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildLocationStatus(),
              _buildQuickActions(),
              _buildStatistics(),
              _buildCurrentTasks(),
              _buildRecentAnomalies(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildLocationStatus() {
    return Consumer<LocationProvider>(
      builder: (context, locationProvider, child) {
        final location = locationProvider.currentLocation;
        
        return Container(
          margin: const EdgeInsets.all(16),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: locationProvider.isTracking
                  ? [Colors.green[600]!, Colors.green[400]!]
                  : [Colors.grey[600]!, Colors.grey[400]!],
            ),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  locationProvider.isTracking
                      ? Icons.location_on
                      : Icons.location_off,
                  color: Colors.white,
                  size: 32,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      locationProvider.isTracking ? '定位已开启' : '定位已关闭',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      location != null
                          ? '${location.latitude.toStringAsFixed(4)}, ${location.longitude.toStringAsFixed(4)}'
                          : '点击开启位置追踪',
                      style: TextStyle(
                        color: Colors.white.withOpacity(0.8),
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              Switch(
                value: locationProvider.isTracking,
                onChanged: (value) {
                  if (value) {
                    locationProvider.startTracking();
                  } else {
                    locationProvider.stopTracking();
                  }
                },
                activeColor: Colors.white,
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildQuickActions() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          Expanded(
            child: _QuickActionCard(
              icon: Icons.list_alt,
              label: '任务列表',
              color: Colors.blue,
              onTap: () => Navigator.pushNamed(context, '/tasks'),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _QuickActionCard(
              icon: Icons.map,
              label: '实时地图',
              color: Colors.green,
              onTap: () => Navigator.pushNamed(context, '/map'),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _QuickActionCard(
              icon: Icons.report_problem,
              label: '异常上报',
              color: Colors.orange,
              onTap: () => _showQuickReport(),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _QuickActionCard(
              icon: Icons.qr_code_scanner,
              label: '扫码',
              color: Colors.purple,
              onTap: () => Navigator.pushNamed(context, '/scanner'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatistics() {
    return Consumer<TaskProvider>(
      builder: (context, taskProvider, child) {
        final tasks = taskProvider.tasks;
        final inProgressCount =
            tasks.where((t) => t.status == TaskStatus.inProgress).length;
        final pendingCount =
            tasks.where((t) => t.status == TaskStatus.assigned).length;
        final completedToday = tasks.where((t) {
          final today = DateTime.now();
          return t.status == TaskStatus.completed &&
              t.actualEndTime != null &&
              t.actualEndTime!.day == today.day &&
              t.actualEndTime!.month == today.month &&
              t.actualEndTime!.year == today.year;
        }).length;
        final abnormalCount =
            tasks.where((t) => t.status == TaskStatus.abnormal).length;

        return Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                '今日统计',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: StatCard(
                      title: '进行中',
                      value: inProgressCount.toString(),
                      icon: Icons.local_shipping,
                      color: Colors.orange,
                      onTap: () => Navigator.pushNamed(context, '/tasks'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: StatCard(
                      title: '待开始',
                      value: pendingCount.toString(),
                      icon: Icons.pending_actions,
                      color: Colors.blue,
                      onTap: () => Navigator.pushNamed(context, '/tasks'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: StatCard(
                      title: '已完成',
                      value: completedToday.toString(),
                      icon: Icons.check_circle,
                      color: Colors.green,
                      onTap: () => Navigator.pushNamed(context, '/tasks'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: StatCard(
                      title: '异常',
                      value: abnormalCount.toString(),
                      icon: Icons.warning,
                      color: Colors.red,
                      onTap: () => Navigator.pushNamed(context, '/anomalies'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildCurrentTasks() {
    return Consumer<TaskProvider>(
      builder: (context, taskProvider, child) {
        final inProgressTasks = taskProvider.tasks
            .where((t) => t.status == TaskStatus.inProgress)
            .take(3)
            .toList();

        if (inProgressTasks.isEmpty) {
          return const SizedBox.shrink();
        }

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    '当前任务',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  TextButton(
                    onPressed: () => Navigator.pushNamed(context, '/tasks'),
                    child: const Text('查看全部'),
                  ),
                ],
              ),
            ),
            ...inProgressTasks.map((task) => TaskCard(
                  task: task,
                  showActions: true,
                  onTap: () => Navigator.pushNamed(
                    context,
                    '/task-detail',
                    arguments: task.id,
                  ),
                  onComplete: (t) => _completeTask(t),
                  onReport: (t) => Navigator.pushNamed(
                    context,
                    '/report-anomaly',
                    arguments: t.id,
                  ),
                )),
          ],
        );
      },
    );
  }

  Widget _buildRecentAnomalies() {
    // 显示最近的异常记录
    return Consumer<TaskProvider>(
      builder: (context, taskProvider, child) {
        final abnormalTasks = taskProvider.tasks
            .where((t) => t.status == TaskStatus.abnormal)
            .take(2)
            .toList();

        if (abnormalTasks.isEmpty) {
          return const SizedBox.shrink();
        }

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    '异常任务',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  TextButton(
                    onPressed: () => Navigator.pushNamed(context, '/anomalies'),
                    child: const Text('查看全部'),
                  ),
                ],
              ),
            ),
            ...abnormalTasks.map((task) => TaskCardCompact(
                  task: task,
                  onTap: () => Navigator.pushNamed(
                    context,
                    '/task-detail',
                    arguments: task.id,
                  ),
                )),
            const SizedBox(height: 24),
          ],
        );
      },
    );
  }

  void _showQuickReport() {
    final taskProvider = context.read<TaskProvider>();
    final inProgressTasks = taskProvider.tasks
        .where((t) => t.status == TaskStatus.inProgress)
        .toList();

    if (inProgressTasks.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('当前没有进行中的任务'),
        ),
      );
      return;
    }

    if (inProgressTasks.length == 1) {
      Navigator.pushNamed(
        context,
        '/report-anomaly',
        arguments: inProgressTasks.first.id,
      );
      return;
    }

    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                '选择要上报异常的任务',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            ...inProgressTasks.map((task) => ListTile(
                  leading: const CircleAvatar(
                    backgroundColor: Colors.orange,
                    child: Icon(Icons.local_shipping, color: Colors.white),
                  ),
                  title: Text(task.cargoName),
                  subtitle: Text('#${task.taskNumber}'),
                  onTap: () {
                    Navigator.pop(context);
                    Navigator.pushNamed(
                      context,
                      '/report-anomaly',
                      arguments: task.id,
                    );
                  },
                )),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Future<void> _completeTask(TransportTask task) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认完成'),
        content: Text('确定要完成任务 ${task.taskNumber} 吗？'),
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
      await context.read<TaskProvider>().completeTask(task.id);
    }
  }
}

/// 快捷操作卡片
class _QuickActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _QuickActionCard({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Icon(icon, color: color, size: 28),
            const SizedBox(height: 8),
            Text(
              label,
              style: TextStyle(
                color: color,
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
