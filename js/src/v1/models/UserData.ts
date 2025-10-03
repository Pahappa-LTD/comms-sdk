export class UserData {
    public username: string;
    public apikey: string;

    constructor(userName: string, apiKey: string) {
        this.username = userName;
        this.apikey = apiKey;
    }

    public toArray(): object {
        return {
            username: this.username,
            password: this.apikey,
        };
    }
}