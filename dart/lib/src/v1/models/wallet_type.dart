enum WalletType {
  local,
  international;

  String get value {
    switch (this) {
      case WalletType.local:
        return 'Local';
      case WalletType.international:
        return 'International';
    }
  }
}
