import 'message_model.dart';
import 'user_data.dart';

class ApiRequest {
  String method;
  UserData userdata;
  List<MessageModel>? messageData;

  ApiRequest({required this.method, required this.userdata, this.messageData});

  Map<String, dynamic> toJson() => {
    'method': method,
    'userdata': userdata.toJson(),
    'msgdata': messageData?.map((e) => e.toJson()).toList(),
  };
}
