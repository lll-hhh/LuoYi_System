import 'package:json_annotation/json_annotation.dart';

part 'cargo_declaration.g.dart';

@JsonSerializable()
class CargoDeclaration {
  final int? declarationId;
  final String declarationNo;
  final int vehicleId;
  final String? plateNumber;
  final String cargoName;
  final String cargoType; // NORMAL, COLD_CHAIN, HAZARDOUS, OVERSIZE
  final double weight;
  final double? volume;
  final String? unit;
  final int quantity;
  final String origin;
  final String destination;
  final String? routeDescription;
  final DateTime expectedDepartureTime;
  final DateTime? expectedArrivalTime;
  final String? driverName;
  final String? driverPhone;
  final String? consigneeName;
  final String? consigneePhone;
  final String? remark;
  final String status; // PENDING, APPROVED, REJECTED, IN_TRANSIT, COMPLETED
  final DateTime? createdAt;

  CargoDeclaration({
    this.declarationId,
    required this.declarationNo,
    required this.vehicleId,
    this.plateNumber,
    required this.cargoName,
    required this.cargoType,
    required this.weight,
    this.volume,
    this.unit,
    required this.quantity,
    required this.origin,
    required this.destination,
    this.routeDescription,
    required this.expectedDepartureTime,
    this.expectedArrivalTime,
    this.driverName,
    this.driverPhone,
    this.consigneeName,
    this.consigneePhone,
    this.remark,
    required this.status,
    this.createdAt,
  });

  factory CargoDeclaration.fromJson(Map<String, dynamic> json) => 
      _$CargoDeclarationFromJson(json);
  Map<String, dynamic> toJson() => _$CargoDeclarationToJson(this);

  String get cargoTypeText {
    switch (cargoType) {
      case 'NORMAL':
        return '普通货物';
      case 'COLD_CHAIN':
        return '冷链货物';
      case 'HAZARDOUS':
        return '危险品';
      case 'OVERSIZE':
        return '超限货物';
      default:
        return cargoType;
    }
  }

  String get statusText {
    switch (status) {
      case 'PENDING':
        return '待审核';
      case 'APPROVED':
        return '已通过';
      case 'REJECTED':
        return '已驳回';
      case 'IN_TRANSIT':
        return '运输中';
      case 'COMPLETED':
        return '已完成';
      default:
        return status;
    }
  }
}
