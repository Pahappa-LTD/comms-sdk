import 'api_response_code.dart';

class ApiResponse {
  ApiResponseCode status;
  String? message;
  int? cost;
  String? currency;
  String? messageFollowUpCode;
  String? balance;

  ApiResponse.fromJson(Map<String, dynamic> json)
    : status = json['Status']!.toString().toLowerCase() == "ok"
          ? ApiResponseCode.OK
          : ApiResponseCode.Failed,
      message = json['Message'],
      cost = json['Cost'] != null ? int.tryParse(json['Cost'].toString()) : null,
      currency = json['Currency'],
      messageFollowUpCode = json['MsgFollowUpUniqueCode'],
      balance = json['Balance']?.toString();
}
