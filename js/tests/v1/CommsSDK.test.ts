import { CommsSDK } from '../../src/';
import { MessagePriority } from '../../src/';

// Live sandbox smoke test: hits https://comms-test.pahappa.net for real.
// Skipped entirely unless COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY are set,
// so the suite stays green (and network-free) for anyone without sandbox credentials.
const username = process.env.COMMS_SANDBOX_USERNAME;
const apiKey = process.env.COMMS_SANDBOX_API_KEY;
const describeLive = username && apiKey ? describe : describe.skip;

describeLive('CommsSDK (live sandbox)', () => {
    let sdk: CommsSDK;

    beforeEach(() => {
        CommsSDK.useSandBox();
        sdk = CommsSDK.authenticate(username as string, apiKey as string);
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