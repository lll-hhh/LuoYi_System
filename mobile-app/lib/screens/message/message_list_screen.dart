import 'package:flutter/material.dart';
import 'package:luoyi_mobile/config/theme.dart';

class MessageListScreen extends StatelessWidget {
  const MessageListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('消息'),
        actions: [
          TextButton(onPressed: () {}, child: const Text('全部已读')),
        ],
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: 10,
        separatorBuilder: (_, __) => const Divider(height: 1),
        itemBuilder: (context, index) => _buildMessageItem(index),
      ),
    );
  }

  Widget _buildMessageItem(int index) {
    final isUnread = index < 3;
    final types = ['SYSTEM', 'APPROVAL', 'ALERT', 'NOTICE'];
    final type = types[index % types.length];

    return ListTile(
      contentPadding: const EdgeInsets.symmetric(vertical: 8),
      leading: CircleAvatar(
        backgroundColor: _getTypeColor(type).withOpacity(0.1),
        child: Icon(_getTypeIcon(type), color: _getTypeColor(type)),
      ),
      title: Row(
        children: [
          Expanded(child: Text('${_getTypeText(type)}通知', maxLines: 1, overflow: TextOverflow.ellipsis)),
          if (isUnread)
            Container(
              width: 8, height: 8,
              decoration: const BoxDecoration(color: AppTheme.errorColor, shape: BoxShape.circle),
            ),
        ],
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 4),
          const Text('您的货物申报已审核通过，请注意查收。', maxLines: 2, overflow: TextOverflow.ellipsis),
          const SizedBox(height: 4),
          Text('${index + 1}小时前', style: const TextStyle(fontSize: 12, color: AppTheme.textSecondary)),
        ],
      ),
    );
  }

  Color _getTypeColor(String type) {
    switch (type) {
      case 'SYSTEM': return AppTheme.primaryColor;
      case 'APPROVAL': return AppTheme.successColor;
      case 'ALERT': return AppTheme.errorColor;
      default: return AppTheme.warningColor;
    }
  }

  IconData _getTypeIcon(String type) {
    switch (type) {
      case 'SYSTEM': return Icons.settings;
      case 'APPROVAL': return Icons.check_circle;
      case 'ALERT': return Icons.warning;
      default: return Icons.campaign;
    }
  }

  String _getTypeText(String type) {
    switch (type) {
      case 'SYSTEM': return '系统';
      case 'APPROVAL': return '审批';
      case 'ALERT': return '预警';
      default: return '公告';
    }
  }
}
