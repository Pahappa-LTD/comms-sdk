import { CommsSDK } from "../CommsSDK";
import { ApiRequest } from "../models/ApiRequest";
import { UserData } from "../models/UserData";
import axios from 'axios';

export class Validator {
    public static async validateCredentials(sdk: CommsSDK): Promise<boolean> {
        if (!sdk) {
            throw new Error('CommsSDK instance cannot be null');
        }

        if (!sdk.apiKey || !sdk.userName) {
            throw new Error('Either API Key or Username and Password must be provided');
        }

        if (!(await Validator.isValidCredential(sdk))) {
            console.log("                                                      _                    ");
            console.log("  /\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | ");
            console.log(" /--\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o ");
            console.log("                                                                           ");
            console.log("\n");
            return false;
        }

        console.log("Validated using an api key");
        console.log("\n");
        sdk.setAuthenticated();
        return true;
    }

    private static async isValidCredential(sdk: CommsSDK): Promise<boolean> {
        const apiRequest = new ApiRequest();
        apiRequest.setMethod('Balance');
        apiRequest.setUserdata(new UserData(sdk.userName, sdk.apiKey));

        try {
            console.log(`API_URL: ${CommsSDK.API_URL}`);
            const response = await axios.post(CommsSDK.API_URL, apiRequest.toArray());
            const apiResponse = response.data;

            if (apiResponse.Status === 'OK') {
                console.log("Credentials validated successfully.\n");
                return true;
            } else {
                throw new Error(apiResponse.Message);
            }
        } catch (e) {
            // @ts-ignore
            console.log(`Error validating credentials: ${e.message}\n`);
            return false;
        }
    }
}