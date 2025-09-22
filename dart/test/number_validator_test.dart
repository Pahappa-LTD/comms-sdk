
import '../lib/src/v1/utils/number_validator.dart';
import 'package:test/test.dart';

void main() {
  test('validateNumbers', () {
    final numbersToValidate = [
      '256712345678',
      '+256712345678',
      '0712345678',
      '235-787-900-123',
      '+257 700 567 234',
      '0745',
    ];
    final validated = NumberValidator.validateNumbers(numbersToValidate);
    expect(validated, isNotNull);
    expect(validated.length, 3);
    expect(validated.contains('256712345678'), isTrue);
    print(validated);
  });
}
