import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:luoyi_mobile/config/theme.dart';
import 'package:luoyi_mobile/providers/message_provider.dart';

class MainScreen extends StatefulWidget {
  final Widget child;

  const MainScreen({super.key, required this.child});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _currentIndex = 0;

  final List<String> _routes = [
    '/',
    '/vehicles',
    '/cargo',
    '/messages',
    '/profile',
  ];

  void _onTap(int index) {
    if (index != _currentIndex) {
      setState(() {
        _currentIndex = index;
      });
      context.go(_routes[index]);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: widget.child,
      bottomNavigationBar: Consumer<MessageProvider>(
        builder: (context, messageProvider, child) {
          return BottomNavigationBar(
            currentIndex: _currentIndex,
            onTap: _onTap,
            items: [
              const BottomNavigationBarItem(
                icon: Icon(Icons.home_outlined),
                activeIcon: Icon(Icons.home),
                label: '首页',
              ),
              const BottomNavigationBarItem(
                icon: Icon(Icons.local_shipping_outlined),
                activeIcon: Icon(Icons.local_shipping),
                label: '车辆',
              ),
              const BottomNavigationBarItem(
                icon: Icon(Icons.inventory_2_outlined),
                activeIcon: Icon(Icons.inventory_2),
                label: '货物',
              ),
              BottomNavigationBarItem(
                icon: Badge(
                  isLabelVisible: messageProvider.unreadCount > 0,
                  label: Text(
                    messageProvider.unreadCount > 99
                        ? '99+'
                        : messageProvider.unreadCount.toString(),
                  ),
                  child: const Icon(Icons.notifications_outlined),
                ),
                activeIcon: Badge(
                  isLabelVisible: messageProvider.unreadCount > 0,
                  label: Text(
                    messageProvider.unreadCount > 99
                        ? '99+'
                        : messageProvider.unreadCount.toString(),
                  ),
                  child: const Icon(Icons.notifications),
                ),
                label: '消息',
              ),
              const BottomNavigationBarItem(
                icon: Icon(Icons.person_outline),
                activeIcon: Icon(Icons.person),
                label: '我的',
              ),
            ],
          );
        },
      ),
    );
  }
}
