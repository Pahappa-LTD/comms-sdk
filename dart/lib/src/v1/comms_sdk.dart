import 'dart:convert';

import 'package:http/http.dart' as http;

import 'models/api_request.dart';
import 'models/api_response.dart';
import 'models/message_model.dart';
import 'models/message_priority.dart';
import 'models/user_data.dart';
import 'utils/number_validator.dart';
import 'utils/validator.dart';

class CommsSDK {
  static String _apiUrl = 'https://comms.egosms.co/api/v1/json/';
  static String getApiUrl() => _apiUrl;

  String? userName;
  String? apiKey;

  String senderId = 'EgoSMS';
  bool isAuthenticated = false;
  final http.Client _client = http.Client();

  CommsSDK._();

  static Future<CommsSDK> authenticate(
    String userName,
    String apiKey,
  ) async {
    final sdk = CommsSDK._();
    sdk.userName = userName;
    sdk.apiKey = apiKey;
    await Validator.validateCredentials(sdk);
    return sdk;
  }

  static void useSandBox() {
    _apiUrl = 'https://comms-test.pahappa.net/api/v1/json';
  }

  static void useLiveServer() {
    _apiUrl = 'https://comms.egosms.co/api/v1/json';
  }

  void setAuthenticated() {
    isAuthenticated = true;
  }

  CommsSDK withSenderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  Future<bool> sendSMS({
    required List<String> numbers,
    required String message,
    String? senderId,
    MessagePriority? priority,
  }) async {
    final apiResponse = await querySendSMS(
      numbers: numbers,
      message: message,
      senderId: senderId ?? this.senderId,
      priority: priority ?? MessagePriority.HIGHEST,
    );

    if (apiResponse == null) {
      print('Failed to get a response from the server.');
      return false;
    }

    if (apiResponse.status.name.toLowerCase() == "ok") {
      print('SMS sent successfully.');
      print('MessageFollowUpUniqueCode: ${apiResponse.messageFollowUpCode}');
      return true;
    } else if (apiResponse.status.name.toLowerCase() == "failed") {
      print('Failed: ${apiResponse.message}');
      return false;
    } else {
      throw Exception('Unexpected response status: ${apiResponse.status}');
    }
  }

  /// Same as [sendSMS] but returns the full [ApiResponse] object.
  Future<ApiResponse?> querySendSMS({
    required List<String> numbers,
    required String message,
    required String senderId,
    required MessagePriority priority,
  }) async {
    if (await _sdkNotAuthenticated()) return null;
    if (numbers.isEmpty) {
      throw ArgumentError('Numbers list cannot be empty');
    }
    if (message.isEmpty) {
      throw ArgumentError('Message cannot be empty');
    }
    if (message.length == 1) {
      throw ArgumentError('Message cannot be a single character');
    }
    if (senderId.trim().isEmpty) {
      senderId = this.senderId;
    }
    if (senderId.length > 11) {
      print('Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.');
    }

    numbers = NumberValidator.validateNumbers(numbers);
    if (numbers.isEmpty) {
      print('No valid phone numbers provided. Please check inputs.');
      return null;
    }

    final messageModels = numbers
        .map(
          (number) => MessageModel(
            number: number,
            message: message,
            senderId: senderId,
            priority: priority,
          ),
        )
        .toList();

    final apiRequest = ApiRequest(
      method: 'SendSms',
      messageData: messageModels,
      userdata: UserData(userName!, apiKey!),
    );

    try {
      final res = await _client.post(
        Uri.parse(_apiUrl),
        body: jsonEncode(apiRequest.toJson()),
        headers: {'Content-Type': 'application/json'},
      );
      return ApiResponse.fromJson(jsonDecode(res.body));
    } catch (e) {
      print('Failed to send SMS: $e');
      print('Request: ${jsonEncode(apiRequest.toJson())}');
      return null;
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

  /// Same as [getBalance] but returns the full [ApiResponse] object.
  Future<ApiResponse?> queryBalance() async {
    if (await _sdkNotAuthenticated()) {
      return null;
    }
    final apiRequest = ApiRequest(
      method: 'Balance',
      userdata: UserData(userName!, apiKey!),
      messageData: [],
    );
    try {
      final res = await _client.post(
        Uri.parse(_apiUrl),
        body: jsonEncode(apiRequest.toJson()),
        headers: {'Content-Type': 'application/json'},
      );
      return ApiResponse.fromJson(jsonDecode(res.body));
    } catch (e) {
      throw Exception('Failed to get balance: $e');
    }
  }

  Future<double?> getBalance() async {
    final response = await queryBalance();
    return response?.balance != null ? double.tryParse(response!.balance!) : null;
  }

  @override
  String toString() {
    return 'SDK($userName => $apiKey)';
  }
}
