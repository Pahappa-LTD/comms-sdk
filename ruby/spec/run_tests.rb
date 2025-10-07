#!/usr/bin/env ruby
# frozen_string_literal: true

# Test runner script for CommsSdk Ruby implementation
# This script runs all RSpec tests and provides a summary

require 'rspec/core'

puts "🚀 Running CommsSdk Ruby Tests"
puts "=" * 50

# Configure RSpec
RSpec.configure do |config|
  config.color = true
  config.formatter = :documentation
  config.fail_fast = false
end

# Run the tests
exit_code = RSpec::Core::Runner.run([
  'spec/comms_sdk_spec.rb',
  'spec/comms_sdk/v1/comms_sdk_spec.rb',
  'spec/comms_sdk/v1/models_spec.rb',
  'spec/comms_sdk/v1/utils/number_validator_spec.rb',
  'spec/comms_sdk/v1/utils/validator_spec.rb',
  'spec/integration/full_workflow_spec.rb'
])

puts "\n" + "=" * 50
if exit_code == 0
  puts "✅ All tests passed!"
else
  puts "❌ Some tests failed!"
end

puts "\nTest Coverage:"
puts "- Main SDK functionality"
puts "- Authentication and validation"
puts "- SMS sending (single and multiple numbers)"
puts "- Balance checking"
puts "- Number validation utility"
puts "- Model serialization/deserialization"
puts "- Error handling and edge cases"

exit exit_code