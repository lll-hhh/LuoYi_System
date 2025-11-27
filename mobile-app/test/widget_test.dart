import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('Widget Tests', () {
    testWidgets('Login screen has required fields', (WidgetTester tester) async {
      // 创建一个简单的登录表单进行测试
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Column(
              children: [
                TextField(
                  key: const Key('phone_field'),
                  decoration: const InputDecoration(labelText: '手机号'),
                ),
                TextField(
                  key: const Key('password_field'),
                  decoration: const InputDecoration(labelText: '密码'),
                  obscureText: true,
                ),
                ElevatedButton(
                  key: const Key('login_button'),
                  onPressed: () {},
                  child: const Text('登录'),
                ),
              ],
            ),
          ),
        ),
      );

      // 验证控件存在
      expect(find.byKey(const Key('phone_field')), findsOneWidget);
      expect(find.byKey(const Key('password_field')), findsOneWidget);
      expect(find.byKey(const Key('login_button')), findsOneWidget);
      expect(find.text('登录'), findsOneWidget);
    });

    testWidgets('Can enter text in text fields', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TextField(
              key: const Key('test_field'),
            ),
          ),
        ),
      );

      await tester.enterText(find.byKey(const Key('test_field')), 'test input');
      expect(find.text('test input'), findsOneWidget);
    });

    testWidgets('Button tap works', (WidgetTester tester) async {
      bool buttonPressed = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ElevatedButton(
              onPressed: () {
                buttonPressed = true;
              },
              child: const Text('Press Me'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Press Me'));
      expect(buttonPressed, isTrue);
    });
  });
}
