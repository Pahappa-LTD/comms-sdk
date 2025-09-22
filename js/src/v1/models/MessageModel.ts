import { MessagePriority } from "./MessagePriority";

export class MessageModel {
    // @ts-ignore
    public number: string;
    // @ts-ignore
    public message: string;
    // @ts-ignore
    public senderId: string;
    // @ts-ignore
    public priority: MessagePriority;

    public setNumber(number: string): void {
        this.number = number;
    }

    public setMessage(message: string): void {
        this.message = message;
    }

    public setSenderId(senderId: string): void {
        this.senderId = senderId;
    }

    public setPriority(priority: MessagePriority): void {
        this.priority = priority;
    }

    public toArray(): object {
        return {
            number: this.number,
            message: this.message,
            senderid: this.senderId,
            priority: this.priority,
        };
    }
}