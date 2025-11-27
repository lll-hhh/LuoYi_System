import 'dart:async';
import 'dart:convert';
import 'package:luoyi_mobile/services/database_service.dart';
import 'package:luoyi_mobile/services/http_service.dart';

/// 数据同步服务
/// 负责本地数据与服务器的双向同步
class SyncService {
  final DatabaseService _db = DatabaseService();
  final HttpService _http = HttpService();
  
  bool _isSyncing = false;
  Timer? _syncTimer;
  
  // 同步状态流
  final _syncStatusController = StreamController<SyncStatus>.broadcast();
  Stream<SyncStatus> get syncStatus => _syncStatusController.stream;

  /// 启动自动同步
  void startAutoSync({Duration interval = const Duration(minutes: 5)}) {
    _syncTimer?.cancel();
    _syncTimer = Timer.periodic(interval, (_) => syncAll());
  }

  /// 停止自动同步
  void stopAutoSync() {
    _syncTimer?.cancel();
    _syncTimer = null;
  }

  /// 同步所有数据
  Future<SyncResult> syncAll() async {
    if (_isSyncing) {
      return SyncResult(success: false, message: '同步正在进行中');
    }
    
    _isSyncing = true;
    _syncStatusController.add(SyncStatus.syncing);
    
    try {
      final results = <String, SyncItemResult>{};
      
      // 同步车辆数据
      results['vehicles'] = await _syncVehicles();
      
      // 同步位置记录
      results['locations'] = await _syncLocationRecords();
      
      // 同步异常事件
      results['anomalies'] = await _syncAnomalies();
      
      // 处理离线队列
      results['offlineQueue'] = await _processOfflineQueue();
      
      // 从服务器拉取最新数据
      results['pullTasks'] = await _pullTasks();
      results['pullNotifications'] = await _pullNotifications();
      
      _syncStatusController.add(SyncStatus.completed);
      
      return SyncResult(
        success: true,
        message: '同步完成',
        details: results,
      );
    } catch (e) {
      _syncStatusController.add(SyncStatus.error);
      return SyncResult(success: false, message: '同步失败: $e');
    } finally {
      _isSyncing = false;
    }
  }

  /// 同步车辆数据到服务器
  Future<SyncItemResult> _syncVehicles() async {
    try {
      final unsyncedVehicles = await _db.getUnsyncedVehicles();
      int synced = 0;
      int failed = 0;
      
      for (var vehicle in unsyncedVehicles) {
        try {
          final response = await _http.post('/vehicles/sync', data: vehicle);
          if (response.statusCode == 200) {
            final remoteId = response.data['id']?.toString() ?? '';
            await _db.markVehicleSynced(vehicle['id'] as int, remoteId);
            synced++;
          } else {
            failed++;
          }
        } catch (e) {
          failed++;
        }
      }
      
      return SyncItemResult(
        total: unsyncedVehicles.length,
        synced: synced,
        failed: failed,
      );
    } catch (e) {
      return SyncItemResult(total: 0, synced: 0, failed: 0, error: e.toString());
    }
  }

  /// 同步位置记录到服务器
  Future<SyncItemResult> _syncLocationRecords() async {
    try {
      final unsyncedLocations = await _db.getUnsyncedLocationRecords(limit: 100);
      
      if (unsyncedLocations.isEmpty) {
        return SyncItemResult(total: 0, synced: 0, failed: 0);
      }
      
      try {
        final response = await _http.post('/locations/batch', data: {
          'records': unsyncedLocations,
        });
        
        if (response.statusCode == 200) {
          final ids = unsyncedLocations.map((r) => r['id'] as int).toList();
          await _db.markLocationsSynced(ids);
          return SyncItemResult(
            total: unsyncedLocations.length,
            synced: unsyncedLocations.length,
            failed: 0,
          );
        }
      } catch (e) {
        return SyncItemResult(
          total: unsyncedLocations.length,
          synced: 0,
          failed: unsyncedLocations.length,
          error: e.toString(),
        );
      }
      
      return SyncItemResult(total: 0, synced: 0, failed: 0);
    } catch (e) {
      return SyncItemResult(total: 0, synced: 0, failed: 0, error: e.toString());
    }
  }

  /// 同步异常事件到服务器
  Future<SyncItemResult> _syncAnomalies() async {
    try {
      final response = await _http.get('/anomalies/unsynced');
      // 实现异常事件同步逻辑
      return SyncItemResult(total: 0, synced: 0, failed: 0);
    } catch (e) {
      return SyncItemResult(total: 0, synced: 0, failed: 0, error: e.toString());
    }
  }

  /// 处理离线操作队列
  Future<SyncItemResult> _processOfflineQueue() async {
    try {
      final queue = await _db.getOfflineQueue();
      int processed = 0;
      int failed = 0;
      
      for (var item in queue) {
        final tableName = item['table_name'] as String;
        final operation = item['operation'] as String;
        final recordId = item['record_id'] as int;
        final retryCount = item['retry_count'] as int;
        
        // 最多重试3次
        if (retryCount >= 3) {
          await _db.removeFromOfflineQueue(item['id'] as int);
          failed++;
          continue;
        }
        
        try {
          bool success = false;
          
          switch (operation) {
            case 'CREATE':
              success = await _processCreate(tableName, recordId);
              break;
            case 'UPDATE':
              success = await _processUpdate(tableName, recordId);
              break;
            case 'DELETE':
              success = await _processDelete(tableName, recordId);
              break;
          }
          
          if (success) {
            await _db.removeFromOfflineQueue(item['id'] as int);
            processed++;
          } else {
            await _db.incrementRetryCount(item['id'] as int);
            failed++;
          }
        } catch (e) {
          await _db.incrementRetryCount(item['id'] as int);
          failed++;
        }
      }
      
      return SyncItemResult(total: queue.length, synced: processed, failed: failed);
    } catch (e) {
      return SyncItemResult(total: 0, synced: 0, failed: 0, error: e.toString());
    }
  }

  /// 处理创建操作
  Future<bool> _processCreate(String tableName, int recordId) async {
    // 根据表名获取数据并发送到服务器
    switch (tableName) {
      case 'vehicles':
        // 获取车辆数据并创建
        break;
      case 'cargoes':
        // 获取货物数据并创建
        break;
      case 'transport_tasks':
        // 获取任务数据并创建
        break;
    }
    return true;
  }

  /// 处理更新操作
  Future<bool> _processUpdate(String tableName, int recordId) async {
    // 根据表名获取数据并更新到服务器
    return true;
  }

  /// 处理删除操作
  Future<bool> _processDelete(String tableName, int recordId) async {
    // 向服务器发送删除请求
    return true;
  }

  /// 从服务器拉取任务数据
  Future<SyncItemResult> _pullTasks() async {
    try {
      final response = await _http.get('/tasks/active');
      if (response.statusCode == 200) {
        final tasks = response.data['data'] as List? ?? [];
        int inserted = 0;
        
        for (var task in tasks) {
          // 检查本地是否已存在
          final existing = await _db.getTasks();
          final exists = existing.any((t) => t['remote_id'] == task['id'].toString());
          
          if (!exists) {
            await _db.insertTask({
              'remote_id': task['id'].toString(),
              'task_number': task['taskNumber'] ?? '',
              'driver_name': task['driverName'] ?? '',
              'start_location': task['startLocation'] ?? '',
              'end_location': task['endLocation'] ?? '',
              'status': task['status'] ?? 'pending',
              'synced': 1,
            });
            inserted++;
          }
        }
        
        return SyncItemResult(total: tasks.length, synced: inserted, failed: 0);
      }
      return SyncItemResult(total: 0, synced: 0, failed: 0);
    } catch (e) {
      return SyncItemResult(total: 0, synced: 0, failed: 0, error: e.toString());
    }
  }

  /// 从服务器拉取通知
  Future<SyncItemResult> _pullNotifications() async {
    try {
      final response = await _http.get('/notifications/unread');
      if (response.statusCode == 200) {
        final notifications = response.data['data'] as List? ?? [];
        int inserted = 0;
        
        for (var notification in notifications) {
          final existing = await _db.getNotifications();
          final exists = existing.any((n) => n['remote_id'] == notification['id'].toString());
          
          if (!exists) {
            await _db.insertNotification({
              'remote_id': notification['id'].toString(),
              'title': notification['title'] ?? '',
              'content': notification['content'] ?? '',
              'type': notification['type'] ?? 'info',
              'data': jsonEncode(notification['data'] ?? {}),
              'created_at': notification['createdAt'],
            });
            inserted++;
          }
        }
        
        return SyncItemResult(total: notifications.length, synced: inserted, failed: 0);
      }
      return SyncItemResult(total: 0, synced: 0, failed: 0);
    } catch (e) {
      return SyncItemResult(total: 0, synced: 0, failed: 0, error: e.toString());
    }
  }

  /// 记录位置（离线优先）
  Future<void> recordLocation(int taskId, double lat, double lng, {double? speed, double? heading}) async {
    // 先存入本地数据库
    await _db.insertLocationRecord({
      'task_id': taskId,
      'latitude': lat,
      'longitude': lng,
      'speed': speed,
      'heading': heading,
      'synced': 0,
    });
    
    // 尝试立即同步（如果有网络）
    try {
      await _syncLocationRecords();
    } catch (e) {
      // 忽略同步错误，等待下次同步
    }
  }

  /// 报告异常事件（离线优先）
  Future<void> reportAnomaly(int taskId, String eventType, String description, {double? lat, double? lng}) async {
    final eventId = await _db.insertAnomalyEvent({
      'task_id': taskId,
      'event_type': eventType,
      'description': description,
      'location_lat': lat,
      'location_lng': lng,
      'synced': 0,
    });
    
    // 添加到离线队列
    await _db.addToOfflineQueue('anomaly_events', eventId, 'CREATE', null);
    
    // 尝试立即同步
    try {
      await syncAll();
    } catch (e) {
      // 忽略同步错误
    }
  }

  /// 释放资源
  void dispose() {
    stopAutoSync();
    _syncStatusController.close();
  }
}

/// 同步状态枚举
enum SyncStatus {
  idle,
  syncing,
  completed,
  error,
}

/// 同步结果
class SyncResult {
  final bool success;
  final String message;
  final Map<String, SyncItemResult>? details;

  SyncResult({
    required this.success,
    required this.message,
    this.details,
  });
}

/// 单项同步结果
class SyncItemResult {
  final int total;
  final int synced;
  final int failed;
  final String? error;

  SyncItemResult({
    required this.total,
    required this.synced,
    required this.failed,
    this.error,
  });

  @override
  String toString() {
    return 'SyncItemResult(total: $total, synced: $synced, failed: $failed, error: $error)';
  }
}
