import 'package:json_annotation/json_annotation.dart';

part 'vehicle.g.dart';

@JsonSerializable()
class Vehicle {
  final int? vehicleId;
  final String plateNumber;
  final String plateColor; // BLUE, YELLOW, GREEN, WHITE
  final String vehicleType; // LIGHT_TRUCK, MEDIUM_TRUCK, HEAVY_TRUCK, CONTAINER, TANKER
  final String? brand;
  final String? model;
  final double? loadCapacity; // 载重量(吨)
  final double? length;
  final double? width;
  final double? height;
  final String? engineNumber;
  final String? vin;
  final String? drivingLicenseImage;
  final String? vehicleImage;
  final String status; // ACTIVE, INACTIVE, PENDING
  final DateTime? createdAt;

  Vehicle({
    this.vehicleId,
    required this.plateNumber,
    required this.plateColor,
    required this.vehicleType,
    this.brand,
    this.model,
    this.loadCapacity,
    this.length,
    this.width,
    this.height,
    this.engineNumber,
    this.vin,
    this.drivingLicenseImage,
    this.vehicleImage,
    required this.status,
    this.createdAt,
  });

  factory Vehicle.fromJson(Map<String, dynamic> json) => _$VehicleFromJson(json);
  Map<String, dynamic> toJson() => _$VehicleToJson(this);

  String get vehicleTypeText {
    switch (vehicleType) {
      case 'LIGHT_TRUCK':
        return '轻型货车';
      case 'MEDIUM_TRUCK':
        return '中型货车';
      case 'HEAVY_TRUCK':
        return '重型货车';
      case 'CONTAINER':
        return '集装箱车';
      case 'TANKER':
        return '罐车';
      default:
        return vehicleType;
    }
  }

  String get plateColorText {
    switch (plateColor) {
      case 'BLUE':
        return '蓝牌';
      case 'YELLOW':
        return '黄牌';
      case 'GREEN':
        return '绿牌';
      case 'WHITE':
        return '白牌';
      default:
        return plateColor;
    }
  }
}
