import '../lib/egosms_sdk.dart';
import 'package:test/test.dart';

void main() {
  test('checkFunctionality', () async {
    EgoSmsSDK.useSandBox();
    final sdk = await EgoSmsSDK.authenticate('aganisandbox', 'SandBox');
    final balance1 = int.parse(await sdk.getBalance() ?? '0');
    print('Balance1: SHS.$balance1');
    await sdk.sendSMS(numbers: ['234'], message: 'testing');

    final numbers = ['256789123456', '+256789123457', '256789123458'];
    await sdk.sendSMS(
        numbers: numbers,
        message: 'Sample SMS Message',
        senderId: 'CustomSenderID',
        priority: MessagePriority.HIGHEST);
    final balance2 = int.parse(await sdk.getBalance() ?? '0');
    print('Balance2: SHS.$balance2');
    expect(balance1 > balance2, isTrue);
  });
}
