import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import 'package:luoyi_mobile/models/location.dart';
import 'package:luoyi_mobile/services/location_service.dart';
import 'package:luoyi_mobile/services/route_service.dart';

/// 络绎智慧交通管理系统 - 位置与路线状态管理
class LocationProvider extends ChangeNotifier {
  final LocationService _locationService = LocationService();
  final RouteService _routeService = RouteService();

  Position? _currentPosition;
  RouteInfo? _currentRoute;
  List<RouteInfo> _alternativeRoutes = [];
  List<LocationRecord> _trackHistory = [];
  bool _isTracking = false;
  bool _isLoading = false;
  String? _error;
  StreamSubscription<Position>? _positionSubscription;

  // Getters
  Position? get currentPosition => _currentPosition;
  RouteInfo? get currentRoute => _currentRoute;
  List<RouteInfo> get alternativeRoutes => _alternativeRoutes;
  List<LocationRecord> get trackHistory => _trackHistory;
  bool get isTracking => _isTracking;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get hasLocation => _currentPosition != null;

  /// 初始化并获取当前位置
  Future<void> init() async {
    await getCurrentLocation();
  }

  /// 获取当前位置
  Future<Position?> getCurrentLocation() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentPosition = await _locationService.getCurrentPosition();
      return _currentPosition;
    } catch (e) {
      _error = '获取位置失败: $e';
      return null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 开始位置追踪
  Future<void> startTracking({int? taskId}) async {
    if (_isTracking) return;

    try {
      await _locationService.startTracking(taskId: taskId);
      _isTracking = true;
      
      _positionSubscription = _locationService.positionStream.listen((position) {
        _currentPosition = position;
        
        // 添加到轨迹历史
        _trackHistory.add(LocationRecord(
          latitude: position.latitude,
          longitude: position.longitude,
          speed: position.speed,
          heading: position.heading,
          accuracy: position.accuracy,
          altitude: position.altitude,
          recordedAt: DateTime.now(),
        ));
        
        // 限制历史记录数量
        if (_trackHistory.length > 1000) {
          _trackHistory.removeAt(0);
        }
        
        notifyListeners();
      });
      
      notifyListeners();
    } catch (e) {
      _error = '启动追踪失败: $e';
      notifyListeners();
    }
  }

  /// 停止位置追踪
  Future<void> stopTracking() async {
    await _locationService.stopTracking();
    await _positionSubscription?.cancel();
    _positionSubscription = null;
    _isTracking = false;
    notifyListeners();
  }

  /// 规划路线
  Future<void> planRoute({
    required double startLat,
    required double startLng,
    required double endLat,
    required double endLng,
    String preference = 'fastest',
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final result = await _routeService.getRecommendedRoute(
        startLat: startLat,
        startLng: startLng,
        endLat: endLat,
        endLng: endLng,
        preference: preference,
      );
      
      if (result != null) {
        _currentRoute = result['recommended'];
        _alternativeRoutes = result['alternatives'] ?? [];
      }
    } catch (e) {
      _error = '路线规划失败: $e';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 从当前位置规划路线
  Future<void> planRouteFromCurrent({
    required double endLat,
    required double endLng,
    String preference = 'fastest',
  }) async {
    if (_currentPosition == null) {
      await getCurrentLocation();
    }
    
    if (_currentPosition != null) {
      await planRoute(
        startLat: _currentPosition!.latitude,
        startLng: _currentPosition!.longitude,
        endLat: endLat,
        endLng: endLng,
        preference: preference,
      );
    }
  }

  /// 选择备选路线
  void selectAlternativeRoute(int index) {
    if (index >= 0 && index < _alternativeRoutes.length) {
      // 将当前路线放入备选
      if (_currentRoute != null) {
        _alternativeRoutes.insert(0, _currentRoute!);
      }
      
      // 选择新路线
      _currentRoute = _alternativeRoutes.removeAt(index);
      notifyListeners();
    }
  }

  /// 清除路线
  void clearRoute() {
    _currentRoute = null;
    _alternativeRoutes.clear();
    notifyListeners();
  }

  /// 清除轨迹历史
  void clearTrackHistory() {
    _trackHistory.clear();
    notifyListeners();
  }

  /// 计算距离
  double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
    return _locationService.calculateDistance(lat1, lng1, lat2, lng2);
  }

  @override
  void dispose() {
    stopTracking();
    super.dispose();
  }
}
