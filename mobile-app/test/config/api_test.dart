import 'package:flutter_test/flutter_test.dart';
import 'package:luoyi_app/config/api.dart';

void main() {
  group('API Configuration Tests', () {
    test('ApiConfig has correct base URL', () {
      expect(ApiConfig.baseUrl, isNotEmpty);
    });

    test('ApiConfig has correct endpoints', () {
      expect(ApiConfig.login, contains('login'));
      expect(ApiConfig.register, contains('register'));
      expect(ApiConfig.vehicles, contains('vehicles'));
      expect(ApiConfig.declarations, contains('declarations'));
    });

    test('ApiConfig timeout is reasonable', () {
      expect(ApiConfig.connectTimeout, greaterThan(0));
      expect(ApiConfig.receiveTimeout, greaterThan(0));
    });
  });
}
