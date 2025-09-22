import { UserData } from "./UserData";
import { MessageModel } from "./MessageModel";

export class ApiRequest {
    // @ts-ignore
    public method: "SendSms" | "Balance";
    // @ts-ignore
    public userdata: UserData;
    public messageData: MessageModel[] = [];

    public setMethod(method: "SendSms" | "Balance"): void {
        this.method = method;
    }

    public setUserdata(userdata: UserData): void {
        this.userdata = userdata;
    }

    public setMessageData(messageData: MessageModel[]): void {
        this.messageData = messageData;
    }

    public toArray(): object {
        return {
            method: this.method,
            userdata: this.userdata.toArray(),
            msgdata: this.messageData.map((message) => message.toArray()),
        };
    }
}