import { EgoSmsSDK } from '../../src/v1/EgoSmsSDK';
import { MessagePriority } from '../../src/v1/models/MessagePriority';

describe('EgoSmsSDK', () => {
    let sdk: EgoSmsSDK;

    beforeEach(() => {
        EgoSmsSDK.useSandBox();
        sdk = EgoSmsSDK.authenticate('aganisandbox', 'SandBox');
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
        const sdk = EgoSmsSDK.authenticate('invalid_user', 'invalid_password');
        expect(await sdk.sendSMS('+256772123456', 'Test message')).toBe(false);
    });

    test('testCheckBalanceAfterSendingSMS', async () => {
        const balanceBefore = await sdk.getBalance();
        await sdk.sendSMS('+256772123456', 'Test message');
        const balanceAfter = await sdk.getBalance();
        expect(Number(balanceAfter)).toBeLessThan(Number(balanceBefore));
    });
});