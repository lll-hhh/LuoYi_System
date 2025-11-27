import 'package:flutter/material.dart';
import 'package:luoyi_mobile/config/theme.dart';
import 'package:luoyi_mobile/services/api_service.dart';

/// 实时路况查询页面
class TrafficQueryScreen extends StatefulWidget {
  const TrafficQueryScreen({super.key});

  @override
  State<TrafficQueryScreen> createState() => _TrafficQueryScreenState();
}

class _TrafficQueryScreenState extends State<TrafficQueryScreen> {
  final ApiService _apiService = ApiService();
  bool _isLoading = false;
  String _selectedArea = '全部区域';
  List<RoadTraffic> _trafficData = [];
  
  final List<String> _areas = ['全部区域', '中心城区', '工业区', '物流园区', '高速公路'];

  @override
  void initState() {
    super.initState();
    _loadTrafficData();
  }

  Future<void> _loadTrafficData() async {
    setState(() => _isLoading = true);
    
    // 模拟数据加载
    await Future.delayed(const Duration(seconds: 1));
    
    setState(() {
      _trafficData = [
        RoadTraffic(
          roadId: 1,
          roadName: '中山路',
          congestionIndex: 5.2,
          avgSpeed: 25,
          vehicleCount: 850,
          status: '拥堵',
          statusColor: AppTheme.errorColor,
        ),
        RoadTraffic(
          roadId: 2,
          roadName: '人民大道',
          congestionIndex: 4.1,
          avgSpeed: 32,
          vehicleCount: 620,
          status: '缓行',
          statusColor: AppTheme.warningColor,
        ),
        RoadTraffic(
          roadId: 3,
          roadName: '建设路',
          congestionIndex: 3.5,
          avgSpeed: 35,
          vehicleCount: 450,
          status: '缓行',
          statusColor: AppTheme.warningColor,
        ),
        RoadTraffic(
          roadId: 4,
          roadName: '解放大道',
          congestionIndex: 2.3,
          avgSpeed: 45,
          vehicleCount: 380,
          status: '畅通',
          statusColor: AppTheme.successColor,
        ),
        RoadTraffic(
          roadId: 5,
          roadName: '和平路',
          congestionIndex: 4.5,
          avgSpeed: 28,
          vehicleCount: 520,
          status: '缓行',
          statusColor: AppTheme.warningColor,
        ),
        RoadTraffic(
          roadId: 6,
          roadName: '科技大道',
          congestionIndex: 1.2,
          avgSpeed: 55,
          vehicleCount: 320,
          status: '畅通',
          statusColor: AppTheme.successColor,
        ),
      ];
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('实时路况'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadTrafficData,
          ),
        ],
      ),
      body: Column(
        children: [
          // 区域筛选
          Container(
            height: 50,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: _areas.length,
              itemBuilder: (context, index) {
                final area = _areas[index];
                final isSelected = _selectedArea == area;
                return Padding(
                  padding: const EdgeInsets.only(right: 8, top: 8, bottom: 8),
                  child: ChoiceChip(
                    label: Text(area),
                    selected: isSelected,
                    onSelected: (selected) {
                      setState(() => _selectedArea = area);
                      _loadTrafficData();
                    },
                    selectedColor: AppTheme.primaryColor.withOpacity(0.2),
                  ),
                );
              },
            ),
          ),
          
          // 统计概览
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                _buildStatCard('拥堵路段', '${_trafficData.where((r) => r.congestionIndex >= 5).length}', AppTheme.errorColor),
                const SizedBox(width: 12),
                _buildStatCard('缓行路段', '${_trafficData.where((r) => r.congestionIndex >= 3 && r.congestionIndex < 5).length}', AppTheme.warningColor),
                const SizedBox(width: 12),
                _buildStatCard('畅通路段', '${_trafficData.where((r) => r.congestionIndex < 3).length}', AppTheme.successColor),
              ],
            ),
          ),
          
          // 路况列表
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : RefreshIndicator(
                    onRefresh: _loadTrafficData,
                    child: ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      itemCount: _trafficData.length,
                      itemBuilder: (context, index) {
                        return _buildTrafficCard(_trafficData[index]);
                      },
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(String label, String value, Color color) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: color.withOpacity(0.3)),
        ),
        child: Column(
          children: [
            Text(
              value,
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: color.withOpacity(0.8),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTrafficCard(RoadTraffic traffic) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => _showTrafficDetail(traffic),
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      traffic.roadName,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: traffic.statusColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      traffic.status,
                      style: TextStyle(
                        color: traffic.statusColor,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  _buildInfoItem(Icons.speed, '平均车速', '${traffic.avgSpeed}km/h'),
                  const SizedBox(width: 24),
                  _buildInfoItem(Icons.directions_car, '当前车流', '${traffic.vehicleCount}辆'),
                  const SizedBox(width: 24),
                  _buildInfoItem(Icons.trending_up, '拥堵指数', traffic.congestionIndex.toStringAsFixed(1)),
                ],
              ),
              const SizedBox(height: 12),
              // 拥堵指数进度条
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: LinearProgressIndicator(
                  value: traffic.congestionIndex / 10,
                  backgroundColor: Colors.grey[200],
                  valueColor: AlwaysStoppedAnimation<Color>(traffic.statusColor),
                  minHeight: 6,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoItem(IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 16, color: AppTheme.textSecondary),
        const SizedBox(width: 4),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              value,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 14,
              ),
            ),
            Text(
              label,
              style: const TextStyle(
                color: AppTheme.textSecondary,
                fontSize: 10,
              ),
            ),
          ],
        ),
      ],
    );
  }

  void _showTrafficDetail(RoadTraffic traffic) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => _TrafficDetailSheet(traffic: traffic),
    );
  }
}

class _TrafficDetailSheet extends StatelessWidget {
  final RoadTraffic traffic;

  const _TrafficDetailSheet({required this.traffic});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  traffic.roadName,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: traffic.statusColor,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Text(
                  traffic.status,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          _buildDetailRow('拥堵指数', traffic.congestionIndex.toStringAsFixed(1)),
          _buildDetailRow('平均车速', '${traffic.avgSpeed} km/h'),
          _buildDetailRow('当前车流量', '${traffic.vehicleCount} 辆/小时'),
          _buildDetailRow('更新时间', '刚刚'),
          const SizedBox(height: 24),
          const Text(
            '出行建议',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: traffic.statusColor.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                Icon(
                  traffic.congestionIndex >= 5 ? Icons.warning : Icons.info,
                  color: traffic.statusColor,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    _getSuggestion(traffic.congestionIndex),
                    style: TextStyle(color: traffic.statusColor),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: () {
                Navigator.pop(context);
                // 跳转到路线规划
              },
              icon: const Icon(Icons.navigation),
              label: const Text('规划路线'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: AppTheme.textSecondary)),
          Text(value, style: const TextStyle(fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  String _getSuggestion(double index) {
    if (index >= 6) return '严重拥堵，建议改变出行时间或选择其他路线';
    if (index >= 4) return '道路拥堵，建议选择备用路线';
    if (index >= 2) return '道路缓行，预计通行时间较长';
    return '道路畅通，可正常出行';
  }
}

/// 道路路况数据模型
class RoadTraffic {
  final int roadId;
  final String roadName;
  final double congestionIndex;
  final int avgSpeed;
  final int vehicleCount;
  final String status;
  final Color statusColor;

  RoadTraffic({
    required this.roadId,
    required this.roadName,
    required this.congestionIndex,
    required this.avgSpeed,
    required this.vehicleCount,
    required this.status,
    required this.statusColor,
  });
}
