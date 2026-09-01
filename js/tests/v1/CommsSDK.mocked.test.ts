import axios from "axios";
import { CommsSDK } from "../../src/";
import { WalletType } from "../../src/v1/models/WalletType";
import { MessagePriority } from "../../src/v1/models/MessagePriority";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

type RequestBody = {
  method: "SendSms" | "Balance";
  walletType?: unknown;
  msgdata?: Array<{ number: string; message: string; senderid: string; priority: unknown }>;
};

const OK_BALANCE = { data: { Status: "OK", Message: "Success", Balance: 100 } };
const OK_SEND = {
  data: { Status: "OK", Message: "Success", MsgFollowUpUniqueCode: "ABC123", Cost: 0.09 },
};
const FAILED_SEND = { data: { Status: "Failed", Message: "SendSms rejected" } };

/**
 * authenticate() fires off credential validation without awaiting it, so a
 * subsequent SDK call can trigger a second, racing "Balance" check before the
 * first resolves (both must succeed for the flow to proceed). Routing mock
 * responses by request method, rather than call order, keeps these tests
 * robust to that race instead of asserting an exact call count.
 */
function mockByMethod(sendSmsResponse: typeof OK_SEND | typeof FAILED_SEND = OK_SEND) {
  mockedAxios.post.mockImplementation((_url, body) => {
    const req = body as RequestBody;
    if (req.method === "Balance") return Promise.resolve(OK_BALANCE);
    if (req.method === "SendSms") return Promise.resolve(sendSmsResponse);
    return Promise.reject(new Error(`Unexpected method: ${req.method}`));
  });
}

function balanceCalls() {
  return mockedAxios.post.mock.calls.filter(
    (call) => (call[1] as RequestBody).method === "Balance",
  );
}

function sendSmsCalls() {
  return mockedAxios.post.mock.calls.filter(
    (call) => (call[1] as RequestBody).method === "SendSms",
  );
}

describe("CommsSDK (mocked, no network)", () => {
  beforeEach(() => {
    mockedAxios.post.mockReset();
    CommsSDK.useSandBox();
  });

  test("querySendSMS always sends an explicit walletType of Local, never omitted or null", async () => {
    mockByMethod();

    const sdk = CommsSDK.authenticate("user", "key");
    await sdk.querySendSMS("+256772123456", "Test message");

    expect(sendSmsCalls().length).toBeGreaterThan(0);
    for (const call of mockedAxios.post.mock.calls) {
      const body = call[1] as RequestBody;
      expect(body.walletType).toBe(WalletType.LOCAL);
      expect(body.walletType).not.toBeUndefined();
      expect(body.walletType).not.toBeNull();
    }
  });

  test("querySendSMS builds the correct request shape with default priority HIGH", async () => {
    mockByMethod();

    const sdk = CommsSDK.authenticate("user", "key");
    await sdk.querySendSMS(["+256772123456"], "Test message", "MySender");

    const body = sendSmsCalls()[0][1] as RequestBody;
    expect(body.method).toBe("SendSms");
    expect(body.msgdata).toHaveLength(1);
    expect(body.msgdata![0].number).toBe("256772123456");
    expect(body.msgdata![0].message).toBe("Test message");
    expect(body.msgdata![0].senderid).toBe("MySender");
    expect(body.msgdata![0].priority).toBe(MessagePriority.HIGH);
  });

  test("a mocked Status: OK response is parsed and sendSMS reports success", async () => {
    mockByMethod(OK_SEND);

    const sdk = CommsSDK.authenticate("user", "key");
    const result = await sdk.sendSMS("+256772123456", "Test message");

    expect(result).toBe(true);
  });

  test("a mocked Status: Failed response is handled gracefully by sendSMS", async () => {
    mockByMethod(FAILED_SEND);

    const sdk = CommsSDK.authenticate("user", "key");
    const result = await sdk.sendSMS("+256772123456", "Test message");

    expect(result).toBe(false);
  });

  test("a mocked Status: Failed response is handled gracefully by querySendSMS (no throw)", async () => {
    mockByMethod(FAILED_SEND);

    const sdk = CommsSDK.authenticate("user", "key");
    const result = await sdk.querySendSMS("+256772123456", "Test message");

    expect(result).not.toBeNull();
    expect(result?.Status).toBe("Failed");
  });

  test("queryBalance() defaults walletType to Local when called with no argument", async () => {
    mockByMethod();

    const sdk = CommsSDK.authenticate("user", "key");
    await sdk.queryBalance();

    const calls = balanceCalls();
    const lastBalanceCall = calls[calls.length - 1];
    expect((lastBalanceCall[1] as RequestBody).walletType).toBe(WalletType.LOCAL);
  });

  test("queryBalance() respects an explicit WalletType.INTERNATIONAL argument", async () => {
    mockByMethod();

    const sdk = CommsSDK.authenticate("user", "key");
    await sdk.queryBalance(WalletType.INTERNATIONAL);

    const calls = balanceCalls();
    const lastBalanceCall = calls[calls.length - 1];
    expect((lastBalanceCall[1] as RequestBody).walletType).toBe(WalletType.INTERNATIONAL);
  });

  test("the credential-validation Balance check always sends an explicit walletType of Local", async () => {
    mockByMethod();

    CommsSDK.authenticate("user", "key");
    // authenticate() doesn't await validation internally, so flush microtasks.
    await new Promise((resolve) => setImmediate(resolve));

    expect(balanceCalls().length).toBeGreaterThan(0);
    for (const call of balanceCalls()) {
      expect((call[1] as RequestBody).walletType).toBe(WalletType.LOCAL);
    }
  });
});
