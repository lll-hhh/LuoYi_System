import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  final int userId;
  final String username;
  final String? realName;
  final String? phone;
  final String? email;
  final String? avatar;
  final String userType; // DRIVER, OWNER, ENTERPRISE
  final String status;
  final DateTime? createdAt;

  User({
    required this.userId,
    required this.username,
    this.realName,
    this.phone,
    this.email,
    this.avatar,
    required this.userType,
    required this.status,
    this.createdAt,
  });

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);

  User copyWith({
    int? userId,
    String? username,
    String? realName,
    String? phone,
    String? email,
    String? avatar,
    String? userType,
    String? status,
    DateTime? createdAt,
  }) {
    return User(
      userId: userId ?? this.userId,
      username: username ?? this.username,
      realName: realName ?? this.realName,
      phone: phone ?? this.phone,
      email: email ?? this.email,
      avatar: avatar ?? this.avatar,
      userType: userType ?? this.userType,
      status: status ?? this.status,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
