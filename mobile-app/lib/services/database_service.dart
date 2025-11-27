import 'dart:async';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

/// 络绎智慧交通管理系统 - 本地数据库服务
/// 提供SQLite数据库的初始化、CRUD操作和数据同步功能
class DatabaseService {
  static Database? _database;
  static const String _databaseName = 'lorries_mobile.db';
  static const int _databaseVersion = 1;

  /// 获取数据库实例
  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  /// 初始化数据库
  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), _databaseName);
    return await openDatabase(
      path,
      version: _databaseVersion,
      onCreate: _onCreate,
      onUpgrade: _onUpgrade,
    );
  }

  /// 创建数据库表
  Future<void> _onCreate(Database db, int version) async {
    // 车辆信息表
    await db.execute('''
      CREATE TABLE vehicles (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        remote_id TEXT UNIQUE,
        plate_number TEXT NOT NULL,
        vehicle_type TEXT,
        brand TEXT,
        model TEXT,
        color TEXT,
        owner_name TEXT,
        owner_phone TEXT,
        status TEXT DEFAULT 'active',
        created_at TEXT,
        updated_at TEXT,
        synced INTEGER DEFAULT 0
      )
    ''');

    // 货物信息表
    await db.execute('''
      CREATE TABLE cargoes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        remote_id TEXT UNIQUE,
        cargo_number TEXT NOT NULL,
        name TEXT,
        category TEXT,
        weight REAL,
        volume REAL,
        sender_name TEXT,
        sender_phone TEXT,
        sender_address TEXT,
        receiver_name TEXT,
        receiver_phone TEXT,
        receiver_address TEXT,
        status TEXT DEFAULT 'pending',
        created_at TEXT,
        updated_at TEXT,
        synced INTEGER DEFAULT 0
      )
    ''');

    // 运输任务表
    await db.execute('''
      CREATE TABLE transport_tasks (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        remote_id TEXT UNIQUE,
        task_number TEXT NOT NULL,
        vehicle_id INTEGER,
        cargo_id INTEGER,
        driver_name TEXT,
        driver_phone TEXT,
        start_location TEXT,
        end_location TEXT,
        start_time TEXT,
        end_time TEXT,
        estimated_arrival TEXT,
        actual_arrival TEXT,
        status TEXT DEFAULT 'pending',
        created_at TEXT,
        updated_at TEXT,
        synced INTEGER DEFAULT 0,
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
        FOREIGN KEY (cargo_id) REFERENCES cargoes (id)
      )
    ''');

    // 位置记录表
    await db.execute('''
      CREATE TABLE location_records (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        task_id INTEGER,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL,
        speed REAL,
        heading REAL,
        accuracy REAL,
        altitude REAL,
        recorded_at TEXT NOT NULL,
        synced INTEGER DEFAULT 0,
        FOREIGN KEY (task_id) REFERENCES transport_tasks (id)
      )
    ''');

    // 异常事件表
    await db.execute('''
      CREATE TABLE anomaly_events (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        remote_id TEXT UNIQUE,
        task_id INTEGER,
        event_type TEXT NOT NULL,
        severity TEXT,
        description TEXT,
        location_lat REAL,
        location_lng REAL,
        snapshot_path TEXT,
        resolved INTEGER DEFAULT 0,
        resolved_at TEXT,
        created_at TEXT,
        synced INTEGER DEFAULT 0,
        FOREIGN KEY (task_id) REFERENCES transport_tasks (id)
      )
    ''');

    // 消息通知表
    await db.execute('''
      CREATE TABLE notifications (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        remote_id TEXT UNIQUE,
        title TEXT NOT NULL,
        content TEXT,
        type TEXT,
        is_read INTEGER DEFAULT 0,
        data TEXT,
        created_at TEXT
      )
    ''');

    // 离线操作队列表
    await db.execute('''
      CREATE TABLE offline_queue (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        table_name TEXT NOT NULL,
        record_id INTEGER NOT NULL,
        operation TEXT NOT NULL,
        data TEXT,
        created_at TEXT,
        retry_count INTEGER DEFAULT 0
      )
    ''');

    // 缓存表
    await db.execute('''
      CREATE TABLE cache (
        key TEXT PRIMARY KEY,
        value TEXT,
        expires_at TEXT
      )
    ''');

    // 创建索引
    await db.execute('CREATE INDEX idx_vehicles_plate ON vehicles (plate_number)');
    await db.execute('CREATE INDEX idx_cargoes_number ON cargoes (cargo_number)');
    await db.execute('CREATE INDEX idx_tasks_number ON transport_tasks (task_number)');
    await db.execute('CREATE INDEX idx_tasks_status ON transport_tasks (status)');
    await db.execute('CREATE INDEX idx_locations_task ON location_records (task_id)');
    await db.execute('CREATE INDEX idx_anomalies_task ON anomaly_events (task_id)');
    await db.execute('CREATE INDEX idx_notifications_read ON notifications (is_read)');
    await db.execute('CREATE INDEX idx_offline_table ON offline_queue (table_name)');
  }

  /// 数据库升级
  Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    // 在这里处理数据库版本升级
    if (oldVersion < 2) {
      // 升级到版本2的SQL
    }
  }

  /// 关闭数据库
  Future<void> close() async {
    final db = await database;
    await db.close();
    _database = null;
  }

  /// 清空所有数据
  Future<void> clearAll() async {
    final db = await database;
    await db.delete('vehicles');
    await db.delete('cargoes');
    await db.delete('transport_tasks');
    await db.delete('location_records');
    await db.delete('anomaly_events');
    await db.delete('notifications');
    await db.delete('offline_queue');
    await db.delete('cache');
  }

  // ==================== 车辆操作 ====================

  /// 插入车辆
  Future<int> insertVehicle(Map<String, dynamic> vehicle) async {
    final db = await database;
    vehicle['created_at'] = DateTime.now().toIso8601String();
    vehicle['updated_at'] = DateTime.now().toIso8601String();
    return await db.insert('vehicles', vehicle);
  }

  /// 更新车辆
  Future<int> updateVehicle(int id, Map<String, dynamic> vehicle) async {
    final db = await database;
    vehicle['updated_at'] = DateTime.now().toIso8601String();
    vehicle['synced'] = 0;
    return await db.update('vehicles', vehicle, where: 'id = ?', whereArgs: [id]);
  }

  /// 获取所有车辆
  Future<List<Map<String, dynamic>>> getVehicles() async {
    final db = await database;
    return await db.query('vehicles', orderBy: 'created_at DESC');
  }

  /// 根据车牌号查询车辆
  Future<Map<String, dynamic>?> getVehicleByPlate(String plateNumber) async {
    final db = await database;
    final results = await db.query(
      'vehicles',
      where: 'plate_number = ?',
      whereArgs: [plateNumber],
    );
    return results.isNotEmpty ? results.first : null;
  }

  /// 获取未同步的车辆
  Future<List<Map<String, dynamic>>> getUnsyncedVehicles() async {
    final db = await database;
    return await db.query('vehicles', where: 'synced = 0');
  }

  /// 标记车辆已同步
  Future<void> markVehicleSynced(int id, String remoteId) async {
    final db = await database;
    await db.update(
      'vehicles',
      {'synced': 1, 'remote_id': remoteId},
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  // ==================== 货物操作 ====================

  /// 插入货物
  Future<int> insertCargo(Map<String, dynamic> cargo) async {
    final db = await database;
    cargo['created_at'] = DateTime.now().toIso8601String();
    cargo['updated_at'] = DateTime.now().toIso8601String();
    return await db.insert('cargoes', cargo);
  }

  /// 更新货物
  Future<int> updateCargo(int id, Map<String, dynamic> cargo) async {
    final db = await database;
    cargo['updated_at'] = DateTime.now().toIso8601String();
    cargo['synced'] = 0;
    return await db.update('cargoes', cargo, where: 'id = ?', whereArgs: [id]);
  }

  /// 获取所有货物
  Future<List<Map<String, dynamic>>> getCargoes() async {
    final db = await database;
    return await db.query('cargoes', orderBy: 'created_at DESC');
  }

  /// 根据货物编号查询
  Future<Map<String, dynamic>?> getCargoByNumber(String cargoNumber) async {
    final db = await database;
    final results = await db.query(
      'cargoes',
      where: 'cargo_number = ?',
      whereArgs: [cargoNumber],
    );
    return results.isNotEmpty ? results.first : null;
  }

  // ==================== 运输任务操作 ====================

  /// 插入任务
  Future<int> insertTask(Map<String, dynamic> task) async {
    final db = await database;
    task['created_at'] = DateTime.now().toIso8601String();
    task['updated_at'] = DateTime.now().toIso8601String();
    return await db.insert('transport_tasks', task);
  }

  /// 更新任务
  Future<int> updateTask(int id, Map<String, dynamic> task) async {
    final db = await database;
    task['updated_at'] = DateTime.now().toIso8601String();
    task['synced'] = 0;
    return await db.update('transport_tasks', task, where: 'id = ?', whereArgs: [id]);
  }

  /// 获取所有任务
  Future<List<Map<String, dynamic>>> getTasks({String? status}) async {
    final db = await database;
    if (status != null) {
      return await db.query(
        'transport_tasks',
        where: 'status = ?',
        whereArgs: [status],
        orderBy: 'created_at DESC',
      );
    }
    return await db.query('transport_tasks', orderBy: 'created_at DESC');
  }

  /// 获取进行中的任务
  Future<List<Map<String, dynamic>>> getActiveTasks() async {
    final db = await database;
    return await db.query(
      'transport_tasks',
      where: 'status IN (?, ?)',
      whereArgs: ['in_progress', 'pending'],
      orderBy: 'start_time DESC',
    );
  }

  // ==================== 位置记录操作 ====================

  /// 插入位置记录
  Future<int> insertLocationRecord(Map<String, dynamic> record) async {
    final db = await database;
    record['recorded_at'] = DateTime.now().toIso8601String();
    return await db.insert('location_records', record);
  }

  /// 批量插入位置记录
  Future<void> insertLocationRecords(List<Map<String, dynamic>> records) async {
    final db = await database;
    final batch = db.batch();
    for (var record in records) {
      record['recorded_at'] ??= DateTime.now().toIso8601String();
      batch.insert('location_records', record);
    }
    await batch.commit(noResult: true);
  }

  /// 获取任务的位置记录
  Future<List<Map<String, dynamic>>> getLocationRecords(int taskId, {int? limit}) async {
    final db = await database;
    return await db.query(
      'location_records',
      where: 'task_id = ?',
      whereArgs: [taskId],
      orderBy: 'recorded_at DESC',
      limit: limit,
    );
  }

  /// 获取未同步的位置记录
  Future<List<Map<String, dynamic>>> getUnsyncedLocationRecords({int limit = 100}) async {
    final db = await database;
    return await db.query(
      'location_records',
      where: 'synced = 0',
      orderBy: 'recorded_at ASC',
      limit: limit,
    );
  }

  /// 标记位置记录已同步
  Future<void> markLocationsSynced(List<int> ids) async {
    final db = await database;
    final batch = db.batch();
    for (var id in ids) {
      batch.update('location_records', {'synced': 1}, where: 'id = ?', whereArgs: [id]);
    }
    await batch.commit(noResult: true);
  }

  // ==================== 异常事件操作 ====================

  /// 插入异常事件
  Future<int> insertAnomalyEvent(Map<String, dynamic> event) async {
    final db = await database;
    event['created_at'] = DateTime.now().toIso8601String();
    return await db.insert('anomaly_events', event);
  }

  /// 获取任务的异常事件
  Future<List<Map<String, dynamic>>> getAnomalyEvents(int taskId) async {
    final db = await database;
    return await db.query(
      'anomaly_events',
      where: 'task_id = ?',
      whereArgs: [taskId],
      orderBy: 'created_at DESC',
    );
  }

  /// 获取未解决的异常事件
  Future<List<Map<String, dynamic>>> getUnresolvedAnomalies() async {
    final db = await database;
    return await db.query(
      'anomaly_events',
      where: 'resolved = 0',
      orderBy: 'created_at DESC',
    );
  }

  /// 解决异常事件
  Future<void> resolveAnomaly(int id) async {
    final db = await database;
    await db.update(
      'anomaly_events',
      {
        'resolved': 1,
        'resolved_at': DateTime.now().toIso8601String(),
        'synced': 0,
      },
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  // ==================== 通知操作 ====================

  /// 插入通知
  Future<int> insertNotification(Map<String, dynamic> notification) async {
    final db = await database;
    notification['created_at'] ??= DateTime.now().toIso8601String();
    return await db.insert('notifications', notification);
  }

  /// 获取所有通知
  Future<List<Map<String, dynamic>>> getNotifications({bool? unreadOnly}) async {
    final db = await database;
    if (unreadOnly == true) {
      return await db.query(
        'notifications',
        where: 'is_read = 0',
        orderBy: 'created_at DESC',
      );
    }
    return await db.query('notifications', orderBy: 'created_at DESC');
  }

  /// 标记通知已读
  Future<void> markNotificationRead(int id) async {
    final db = await database;
    await db.update('notifications', {'is_read': 1}, where: 'id = ?', whereArgs: [id]);
  }

  /// 标记所有通知已读
  Future<void> markAllNotificationsRead() async {
    final db = await database;
    await db.update('notifications', {'is_read': 1}, where: 'is_read = 0');
  }

  /// 获取未读通知数量
  Future<int> getUnreadNotificationCount() async {
    final db = await database;
    final result = await db.rawQuery('SELECT COUNT(*) as count FROM notifications WHERE is_read = 0');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  // ==================== 离线队列操作 ====================

  /// 添加到离线队列
  Future<int> addToOfflineQueue(String tableName, int recordId, String operation, Map<String, dynamic>? data) async {
    final db = await database;
    return await db.insert('offline_queue', {
      'table_name': tableName,
      'record_id': recordId,
      'operation': operation,
      'data': data != null ? data.toString() : null,
      'created_at': DateTime.now().toIso8601String(),
    });
  }

  /// 获取离线队列
  Future<List<Map<String, dynamic>>> getOfflineQueue() async {
    final db = await database;
    return await db.query('offline_queue', orderBy: 'created_at ASC');
  }

  /// 从离线队列移除
  Future<void> removeFromOfflineQueue(int id) async {
    final db = await database;
    await db.delete('offline_queue', where: 'id = ?', whereArgs: [id]);
  }

  /// 增加重试次数
  Future<void> incrementRetryCount(int id) async {
    final db = await database;
    await db.rawUpdate('UPDATE offline_queue SET retry_count = retry_count + 1 WHERE id = ?', [id]);
  }

  // ==================== 缓存操作 ====================

  /// 设置缓存
  Future<void> setCache(String key, String value, {Duration? expiry}) async {
    final db = await database;
    final expiresAt = expiry != null
        ? DateTime.now().add(expiry).toIso8601String()
        : null;
    
    await db.insert(
      'cache',
      {'key': key, 'value': value, 'expires_at': expiresAt},
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  /// 获取缓存
  Future<String?> getCache(String key) async {
    final db = await database;
    final results = await db.query('cache', where: 'key = ?', whereArgs: [key]);
    
    if (results.isEmpty) return null;
    
    final cache = results.first;
    final expiresAt = cache['expires_at'] as String?;
    
    if (expiresAt != null && DateTime.parse(expiresAt).isBefore(DateTime.now())) {
      await db.delete('cache', where: 'key = ?', whereArgs: [key]);
      return null;
    }
    
    return cache['value'] as String?;
  }

  /// 删除缓存
  Future<void> deleteCache(String key) async {
    final db = await database;
    await db.delete('cache', where: 'key = ?', whereArgs: [key]);
  }

  /// 清理过期缓存
  Future<void> cleanExpiredCache() async {
    final db = await database;
    await db.delete(
      'cache',
      where: 'expires_at IS NOT NULL AND expires_at < ?',
      whereArgs: [DateTime.now().toIso8601String()],
    );
  }

  // ==================== 统计查询 ====================

  /// 获取统计信息
  Future<Map<String, int>> getStatistics() async {
    final db = await database;
    
    final vehicleCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM vehicles'),
    ) ?? 0;
    
    final cargoCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM cargoes'),
    ) ?? 0;
    
    final taskCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM transport_tasks'),
    ) ?? 0;
    
    final activeTaskCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM transport_tasks WHERE status IN ("pending", "in_progress")'),
    ) ?? 0;
    
    final anomalyCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM anomaly_events WHERE resolved = 0'),
    ) ?? 0;
    
    final offlineQueueCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM offline_queue'),
    ) ?? 0;
    
    return {
      'vehicles': vehicleCount,
      'cargoes': cargoCount,
      'tasks': taskCount,
      'activeTasks': activeTaskCount,
      'unresolvedAnomalies': anomalyCount,
      'offlineQueue': offlineQueueCount,
    };
  }
}
