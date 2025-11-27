import 'package:flutter/material.dart';
import 'package:luoyi_mobile/config/theme.dart';

class RoutePlanScreen extends StatelessWidget {
  const RoutePlanScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('路线规划')),
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            color: Colors.white,
            child: Column(
              children: [
                TextField(
                  decoration: InputDecoration(
                    hintText: '起点',
                    prefixIcon: const Icon(Icons.my_location, color: AppTheme.primaryColor),
                    filled: true,
                    fillColor: Colors.grey[100],
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide.none),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  decoration: InputDecoration(
                    hintText: '终点',
                    prefixIcon: const Icon(Icons.location_on, color: AppTheme.errorColor),
                    filled: true,
                    fillColor: Colors.grey[100],
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide.none),
                  ),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(onPressed: () {}, child: const Text('搜索路线')),
                ),
              ],
            ),
          ),
          Expanded(
            child: Container(
              color: Colors.grey[200],
              child: const Center(child: Text('地图显示区域', style: TextStyle(color: AppTheme.textSecondary))),
            ),
          ),
        ],
      ),
    );
  }
}
