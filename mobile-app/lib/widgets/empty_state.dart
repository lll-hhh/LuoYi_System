import 'package:flutter/material.dart';

/// 空状态组件
class EmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? action;
  final double iconSize;
  final Color? iconColor;

  const EmptyState({
    super.key,
    required this.icon,
    required this.title,
    this.subtitle,
    this.action,
    this.iconSize = 80,
    this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: iconSize,
              color: iconColor ?? Colors.grey[300],
            ),
            const SizedBox(height: 16),
            Text(
              title,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w600,
                color: Colors.grey[600],
              ),
              textAlign: TextAlign.center,
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(
                subtitle!,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.grey[500],
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (action != null) ...[
              const SizedBox(height: 24),
              action!,
            ],
          ],
        ),
      ),
    );
  }
}

/// 预设的空状态组件
class NoTasksState extends StatelessWidget {
  final VoidCallback? onRefresh;

  const NoTasksState({super.key, this.onRefresh});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.assignment_outlined,
      title: '暂无任务',
      subtitle: '当前没有待处理的运输任务',
      action: onRefresh != null
          ? ElevatedButton.icon(
              onPressed: onRefresh,
              icon: const Icon(Icons.refresh),
              label: const Text('刷新'),
            )
          : null,
    );
  }
}

class NoDataState extends StatelessWidget {
  final String? message;
  final VoidCallback? onRetry;

  const NoDataState({super.key, this.message, this.onRetry});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.inbox_outlined,
      title: message ?? '暂无数据',
      action: onRetry != null
          ? TextButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('重试'),
            )
          : null,
    );
  }
}

class NoConnectionState extends StatelessWidget {
  final VoidCallback? onRetry;

  const NoConnectionState({super.key, this.onRetry});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.wifi_off_outlined,
      title: '网络连接失败',
      subtitle: '请检查您的网络设置后重试',
      action: onRetry != null
          ? ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('重试'),
            )
          : null,
    );
  }
}

class ErrorState extends StatelessWidget {
  final String? message;
  final VoidCallback? onRetry;

  const ErrorState({super.key, this.message, this.onRetry});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.error_outline,
      iconColor: Colors.red[300],
      title: '出错了',
      subtitle: message ?? '加载数据时出现错误',
      action: onRetry != null
          ? ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('重试'),
            )
          : null,
    );
  }
}

class NoLocationState extends StatelessWidget {
  final VoidCallback? onEnable;

  const NoLocationState({super.key, this.onEnable});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.location_off_outlined,
      title: '位置服务未开启',
      subtitle: '请开启位置权限以使用定位功能',
      action: onEnable != null
          ? ElevatedButton.icon(
              onPressed: onEnable,
              icon: const Icon(Icons.location_on),
              label: const Text('开启定位'),
            )
          : null,
    );
  }
}

class SearchEmptyState extends StatelessWidget {
  final String query;

  const SearchEmptyState({super.key, required this.query});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.search_off,
      title: '未找到结果',
      subtitle: '没有找到与"$query"相关的内容',
    );
  }
}
