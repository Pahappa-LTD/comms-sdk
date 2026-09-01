import 'dart:convert';

import 'package:comms_sdk/comms_sdk.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:test/test.dart';

/// Builds a [MockClient] that answers Balance requests with [balanceStatus]
/// and SendSms requests with [sendSmsStatus], capturing every decoded
/// request body it sees via [onRequest].
http.Client _mockClient({
  String balanceStatus = 'OK',
  String sendSmsStatus = 'OK',
  void Function(Map<String, dynamic> body)? onRequest,
}) {
  return MockClient((request) async {
    final body = jsonDecode(request.body) as Map<String, dynamic>;
    onRequest?.call(body);
    if (body['method'] == 'Balance') {
      return http.Response(
        jsonEncode({
          'Status': balanceStatus,
          'Message': balanceStatus == 'OK' ? 'Success' : 'Invalid credentials',
          'Balance': '500.0',
        }),
        200,
      );
    }
    return http.Response(
      jsonEncode({
        'Status': sendSmsStatus,
        'Message': sendSmsStatus == 'OK' ? 'SMS sent successfully' : 'Send failed',
        'MsgFollowUpUniqueCode': 'ABC123',
        'Cost': 0.09,
      }),
      200,
    );
  });
}

void main() {
  group('golden path: sending an SMS (mocked, no network)', () {
    test('always sends an explicit walletType of Local, never null/omitted', () async {
      Map<String, dynamic>? sendSmsBody;
      final client = _mockClient(
        onRequest: (body) {
          if (body['method'] == 'SendSms') sendSmsBody = body;
        },
      );
      final sdk = await CommsSDK.authenticate('user', 'key', client: client);

      final ok = await sdk.sendSMS(numbers: '256700000000', message: 'hello there');

      expect(ok, isTrue);
      expect(sendSmsBody, isNotNull);
      expect(sendSmsBody!['walletType'], 'Local');
    });

    test('defaults senderId and priority (HIGH) when unspecified', () async {
      Map<String, dynamic>? sendSmsBody;
      final client = _mockClient(
        onRequest: (body) {
          if (body['method'] == 'SendSms') sendSmsBody = body;
        },
      );
      final sdk = await CommsSDK.authenticate('user', 'key', client: client);

      await sdk.sendSMS(numbers: '256700000000', message: 'hello there');

      final msg = (sendSmsBody!['msgdata'] as List).single as Map<String, dynamic>;
      expect(msg['senderid'], 'EgoSMS');
      expect(msg['priority'], MessagePriority.HIGH.index.toString());
    });

    test('reports success when the API returns Status OK', () async {
      final sdk = await CommsSDK.authenticate('user', 'key', client: _mockClient());
      final ok = await sdk.sendSMS(numbers: '256700000000', message: 'hello there');
      expect(ok, isTrue);
    });

    test('reports failure when the API returns Status Failed', () async {
      final client = _mockClient(sendSmsStatus: 'Failed');
      final sdk = await CommsSDK.authenticate('user', 'key', client: client);
      final ok = await sdk.sendSMS(numbers: '256700000000', message: 'hello there');
      expect(ok, isFalse);
    });

    test('querySendSMS returns the full parsed ApiResponse', () async {
      final sdk = await CommsSDK.authenticate('user', 'key', client: _mockClient());
      final response = await sdk.querySendSMS(numbers: '256700000000', message: 'hello there');
      expect(response, isNotNull);
      expect(response!.status.name.toLowerCase(), 'ok');
      expect(response.messageFollowUpCode, 'ABC123');
      expect(response.cost, 0.09);
    });
  });

  group('credential validation (mocked, no network)', () {
    test('always sends an explicit walletType of Local on the Balance check', () async {
      Map<String, dynamic>? balanceBody;
      final client = _mockClient(
        onRequest: (body) {
          if (body['method'] == 'Balance') balanceBody = body;
        },
      );

      await CommsSDK.authenticate('user', 'key', client: client);

      expect(balanceBody, isNotNull);
      expect(balanceBody!['walletType'], 'Local');
    });
  });

  group('queryBalance/getBalance walletType parameter', () {
    test('defaults to Local when called with no argument', () async {
      Map<String, dynamic>? lastBody;
      final client = _mockClient(onRequest: (body) => lastBody = body);
      final sdk = await CommsSDK.authenticate('user', 'key', client: client);

      final response = await sdk.queryBalance();

      expect(response, isNotNull);
      expect(lastBody!['walletType'], 'Local');
    });

    test('respects an explicit WalletType.international argument', () async {
      Map<String, dynamic>? lastBody;
      final client = _mockClient(onRequest: (body) => lastBody = body);
      final sdk = await CommsSDK.authenticate('user', 'key', client: client);

      await sdk.queryBalance(walletType: WalletType.international);

      expect(lastBody!['walletType'], 'International');
    });
  });
}
