class NumberValidator {
  static final RegExp _regex = RegExp(r'^\+?(0|\d{3})\d{9}$');

  static List<String> validateNumbers(List<String> numbers) {
    if (numbers.isEmpty) {
      print('Number list cannot be null or empty');
      return [];
    }

    final cleansed = <String>{};
    for (var number in numbers) {
      if (number.trim().isEmpty) {
        print('Number ($number) cannot be null or empty!');
        continue;
      }
      number = number.trim().replaceAll(RegExp(r'-|\s'), '');
      if (_regex.hasMatch(number)) {
        if (number.startsWith('0')) {
          number = '256${number.substring(1)}';
        } else if (number.startsWith('+')) {
          number = number.substring(1);
        }
        cleansed.add(number);
      } else {
        print('Number ($number) is not valid!');
      }
    }
    return cleansed.toList();
  }
}