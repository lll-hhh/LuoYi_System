import 'package:luoyi_mobile/config/api.dart';
import 'package:luoyi_mobile/models/vehicle.dart';
import 'package:luoyi_mobile/services/http_service.dart';

class VehicleService {
  final HttpService _http = HttpService();

  Future<List<Vehicle>> getVehicles() async {
    final response = await _http.get(ApiConfig.vehicles);
    final List<dynamic> data = response.data['data'] ?? [];
    return data.map((json) => Vehicle.fromJson(json)).toList();
  }

  Future<Vehicle> getVehicleDetail(int vehicleId) async {
    final response = await _http.get(
      ApiConfig.vehicleDetail.replaceFirst('{id}', vehicleId.toString()),
    );
    return Vehicle.fromJson(response.data);
  }

  Future<Vehicle> addVehicle(Vehicle vehicle) async {
    final response = await _http.post(
      ApiConfig.vehicles,
      data: vehicle.toJson(),
    );
    return Vehicle.fromJson(response.data);
  }

  Future<Vehicle> updateVehicle(Vehicle vehicle) async {
    final response = await _http.put(
      ApiConfig.vehicleDetail.replaceFirst('{id}', vehicle.vehicleId.toString()),
      data: vehicle.toJson(),
    );
    return Vehicle.fromJson(response.data);
  }

  Future<void> deleteVehicle(int vehicleId) async {
    await _http.delete(
      ApiConfig.vehicleDetail.replaceFirst('{id}', vehicleId.toString()),
    );
  }

  Future<Map<String, dynamic>> getVehicleLocation(int vehicleId) async {
    final response = await _http.get(
      ApiConfig.vehicleLocation.replaceFirst('{id}', vehicleId.toString()),
    );
    return response.data;
  }
}
