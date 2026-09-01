import 'message_model.dart';
import 'user_data.dart';
import 'wallet_type.dart';

class ApiRequest {
  String method;
  UserData userdata;
  List<MessageModel>? messageData;
  WalletType? walletType;

  ApiRequest({
    required this.method,
    required this.userdata,
    this.messageData,
    this.walletType,
  });

  Map<String, dynamic> toJson() => {
    'method': method,
    'userdata': userdata.toJson(),
    'msgdata': messageData?.map((e) => e.toJson()).toList(),
    'walletType': walletType?.value,
  };
}
