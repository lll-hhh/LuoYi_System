import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:luoyi_mobile/config/theme.dart';

class CargoListScreen extends StatelessWidget {
  const CargoListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('货物申报'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () => context.push('/cargo/declare'),
          ),
        ],
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: 5,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (context, index) => _buildCargoItem(context, index),
      ),
    );
  }

  Widget _buildCargoItem(BuildContext context, int index) {
    final statuses = ['PENDING', 'APPROVED', 'IN_TRANSIT', 'COMPLETED', 'REJECTED'];
    final status = statuses[index % statuses.length];

    return InkWell(
      onTap: () => context.push('/cargo/$index'),
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('CG20240115000${index + 1}', style: const TextStyle(fontWeight: FontWeight.w500)),
                _buildStatusTag(status),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(Icons.inventory_2, size: 16, color: AppTheme.textSecondary),
                const SizedBox(width: 4),
                const Text('建材', style: TextStyle(color: AppTheme.textSecondary)),
                const SizedBox(width: 16),
                const Icon(Icons.scale, size: 16, color: AppTheme.textSecondary),
                const SizedBox(width: 4),
                Text('${10 + index * 5}吨', style: const TextStyle(color: AppTheme.textSecondary)),
              ],
            ),
            const SizedBox(height: 8),
            const Row(
              children: [
                Icon(Icons.my_location, size: 16, color: AppTheme.primaryColor),
                SizedBox(width: 4),
                Text('北京市朝阳区'),
                Spacer(),
                Icon(Icons.location_on, size: 16, color: AppTheme.errorColor),
                SizedBox(width: 4),
                Text('天津市滨海新区'),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusTag(String status) {
    Color color;
    String text;
    switch (status) {
      case 'PENDING':
        color = AppTheme.warningColor;
        text = '待审核';
        break;
      case 'APPROVED':
        color = AppTheme.successColor;
        text = '已通过';
        break;
      case 'IN_TRANSIT':
        color = AppTheme.primaryColor;
        text = '运输中';
        break;
      case 'COMPLETED':
        color = AppTheme.textSecondary;
        text = '已完成';
        break;
      default:
        color = AppTheme.errorColor;
        text = '已驳回';
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(text, style: TextStyle(fontSize: 12, color: color)),
    );
  }
}
