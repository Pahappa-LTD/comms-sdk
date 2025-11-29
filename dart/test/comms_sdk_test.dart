import 'package:comms_sdk/comms_sdk.dart';
import 'package:test/test.dart';

void main() {
  test('checkFunctionality', () async {
    CommsSDK.useSandBox();
    var username = 'sandbox';
    var apikey = 'sandbox35';
    final sdk = await CommsSDK.authenticate(username, apikey);
    final balance1 = await sdk.getBalance() ?? 0;
    print('Balance1: SHS.$balance1');
    await sdk.sendSMS(numbers: ['234'], message: 'testing');

    final numbers = ['256789123456', '+256789123457', '256789123458'];
    await sdk.sendSMS(
      numbers: numbers,
      message: 'Sample SMS Message',
      senderId: 'CustomSenderID',
      priority: MessagePriority.HIGHEST,
    );
    final balance2 = await sdk.getBalance() ?? 0;
    print('Balance2: SHS.$balance2');
    expect(balance1 > balance2, isTrue);
  });
}
