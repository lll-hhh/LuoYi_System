import 'package:flutter_test/flutter_test.dart';
import 'package:luoyi_app/models/cargo_declaration.dart';

void main() {
  group('CargoDeclaration Model Tests', () {
    test('CargoDeclaration fromJson creates correct declaration', () {
      final json = {
        'declarationId': 1,
        'userId': 1,
        'vehicleId': 1,
        'declarationNo': 'CD20240101001',
        'cargoName': '电子产品',
        'cargoType': '普通货物',
        'weight': 500.0,
        'volume': 2.5,
        'origin': '北京',
        'destination': '上海',
        'status': 'pending',
        'createdAt': '2024-01-01T00:00:00.000Z',
      };

      final declaration = CargoDeclaration.fromJson(json);

      expect(declaration.declarationId, 1);
      expect(declaration.declarationNo, 'CD20240101001');
      expect(declaration.cargoName, '电子产品');
      expect(declaration.weight, 500.0);
      expect(declaration.status, 'pending');
    });

    test('CargoDeclaration toJson creates correct json', () {
      final declaration = CargoDeclaration(
        declarationId: 1,
        userId: 1,
        vehicleId: 1,
        declarationNo: 'CD20240101001',
        cargoName: '电子产品',
        cargoType: '普通货物',
        weight: 500.0,
        origin: '北京',
        destination: '上海',
        status: 'pending',
      );

      final json = declaration.toJson();

      expect(json['declarationId'], 1);
      expect(json['declarationNo'], 'CD20240101001');
      expect(json['cargoName'], '电子产品');
    });

    test('CargoDeclaration status check', () {
      final pendingDeclaration = CargoDeclaration(
        declarationNo: 'CD001',
        status: 'pending',
      );

      final approvedDeclaration = CargoDeclaration(
        declarationNo: 'CD002',
        status: 'approved',
      );

      expect(pendingDeclaration.status, 'pending');
      expect(approvedDeclaration.status, 'approved');
    });
  });
}
