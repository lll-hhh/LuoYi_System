import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/task_provider.dart';
import '../providers/location_provider.dart';
import '../models/transport_task.dart';
import '../models/location.dart';

/// 实时追踪地图屏幕
class TrackingMapScreen extends StatefulWidget {
  final String taskId;

  const TrackingMapScreen({super.key, required this.taskId});

  @override
  State<TrackingMapScreen> createState() => _TrackingMapScreenState();
}

class _TrackingMapScreenState extends State<TrackingMapScreen> {
  bool _showRoute = true;
  bool _showHistory = false;
  bool _isFullScreen = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TaskProvider>().loadTaskDetail(widget.taskId);
      context.read<LocationProvider>().loadLocationHistory(widget.taskId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _isFullScreen
          ? null
          : AppBar(
              title: const Text('实时追踪'),
              actions: [
                IconButton(
                  icon: const Icon(Icons.layers),
                  onPressed: _showLayerOptions,
                ),
                IconButton(
                  icon: const Icon(Icons.fullscreen),
                  onPressed: () => setState(() => _isFullScreen = true),
                ),
              ],
            ),
      body: Consumer2<TaskProvider, LocationProvider>(
        builder: (context, taskProvider, locationProvider, child) {
          final task = taskProvider.selectedTask;

          if (task == null) {
            return const Center(child: CircularProgressIndicator());
          }

          return Stack(
            children: [
              // 地图视图 (使用占位符，实际项目中使用地图SDK)
              _buildMapPlaceholder(task, locationProvider),
              
              // 顶部状态栏
              if (!_isFullScreen) _buildTopBar(locationProvider),
              
              // 退出全屏按钮
              if (_isFullScreen)
                Positioned(
                  top: MediaQuery.of(context).padding.top + 8,
                  left: 8,
                  child: FloatingActionButton.small(
                    onPressed: () => setState(() => _isFullScreen = false),
                    child: const Icon(Icons.fullscreen_exit),
                  ),
                ),
              
              // 底部信息面板
              _buildBottomPanel(task, locationProvider),
              
              // 右侧控制按钮
              _buildControlButtons(locationProvider),
            ],
          );
        },
      ),
    );
  }

  Widget _buildMapPlaceholder(TransportTask task, LocationProvider locationProvider) {
    final currentLocation = locationProvider.currentLocation;
    final history = locationProvider.locationHistory;

    return Container(
      color: Colors.grey[200],
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.map,
              size: 80,
              color: Colors.grey[400],
            ),
            const SizedBox(height: 16),
            Text(
              '地图视图',
              style: TextStyle(
                fontSize: 18,
                color: Colors.grey[600],
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '请集成高德地图或百度地图SDK',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[500],
              ),
            ),
            const SizedBox(height: 24),
            if (currentLocation != null)
              Container(
                padding: const EdgeInsets.all(16),
                margin: const EdgeInsets.symmetric(horizontal: 32),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 8,
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    _buildCoordinateRow(
                      '当前位置',
                      currentLocation.latitude,
                      currentLocation.longitude,
                      Colors.blue,
                    ),
                    const Divider(),
                    _buildCoordinateRow(
                      '起点',
                      task.originLatitude,
                      task.originLongitude,
                      Colors.green,
                    ),
                    const Divider(),
                    _buildCoordinateRow(
                      '终点',
                      task.destinationLatitude,
                      task.destinationLongitude,
                      Colors.red,
                    ),
                    if (history.isNotEmpty) ...[
                      const Divider(),
                      Text(
                        '轨迹点: ${history.length}',
                        style: TextStyle(
                          color: Colors.grey[600],
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildCoordinateRow(
    String label,
    double lat,
    double lng,
    Color color,
  ) {
    return Row(
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 8),
        Text(
          label,
          style: const TextStyle(fontWeight: FontWeight.w500),
        ),
        const Spacer(),
        Text(
          '${lat.toStringAsFixed(4)}, ${lng.toStringAsFixed(4)}',
          style: TextStyle(
            color: Colors.grey[600],
            fontSize: 12,
          ),
        ),
      ],
    );
  }

  Widget _buildTopBar(LocationProvider locationProvider) {
    final location = locationProvider.currentLocation;
    
    return Positioned(
      top: 0,
      left: 0,
      right: 0,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Colors.white,
              Colors.white.withOpacity(0),
            ],
          ),
        ),
        child: SafeArea(
          bottom: false,
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 6,
                ),
                decoration: BoxDecoration(
                  color: locationProvider.isTracking
                      ? Colors.green
                      : Colors.grey,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.circle,
                        boxShadow: locationProvider.isTracking
                            ? [
                                BoxShadow(
                                  color: Colors.white.withOpacity(0.5),
                                  blurRadius: 4,
                                  spreadRadius: 2,
                                ),
                              ]
                            : null,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      locationProvider.isTracking ? '定位中' : '已停止',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              if (location != null)
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(20),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.1),
                        blurRadius: 4,
                      ),
                    ],
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.speed, size: 16, color: Colors.blue),
                      const SizedBox(width: 4),
                      Text(
                        '${location.speed.toStringAsFixed(1)} km/h',
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildBottomPanel(TransportTask task, LocationProvider locationProvider) {
    return Positioned(
      bottom: 0,
      left: 0,
      right: 0,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 10,
              offset: const Offset(0, -2),
            ),
          ],
        ),
        child: SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                margin: const EdgeInsets.only(top: 8),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Colors.grey[300],
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    _buildTaskInfo(task),
                    const SizedBox(height: 16),
                    _buildStats(locationProvider),
                    const SizedBox(height: 16),
                    _buildTrackingButton(locationProvider),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTaskInfo(TransportTask task) {
    return Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                task.cargoName,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                '${task.originAddress} → ${task.destinationAddress}',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontSize: 12,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          decoration: BoxDecoration(
            color: _getStatusColor(task.status).withOpacity(0.1),
            borderRadius: BorderRadius.circular(20),
          ),
          child: Text(
            _getStatusText(task.status),
            style: TextStyle(
              color: _getStatusColor(task.status),
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildStats(LocationProvider locationProvider) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        _buildStatItem(
          icon: Icons.straighten,
          label: '总里程',
          value: '${locationProvider.totalDistance.toStringAsFixed(1)} km',
        ),
        Container(
          width: 1,
          height: 40,
          color: Colors.grey[300],
        ),
        _buildStatItem(
          icon: Icons.timer,
          label: '行驶时长',
          value: _formatDuration(locationProvider.trackingDuration),
        ),
        Container(
          width: 1,
          height: 40,
          color: Colors.grey[300],
        ),
        _buildStatItem(
          icon: Icons.speed,
          label: '平均速度',
          value: '${locationProvider.averageSpeed.toStringAsFixed(1)} km/h',
        ),
      ],
    );
  }

  Widget _buildStatItem({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Column(
      children: [
        Icon(icon, size: 20, color: Colors.blue),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          label,
          style: TextStyle(
            fontSize: 11,
            color: Colors.grey[500],
          ),
        ),
      ],
    );
  }

  Widget _buildTrackingButton(LocationProvider locationProvider) {
    return Row(
      children: [
        Expanded(
          child: ElevatedButton.icon(
            onPressed: () {
              if (locationProvider.isTracking) {
                locationProvider.stopTracking();
              } else {
                locationProvider.startTracking();
              }
            },
            icon: Icon(
              locationProvider.isTracking
                  ? Icons.pause
                  : Icons.play_arrow,
            ),
            label: Text(
              locationProvider.isTracking ? '暂停追踪' : '开始追踪',
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor:
                  locationProvider.isTracking ? Colors.orange : Colors.blue,
              padding: const EdgeInsets.symmetric(vertical: 12),
            ),
          ),
        ),
        const SizedBox(width: 12),
        Container(
          decoration: BoxDecoration(
            color: Colors.grey[100],
            borderRadius: BorderRadius.circular(8),
          ),
          child: IconButton(
            onPressed: () => _centerOnCurrentLocation(locationProvider),
            icon: const Icon(Icons.my_location),
            color: Colors.blue,
          ),
        ),
      ],
    );
  }

  Widget _buildControlButtons(LocationProvider locationProvider) {
    return Positioned(
      right: 16,
      bottom: 250,
      child: Column(
        children: [
          FloatingActionButton.small(
            heroTag: 'zoom_in',
            onPressed: () {
              // 放大地图
            },
            child: const Icon(Icons.add),
          ),
          const SizedBox(height: 8),
          FloatingActionButton.small(
            heroTag: 'zoom_out',
            onPressed: () {
              // 缩小地图
            },
            child: const Icon(Icons.remove),
          ),
          const SizedBox(height: 16),
          FloatingActionButton.small(
            heroTag: 'refresh',
            onPressed: () {
              locationProvider.getCurrentLocation();
            },
            child: const Icon(Icons.refresh),
          ),
        ],
      ),
    );
  }

  void _showLayerOptions() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => Container(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                '地图图层',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 16),
              SwitchListTile(
                title: const Text('显示路线'),
                subtitle: const Text('在地图上显示规划路线'),
                value: _showRoute,
                onChanged: (value) {
                  setState(() => _showRoute = value);
                  this.setState(() {});
                },
              ),
              SwitchListTile(
                title: const Text('显示历史轨迹'),
                subtitle: const Text('显示车辆行驶历史轨迹'),
                value: _showHistory,
                onChanged: (value) {
                  setState(() => _showHistory = value);
                  this.setState(() {});
                },
              ),
              const SizedBox(height: 16),
              const Text(
                '地图样式',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                children: [
                  ChoiceChip(
                    label: const Text('标准'),
                    selected: true,
                    onSelected: (_) {},
                  ),
                  ChoiceChip(
                    label: const Text('卫星'),
                    selected: false,
                    onSelected: (_) {},
                  ),
                  ChoiceChip(
                    label: const Text('夜间'),
                    selected: false,
                    onSelected: (_) {},
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _centerOnCurrentLocation(LocationProvider locationProvider) {
    locationProvider.getCurrentLocation();
    // 实际实现中需要调用地图SDK的方法来居中显示当前位置
  }

  String _formatDuration(Duration duration) {
    final hours = duration.inHours;
    final minutes = duration.inMinutes % 60;
    if (hours > 0) {
      return '$hours时${minutes}分';
    }
    return '$minutes分钟';
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

  String _getStatusText(TaskStatus status) {
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
}
