import 'dart:async';
import 'package:geolocator/geolocator.dart';
import 'package:luoyi_mobile/models/location.dart';
import 'package:luoyi_mobile/services/database_service.dart';

/// 络绎智慧交通管理系统 - 位置服务
/// 提供GPS定位、位置追踪、轨迹记录功能
class LocationService {
  static final LocationService _instance = LocationService._internal();
  factory LocationService() => _instance;
  LocationService._internal();

  final DatabaseService _db = DatabaseService();
  
  StreamSubscription<Position>? _positionSubscription;
  Position? _currentPosition;
  int? _currentTaskId;
  bool _isTracking = false;

  // 位置更新流
  final _positionController = StreamController<Position>.broadcast();
  Stream<Position> get positionStream => _positionController.stream;

  /// 当前位置
  Position? get currentPosition => _currentPosition;

  /// 是否正在追踪
  bool get isTracking => _isTracking;

  /// 检查并请求位置权限
  Future<bool> checkAndRequestPermission() async {
    bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      return false;
    }

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        return false;
      }
    }

    if (permission == LocationPermission.deniedForever) {
      return false;
    }

    return true;
  }

  /// 获取当前位置
  Future<Position?> getCurrentPosition() async {
    try {
      bool hasPermission = await checkAndRequestPermission();
      if (!hasPermission) return null;

      _currentPosition = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
        timeLimit: const Duration(seconds: 15),
      );
      
      return _currentPosition;
    } catch (e) {
      print('获取位置失败: $e');
      return null;
    }
  }

  /// 开始追踪位置
  Future<void> startTracking({
    int? taskId,
    int intervalSeconds = 10,
    double distanceFilter = 10,
  }) async {
    if (_isTracking) {
      await stopTracking();
    }

    bool hasPermission = await checkAndRequestPermission();
    if (!hasPermission) {
      throw Exception('没有位置权限');
    }

    _currentTaskId = taskId;
    _isTracking = true;

    LocationSettings locationSettings = LocationSettings(
      accuracy: LocationAccuracy.high,
      distanceFilter: distanceFilter,
    );

    _positionSubscription = Geolocator.getPositionStream(
      locationSettings: locationSettings,
    ).listen(
      (Position position) async {
        _currentPosition = position;
        _positionController.add(position);

        // 保存到本地数据库
        if (_currentTaskId != null) {
          await _saveLocationRecord(position);
        }
      },
      onError: (error) {
        print('位置追踪错误: $error');
      },
    );
  }

  /// 停止追踪
  Future<void> stopTracking() async {
    await _positionSubscription?.cancel();
    _positionSubscription = null;
    _isTracking = false;
    _currentTaskId = null;
  }

  /// 保存位置记录到数据库
  Future<void> _saveLocationRecord(Position position) async {
    await _db.insertLocationRecord({
      'task_id': _currentTaskId,
      'latitude': position.latitude,
      'longitude': position.longitude,
      'speed': position.speed,
      'heading': position.heading,
      'accuracy': position.accuracy,
      'altitude': position.altitude,
      'synced': 0,
    });
  }

  /// 获取任务的位置记录
  Future<List<LocationRecord>> getTaskLocations(int taskId) async {
    final records = await _db.getLocationRecords(taskId);
    return records.map((r) => LocationRecord.fromJson(r)).toList();
  }

  /// 计算两点之间的距离（米）
  double calculateDistance(
    double startLat,
    double startLng,
    double endLat,
    double endLng,
  ) {
    return Geolocator.distanceBetween(startLat, startLng, endLat, endLng);
  }

  /// 计算方位角
  double calculateBearing(
    double startLat,
    double startLng,
    double endLat,
    double endLng,
  ) {
    return Geolocator.bearingBetween(startLat, startLng, endLat, endLng);
  }

  /// 释放资源
  void dispose() {
    stopTracking();
    _positionController.close();
  }
}
