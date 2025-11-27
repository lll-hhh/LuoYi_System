import 'package:flutter/material.dart';
import 'package:luoyi_mobile/models/cargo_declaration.dart';
import 'package:luoyi_mobile/services/cargo_service.dart';

class CargoProvider with ChangeNotifier {
  List<CargoDeclaration> _declarations = [];
  CargoDeclaration? _currentDeclaration;
  bool _isLoading = false;
  String? _error;

  List<CargoDeclaration> get declarations => _declarations;
  CargoDeclaration? get currentDeclaration => _currentDeclaration;
  bool get isLoading => _isLoading;
  String? get error => _error;

  final CargoService _cargoService = CargoService();

  Future<void> loadDeclarations() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _declarations = await _cargoService.getDeclarations();
    } catch (e) {
      _error = e.toString();
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> loadDeclarationDetail(int declarationId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentDeclaration = await _cargoService.getDeclarationDetail(declarationId);
    } catch (e) {
      _error = e.toString();
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<bool> submitDeclaration(CargoDeclaration declaration) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final newDeclaration = await _cargoService.submitDeclaration(declaration);
      _declarations.insert(0, newDeclaration);
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
