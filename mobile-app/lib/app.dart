import 'package:flutter/material.dart';
import 'package:luoyi_mobile/config/theme.dart';
import 'package:luoyi_mobile/config/routes.dart';

class LuoyiApp extends StatelessWidget {
  const LuoyiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: '络绎',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.light,
      routerConfig: AppRouter.router,
      builder: (context, child) {
        return MediaQuery(
          data: MediaQuery.of(context).copyWith(textScaleFactor: 1.0),
          child: child!,
        );
      },
    );
  }
}
