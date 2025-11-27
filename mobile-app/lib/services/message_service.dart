import 'package:luoyi_mobile/config/api.dart';
import 'package:luoyi_mobile/models/message.dart';
import 'package:luoyi_mobile/services/http_service.dart';

class MessageService {
  final HttpService _http = HttpService();

  Future<List<Message>> getMessages({bool? unreadOnly}) async {
    final response = await _http.get(
      ApiConfig.messages,
      params: unreadOnly == true ? {'unreadOnly': true} : null,
    );
    final List<dynamic> data = response.data['data'] ?? [];
    return data.map((json) => Message.fromJson(json)).toList();
  }

  Future<void> markAsRead(int messageId) async {
    await _http.post(
      ApiConfig.messageRead.replaceFirst('{id}', messageId.toString()),
    );
  }

  Future<void> markAllAsRead() async {
    await _http.post('${ApiConfig.messages}/read-all');
  }

  Future<void> deleteMessage(int messageId) async {
    await _http.delete('${ApiConfig.messages}/$messageId');
  }
}
