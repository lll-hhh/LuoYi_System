import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:luoyi_mobile/app.dart';
import 'package:luoyi_mobile/providers/auth_provider.dart';
import 'package:luoyi_mobile/providers/vehicle_provider.dart';
import 'package:luoyi_mobile/providers/cargo_provider.dart';
import 'package:luoyi_mobile/providers/message_provider.dart';
import 'package:luoyi_mobile/providers/task_provider.dart';
import 'package:luoyi_mobile/providers/location_provider.dart';
import 'package:luoyi_mobile/services/location_service.dart';
import 'package:luoyi_mobile/services/task_service.dart';
import 'package:luoyi_mobile/services/database_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // 设置状态栏样式
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.dark,
  ));
  
  // 设置屏幕方向为竖屏
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);
  
  // 初始化数据库服务
  final databaseService = DatabaseService();
  await databaseService.database;
  
  // 初始化位置服务
  final locationService = LocationService(databaseService: databaseService);
  
  // 初始化任务服务
  final taskService = TaskService(databaseService: databaseService);
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => VehicleProvider()),
        ChangeNotifierProvider(create: (_) => CargoProvider()),
        ChangeNotifierProvider(create: (_) => MessageProvider()),
        ChangeNotifierProvider(
          create: (_) => TaskProvider(
            taskService: taskService,
            locationService: locationService,
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => LocationProvider(
            locationService: locationService,
          ),
        ),
      ],
      child: const LuoyiApp(),
    ),
  );
}
