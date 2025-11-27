import 'dart:async';
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// WebSocket 事件类型
enum WebSocketEventType {
  connected,
  disconnected,
  message,
  error,
}

/// WebSocket 事件
class WebSocketEvent {
  final WebSocketEventType type;
  final dynamic data;
  final String? error;

  WebSocketEvent({
    required this.type,
    this.data,
    this.error,
  });
}

/// WebSocket 服务
class WebSocketService {
  static final WebSocketService _instance = WebSocketService._internal();
  factory WebSocketService() => _instance;

  WebSocketChannel? _channel;
  Timer? _heartbeatTimer;
  Timer? _reconnectTimer;
  bool _isConnected = false;
  bool _shouldReconnect = true;
  int _reconnectAttempts = 0;
  static const int _maxReconnectAttempts = 5;
  static const Duration _heartbeatInterval = Duration(seconds: 30);
  static const Duration _reconnectDelay = Duration(seconds: 5);

  final _eventController = StreamController<WebSocketEvent>.broadcast();
  Stream<WebSocketEvent> get events => _eventController.stream;

  bool get isConnected => _isConnected;

  WebSocketService._internal();

  /// 连接 WebSocket
  Future<void> connect() async {
    if (_isConnected) return;

    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('auth_token');
      
      final uri = Uri.parse('ws://localhost:8080/ws?token=$token');
      _channel = WebSocketChannel.connect(uri);

      _channel!.stream.listen(
        _onMessage,
        onError: _onError,
        onDone: _onDone,
      );

      _isConnected = true;
      _reconnectAttempts = 0;
      _startHeartbeat();
      
      _eventController.add(WebSocketEvent(
        type: WebSocketEventType.connected,
      ));
    } catch (e) {
      _onError(e);
    }
  }

  /// 断开连接
  void disconnect() {
    _shouldReconnect = false;
    _stopHeartbeat();
    _stopReconnect();
    _channel?.sink.close();
    _channel = null;
    _isConnected = false;
    
    _eventController.add(WebSocketEvent(
      type: WebSocketEventType.disconnected,
    ));
  }

  /// 发送消息
  void send(Map<String, dynamic> message) {
    if (!_isConnected || _channel == null) {
      throw Exception('WebSocket 未连接');
    }
    _channel!.sink.add(jsonEncode(message));
  }

  /// 发送位置更新
  void sendLocationUpdate({
    required double latitude,
    required double longitude,
    required double speed,
    required double heading,
    String? taskId,
  }) {
    send({
      'type': 'location_update',
      'data': {
        'latitude': latitude,
        'longitude': longitude,
        'speed': speed,
        'heading': heading,
        'taskId': taskId,
        'timestamp': DateTime.now().toIso8601String(),
      },
    });
  }

  /// 发送任务状态更新
  void sendTaskStatusUpdate({
    required String taskId,
    required String status,
    String? notes,
  }) {
    send({
      'type': 'task_status_update',
      'data': {
        'taskId': taskId,
        'status': status,
        'notes': notes,
        'timestamp': DateTime.now().toIso8601String(),
      },
    });
  }

  /// 发送异常报告
  void sendAnomalyReport({
    required String taskId,
    required String type,
    required String severity,
    required String description,
    required double latitude,
    required double longitude,
    List<String>? photos,
  }) {
    send({
      'type': 'anomaly_report',
      'data': {
        'taskId': taskId,
        'type': type,
        'severity': severity,
        'description': description,
        'latitude': latitude,
        'longitude': longitude,
        'photos': photos,
        'timestamp': DateTime.now().toIso8601String(),
      },
    });
  }

  void _onMessage(dynamic message) {
    try {
      final data = jsonDecode(message as String);
      _eventController.add(WebSocketEvent(
        type: WebSocketEventType.message,
        data: data,
      ));

      // 处理特定消息类型
      _handleMessage(data);
    } catch (e) {
      _eventController.add(WebSocketEvent(
        type: WebSocketEventType.error,
        error: '消息解析失败: $e',
      ));
    }
  }

  void _handleMessage(Map<String, dynamic> data) {
    final type = data['type'] as String?;
    
    switch (type) {
      case 'pong':
        // 心跳响应
        break;
      case 'new_task':
        // 新任务通知
        break;
      case 'task_update':
        // 任务更新通知
        break;
      case 'alert':
        // 预警通知
        break;
      case 'message':
        // 消息通知
        break;
    }
  }

  void _onError(dynamic error) {
    _isConnected = false;
    _eventController.add(WebSocketEvent(
      type: WebSocketEventType.error,
      error: error.toString(),
    ));
    _scheduleReconnect();
  }

  void _onDone() {
    _isConnected = false;
    _stopHeartbeat();
    _eventController.add(WebSocketEvent(
      type: WebSocketEventType.disconnected,
    ));
    _scheduleReconnect();
  }

  void _startHeartbeat() {
    _heartbeatTimer?.cancel();
    _heartbeatTimer = Timer.periodic(_heartbeatInterval, (_) {
      if (_isConnected) {
        send({'type': 'ping'});
      }
    });
  }

  void _stopHeartbeat() {
    _heartbeatTimer?.cancel();
    _heartbeatTimer = null;
  }

  void _scheduleReconnect() {
    if (!_shouldReconnect) return;
    if (_reconnectAttempts >= _maxReconnectAttempts) {
      _eventController.add(WebSocketEvent(
        type: WebSocketEventType.error,
        error: '重连失败，已达最大重试次数',
      ));
      return;
    }

    _reconnectTimer?.cancel();
    _reconnectTimer = Timer(_reconnectDelay, () {
      _reconnectAttempts++;
      connect();
    });
  }

  void _stopReconnect() {
    _reconnectTimer?.cancel();
    _reconnectTimer = null;
  }

  void dispose() {
    disconnect();
    _eventController.close();
  }
}
