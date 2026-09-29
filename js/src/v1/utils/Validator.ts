import anylogger from "anylogger";
import { CommsSDK } from "../CommsSDK";
import { ApiRequest } from "../models/ApiRequest";
import { UserData } from "../models/UserData";
import { WalletType } from "../models/WalletType";
import axios from 'axios';
const logger = anylogger('@pahappalimited/comms-sdk')

export class Validator {
    public static async validateCredentials(sdk: CommsSDK): Promise<boolean> {
        if (!sdk) {
            throw new Error('CommsSDK instance cannot be null');
        }

        if (!sdk.apiKey || !sdk.userName) {
            throw new Error('Either API Key or Username and Password must be provided');
        }

        if (!(await Validator.isValidCredential(sdk))) {
          logger.error('Authentication Failed')
            return false;
        }

        logger.info("Validated using an api key");
        sdk.setAuthenticated();
        return true;
    }

    private static async isValidCredential(sdk: CommsSDK): Promise<boolean> {
        const apiRequest = new ApiRequest();
        apiRequest.setMethod('Balance');
        apiRequest.setUserdata(new UserData(sdk.userName, sdk.apiKey));
        apiRequest.setWalletType(WalletType.LOCAL);

        try {
            logger.debug(`API_URL: ${sdk.apiUrl}`);
            const response = await axios.post(sdk.apiUrl, apiRequest.toArray());
            const apiResponse = response.data;

            if (apiResponse.Status === 'OK') {
                logger.info("Credentials validated successfully.\n");
                return true;
            } else {
                throw new Error(apiResponse.Message);
            }
        } catch (e) {
            // @ts-ignore
            logger.error(`Error validating credentials: ${e.message}\n`);
            return false;
        }
    }
}