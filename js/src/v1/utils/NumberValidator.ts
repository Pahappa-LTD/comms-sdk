import anylogger from "anylogger";
const logger = anylogger('@pahappalimited/comms-sdk')

export class NumberValidator {
    private static regex = /^\+?(0|\d{3})\d{9}$/;

    public static validateNumbers(numbers: string[]): string[] {
        if (!numbers || numbers.length === 0) {
            logger.warn('Number list cannot be null or empty');
            return [];
        }

        const cleansed: string[] = [];
        for (const number of numbers) {
            if (!number || number.trim().length === 0) {
                logger.warn(`Number (${number}) cannot be null or empty!`);
                continue;
            }

            let cleanedNumber = number.replace(/-|\s/g, '').trim();
            if (NumberValidator.regex.test(cleanedNumber)) {
                if (cleanedNumber.startsWith('0')) {
                    cleanedNumber = '256' + cleanedNumber.substring(1);
                } else if (cleanedNumber.startsWith('+')) {
                    cleanedNumber = cleanedNumber.substring(1);
                }
                cleansed.push(cleanedNumber);
            } else {
                logger.error(`Number (${number}) is not valid!`);
            }
        }
        return [...new Set(cleansed)];
    }
}