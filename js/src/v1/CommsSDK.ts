import { ApiRequest } from "./models/ApiRequest";
import { ApiResponse } from "./models/ApiResponse";
import { MessageModel } from "./models/MessageModel";
import { MessagePriority } from "./models/MessagePriority";
import { UserData } from "./models/UserData";
import { NumberValidator } from "./utils/NumberValidator";
import { Validator } from "./utils/Validator";
import axios from "axios";

export class CommsSDK {
  public static API_URL = "https://comms.egosms.co/api/v1/json/";

  private _apiKey?: string;
  private _userName?: string;
  private _senderId = "EgoSMS";
  private _isAuthenticated = false;

  private constructor() {}

  public static useSandBox(): void {
    CommsSDK.API_URL = "https://comms-test.pahappa.net/api/v1/json";
  }

  public static useLiveServer(): void {
    CommsSDK.API_URL = "https://comms.egosms.co/api/v1/json";
  }

  public static authenticate(userName: string, apiKey: string): CommsSDK {
    const sdk = new CommsSDK();
    sdk._userName = userName;
    sdk._apiKey = apiKey;
    Validator.validateCredentials(sdk);
    return sdk;
  }

  public withSenderId(senderId: string): CommsSDK {
    this._senderId = senderId;
    return this;
  }

  public setAuthenticated(): void {
    this._isAuthenticated = true;
  }

  public get userName(): string {
    return this._userName || "";
  }

  public get apiKey(): string {
    return this._apiKey || "";
  }

  public get isAuthenticated(): boolean {
    return this._isAuthenticated;
  }

  public get senderId(): string {
    return this._senderId;
  }

  // public async sendSMS(
  //     number: string,
  //     message: string,
  //     senderId: string = this._senderId,
  //     priority: MessagePriority = MessagePriority.HIGHEST
  // ): Promise<boolean>;
  // public async sendSMS(
  //     numbers: string[],
  //     message: string,
  //     senderId: string = this._senderId,
  //     priority: MessagePriority = MessagePriority.HIGHEST
  // ): Promise<boolean>;
  public async sendSMS(
    numbers: string | string[],
    message: string,
    senderId: string = this._senderId,
    priority: MessagePriority = MessagePriority.HIGHEST,
  ): Promise<boolean> {
    const numbersArray = Array.isArray(numbers) ? numbers : [numbers];
    const apiResponse = await this.querySendSMS(
      numbersArray,
      message,
      senderId,
      priority,
    );

    if (apiResponse === null) {
      console.log("Failed to get a response from the server.");
      return false;
    }

    if (apiResponse.Status === "OK") {
      console.log("SMS sent successfully.");
      console.log(
        `MessageFollowUpUniqueCode: ${apiResponse.MsgFollowUpUniqueCode}`,
      );
      return true;
    } else if (apiResponse.Status === "Failed") {
      console.log(`Failed: ${apiResponse.Message}`);
      return false;
    } else {
      throw new Error(`Unexpected response status: ${apiResponse.Status}`);
    }
  }

  public async querySendSMS(
    numbers: string[],
    message: string,
    senderId: string,
    priority: MessagePriority,
  ): Promise<ApiResponse | null> {
    if (await this.sdkNotAuthenticated()) {
      return null;
    }
    if (!numbers || numbers.length === 0) {
      throw new Error("Numbers list cannot be empty");
    }
    if (!message) {
      throw new Error("Message cannot be empty");
    }
    if (message.length === 1) {
      throw new Error("Message cannot be a single character");
    }
    if (!senderId || senderId.trim() === "") {
      senderId = this._senderId;
    }

    if (senderId && senderId.length > 11) {
      console.log(
        "Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.",
      );
    }

    const validatedNumbers = NumberValidator.validateNumbers(numbers);

    if (validatedNumbers.length === 0) {
      console.error("No valid phone numbers provided. Please check inputs.");
      return null;
    }

    const apiRequest = new ApiRequest();
    apiRequest.setMethod("SendSms");

    const messageModels = validatedNumbers.map((number) => {
      const messageModel = new MessageModel();
      messageModel.setNumber(number);
      messageModel.setMessage(message);
      messageModel.setSenderId(senderId);
      messageModel.setPriority(priority);
      return messageModel;
    });

    apiRequest.setMessageData(messageModels);
    apiRequest.setUserdata(new UserData(this._userName!, this._apiKey!));

    try {
      const response = await axios.post(CommsSDK.API_URL, apiRequest.toArray());
      return response.data as ApiResponse;
    } catch (e) {
      console.error(`Failed to send SMS: ${(e as Error).message}`);
      try {
        console.error(`Request: ${JSON.stringify(apiRequest.toArray())}`);
      } catch (_) {
        // Ignore serialization errors
      }
      return null;
    }
  }

  private async sdkNotAuthenticated(): Promise<boolean> {
    if (!this._isAuthenticated) {
      console.error(
        "SDK is not authenticated. Please authenticate before performing actions.",
      );
      console.error(
        "Attempting to re-authenticate with provided credentials...",
      );
      return !(await Validator.validateCredentials(this));
    }
    return false;
  }

  public async queryBalance(): Promise<ApiResponse | null> {
    if (await this.sdkNotAuthenticated()) {
      return null;
    }

    const apiRequest = new ApiRequest();
    apiRequest.setMethod("Balance");
    apiRequest.setUserdata(new UserData(this._userName!, this._apiKey!));

    try {
      const response = await axios.post(CommsSDK.API_URL, apiRequest.toArray());
      return response.data as ApiResponse;
    } catch (e) {
      throw new Error(`Failed to get balance: ${(e as Error).message}`);
    }
  }

  public async getBalance(): Promise<number | null> {
    const response = await this.queryBalance();
    return response?.Balance ?? null;
  }

  public toString(): string {
    return `SDK(${this._userName} => ${this._apiKey})`;
  }
}
