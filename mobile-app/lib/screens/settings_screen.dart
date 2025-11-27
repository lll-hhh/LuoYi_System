import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 设置屏幕
class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _enableNotifications = true;
  bool _enableLocationTracking = true;
  bool _enableAutoSync = true;
  bool _enableDarkMode = false;
  bool _enableSoundAlert = true;
  bool _enableVibration = true;
  int _locationUpdateInterval = 10; // 秒
  int _syncInterval = 5; // 分钟
  String _mapProvider = '高德地图';
  String _language = '中文';

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _enableNotifications = prefs.getBool('enableNotifications') ?? true;
      _enableLocationTracking = prefs.getBool('enableLocationTracking') ?? true;
      _enableAutoSync = prefs.getBool('enableAutoSync') ?? true;
      _enableDarkMode = prefs.getBool('enableDarkMode') ?? false;
      _enableSoundAlert = prefs.getBool('enableSoundAlert') ?? true;
      _enableVibration = prefs.getBool('enableVibration') ?? true;
      _locationUpdateInterval = prefs.getInt('locationUpdateInterval') ?? 10;
      _syncInterval = prefs.getInt('syncInterval') ?? 5;
      _mapProvider = prefs.getString('mapProvider') ?? '高德地图';
      _language = prefs.getString('language') ?? '中文';
    });
  }

  Future<void> _saveSetting(String key, dynamic value) async {
    final prefs = await SharedPreferences.getInstance();
    if (value is bool) {
      await prefs.setBool(key, value);
    } else if (value is int) {
      await prefs.setInt(key, value);
    } else if (value is String) {
      await prefs.setString(key, value);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('设置'),
      ),
      body: ListView(
        children: [
          _buildSection(
            title: '通知设置',
            children: [
              SwitchListTile(
                title: const Text('推送通知'),
                subtitle: const Text('接收任务和异常通知'),
                value: _enableNotifications,
                onChanged: (value) {
                  setState(() => _enableNotifications = value);
                  _saveSetting('enableNotifications', value);
                },
              ),
              SwitchListTile(
                title: const Text('声音提醒'),
                subtitle: const Text('收到通知时播放提示音'),
                value: _enableSoundAlert,
                onChanged: _enableNotifications
                    ? (value) {
                        setState(() => _enableSoundAlert = value);
                        _saveSetting('enableSoundAlert', value);
                      }
                    : null,
              ),
              SwitchListTile(
                title: const Text('振动提醒'),
                subtitle: const Text('收到通知时振动'),
                value: _enableVibration,
                onChanged: _enableNotifications
                    ? (value) {
                        setState(() => _enableVibration = value);
                        _saveSetting('enableVibration', value);
                      }
                    : null,
              ),
            ],
          ),
          _buildSection(
            title: '位置设置',
            children: [
              SwitchListTile(
                title: const Text('位置追踪'),
                subtitle: const Text('允许应用在后台获取位置'),
                value: _enableLocationTracking,
                onChanged: (value) {
                  setState(() => _enableLocationTracking = value);
                  _saveSetting('enableLocationTracking', value);
                },
              ),
              ListTile(
                title: const Text('位置更新间隔'),
                subtitle: Text('每 $_locationUpdateInterval 秒更新一次'),
                trailing: const Icon(Icons.chevron_right),
                enabled: _enableLocationTracking,
                onTap: _enableLocationTracking
                    ? () => _showIntervalPicker()
                    : null,
              ),
              ListTile(
                title: const Text('地图服务'),
                subtitle: Text(_mapProvider),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showMapProviderPicker(),
              ),
            ],
          ),
          _buildSection(
            title: '数据同步',
            children: [
              SwitchListTile(
                title: const Text('自动同步'),
                subtitle: const Text('自动将数据同步到服务器'),
                value: _enableAutoSync,
                onChanged: (value) {
                  setState(() => _enableAutoSync = value);
                  _saveSetting('enableAutoSync', value);
                },
              ),
              ListTile(
                title: const Text('同步间隔'),
                subtitle: Text('每 $_syncInterval 分钟同步一次'),
                trailing: const Icon(Icons.chevron_right),
                enabled: _enableAutoSync,
                onTap: _enableAutoSync ? () => _showSyncIntervalPicker() : null,
              ),
              ListTile(
                title: const Text('立即同步'),
                subtitle: const Text('手动将所有数据同步到服务器'),
                trailing: const Icon(Icons.sync),
                onTap: () => _performSync(),
              ),
              ListTile(
                title: const Text('清除缓存'),
                subtitle: const Text('清除本地缓存数据'),
                trailing: const Icon(Icons.delete_outline),
                onTap: () => _clearCache(),
              ),
            ],
          ),
          _buildSection(
            title: '显示设置',
            children: [
              SwitchListTile(
                title: const Text('深色模式'),
                subtitle: const Text('使用深色主题'),
                value: _enableDarkMode,
                onChanged: (value) {
                  setState(() => _enableDarkMode = value);
                  _saveSetting('enableDarkMode', value);
                  // 实际项目中需要触发主题切换
                },
              ),
              ListTile(
                title: const Text('语言'),
                subtitle: Text(_language),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showLanguagePicker(),
              ),
            ],
          ),
          _buildSection(
            title: '关于',
            children: [
              ListTile(
                title: const Text('版本'),
                subtitle: const Text('1.0.0'),
              ),
              ListTile(
                title: const Text('检查更新'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _checkUpdate(),
              ),
              ListTile(
                title: const Text('用户协议'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showUserAgreement(),
              ),
              ListTile(
                title: const Text('隐私政策'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showPrivacyPolicy(),
              ),
              ListTile(
                title: const Text('问题反馈'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showFeedback(),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: OutlinedButton(
              onPressed: () => _logout(),
              style: OutlinedButton.styleFrom(
                foregroundColor: Colors.red,
                side: const BorderSide(color: Colors.red),
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
              child: const Text('退出登录'),
            ),
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _buildSection({
    required String title,
    required List<Widget> children,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
          child: Text(
            title,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Colors.grey[600],
            ),
          ),
        ),
        Card(
          margin: const EdgeInsets.symmetric(horizontal: 16),
          child: Column(children: children),
        ),
      ],
    );
  }

  void _showIntervalPicker() {
    final intervals = [5, 10, 15, 30, 60];
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                '选择位置更新间隔',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            ...intervals.map((interval) => ListTile(
                  title: Text('$interval 秒'),
                  trailing: _locationUpdateInterval == interval
                      ? const Icon(Icons.check, color: Colors.blue)
                      : null,
                  onTap: () {
                    setState(() => _locationUpdateInterval = interval);
                    _saveSetting('locationUpdateInterval', interval);
                    Navigator.pop(context);
                  },
                )),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  void _showSyncIntervalPicker() {
    final intervals = [1, 5, 10, 15, 30];
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                '选择同步间隔',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            ...intervals.map((interval) => ListTile(
                  title: Text('$interval 分钟'),
                  trailing: _syncInterval == interval
                      ? const Icon(Icons.check, color: Colors.blue)
                      : null,
                  onTap: () {
                    setState(() => _syncInterval = interval);
                    _saveSetting('syncInterval', interval);
                    Navigator.pop(context);
                  },
                )),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  void _showMapProviderPicker() {
    final providers = ['高德地图', '百度地图', '腾讯地图'];
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                '选择地图服务',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            ...providers.map((provider) => ListTile(
                  title: Text(provider),
                  trailing: _mapProvider == provider
                      ? const Icon(Icons.check, color: Colors.blue)
                      : null,
                  onTap: () {
                    setState(() => _mapProvider = provider);
                    _saveSetting('mapProvider', provider);
                    Navigator.pop(context);
                  },
                )),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  void _showLanguagePicker() {
    final languages = ['中文', 'English'];
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                '选择语言',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            ...languages.map((language) => ListTile(
                  title: Text(language),
                  trailing: _language == language
                      ? const Icon(Icons.check, color: Colors.blue)
                      : null,
                  onTap: () {
                    setState(() => _language = language);
                    _saveSetting('language', language);
                    Navigator.pop(context);
                  },
                )),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Future<void> _performSync() async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const AlertDialog(
        content: Row(
          children: [
            CircularProgressIndicator(),
            SizedBox(width: 16),
            Text('正在同步...'),
          ],
        ),
      ),
    );

    // 模拟同步
    await Future.delayed(const Duration(seconds: 2));

    if (mounted) {
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('同步完成'),
          backgroundColor: Colors.green,
        ),
      );
    }
  }

  Future<void> _clearCache() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('清除缓存'),
        content: const Text('确定要清除本地缓存数据吗？这不会影响已同步的数据。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('清除'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      // 清除缓存逻辑
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('缓存已清除')),
        );
      }
    }
  }

  void _checkUpdate() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('当前已是最新版本')),
    );
  }

  void _showUserAgreement() {
    Navigator.pushNamed(context, '/user-agreement');
  }

  void _showPrivacyPolicy() {
    Navigator.pushNamed(context, '/privacy-policy');
  }

  void _showFeedback() {
    Navigator.pushNamed(context, '/feedback');
  }

  Future<void> _logout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('退出'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      // 执行登出逻辑
      final prefs = await SharedPreferences.getInstance();
      await prefs.clear();
      if (mounted) {
        Navigator.pushNamedAndRemoveUntil(context, '/login', (route) => false);
      }
    }
  }
}
