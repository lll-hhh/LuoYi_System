import 'package:luoyi_mobile/config/api.dart';
import 'package:luoyi_mobile/services/http_service.dart';

class AuthService {
  final HttpService _http = HttpService();

  Future<Map<String, dynamic>> login(String username, String password) async {
    final response = await _http.post(
      ApiConfig.login,
      data: {
        'username': username,
        'password': password,
      },
    );
    return response.data;
  }

  Future<void> register({
    required String username,
    required String password,
    required String phone,
    required String realName,
  }) async {
    await _http.post(
      ApiConfig.register,
      data: {
        'username': username,
        'password': password,
        'phone': phone,
        'realName': realName,
      },
    );
  }

  Future<void> logout() async {
    await _http.post(ApiConfig.logout);
  }

  Future<Map<String, dynamic>> refreshToken() async {
    final response = await _http.post(ApiConfig.refreshToken);
    return response.data;
  }

  Future<Map<String, dynamic>> getUserInfo() async {
    final response = await _http.get(ApiConfig.userInfo);
    return response.data;
  }

  Future<void> updateProfile(Map<String, dynamic> data) async {
    await _http.put(ApiConfig.updateProfile, data: data);
  }

  Future<void> changePassword(String oldPassword, String newPassword) async {
    await _http.post(
      ApiConfig.changePassword,
      data: {
        'oldPassword': oldPassword,
        'newPassword': newPassword,
      },
    );
  }
}
