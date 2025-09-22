import 'dart:convert';

import '../egosms_sdk.dart';
import '../models/api_request.dart';
import '../models/api_response.dart';
import '../models/user_data.dart';
import 'package:http/http.dart' as http;

class Validator {
  static Future<bool> validateCredentials(EgoSmsSDK sdk) async {
    if (sdk.username == null || sdk.password == null) {
      throw ArgumentError("Username and Password must be provided");
    }

    if (!await _isValidCredential(sdk)) {
      print("""
                                                      _                    
  /\\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | 
 /--\\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o 

""");
      print("\n");
      return false;
    }
    print("Validated using basic auth");
    sdk.isAuthenticated = true;
    return true;
  }

  static Future<bool> _isValidCredential(EgoSmsSDK sdk) async {
    final client = http.Client();
    final apiRequest = ApiRequest(
      method: 'Balance',
      userdata: UserData(sdk.username!, sdk.password!),
    );
    try {
      final res = await client.post(
        Uri.parse(EgoSmsSDK.getApiUrl()),
        body: jsonEncode(apiRequest.toJson()),
        headers: {'Content-Type': 'application/json'},
      );
      final apiResponse = ApiResponse.fromJson(jsonDecode(res.body));
      if (apiResponse.status.name.toLowerCase() == "ok") {
        print("Credentials validated successfully.");
        return true;
      } else {
        throw Exception(apiResponse.message);
      }
    } catch (e) {
      print("Error validating credentials: $e");
      return false;
    }
  }
}
