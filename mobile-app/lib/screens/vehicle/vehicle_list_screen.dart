import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:luoyi_mobile/config/theme.dart';
import 'package:luoyi_mobile/providers/vehicle_provider.dart';
import 'package:luoyi_mobile/models/vehicle.dart';

class VehicleListScreen extends StatefulWidget {
  const VehicleListScreen({super.key});

  @override
  State<VehicleListScreen> createState() => _VehicleListScreenState();
}

class _VehicleListScreenState extends State<VehicleListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<VehicleProvider>().loadVehicles();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('我的车辆'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () => context.push('/vehicles/add'),
          ),
        ],
      ),
      body: Consumer<VehicleProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.vehicles.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.local_shipping_outlined,
                    size: 80,
                    color: Colors.grey[300],
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    '暂无车辆',
                    style: TextStyle(color: AppTheme.textSecondary),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => context.push('/vehicles/add'),
                    child: const Text('添加车辆'),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: () => provider.loadVehicles(),
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: provider.vehicles.length,
              separatorBuilder: (context, index) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                return _buildVehicleItem(context, provider.vehicles[index]);
              },
            ),
          );
        },
      ),
    );
  }

  Widget _buildVehicleItem(BuildContext context, Vehicle vehicle) {
    return InkWell(
      onTap: () => context.push('/vehicles/${vehicle.vehicleId}'),
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              width: 80,
              height: 50,
              decoration: BoxDecoration(
                color: _getPlateColor(vehicle.plateColor),
                borderRadius: BorderRadius.circular(4),
              ),
              alignment: Alignment.center,
              child: Text(
                vehicle.plateNumber,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    vehicle.vehicleTypeText,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${vehicle.brand ?? ''} ${vehicle.model ?? ''}'.trim(),
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppTheme.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '载重: ${vehicle.loadCapacity ?? 0}吨',
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppTheme.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
            _buildStatusTag(vehicle.status),
          ],
        ),
      ),
    );
  }

  Color _getPlateColor(String plateColor) {
    switch (plateColor) {
      case 'BLUE':
        return AppTheme.primaryColor;
      case 'YELLOW':
        return AppTheme.warningColor;
      case 'GREEN':
        return AppTheme.successColor;
      default:
        return Colors.grey;
    }
  }

  Widget _buildStatusTag(String status) {
    Color color;
    String text;

    switch (status) {
      case 'ACTIVE':
        color = AppTheme.successColor;
        text = '已认证';
        break;
      case 'PENDING':
        color = AppTheme.warningColor;
        text = '待审核';
        break;
      default:
        color = AppTheme.textSecondary;
        text = '未激活';
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        text,
        style: TextStyle(fontSize: 12, color: color),
      ),
    );
  }
}
