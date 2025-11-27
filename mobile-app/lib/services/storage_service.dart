import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:luoyi_mobile/models/user.dart';

class StorageService {
  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'user_info';
  static const String _settingsKey = 'app_settings';

  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  // Token 存储
  Future<void> saveToken(String token) async {
    await _secureStorage.write(key: _tokenKey, value: token);
  }

  Future<String?> getToken() async {
    return await _secureStorage.read(key: _tokenKey);
  }

  Future<void> deleteToken() async {
    await _secureStorage.delete(key: _tokenKey);
  }

  // 用户信息存储
  Future<void> saveUser(User user) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userKey, jsonEncode(user.toJson()));
  }

  Future<User?> getUser() async {
    final prefs = await SharedPreferences.getInstance();
    final userStr = prefs.getString(_userKey);
    if (userStr != null) {
      return User.fromJson(jsonDecode(userStr));
    }
    return null;
  }

  Future<void> deleteUser() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_userKey);
  }

  // 清除认证信息
  Future<void> clearAuth() async {
    await deleteToken();
    await deleteUser();
  }

  // 应用设置
  Future<void> saveSetting(String key, dynamic value) async {
    final prefs = await SharedPreferences.getInstance();
    if (value is String) {
      await prefs.setString('$_settingsKey.$key', value);
    } else if (value is bool) {
      await prefs.setBool('$_settingsKey.$key', value);
    } else if (value is int) {
      await prefs.setInt('$_settingsKey.$key', value);
    } else if (value is double) {
      await prefs.setDouble('$_settingsKey.$key', value);
    }
  }

  Future<T?> getSetting<T>(String key) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.get('$_settingsKey.$key') as T?;
  }
}
