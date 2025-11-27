import 'package:go_router/go_router.dart';
import 'package:luoyi_mobile/screens/splash_screen.dart';
import 'package:luoyi_mobile/screens/login_screen.dart';
import 'package:luoyi_mobile/screens/register_screen.dart';
import 'package:luoyi_mobile/screens/main_screen.dart';
import 'package:luoyi_mobile/screens/home/home_screen.dart';
import 'package:luoyi_mobile/screens/vehicle/vehicle_list_screen.dart';
import 'package:luoyi_mobile/screens/vehicle/vehicle_detail_screen.dart';
import 'package:luoyi_mobile/screens/vehicle/vehicle_add_screen.dart';
import 'package:luoyi_mobile/screens/cargo/cargo_list_screen.dart';
import 'package:luoyi_mobile/screens/cargo/cargo_declare_screen.dart';
import 'package:luoyi_mobile/screens/cargo/cargo_detail_screen.dart';
import 'package:luoyi_mobile/screens/route/route_plan_screen.dart';
import 'package:luoyi_mobile/screens/route/route_navigation_screen.dart';
import 'package:luoyi_mobile/screens/message/message_list_screen.dart';
import 'package:luoyi_mobile/screens/profile/profile_screen.dart';
import 'package:luoyi_mobile/screens/profile/settings_screen.dart';

class AppRouter {
  static final router = GoRouter(
    initialLocation: '/splash',
    routes: [
      GoRoute(
        path: '/splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) => MainScreen(child: child),
        routes: [
          GoRoute(
            path: '/',
            builder: (context, state) => const HomeScreen(),
          ),
          GoRoute(
            path: '/vehicles',
            builder: (context, state) => const VehicleListScreen(),
            routes: [
              GoRoute(
                path: 'add',
                builder: (context, state) => const VehicleAddScreen(),
              ),
              GoRoute(
                path: ':id',
                builder: (context, state) => VehicleDetailScreen(
                  vehicleId: int.parse(state.pathParameters['id']!),
                ),
              ),
            ],
          ),
          GoRoute(
            path: '/cargo',
            builder: (context, state) => const CargoListScreen(),
            routes: [
              GoRoute(
                path: 'declare',
                builder: (context, state) => const CargoDeclareScreen(),
              ),
              GoRoute(
                path: ':id',
                builder: (context, state) => CargoDetailScreen(
                  cargoId: int.parse(state.pathParameters['id']!),
                ),
              ),
            ],
          ),
          GoRoute(
            path: '/route',
            builder: (context, state) => const RoutePlanScreen(),
            routes: [
              GoRoute(
                path: 'navigation',
                builder: (context, state) => const RouteNavigationScreen(),
              ),
            ],
          ),
          GoRoute(
            path: '/messages',
            builder: (context, state) => const MessageListScreen(),
          ),
          GoRoute(
            path: '/profile',
            builder: (context, state) => const ProfileScreen(),
            routes: [
              GoRoute(
                path: 'settings',
                builder: (context, state) => const SettingsScreen(),
              ),
            ],
          ),
        ],
      ),
    ],
  );
}
