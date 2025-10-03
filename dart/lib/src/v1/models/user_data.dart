class UserData {
  final String username;
  final String apiKey;

  UserData(this.username, this.apiKey);

  Map<String, dynamic> toJson() => {'username': username, 'password': apiKey};
}
