import 'dart:io';

import 'package:comms_sdk/comms_sdk.dart';
import 'package:test/test.dart';

void main() {
  test('sends a real SMS against the sandbox (skipped unless credentials are set)', () async {
    final username = Platform.environment['COMMS_SANDBOX_USERNAME'];
    final apiKey = Platform.environment['COMMS_SANDBOX_API_KEY'];

    if (username == null || username.isEmpty || apiKey == null || apiKey.isEmpty) {
      print(
        'Skipping live sandbox test: set COMMS_SANDBOX_USERNAME and '
        'COMMS_SANDBOX_API_KEY to exercise it.',
      );
      return;
    }

    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate(username, apiKey);

    final response = await sdk.querySendSMS(
      numbers: '256700000000',
      message: 'Live sandbox test from Dart SDK',
    );

    expect(response, isNotNull);
    expect(response!.status.name.toLowerCase(), 'ok');
  });
}
