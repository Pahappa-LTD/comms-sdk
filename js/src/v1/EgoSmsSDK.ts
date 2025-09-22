import { ApiRequest } from "./models/ApiRequest";
import { ApiResponse } from "./models/ApiResponse";
import { MessageModel } from "./models/MessageModel";
import { MessagePriority } from "./models/MessagePriority";
import { UserData } from "./models/UserData";
import { NumberValidator } from "./utils/NumberValidator";
import { Validator } from "./utils/Validator";
import axios from 'axios';

export class EgoSmsSDK {
    public static API_URL = 'https://www.egosms.co/api/v1/json/';

    private apiKey?: string;
    private username?: string;
    private password?: string;
    private senderId = 'EgoSms';
    private isAuthenticated = false;

    private constructor() {}

    public static useSandBox(): void {
        EgoSmsSDK.API_URL = 'http://sandbox.egosms.co/api/v1/json/';
    }

    public static useLiveServer(): void {
        EgoSmsSDK.API_URL = 'https://www.egosms.co/api/v1/json/';
    }

    public static authenticateWithApiKey(apiKey: string): EgoSmsSDK {
        throw new Error('API Key authentication is not supported in this version. Please use username and password authentication.');
    }

    public static authenticate(username: string, password: string): EgoSmsSDK {
        const sdk = new EgoSmsSDK();
        sdk.username = username;
        sdk.password = password;
        Validator.validateCredentials(sdk);
        return sdk;
    }

    public withSenderId(senderId: string): EgoSmsSDK {
        this.senderId = senderId;
        return this;
    }

    public async sendSMS(numbers: string | string[], message: string, senderId?: string, priority: MessagePriority = MessagePriority.HIGHEST): Promise<boolean> {
        if (await this.sdkNotAuthenticated()) {
            return false;
        }
        if (!numbers || numbers.length === 0) {
            throw new Error('Numbers list cannot be null or empty');
        }
        if (!message) {
            throw new Error('Message cannot be null or empty');
        }
        if (message.length === 1) {
            throw new Error('Message cannot be a single character');
        }
        if (!senderId || senderId.trim() === '') {
            senderId = this.senderId;
        }

        if (senderId && senderId.length > 11) {
            console.log("Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.");
        }

        const validatedNumbers = NumberValidator.validateNumbers(Array.isArray(numbers) ? numbers : [numbers]);

        if (validatedNumbers.length === 0) {
            console.log('No valid phone numbers provided. Please check inputs.');
            return false;
        }

        const apiRequest = new ApiRequest();
        apiRequest.setMethod('SendSms');

        const messageModels = validatedNumbers.map(number => {
            const messageModel = new MessageModel();
            messageModel.setNumber(number);
            messageModel.setMessage(message);
            messageModel.setSenderId(senderId);
            messageModel.setPriority(priority);
            return messageModel;
        });

        apiRequest.setMessageData(messageModels);
        apiRequest.setUserdata(new UserData(this.username!, this.password!));

        try {
            const response = await axios.post(EgoSmsSDK.API_URL, apiRequest.toArray());
            const apiResponse = response.data;

            if (apiResponse.Status === 'OK') {
                console.log("SMS sent successfully.\n");
                console.log(`MessageFollowUpUniqueCode: ${apiResponse['MsgFollowUpUniqueCode']}`);
                return true;
            }
            else {
                throw new Error(apiResponse.Message);
            }
        } catch (e) {
            // @ts-ignore
            console.log(`Failed to send SMS: ${e.message}`);
            console.log(`Request: ${JSON.stringify(apiRequest.toArray())}`);
            return false;
        }
    }

    private async sdkNotAuthenticated(): Promise<boolean> {
        if (!this.isAuthenticated) {
            console.log('SDK is not authenticated. Please authenticate before performing actions.');
            console.log('Attempting to re-authenticate with provided credentials...');
            return !(await Validator.validateCredentials(this));
        }
        return false;
    }

    public async getBalance(): Promise<string | undefined> {
        if (await this.sdkNotAuthenticated()) {
            return undefined;
        }

        const apiRequest = new ApiRequest();
        apiRequest.setMethod('Balance');
        apiRequest.setUserdata(new UserData(this.username!, this.password!));

        try {
            const response = await axios.post(EgoSmsSDK.API_URL, apiRequest.toArray());
            const apiResponse = response.data as ApiResponse;
            console.log(`MessageFollowUpUniqueCode: ${apiResponse['MsgFollowUpUniqueCode']}`);
            return apiResponse.Balance;
        } catch (e) {
            // @ts-ignore
            throw new Error(`Failed to get balance: ${e.message}`);
        }
    }

    public getApiKey(): string {
        return this.apiKey!;
    }

    public getUsername(): string {
        return this.username!;
    }

    public getPassword(): string {
        return this.password!;
    }

    public setAuthenticated(isAuthenticated: boolean): void {
        this.isAuthenticated = isAuthenticated;
    }
}