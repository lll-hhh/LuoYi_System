import 'package:flutter_test/flutter_test.dart';
import 'package:luoyi_app/models/vehicle.dart';

void main() {
  group('Vehicle Model Tests', () {
    test('Vehicle fromJson creates correct vehicle', () {
      final json = {
        'vehicleId': 1,
        'userId': 1,
        'plateNumber': '京A12345',
        'vehicleType': '货车',
        'brand': '东风',
        'model': 'EQ2102',
        'color': '蓝色',
        'loadCapacity': 10.5,
        'isDefault': true,
        'status': 'normal',
        'createdAt': '2024-01-01T00:00:00.000Z',
      };

      final vehicle = Vehicle.fromJson(json);

      expect(vehicle.vehicleId, 1);
      expect(vehicle.plateNumber, '京A12345');
      expect(vehicle.vehicleType, '货车');
      expect(vehicle.brand, '东风');
      expect(vehicle.isDefault, true);
    });

    test('Vehicle toJson creates correct json', () {
      final vehicle = Vehicle(
        vehicleId: 1,
        userId: 1,
        plateNumber: '京A12345',
        vehicleType: '货车',
        brand: '东风',
        isDefault: true,
        status: 'normal',
      );

      final json = vehicle.toJson();

      expect(json['vehicleId'], 1);
      expect(json['plateNumber'], '京A12345');
      expect(json['vehicleType'], '货车');
      expect(json['isDefault'], true);
    });

    test('Vehicle default values', () {
      final vehicle = Vehicle(
        plateNumber: '京B67890',
        vehicleType: '小型车',
      );

      expect(vehicle.vehicleId, isNull);
      expect(vehicle.isDefault, isNull);
    });
  });
}
