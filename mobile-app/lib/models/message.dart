import 'package:json_annotation/json_annotation.dart';

part 'message.g.dart';

@JsonSerializable()
class Message {
  final int messageId;
  final String title;
  final String content;
  final String messageType; // SYSTEM, APPROVAL, ALERT, NOTICE
  final bool isRead;
  final DateTime createdAt;
  final Map<String, dynamic>? extra;

  Message({
    required this.messageId,
    required this.title,
    required this.content,
    required this.messageType,
    required this.isRead,
    required this.createdAt,
    this.extra,
  });

  factory Message.fromJson(Map<String, dynamic> json) => _$MessageFromJson(json);
  Map<String, dynamic> toJson() => _$MessageToJson(this);

  Message copyWith({
    int? messageId,
    String? title,
    String? content,
    String? messageType,
    bool? isRead,
    DateTime? createdAt,
    Map<String, dynamic>? extra,
  }) {
    return Message(
      messageId: messageId ?? this.messageId,
      title: title ?? this.title,
      content: content ?? this.content,
      messageType: messageType ?? this.messageType,
      isRead: isRead ?? this.isRead,
      createdAt: createdAt ?? this.createdAt,
      extra: extra ?? this.extra,
    );
  }

  String get messageTypeText {
    switch (messageType) {
      case 'SYSTEM':
        return '系统消息';
      case 'APPROVAL':
        return '审批通知';
      case 'ALERT':
        return '预警提醒';
      case 'NOTICE':
        return '公告通知';
      default:
        return messageType;
    }
  }
}
