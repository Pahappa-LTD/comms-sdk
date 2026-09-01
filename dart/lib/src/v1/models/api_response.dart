import 'api_response_code.dart';

class ApiResponse {
  ApiResponseCode status;
  String? message;
  double? cost;
  String? messageFollowUpCode;
  String? balance;

  ApiResponse.fromJson(Map<String, dynamic> json)
    : status = json['Status']!.toString().toLowerCase() == "ok"
          ? ApiResponseCode.OK
          : ApiResponseCode.Failed,
      message = json['Message'],
      cost = json['Cost'] != null ? double.tryParse(json['Cost'].toString()) : null,
      messageFollowUpCode = json['MsgFollowUpUniqueCode'],
      balance = json['Balance']?.toString();
}
