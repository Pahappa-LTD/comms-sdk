import { NumberValidator } from "../../../src/v1/utils/NumberValidator";

describe('NumberValidator', () => {
    test('testValidateNumbersWithValidNumbers', () => {
        const numbers = ['+256772123456', '0772123457', '256772123458'];
        const expected = ['256772123456', '256772123457', '256772123458'];
        expect(NumberValidator.validateNumbers(numbers)).toEqual(expected);
    });

    test('testValidateNumbersWithInvalidNumbers', () => {
        const numbers = ['123', 'not a number', '077212345'];
        expect(NumberValidator.validateNumbers(numbers)).toStrictEqual([]);
    });

    test('testValidateNumbersWithMixedNumbers', () => {
        const numbers = ['+256772123456', '123', '0772123457'];
        const expected = ['256772123456', '256772123457'];
        expect(NumberValidator.validateNumbers(numbers)).toStrictEqual(expected);
    });

    test('testValidateNumbersWithEmptyArray', () => {
        expect(NumberValidator.validateNumbers([])).toStrictEqual([]);
    });

    test('testValidateNumbersWithDuplicateNumbers', () => {
        const numbers = ['+256772123456', '0772123456'];
        const expected = ['256772123456'];
        expect(NumberValidator.validateNumbers(numbers)).toEqual(expected);
    });

    test('testValidateNumbersWithDifferentFormats', () => {
        const numbers = ['+256 772 123 456', '0772-123-457', ' 256772123458 '];
        const expected = ['256772123456', '256772123457', '256772123458'];
        expect(NumberValidator.validateNumbers(numbers)).toEqual(expected);
    });
});