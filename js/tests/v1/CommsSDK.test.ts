import { CommsSDK } from '../../src/v1/CommsSDK';
import { MessagePriority } from '../../src/v1/models/MessagePriority';

describe('CommsSDK', () => {
    let sdk: CommsSDK;

    beforeEach(() => {
        CommsSDK.useSandBox();
        sdk = CommsSDK.authenticate('agabu-idaniel', 'dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99');
    });

    test('testSendSMSToSingleNumber', async () => {
        expect(await sdk.sendSMS('+256772123456', 'Test message')).toBe(true);
    });

    test('testSendSMSToMultipleNumbers', async () => {
        const numbers = ['+256772123456', '0772123457'];
        expect(await sdk.sendSMS(numbers, 'Test message')).toBe(true);
    });

    test('testSendSMSWithShortNumberLength', async () => {
        expect(await sdk.sendSMS('123', 'Test message')).toBe(false);
    });

    test('testSendSMSWithCustomMessagePriority', async () => {
        expect(await sdk.sendSMS('+256772123456', 'Test message', undefined, MessagePriority.LOW)).toBe(true);
    });

    test('testSendSMSWithInvalidCredentials', async () => {
        const sdk = CommsSDK.authenticate('invalid_user', 'invalid_password');
        expect(await sdk.sendSMS('+256772123456', 'Test message')).toBe(false);
    });

    test('testCheckBalanceAfterSendingSMS', async () => {
        const balanceBefore = await sdk.getBalance();
        await sdk.sendSMS('+256772123456', 'Test message');
        const balanceAfter = await sdk.getBalance();
        expect(Number(balanceAfter)).toBeLessThan(Number(balanceBefore));
    });
});