import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import 'package:luoyi_mobile/models/transport_task.dart';
import 'package:luoyi_mobile/services/task_service.dart';
import 'package:luoyi_mobile/services/location_service.dart';

/// 络绎智慧交通管理系统 - 任务状态管理
class TaskProvider extends ChangeNotifier {
  final TaskService _taskService = TaskService();
  final LocationService _locationService = LocationService();

  List<TransportTask> _tasks = [];
  TransportTask? _currentTask;
  Position? _currentPosition;
  Map<String, int> _todayStats = {};
  bool _isLoading = false;
  String? _error;
  StreamSubscription<Position>? _positionSubscription;

  // Getters
  List<TransportTask> get tasks => _tasks;
  TransportTask? get currentTask => _currentTask;
  Position? get currentPosition => _currentPosition;
  Map<String, int> get todayStats => _todayStats;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get hasCurrentTask => _currentTask != null;

  List<TransportTask> get pendingTasks => 
      _tasks.where((t) => t.status == TaskStatus.pending).toList();
  
  List<TransportTask> get inProgressTasks => 
      _tasks.where((t) => t.status == TaskStatus.inProgress).toList();
  
  List<TransportTask> get completedTasks => 
      _tasks.where((t) => t.status == TaskStatus.completed).toList();

  /// 初始化
  Future<void> init() async {
    await loadTasks();
    await loadTodayStats();
    await _loadCurrentTask();
  }

  /// 加载任务列表
  Future<void> loadTasks({TaskStatus? status}) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _tasks = await _taskService.getMyTasks(status: status);
    } catch (e) {
      _error = '加载任务失败: $e';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 加载今日统计
  Future<void> loadTodayStats() async {
    try {
      _todayStats = await _taskService.getTodayStats();
      notifyListeners();
    } catch (e) {
      print('加载统计失败: $e');
    }
  }

  /// 加载当前任务
  Future<void> _loadCurrentTask() async {
    _currentTask = await _taskService.getCurrentTask();
    if (_currentTask != null) {
      // 如果有进行中的任务，开始位置追踪
      await _startLocationTracking();
    }
    notifyListeners();
  }

  /// 开始任务
  Future<bool> startTask(int taskId) async {
    _isLoading = true;
    notifyListeners();

    try {
      bool success = await _taskService.startTask(taskId);
      if (success) {
        // 更新本地状态
        final index = _tasks.indexWhere((t) => t.id == taskId);
        if (index != -1) {
          _tasks[index] = _tasks[index].copyWith(
            status: TaskStatus.inProgress,
            startTime: DateTime.now(),
          );
          _currentTask = _tasks[index];
        }
        
        // 开始位置追踪
        await _startLocationTracking();
        await loadTodayStats();
      }
      return success;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 完成任务
  Future<bool> completeTask(int taskId, {String? notes}) async {
    _isLoading = true;
    notifyListeners();

    try {
      bool success = await _taskService.completeTask(taskId, notes: notes);
      if (success) {
        // 更新本地状态
        final index = _tasks.indexWhere((t) => t.id == taskId);
        if (index != -1) {
          _tasks[index] = _tasks[index].copyWith(
            status: TaskStatus.completed,
            actualArrival: DateTime.now(),
          );
        }
        
        // 停止位置追踪
        if (_currentTask?.id == taskId) {
          await _stopLocationTracking();
          _currentTask = null;
        }
        
        await loadTodayStats();
      }
      return success;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 取消任务
  Future<bool> cancelTask(int taskId, String reason) async {
    _isLoading = true;
    notifyListeners();

    try {
      bool success = await _taskService.cancelTask(taskId, reason);
      if (success) {
        final index = _tasks.indexWhere((t) => t.id == taskId);
        if (index != -1) {
          _tasks[index] = _tasks[index].copyWith(status: TaskStatus.cancelled);
        }
        
        if (_currentTask?.id == taskId) {
          await _stopLocationTracking();
          _currentTask = null;
        }
        
        await loadTodayStats();
      }
      return success;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 开始位置追踪
  Future<void> _startLocationTracking() async {
    if (_currentTask == null) return;

    try {
      await _locationService.startTracking(taskId: _currentTask!.id);
      _positionSubscription = _locationService.positionStream.listen((position) {
        _currentPosition = position;
        notifyListeners();
      });
    } catch (e) {
      print('启动位置追踪失败: $e');
    }
  }

  /// 停止位置追踪
  Future<void> _stopLocationTracking() async {
    await _locationService.stopTracking();
    await _positionSubscription?.cancel();
    _positionSubscription = null;
    _currentPosition = null;
  }

  /// 获取任务详情
  Future<TransportTask?> getTaskDetail(int taskId) async {
    return await _taskService.getTaskDetail(taskId);
  }

  /// 刷新
  Future<void> refresh() async {
    await loadTasks();
    await loadTodayStats();
    await _loadCurrentTask();
  }

  @override
  void dispose() {
    _stopLocationTracking();
    super.dispose();
  }
}
