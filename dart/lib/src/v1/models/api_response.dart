import 'api_response_code.dart';

class ApiResponse {
  ApiResponseCode status;
  String? message;
  String? cost;
  String? messageFollowUpCode;
  String? balance;

  ApiResponse.fromJson(Map<String, dynamic> json)
    : status = json['Status']!.toString().toLowerCase() == "ok"
          ? ApiResponseCode.OK
          : ApiResponseCode.Failed,
      message = json['Message'],
      cost = json['Cost'].toString(),
      messageFollowUpCode = json['MsgFollowUpUniqueCode'],
      balance = json['Balance'].toString();
}
