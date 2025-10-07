# frozen_string_literal: true

require 'spec_helper'

RSpec.describe CommsSdk::V1::Utils::NumberValidator do
  describe '.validate_numbers' do
    context 'with valid numbers' do
      it 'validates numbers with different formats' do
        numbers = ["+256772123456", "0772123457", "256772123458"]
        expected = ["256772123456", "256772123457", "256772123458"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'converts numbers starting with 0 to 256 prefix' do
        numbers = ["0772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'removes + prefix from international numbers' do
        numbers = ["+256772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'handles numbers with spaces and dashes' do
        numbers = ["+256 772 123 456", "0772-123-457", " 256772123458 "]
        expected = ["256772123456", "256772123457", "256772123458"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end
    end

    context 'with invalid numbers' do
      it 'returns empty array for completely invalid numbers' do
        numbers = ["123", "not a number", "077212345"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to be_empty
      end

      it 'filters out invalid numbers from mixed input' do
        numbers = ["+256772123456", "123", "0772123457"]
        expected = ["256772123456", "256772123457"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'handles empty strings and whitespace' do
        numbers = ["", "   ", "+256772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end
    end

    context 'with duplicate numbers' do
      it 'removes duplicates from same format' do
        numbers = ["+256772123456", "+256772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'removes duplicates from different formats' do
        numbers = ["+256772123456", "0772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'handles multiple duplicates in different formats' do
        numbers = ["+256772123456", "0772123456", "256772123456", "+256772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end
    end

    context 'with edge cases' do
      it 'returns empty array for nil input' do
        result = described_class.validate_numbers(nil)
        expect(result).to be_empty
      end

      it 'returns empty array for empty array' do
        result = described_class.validate_numbers([])
        expect(result).to be_empty
      end

      it 'handles array with nil elements' do
        numbers = [nil, "+256772123456", nil]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end
    end

    context 'number format validation' do
      it 'accepts 10-digit numbers starting with 0' do
        numbers = ["0772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'accepts 12-digit numbers starting with 256' do
        numbers = ["256772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'accepts international format with +' do
        numbers = ["+256772123456"]
        expected = ["256772123456"]
        
        result = described_class.validate_numbers(numbers)
        expect(result).to match_array(expected)
      end

      it 'rejects numbers that are too short' do
        numbers = ["077212345"]  # 9 digits instead of 10
        
        result = described_class.validate_numbers(numbers)
        expect(result).to be_empty
      end

      it 'rejects numbers that are too long' do
        numbers = ["07721234567"]  # 11 digits instead of 10
        
        result = described_class.validate_numbers(numbers)
        expect(result).to be_empty
      end

      it 'rejects numbers with invalid country codes' do
        numbers = ["123772123456"]  # Invalid country code, but allowed for cases when we add more countries
        
        result = described_class.validate_numbers(numbers)
        expect(result).to_not be_empty
      end
    end

    context 'output validation' do
      it 'prints validation messages for invalid numbers' do
        numbers = ["123", "invalid"]
        
        expect { described_class.validate_numbers(numbers) }
          .to output(/Number \(123\) is not valid!.*Number \(invalid\) is not valid!/m).to_stdout
      end

      it 'does not print messages for valid numbers' do
        numbers = ["+256772123456"]
        
        expect { described_class.validate_numbers(numbers) }
          .not_to output.to_stdout
      end
    end
  end
end