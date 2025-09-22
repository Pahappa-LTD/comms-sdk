import 'dart:convert';

import 'package:http/http.dart' as http;

import 'models/api_request.dart';
import 'models/api_response.dart';
import 'models/message_model.dart';
import 'models/message_priority.dart';
import 'models/user_data.dart';
import 'utils/number_validator.dart';
import 'utils/validator.dart';

class EgoSmsSDK {
  static String _apiUrl = 'https://www.egosms.co/api/v1/json/';
  static String getApiUrl() => _apiUrl;

  String? username;
  String? password;

  String senderId = 'EgoSms';
  bool isAuthenticated = false;
  final http.Client _client = http.Client();

  EgoSmsSDK._();

  static Future<EgoSmsSDK> authenticate(
    String username,
    String password,
  ) async {
    final sdk = EgoSmsSDK._();
    sdk.username = username;
    sdk.password = password;
    await Validator.validateCredentials(sdk);
    return sdk;
  }

  static void useSandBox() {
    _apiUrl = 'http://sandbox.egosms.co/api/v1/json/';
  }

  static void useLiveServer() {
    _apiUrl = 'https://www.egosms.co/api/v1/json/';
  }

  EgoSmsSDK withSenderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  Future<bool> sendSMS({
    required List<String> numbers,
    required String message,
    String? senderId,
    MessagePriority? priority,
  }) async {
    if (await _sdkNotAuthenticated()) return false;
    if (numbers.isEmpty) {
      throw ArgumentError('Numbers list cannot be null or empty');
    }
    if (message.isEmpty) {
      throw ArgumentError('Message cannot be null or empty');
    }
    if (message.length == 1) {
      throw ArgumentError('Message cannot be a single character');
    }
    if (senderId == null || senderId.trim().isEmpty) {
      senderId = this.senderId;
    }
    if (senderId.length > 11) {
      print('Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.');
    }
    priority ??= MessagePriority.HIGHEST;

    numbers = NumberValidator.validateNumbers(numbers);
    if (numbers.isEmpty) {
      print('No valid phone numbers provided. Please check inputs.');
      return false;
    }

    final messageModels = numbers
        .map(
          (number) => MessageModel(
            number: number,
            message: message,
            senderId: senderId!,
            priority: priority!,
          ),
        )
        .toList();

    final apiRequest = ApiRequest(
      method: 'SendSms',
      messageData: messageModels,
      userdata: UserData(username!, password!),
    );

    final res = await _client.post(
      Uri.parse(_apiUrl),
      body: jsonEncode(apiRequest.toJson()),
      headers: {'Content-Type': 'application/json'},
    );

    try {
      final apiResponse = ApiResponse.fromJson(jsonDecode(res.body));
      if (apiResponse.status.name.toLowerCase() == "ok") {
        print('SMS sent successfully.');
        print('MessageFollowUpUniqueCode: ${apiResponse.messageFollowUpCode}');
        return true;
      } else {
        throw Exception(apiResponse.message);
      }
    } catch (e) {
      print('Failed to send SMS: $e');
      print('Request: ${jsonEncode(apiRequest.toJson())}');
      return false;
    }
  }

  Future<bool> _sdkNotAuthenticated() async {
    if (!isAuthenticated) {
      print(
        'SDK is not authenticated. Please authenticate before performing actions.',
      );
      print('Attempting to re-authenticate with provided credentials...');
      return !await Validator.validateCredentials(this);
    }
    return false;
  }

  Future<String?> getBalance() async {
    if (await _sdkNotAuthenticated()) {
      return null;
    }
    final apiRequest = ApiRequest(
      method: 'Balance',
      userdata: UserData(username!, password!),
      messageData: [],
    );
    try {
      final res = await _client.post(
        Uri.parse(_apiUrl),
        body: jsonEncode(apiRequest.toJson()),
        headers: {'Content-Type': 'application/json'},
      );
      final response = ApiResponse.fromJson(jsonDecode(res.body));
      print('MessageFollowUpUniqueCode: ${response.messageFollowUpCode}');
      return response.balance;
    } catch (e) {
      throw Exception('Failed to get balance: $e');
    }
  }
}
