import 'package:flutter/material.dart';
import 'package:luoyi_mobile/models/message.dart';
import 'package:luoyi_mobile/services/message_service.dart';

class MessageProvider with ChangeNotifier {
  List<Message> _messages = [];
  int _unreadCount = 0;
  bool _isLoading = false;
  String? _error;

  List<Message> get messages => _messages;
  int get unreadCount => _unreadCount;
  bool get isLoading => _isLoading;
  String? get error => _error;

  final MessageService _messageService = MessageService();

  Future<void> loadMessages() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _messages = await _messageService.getMessages();
      _unreadCount = _messages.where((m) => !m.isRead).length;
    } catch (e) {
      _error = e.toString();
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> markAsRead(int messageId) async {
    try {
      await _messageService.markAsRead(messageId);
      final index = _messages.indexWhere((m) => m.messageId == messageId);
      if (index != -1) {
        _messages[index] = _messages[index].copyWith(isRead: true);
        _unreadCount = _messages.where((m) => !m.isRead).length;
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> markAllAsRead() async {
    try {
      await _messageService.markAllAsRead();
      _messages = _messages.map((m) => m.copyWith(isRead: true)).toList();
      _unreadCount = 0;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
