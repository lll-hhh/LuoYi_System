import 'package:flutter/material.dart';
import 'package:luoyi_mobile/models/vehicle.dart';
import 'package:luoyi_mobile/services/vehicle_service.dart';

class VehicleProvider with ChangeNotifier {
  List<Vehicle> _vehicles = [];
  Vehicle? _currentVehicle;
  bool _isLoading = false;
  String? _error;

  List<Vehicle> get vehicles => _vehicles;
  Vehicle? get currentVehicle => _currentVehicle;
  bool get isLoading => _isLoading;
  String? get error => _error;

  final VehicleService _vehicleService = VehicleService();

  Future<void> loadVehicles() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _vehicles = await _vehicleService.getVehicles();
    } catch (e) {
      _error = e.toString();
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> loadVehicleDetail(int vehicleId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentVehicle = await _vehicleService.getVehicleDetail(vehicleId);
    } catch (e) {
      _error = e.toString();
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<bool> addVehicle(Vehicle vehicle) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final newVehicle = await _vehicleService.addVehicle(vehicle);
      _vehicles.add(newVehicle);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateVehicle(Vehicle vehicle) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final updated = await _vehicleService.updateVehicle(vehicle);
      final index = _vehicles.indexWhere((v) => v.vehicleId == vehicle.vehicleId);
      if (index != -1) {
        _vehicles[index] = updated;
      }
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteVehicle(int vehicleId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _vehicleService.deleteVehicle(vehicleId);
      _vehicles.removeWhere((v) => v.vehicleId == vehicleId);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
