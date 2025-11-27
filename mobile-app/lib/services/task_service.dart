import 'package:luoyi_mobile/models/transport_task.dart';
import 'package:luoyi_mobile/services/http_service.dart';
import 'package:luoyi_mobile/services/database_service.dart';

/// 络绎智慧交通管理系统 - 任务服务
/// 提供运输任务的CRUD和状态管理
class TaskService {
  final HttpService _http = HttpService();
  final DatabaseService _db = DatabaseService();

  /// 获取我的任务列表
  Future<List<TransportTask>> getMyTasks({TaskStatus? status}) async {
    try {
      String url = '/tasks/my';
      if (status != null) {
        url += '?status=${status.value}';
      }
      
      final response = await _http.get(url);
      if (response.statusCode == 200 && response.data['success'] == true) {
        final List data = response.data['data'] ?? [];
        return data.map((e) => TransportTask.fromJson(e)).toList();
      }
    } catch (e) {
      print('获取任务列表失败: $e');
    }
    
    // 网络失败时从本地获取
    final localTasks = await _db.getTasks(status: status?.value);
    return localTasks.map((e) => TransportTask.fromJson(e)).toList();
  }

  /// 获取任务详情
  Future<TransportTask?> getTaskDetail(int taskId) async {
    try {
      final response = await _http.get('/tasks/$taskId');
      if (response.statusCode == 200 && response.data['success'] == true) {
        return TransportTask.fromJson(response.data['data']);
      }
    } catch (e) {
      print('获取任务详情失败: $e');
    }
    return null;
  }

  /// 开始任务
  Future<bool> startTask(int taskId) async {
    try {
      final response = await _http.post('/tasks/$taskId/start');
      if (response.statusCode == 200 && response.data['success'] == true) {
        // 更新本地数据库
        await _db.updateTask(taskId, {'status': TaskStatus.inProgress.value});
        return true;
      }
    } catch (e) {
      print('开始任务失败: $e');
      // 离线模式：添加到离线队列
      await _db.addToOfflineQueue('transport_tasks', taskId, 'START', null);
    }
    return false;
  }

  /// 完成任务
  Future<bool> completeTask(int taskId, {String? notes}) async {
    try {
      final response = await _http.post('/tasks/$taskId/complete', data: {
        'notes': notes,
        'completedAt': DateTime.now().toIso8601String(),
      });
      if (response.statusCode == 200 && response.data['success'] == true) {
        await _db.updateTask(taskId, {
          'status': TaskStatus.completed.value,
          'actual_arrival': DateTime.now().toIso8601String(),
        });
        return true;
      }
    } catch (e) {
      print('完成任务失败: $e');
      await _db.addToOfflineQueue('transport_tasks', taskId, 'COMPLETE', {'notes': notes});
    }
    return false;
  }

  /// 取消任务
  Future<bool> cancelTask(int taskId, String reason) async {
    try {
      final response = await _http.post('/tasks/$taskId/cancel', data: {
        'reason': reason,
      });
      if (response.statusCode == 200 && response.data['success'] == true) {
        await _db.updateTask(taskId, {'status': TaskStatus.cancelled.value});
        return true;
      }
    } catch (e) {
      print('取消任务失败: $e');
    }
    return false;
  }

  /// 上报位置
  Future<bool> reportLocation(int taskId, double lat, double lng, {double? speed}) async {
    try {
      final response = await _http.post('/tasks/$taskId/location', data: {
        'latitude': lat,
        'longitude': lng,
        'speed': speed,
        'timestamp': DateTime.now().toIso8601String(),
      });
      return response.statusCode == 200;
    } catch (e) {
      // 保存到本地，稍后同步
      await _db.insertLocationRecord({
        'task_id': taskId,
        'latitude': lat,
        'longitude': lng,
        'speed': speed,
        'synced': 0,
      });
      return false;
    }
  }

  /// 获取进行中的任务
  Future<TransportTask?> getCurrentTask() async {
    final tasks = await getMyTasks(status: TaskStatus.inProgress);
    return tasks.isNotEmpty ? tasks.first : null;
  }

  /// 获取今日任务统计
  Future<Map<String, int>> getTodayStats() async {
    try {
      final response = await _http.get('/tasks/stats/today');
      if (response.statusCode == 200 && response.data['success'] == true) {
        final data = response.data['data'];
        return {
          'total': data['total'] ?? 0,
          'completed': data['completed'] ?? 0,
          'inProgress': data['inProgress'] ?? 0,
          'pending': data['pending'] ?? 0,
        };
      }
    } catch (e) {
      print('获取统计失败: $e');
    }
    return {'total': 0, 'completed': 0, 'inProgress': 0, 'pending': 0};
  }
}
