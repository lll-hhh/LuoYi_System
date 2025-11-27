import 'package:luoyi_mobile/config/api.dart';
import 'package:luoyi_mobile/models/cargo_declaration.dart';
import 'package:luoyi_mobile/services/http_service.dart';

class CargoService {
  final HttpService _http = HttpService();

  Future<List<CargoDeclaration>> getDeclarations({String? status}) async {
    final response = await _http.get(
      ApiConfig.cargoDeclarations,
      params: status != null ? {'status': status} : null,
    );
    final List<dynamic> data = response.data['data'] ?? [];
    return data.map((json) => CargoDeclaration.fromJson(json)).toList();
  }

  Future<CargoDeclaration> getDeclarationDetail(int declarationId) async {
    final response = await _http.get(
      ApiConfig.cargoDetail.replaceFirst('{id}', declarationId.toString()),
    );
    return CargoDeclaration.fromJson(response.data);
  }

  Future<CargoDeclaration> submitDeclaration(CargoDeclaration declaration) async {
    final response = await _http.post(
      ApiConfig.cargoDeclarations,
      data: declaration.toJson(),
    );
    return CargoDeclaration.fromJson(response.data);
  }

  Future<void> cancelDeclaration(int declarationId) async {
    await _http.delete(
      ApiConfig.cargoDetail.replaceFirst('{id}', declarationId.toString()),
    );
  }
}
