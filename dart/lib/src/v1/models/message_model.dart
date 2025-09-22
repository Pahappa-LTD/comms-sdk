import 'message_priority.dart';

class MessageModel {
  String number;
  String message;
  String senderId;
  MessagePriority priority;

  MessageModel({
    required this.number,
    required this.message,
    this.senderId = "EgoSms",
    this.priority = MessagePriority.HIGHEST,
  });

  Map<String, dynamic> toJson() => {
    'number': number,
    'message': message,
    'senderid': senderId,
    'priority': priority.index.toString(),
  };
}
