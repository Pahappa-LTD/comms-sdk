import 'dart:convert';

import '../comms_sdk.dart';
import '../models/api_request.dart';
import '../models/api_response.dart';
import '../models/user_data.dart';
import 'package:http/http.dart' as http;

class Validator {
  static Future<bool> validateCredentials(CommsSDK sdk) async {
    if (sdk.userName == null || sdk.apiKey == null) {
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

  static Future<bool> _isValidCredential(CommsSDK sdk) async {
    final client = http.Client();
    final apiRequest = ApiRequest(
      method: 'Balance',
      userdata: UserData(sdk.userName!, sdk.apiKey!),
    );
    try {
      final res = await client.post(
        Uri.parse(CommsSDK.getApiUrl()),
        body: jsonEncode(apiRequest.toJson()),
        headers: {'Content-Type': 'application/json'},
      );
      final apiResponse = ApiResponse.fromJson(jsonDecode(res.body));
      if (apiResponse.status.name.toLowerCase() == "ok") {
        print("Credentials validated successfully.");
        return true;
      }
    } catch (e) {
      print("Error validating credentials: $e");
    }
    return false;
  }
}