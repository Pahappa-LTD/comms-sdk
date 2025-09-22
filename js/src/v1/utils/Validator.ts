import { EgoSmsSDK } from "../EgoSmsSDK";
import { ApiRequest } from "../models/ApiRequest";
import { UserData } from "../models/UserData";
import axios from 'axios';

export class Validator {
    public static async validateCredentials(sdk: EgoSmsSDK): Promise<boolean> {
        if (!sdk) {
            throw new Error('EgoSmsSDK instance cannot be null');
        }

        const isApiKey = !!sdk.getApiKey();
        if (!isApiKey && (!sdk.getPassword() || !sdk.getUsername())) {
            throw new Error('Either API Key or Username and Password must be provided');
        }

        if (!(await Validator.isValidCredential(sdk, isApiKey))) {
            console.log("                                                      _                    ");
            console.log("  /\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | ");
            console.log(" /--\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o ");
            console.log("                                                                           ");
            console.log("\n");
            return false;
        }

        console.log(isApiKey ? "Validated using an api key" : "Validated using basic auth");
        console.log("\n");
        sdk.setAuthenticated(true);
        return true;
    }

    private static async isValidCredential(sdk: EgoSmsSDK, isApiKey: boolean): Promise<boolean> {
        const apiRequest = new ApiRequest();
        apiRequest.setMethod('Balance');
        apiRequest.setUserdata(new UserData(sdk.getUsername(), sdk.getPassword()));

        try {
            console.log(`API_URL: ${EgoSmsSDK.API_URL}`);
            const response = await axios.post(EgoSmsSDK.API_URL, apiRequest.toArray());
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