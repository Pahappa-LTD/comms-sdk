export class UserData {
    public username: string;
    public password: string;

    constructor(username: string, password: string) {
        this.username = username;
        this.password = password;
    }

    public getUsername(): string {
        return this.username;
    }

    public getPassword(): string {
        return this.password;
    }

    public toArray(): object {
        return {
            username: this.username,
            password: this.password,
        };
    }
}