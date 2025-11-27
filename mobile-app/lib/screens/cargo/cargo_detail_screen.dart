import 'package:flutter/material.dart';
import 'package:luoyi_mobile/config/theme.dart';

class CargoDetailScreen extends StatelessWidget {
  final int cargoId;
  const CargoDetailScreen({super.key, required this.cargoId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('申报详情')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            _buildStatusCard(),
            const SizedBox(height: 16),
            _buildRouteCard(),
            const SizedBox(height: 16),
            _buildCargoCard(),
            const SizedBox(height: 16),
            _buildContactCard(),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: const LinearGradient(colors: [Color(0xFFFAAD14), Color(0xFFFA8C16)]),
        borderRadius: BorderRadius.circular(12),
      ),
      child: const Column(
        children: [
          Icon(Icons.hourglass_top, size: 48, color: Colors.white),
          SizedBox(height: 12),
          Text('待审核', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white)),
          SizedBox(height: 4),
          Text('CG2024011500001', style: TextStyle(color: Colors.white70)),
        ],
      ),
    );
  }

  Widget _buildRouteCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('运输路线', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          Row(
            children: [
              Column(
                children: [
                  Container(width: 12, height: 12, decoration: const BoxDecoration(color: AppTheme.primaryColor, shape: BoxShape.circle)),
                  Container(width: 2, height: 40, color: AppTheme.borderColor),
                  Container(width: 12, height: 12, decoration: const BoxDecoration(color: AppTheme.errorColor, shape: BoxShape.circle)),
                ],
              ),
              const SizedBox(width: 16),
              const Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('北京市朝阳区建国路88号'),
                    SizedBox(height: 32),
                    Text('天津市滨海新区海滨大道100号'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildCargoCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('货物信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          _buildRow('货物名称', '建筑材料'),
          _buildRow('货物类型', '普通货物'),
          _buildRow('重量', '15吨'),
          _buildRow('数量', '1批'),
          _buildRow('车辆', '京A12345'),
          _buildRow('预计发车', '2024-01-16 08:00'),
        ],
      ),
    );
  }

  Widget _buildContactCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('联系信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          _buildRow('收货人', '张先生'),
          _buildRow('联系电话', '13800138000'),
        ],
      ),
    );
  }

  Widget _buildRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: AppTheme.textSecondary)),
          Text(value),
        ],
      ),
    );
  }
}
