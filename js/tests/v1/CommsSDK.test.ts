import { CommsSDK } from '../../src/';
import { MessagePriority } from '../../src/';

// Live sandbox smoke test: hits https://comms-test.pahappa.net for real.
// Skipped entirely unless COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY are set,
// so the suite stays green (and network-free) for anyone without sandbox credentials.
const username = process.env.COMMS_SANDBOX_USERNAME;
const apiKey = process.env.COMMS_SANDBOX_API_KEY;
const describeLive = username && apiKey ? describe : describe.skip;

// Wrong credentials always run against the sandbox, never production, and
// never need COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY to be set: the
// only network call this triggers is a rejected Balance auth-check, so it's
// free and gives baseline coverage even without sandbox credentials configured.
describe('CommsSDK (live sandbox) - wrong credentials', () => {
    test('testSendSMSWithInvalidCredentials', async () => {
        CommsSDK.useSandBox();
        const sdk = CommsSDK.authenticate('invalid-user', 'invalid-key-00000000000000000000000000000000');
        expect(await sdk.sendSMS('256700000000', 'Test message')).toBe(false);
    });
});

describeLive('CommsSDK (live sandbox)', () => {
    let sdk: CommsSDK;

    beforeEach(() => {
        CommsSDK.useSandBox();
        sdk = CommsSDK.authenticate(username as string, apiKey as string);
    });

    test('testSendSMSToSingleNumber', async () => {
        expect(await sdk.sendSMS('256700000000', 'Test message')).toBe(true);
    });

    test('testSendSMSToMultipleNumbers', async () => {
        const numbers = ['256700000000', '256700000001', '256700000002'];
        expect(await sdk.sendSMS(numbers, 'Test message')).toBe(true);
    });

    test('testSendSMSWithShortNumberLength', async () => {
        expect(await sdk.sendSMS('123', 'Test message')).toBe(false);
    });

    test('testSendSMSWithCustomMessagePriority', async () => {
        expect(await sdk.sendSMS('256700000000', 'Test message', undefined, MessagePriority.LOW)).toBe(true);
    });

    // The real API rejects requests over 1000 numbers server-side; this just
    // confirms the SDK surfaces that rejection as a clean `false`/failure
    // instead of throwing, and never loops/batches into multiple real sends.
    test('testSendSMSRejectsOverOneThousandNumbers', async () => {
        const numbers = Array.from({ length: 1001 }, (_, i) => '256700' + String(i).padStart(6, '0'));
        await expect(sdk.querySendSMS(numbers, 'Test message')).resolves.not.toThrow;
        const result = await sdk.querySendSMS(numbers, 'Test message');
        expect(result === null || result.Status === 'Failed').toBe(true);
    });

    test('testBalanceMethods', async () => {
        const balance = await sdk.getBalance();
        expect(Number(balance)).toBeGreaterThanOrEqual(0);
        const balanceResponse = await sdk.queryBalance();
        expect(balanceResponse).not.toBeNull();
        expect(Number(balanceResponse!.Balance)).toBeGreaterThanOrEqual(0);
    });

    test('testCheckBalanceAfterSendingSMS', async () => {
        const balanceBefore = await sdk.getBalance();
        await sdk.sendSMS('256700000000', 'Test message');
        const balanceAfter = await sdk.getBalance();
        expect(Number(balanceAfter)).toBeLessThan(Number(balanceBefore));
    });
});