import 'package:comms_sdk/comms_sdk.dart';
import 'package:test/test.dart';

void main() {
  test('checkFunctionality', () async {
    CommsSDK.useSandBox();
    final sdk = await CommsSDK.authenticate('agabu-idaniel', 'dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99');
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
