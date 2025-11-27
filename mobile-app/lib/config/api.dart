class ApiConfig {
  static const String baseUrl = 'http://localhost:8082/api';
  
  // 认证相关
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String refreshToken = '/auth/refresh';
  static const String logout = '/auth/logout';
  
  // 用户相关
  static const String userInfo = '/user/info';
  static const String updateProfile = '/user/profile';
  static const String changePassword = '/user/password';
  
  // 车辆相关
  static const String vehicles = '/vehicles';
  static const String vehicleDetail = '/vehicles/{id}';
  static const String vehicleLocation = '/vehicles/{id}/location';
  
  // 货物申报
  static const String cargoDeclarations = '/cargo-declarations';
  static const String cargoDetail = '/cargo-declarations/{id}';
  static const String cargoApprove = '/cargo-declarations/{id}/approve';
  
  // 路线规划
  static const String routePlan = '/route/plan';
  static const String routeRecommend = '/route/recommend';
  
  // 消息
  static const String messages = '/messages';
  static const String messageRead = '/messages/{id}/read';
  
  // 文件上传
  static const String uploadImage = '/upload/image';
}
