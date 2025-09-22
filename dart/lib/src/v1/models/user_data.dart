class UserData {
  final String username;
  final String password;

  UserData(this.username, this.password);

  Map<String, dynamic> toJson() => {'username': username, 'password': password};
}
