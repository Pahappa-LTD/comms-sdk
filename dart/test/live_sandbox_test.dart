import 'dart:io';

import 'package:comms_sdk/comms_sdk.dart';
import 'package:test/test.dart';

/// Returns null (and prints why) if COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY
/// aren't set, so gated tests below can skip cleanly instead of failing.
(String, String)? _sandboxCredentials() {
  final username = Platform.environment['COMMS_SANDBOX_USERNAME'];
  final apiKey = Platform.environment['COMMS_SANDBOX_API_KEY'];

  if (username == null || username.isEmpty || apiKey == null || apiKey.isEmpty) {
    print(
      'Skipping live sandbox test: set COMMS_SANDBOX_USERNAME and '
      'COMMS_SANDBOX_API_KEY to exercise it.',
    );
    return null;
  }
  return (username, apiKey);
}

void main() {
  test('rejects wrong credentials against the sandbox', () async {
    // Always runs (no env vars needed) - an obviously-fake credential never
    // reaches the live production API, only the sandbox, and costs nothing.
    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate(
      'invalid-user',
      'invalid-key-00000000000000000000000000000000',
    );

    final response = await sdk.querySendSMS(
      numbers: '256700000000',
      message: 'This should never be sent',
    );

    expect(response, isNull);
  });

  test('sends a real SMS against the sandbox (skipped unless credentials are set)', () async {
    final creds = _sandboxCredentials();
    if (creds == null) return;
    final (username, apiKey) = creds;

    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate(username, apiKey);

    final response = await sdk.querySendSMS(
      numbers: '256700000000',
      message: 'Live sandbox test from Dart SDK',
    );

    expect(response, isNotNull);
    expect(response!.status.name.toLowerCase(), 'ok');
  });

  test('sends to multiple numbers against the sandbox (skipped unless credentials are set)', () async {
    final creds = _sandboxCredentials();
    if (creds == null) return;
    final (username, apiKey) = creds;

    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate(username, apiKey);

    final response = await sdk.querySendSMS(
      numbers: ['256700000000', '256700000001', '256700000002'],
      message: 'Live sandbox multi-number test from Dart SDK',
    );

    expect(response, isNotNull);
    expect(response!.status.name.toLowerCase(), 'ok');
  });

  test('rejects more than 1000 numbers (skipped unless credentials are set)', () async {
    final creds = _sandboxCredentials();
    if (creds == null) return;
    final (username, apiKey) = creds;

    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate(username, apiKey);

    final tooMany = List<String>.generate(
      1001,
      (i) => '256700${i.toString().padLeft(6, '0')}',
    );

    final response = await sdk.querySendSMS(
      numbers: tooMany,
      message: 'This batch is too large and should be rejected',
    );

    // The real API rejects requests over 1000 numbers server-side - this
    // just proves the SDK surfaces that as a clean failure, not a crash.
    expect(response, isNotNull);
    expect(response!.status.name.toLowerCase(), isNot('ok'));
  });

  test('queries balance against the sandbox (skipped unless credentials are set)', () async {
    final creds = _sandboxCredentials();
    if (creds == null) return;
    final (username, apiKey) = creds;

    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate(username, apiKey);

    final balanceResponse = await sdk.queryBalance();
    expect(balanceResponse, isNotNull);
    expect(balanceResponse!.status.name.toLowerCase(), 'ok');

    final balance = await sdk.getBalance();
    expect(balance, isNotNull);
    expect(balance! >= 0, isTrue);
  });
}
