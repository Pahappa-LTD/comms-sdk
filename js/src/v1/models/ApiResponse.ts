import { ApiResponseCode } from "./ApiResponseCode";

export class ApiResponse {
  // @ts-ignore
  public Status: ApiResponseCode;
  // @ts-ignore
  public Message: string;
  // @ts-ignore
  public Cost: string;
  // @ts-ignore
  public MsgFollowUpUniqueCode: string;
  // @ts-ignore
  public Balance: string;

  public setStatus(status: ApiResponseCode): void {
    this.Status = status;
  }

  public setMessage(message: string): void {
    this.Message = message;
  }

  public setCost(cost: string): void {
    this.Cost = cost;
  }

  public setMsgFollowUpUniqueCode(MsgFollowUpUniqueCode: string): void {
    this.MsgFollowUpUniqueCode = MsgFollowUpUniqueCode;
  }

  public setBalance(balance: string): void {
    this.Balance = balance;
  }
}
