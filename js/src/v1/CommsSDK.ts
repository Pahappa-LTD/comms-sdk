import anylogger from 'anylogger'
import axios from 'axios'
import { ApiRequest } from './models/ApiRequest'
import { ApiResponse } from './models/ApiResponse'
import { MessageModel } from './models/MessageModel'
import { MessagePriority } from './models/MessagePriority'
import { UserData } from './models/UserData'
import { WalletType } from './models/WalletType'
import { NumberValidator } from './utils/NumberValidator'
import { Validator } from './utils/Validator'

const LIVE_API_URL = 'https://comms.egosms.co/api/v1/json'
const SANDBOX_API_URL = 'https://comms-test.pahappa.net/api/v1/json'
const logger = anylogger('@pahappalimited/comms-sdk')

let defaultUrl: string = LIVE_API_URL

export class CommsSDK {
  private _apiKey?: string
  private _userName?: string
  private _senderId = 'EgoSMS'
  private _isAuthenticated = false
  private _apiUrl = LIVE_API_URL

  public constructor(userName: string, apiKey: string) {
    this._userName = userName
    this._apiKey = apiKey
  }

  /**@deprecated This will be discontinued. Use sdk.authenticateSandbox() instead */
  public static useSandBox(): void {
    defaultUrl = SANDBOX_API_URL
  }

  /**@deprecated This will be discontinued. Use sdk.authenticate() instead */
  public static useLiveServer(): void {
    defaultUrl = LIVE_API_URL
  }

  /**@deprecated This will be discontinued. Use sdk.authenticate() instead */
  public static authenticate(userName: string, apiKey: string): CommsSDK {
    const commsSdk = new CommsSDK(userName, apiKey)
    commsSdk._apiUrl = defaultUrl
    Validator.validateCredentials(commsSdk).catch(() => {}) // this is an error, but it's re-verified when doing a send
    return commsSdk
  }

  public withSenderId(senderId: string): CommsSDK {
    this._senderId = senderId
    return this
  }

  public setAuthenticated(): void {
    this._isAuthenticated = true
  }

  public get userName(): string {
    return this._userName || ''
  }

  public get apiKey(): string {
    return this._apiKey || ''
  }

  /**@internal */
  public get apiUrl(): string {
    return this._apiUrl
  }

  public get isAuthenticated(): boolean {
    return this._isAuthenticated
  }

  public get senderId(): string {
    return this._senderId
  }

  public async authenticate(): Promise<CommsSDK> {
    this._apiUrl = LIVE_API_URL
    this._isAuthenticated = await Validator.validateCredentials(this)
    return this
  }

  public async authenticateSandbox(): Promise<CommsSDK> {
    this._apiUrl = SANDBOX_API_URL
    this._isAuthenticated = await Validator.validateCredentials(this)
    return this
  }

  public async sendSMS(
    numbers: string | string[],
    message: string,
    senderId: string = this._senderId,
    priority: MessagePriority = MessagePriority.HIGH,
  ): Promise<boolean> {
    const apiResponse = await this.querySendSMS(
      numbers,
      message,
      senderId,
      priority,
    )

    if (apiResponse === null) {
      logger.log('Failed to get a response from the server.')
      return false
    }

    if (apiResponse.Status === 'OK') {
      logger.log('SMS sent successfully.')
      logger.log(
        `MessageFollowUpUniqueCode: ${apiResponse.MsgFollowUpUniqueCode}`,
      )
      return true
    } else if (apiResponse.Status === 'Failed') {
      logger.log(`Failed: ${apiResponse.Message}`)
      return false
    } else {
      throw new Error(`Unexpected response status: ${apiResponse.Status}`)
    }
  }

  public async querySendSMS(
    numbers: string | string[],
    message: string,
    senderId: string = this._senderId,
    priority: MessagePriority = MessagePriority.HIGH,
  ): Promise<ApiResponse | null> {
    if (await this.sdkNotAuthenticated()) {
      return null
    }
    numbers = Array.isArray(numbers) ? numbers : [numbers]
    if (!numbers || numbers.length === 0) {
      throw new Error('Numbers list cannot be empty')
    }
    if (!message) {
      throw new Error('Message cannot be empty')
    }
    if (message.length === 1) {
      throw new Error('Message cannot be a single character')
    }
    if (!senderId || senderId.trim() === '') {
      senderId = this._senderId
    }

    if (senderId && senderId.length > 11) {
      logger.log(
        'Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages.',
      )
    }

    const validatedNumbers = NumberValidator.validateNumbers(numbers)

    if (validatedNumbers.length === 0) {
      logger.error('No valid phone numbers provided. Please check inputs.')
      return null
    }

    const messageModels = validatedNumbers.map((number) => {
      const messageModel = new MessageModel()
      messageModel.setNumber(number)
      messageModel.setMessage(message)
      messageModel.setSenderId(senderId)
      messageModel.setPriority(priority)
      return messageModel
    })

    return this.sendCustomSMS(messageModels)
  }

  public async sendCustomSMS(
    messageModels: MessageModel[],
  ): Promise<ApiResponse | null> {
    if (await this.sdkNotAuthenticated()) {
      return null
    }
    const apiRequest = new ApiRequest()
    apiRequest.setMethod('SendSms')
    apiRequest.setMessageData(messageModels)
    apiRequest.setUserdata(new UserData(this._userName!, this._apiKey!))
    apiRequest.setWalletType(WalletType.LOCAL)

    try {
      const response = await axios.post(this._apiUrl, apiRequest.toArray())
      return response.data as ApiResponse
    } catch (e) {
      logger.error(`Failed to send SMS: ${(e as Error).message}`)
      try {
        logger.debug(`Request: ${JSON.stringify(apiRequest.toArray())}`)
      } catch (_) {
        // Ignore serialization errors
      }
      return null
    }
  }

  private async sdkNotAuthenticated(): Promise<boolean> {
    if (!this._isAuthenticated) {
      logger.warn(
        'SDK is not authenticated. Please authenticate before performing actions.',
      )
      logger.warn('Attempting to re-authenticate with provided credentials...')
      return !(await Validator.validateCredentials(this))
    }
    return false
  }

  public async queryBalance(
    walletType: WalletType = WalletType.LOCAL,
  ): Promise<ApiResponse | null> {
    if (await this.sdkNotAuthenticated()) {
      return null
    }

    const apiRequest = new ApiRequest()
    apiRequest.setMethod('Balance')
    apiRequest.setUserdata(new UserData(this._userName!, this._apiKey!))
    apiRequest.setWalletType(walletType ?? WalletType.LOCAL)

    try {
      const response = await axios.post(this._apiUrl, apiRequest.toArray())
      return response.data as ApiResponse
    } catch (e) {
      throw new Error(`Failed to get balance: ${(e as Error).message}`)
    }
  }

  public async getBalance(
    walletType: WalletType = WalletType.LOCAL,
  ): Promise<number | null> {
    const response = await this.queryBalance(walletType)
    return response?.Balance ?? null
  }

  public toString(): string {
    return `SDK(${this.userName}, ${this.senderId}, ${this.apiUrl})`
  }
}
