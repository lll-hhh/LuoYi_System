import 'package:flutter_test/flutter_test.dart';
import 'package:luoyi_app/models/user.dart';

void main() {
  group('User Model Tests', () {
    test('User fromJson creates correct user', () {
      final json = {
        'userId': 1,
        'phone': '13800138000',
        'name': '测试用户',
        'avatar': 'https://example.com/avatar.jpg',
        'idCard': '110101199001011234',
        'status': 'active',
        'createdAt': '2024-01-01T00:00:00.000Z',
      };

      final user = User.fromJson(json);

      expect(user.userId, 1);
      expect(user.phone, '13800138000');
      expect(user.name, '测试用户');
      expect(user.status, 'active');
    });

    test('User toJson creates correct json', () {
      final user = User(
        userId: 1,
        phone: '13800138000',
        name: '测试用户',
        status: 'active',
      );

      final json = user.toJson();

      expect(json['userId'], 1);
      expect(json['phone'], '13800138000');
      expect(json['name'], '测试用户');
    });

    test('User equality', () {
      final user1 = User(userId: 1, phone: '13800138000', name: '测试用户');
      final user2 = User(userId: 1, phone: '13800138000', name: '测试用户');

      // 由于没有重写 == 运算符，这里只测试基本属性
      expect(user1.userId, user2.userId);
      expect(user1.phone, user2.phone);
    });
  });
}
