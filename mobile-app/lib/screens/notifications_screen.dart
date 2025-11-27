import 'package:flutter/material.dart';

/// 通知列表屏幕
class NotificationsScreen extends StatefulWidget {
  const NotificationsScreen({super.key});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final List<NotificationItem> _notifications = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadNotifications();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadNotifications() async {
    setState(() => _isLoading = true);
    
    // 模拟加载通知数据
    await Future.delayed(const Duration(seconds: 1));
    
    setState(() {
      _notifications.addAll([
        NotificationItem(
          id: '1',
          type: NotificationType.task,
          title: '新任务分配',
          content: '您有一个新的运输任务：北京 → 上海，请及时处理',
          time: DateTime.now().subtract(const Duration(minutes: 10)),
          isRead: false,
        ),
        NotificationItem(
          id: '2',
          type: NotificationType.alert,
          title: '路况预警',
          content: 'G2高速公路前方5公里处发生事故，建议绕行',
          time: DateTime.now().subtract(const Duration(hours: 1)),
          isRead: false,
        ),
        NotificationItem(
          id: '3',
          type: NotificationType.system,
          title: '系统更新',
          content: '新版本1.1.0已发布，请及时更新以获取最新功能',
          time: DateTime.now().subtract(const Duration(hours: 3)),
          isRead: true,
        ),
        NotificationItem(
          id: '4',
          type: NotificationType.task,
          title: '任务完成确认',
          content: '您的任务 #T20240101001 已完成，感谢您的付出！',
          time: DateTime.now().subtract(const Duration(days: 1)),
          isRead: true,
        ),
        NotificationItem(
          id: '5',
          type: NotificationType.alert,
          title: '天气预警',
          content: '未来2小时将有大雨，请注意行车安全',
          time: DateTime.now().subtract(const Duration(days: 1, hours: 5)),
          isRead: true,
        ),
      ]);
      _isLoading = false;
    });
  }

  List<NotificationItem> _getFilteredNotifications(NotificationType? type) {
    if (type == null) return _notifications;
    return _notifications.where((n) => n.type == type).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('消息通知'),
        actions: [
          PopupMenuButton<String>(
            onSelected: _handleMenuAction,
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'read_all',
                child: Text('全部标为已读'),
              ),
              const PopupMenuItem(
                value: 'clear_all',
                child: Text('清空所有通知'),
              ),
            ],
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: '全部'),
            Tab(text: '任务'),
            Tab(text: '预警'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildNotificationList(null),
          _buildNotificationList(NotificationType.task),
          _buildNotificationList(NotificationType.alert),
        ],
      ),
    );
  }

  Widget _buildNotificationList(NotificationType? type) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    final notifications = _getFilteredNotifications(type);

    if (notifications.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.notifications_off_outlined,
              size: 80,
              color: Colors.grey[300],
            ),
            const SizedBox(height: 16),
            Text(
              '暂无通知',
              style: TextStyle(
                fontSize: 18,
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadNotifications,
      child: ListView.builder(
        itemCount: notifications.length,
        itemBuilder: (context, index) {
          final notification = notifications[index];
          return _buildNotificationItem(notification);
        },
      ),
    );
  }

  Widget _buildNotificationItem(NotificationItem notification) {
    return Dismissible(
      key: Key(notification.id),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        color: Colors.red,
        child: const Icon(Icons.delete, color: Colors.white),
      ),
      onDismissed: (_) => _deleteNotification(notification),
      child: InkWell(
        onTap: () => _viewNotification(notification),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: notification.isRead ? Colors.white : Colors.blue[50],
            border: Border(
              bottom: BorderSide(color: Colors.grey[200]!),
            ),
          ),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildTypeIcon(notification.type),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            notification.title,
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: notification.isRead
                                  ? FontWeight.normal
                                  : FontWeight.bold,
                            ),
                          ),
                        ),
                        Text(
                          _formatTime(notification.time),
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[500],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      notification.content,
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey[600],
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              if (!notification.isRead)
                Container(
                  width: 8,
                  height: 8,
                  margin: const EdgeInsets.only(left: 8),
                  decoration: const BoxDecoration(
                    color: Colors.red,
                    shape: BoxShape.circle,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTypeIcon(NotificationType type) {
    IconData icon;
    Color color;

    switch (type) {
      case NotificationType.task:
        icon = Icons.assignment;
        color = Colors.blue;
        break;
      case NotificationType.alert:
        icon = Icons.warning;
        color = Colors.orange;
        break;
      case NotificationType.system:
        icon = Icons.settings;
        color = Colors.grey;
        break;
    }

    return Container(
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        shape: BoxShape.circle,
      ),
      child: Icon(icon, color: color, size: 24),
    );
  }

  String _formatTime(DateTime time) {
    final now = DateTime.now();
    final diff = now.difference(time);

    if (diff.inMinutes < 60) {
      return '${diff.inMinutes}分钟前';
    } else if (diff.inHours < 24) {
      return '${diff.inHours}小时前';
    } else if (diff.inDays < 7) {
      return '${diff.inDays}天前';
    } else {
      return '${time.month}/${time.day}';
    }
  }

  void _handleMenuAction(String action) {
    switch (action) {
      case 'read_all':
        _markAllAsRead();
        break;
      case 'clear_all':
        _clearAllNotifications();
        break;
    }
  }

  void _markAllAsRead() {
    setState(() {
      for (var notification in _notifications) {
        notification.isRead = true;
      }
    });
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('已全部标为已读')),
    );
  }

  void _clearAllNotifications() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认清空'),
        content: const Text('确定要清空所有通知吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              setState(() => _notifications.clear());
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  void _viewNotification(NotificationItem notification) {
    setState(() => notification.isRead = true);
    
    // 根据通知类型导航到相应页面
    switch (notification.type) {
      case NotificationType.task:
        // Navigator.pushNamed(context, '/task-detail', arguments: notification.relatedId);
        break;
      case NotificationType.alert:
        _showAlertDetail(notification);
        break;
      case NotificationType.system:
        _showSystemNotificationDetail(notification);
        break;
    }
  }

  void _showAlertDetail(NotificationItem notification) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(notification.title),
        content: Text(notification.content),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('关闭'),
          ),
        ],
      ),
    );
  }

  void _showSystemNotificationDetail(NotificationItem notification) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(notification.title),
        content: Text(notification.content),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('关闭'),
          ),
          if (notification.title.contains('更新'))
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                // 跳转到应用商店更新
              },
              child: const Text('立即更新'),
            ),
        ],
      ),
    );
  }

  void _deleteNotification(NotificationItem notification) {
    setState(() => _notifications.remove(notification));
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: const Text('通知已删除'),
        action: SnackBarAction(
          label: '撤销',
          onPressed: () {
            setState(() => _notifications.add(notification));
          },
        ),
      ),
    );
  }
}

/// 通知类型
enum NotificationType {
  task,   // 任务通知
  alert,  // 预警通知
  system, // 系统通知
}

/// 通知项
class NotificationItem {
  final String id;
  final NotificationType type;
  final String title;
  final String content;
  final DateTime time;
  bool isRead;
  final String? relatedId;

  NotificationItem({
    required this.id,
    required this.type,
    required this.title,
    required this.content,
    required this.time,
    required this.isRead,
    this.relatedId,
  });
}
