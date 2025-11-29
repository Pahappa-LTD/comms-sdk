import { CommsSDK } from '../../src/v1/CommsSDK';
import { MessagePriority } from '../../src/v1/models/MessagePriority';

describe('CommsSDK', () => {
    let sdk: CommsSDK;
    const username = "sandbox" // replace with your sandbox credentials
    const apiKey = "sandbox35" // replace with your sandbox credentials

    beforeEach(() => {
        CommsSDK.useSandBox();
        sdk = CommsSDK.authenticate(username, apiKey);
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