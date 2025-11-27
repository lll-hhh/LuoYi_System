import 'package:flutter/material.dart';
import 'package:luoyi_mobile/config/theme.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _pushEnabled = true;
  bool _soundEnabled = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('设置')),
      body: ListView(
        children: [
          const SizedBox(height: 12),
          Container(
            color: Colors.white,
            child: Column(
              children: [
                ListTile(
                  title: const Text('推送通知'),
                  trailing: Switch(value: _pushEnabled, onChanged: (v) => setState(() => _pushEnabled = v)),
                ),
                ListTile(
                  title: const Text('声音提醒'),
                  trailing: Switch(value: _soundEnabled, onChanged: (v) => setState(() => _soundEnabled = v)),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Container(
            color: Colors.white,
            child: Column(
              children: [
                ListTile(
                  title: const Text('修改密码'),
                  trailing: const Icon(Icons.chevron_right, color: AppTheme.textSecondary),
                  onTap: () {},
                ),
                ListTile(
                  title: const Text('清除缓存'),
                  trailing: const Text('12.5MB', style: TextStyle(color: AppTheme.textSecondary)),
                  onTap: () => ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('缓存已清除'))),
                ),
                ListTile(
                  title: const Text('检查更新'),
                  trailing: const Text('v1.0.0', style: TextStyle(color: AppTheme.textSecondary)),
                  onTap: () => ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('已是最新版本'))),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Container(
            color: Colors.white,
            child: ListTile(
              title: const Text('隐私政策'),
              trailing: const Icon(Icons.chevron_right, color: AppTheme.textSecondary),
              onTap: () {},
            ),
          ),
        ],
      ),
    );
  }
}
