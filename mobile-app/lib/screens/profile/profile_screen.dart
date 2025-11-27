import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:luoyi_mobile/config/theme.dart';
import 'package:luoyi_mobile/providers/auth_provider.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('我的')),
      body: SingleChildScrollView(
        child: Column(
          children: [
            Consumer<AuthProvider>(
              builder: (context, auth, child) {
                return Container(
                  padding: const EdgeInsets.all(24),
                  color: Colors.white,
                  child: Row(
                    children: [
                      CircleAvatar(
                        radius: 35,
                        backgroundColor: AppTheme.primaryColor,
                        child: Text(
                          auth.user?.realName?.substring(0, 1) ?? 'U',
                          style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.white),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(auth.user?.realName ?? '用户', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                            const SizedBox(height: 4),
                            Text(auth.user?.phone ?? '', style: const TextStyle(color: AppTheme.textSecondary)),
                          ],
                        ),
                      ),
                      const Icon(Icons.qr_code, color: AppTheme.textSecondary),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 12),
            Container(
              color: Colors.white,
              child: Column(
                children: [
                  _buildMenuItem(Icons.local_shipping, '我的车辆', () => context.push('/vehicles')),
                  _buildMenuItem(Icons.inventory_2, '申报记录', () => context.push('/cargo')),
                  _buildMenuItem(Icons.route, '行驶轨迹', () {}),
                  _buildMenuItem(Icons.star, '我的收藏', () {}),
                ],
              ),
            ),
            const SizedBox(height: 12),
            Container(
              color: Colors.white,
              child: Column(
                children: [
                  _buildMenuItem(Icons.settings, '设置', () => context.push('/profile/settings')),
                  _buildMenuItem(Icons.help_outline, '帮助与反馈', () {}),
                  _buildMenuItem(Icons.info_outline, '关于我们', () {}),
                ],
              ),
            ),
            const SizedBox(height: 24),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: SizedBox(
                width: double.infinity,
                child: OutlinedButton(
                  onPressed: () {
                    context.read<AuthProvider>().logout();
                    context.go('/login');
                  },
                  style: OutlinedButton.styleFrom(foregroundColor: AppTheme.errorColor, side: const BorderSide(color: AppTheme.errorColor)),
                  child: const Text('退出登录'),
                ),
              ),
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  Widget _buildMenuItem(IconData icon, String title, VoidCallback onTap) {
    return ListTile(
      leading: Icon(icon, color: AppTheme.textSecondary),
      title: Text(title),
      trailing: const Icon(Icons.chevron_right, color: AppTheme.textSecondary),
      onTap: onTap,
    );
  }
}
