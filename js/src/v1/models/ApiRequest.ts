import { UserData } from "./UserData";
import { MessageModel } from "./MessageModel";
import { WalletType } from "./WalletType";

export class ApiRequest {
    // @ts-ignore
    public method: "SendSms" | "Balance";
    // @ts-ignore
    public userdata: UserData;
    public messageData: MessageModel[] = [];
    public walletType?: WalletType;

    public setMethod(method: "SendSms" | "Balance"): void {
        this.method = method;
    }

    public setUserdata(userdata: UserData): void {
        this.userdata = userdata;
    }

    public setMessageData(messageData: MessageModel[]): void {
        this.messageData = messageData;
    }

    public setWalletType(walletType: WalletType): void {
        this.walletType = walletType;
    }

    public toArray(): object {
        return {
            method: this.method,
            userdata: this.userdata.toArray(),
            msgdata: this.messageData.map((message) => message.toArray()),
            walletType: this.walletType,
        };
    }
}