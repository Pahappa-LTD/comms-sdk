import { ApiResponseCode } from "./ApiResponseCode";

export class ApiResponse {
  public Status?: ApiResponseCode;
  public Message?: string;
  public Cost?: number;
  public MsgFollowUpUniqueCode?: string;
  public Balance?: number;
}
