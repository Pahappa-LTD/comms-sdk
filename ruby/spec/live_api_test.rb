#!/usr/bin/env ruby
# frozen_string_literal: true

# Live API Test for CommsSdk Ruby Implementation
# This file contains tests that make actual API calls
# Uncomment the tests below to run live API tests
# WARNING: These tests will consume actual SMS credits!

require_relative '../lib/comms_sdk'

# Test credentials
USERNAME = "sandbox"
API_KEY = "sandbox35"
TEST_PHONE = "+256772123456"

puts "🧪 CommsSdk Ruby Live API Tests"
puts "=" * 50
# puts "⚠️  WARNING: These tests make real API calls and consume credits!"
# puts "Uncomment the test sections below to run live tests."
puts

# Uncomment the sections below to run live API tests

# =begin
puts "🔐 Testing Authentication..."
begin
  CommsSdk::V1::CommsSDK.use_sandbox
  sdk = CommsSdk::V1::CommsSDK.authenticate(USERNAME, API_KEY)
  puts "✅ Authentication successful!"
  puts "   Username: #{sdk.user_name}"
  puts "   Authenticated: #{sdk.is_authenticated}"
  puts "   Default Sender ID: #{sdk.sender_id}"
rescue => e
  puts "❌ Authentication failed: #{e.message}"
  exit 1
end

puts "\n💰 Testing Balance Check..."
begin
  balance = sdk.get_balance
  puts "✅ Balance check successful!"
  puts "   Current balance: #{balance}"
  
  # Test full balance response
  balance_response = sdk.query_balance
  puts "   Full response status: #{balance_response.status}"
  puts "   Balance from response: #{balance_response.balance}"
rescue => e
  puts "❌ Balance check failed: #{e.message}"
end

puts "\n📱 Testing SMS Sending..."
begin
  # Test single number SMS
  success = sdk.send_sms(TEST_PHONE, "Test message from Ruby SDK!")
  puts "✅ Single SMS sent: #{success}"
  
  # Test with custom sender ID
  sdk.with_sender_id("RubySDK")
  success = sdk.send_sms(TEST_PHONE, "Test with custom sender ID", sender_id: "RubyTest")
  puts "✅ SMS with custom sender sent: #{success}"
  
  # Test with different priority
  success = sdk.send_sms(
    TEST_PHONE, 
    "Test with low priority", 
    priority: CommsSdk::V1::MessagePriority::LOW
  )
  puts "✅ SMS with low priority sent: #{success}"
  
  # Test multiple numbers
  numbers = [TEST_PHONE, "0772123457"]  # Add more test numbers as needed
  success = sdk.send_sms(numbers, "Bulk test message from Ruby SDK!")
  puts "✅ Bulk SMS sent: #{success}"
  
rescue => e
  puts "❌ SMS sending failed: #{e.message}"
end

puts "\n🔍 Testing Full Response Methods..."
begin
  # Test query_send_sms for full response
  response = sdk.query_send_sms(
    [TEST_PHONE], 
    "Test query response", 
    "RubySDK", 
    CommsSdk::V1::MessagePriority::HIGHEST
  )
  
  if response
    puts "✅ Query SMS response received!"
    puts "   Status: #{response.status}"
    puts "   Message: #{response.message}"
    puts "   Follow-up Code: #{response.msg_follow_up_unique_code}"
    puts "   Cost: #{response.cost}"
  else
    puts "❌ No response received from query_send_sms"
  end
rescue => e
  puts "❌ Query SMS failed: #{e.message}"
end

puts "\n💸 Testing Balance After SMS..."
begin
  final_balance = sdk.get_balance
  puts "✅ Final balance check successful!"
  puts "   Final balance: #{final_balance}"
rescue => e
  puts "❌ Final balance check failed: #{e.message}"
end

puts "\n🧪 Testing Error Scenarios..."
begin
  # Test with invalid number
  success = sdk.send_sms("123", "This should fail")
  puts "📝 Invalid number test result: #{success}"
  
  # Test with empty message
  begin
    sdk.send_sms(TEST_PHONE, "")
  rescue ArgumentError => e
    puts "✅ Empty message validation works: #{e.message}"
  end
  
  # Test with single character message
  begin
    sdk.send_sms(TEST_PHONE, "A")
  rescue ArgumentError => e
    puts "✅ Single character validation works: #{e.message}"
  end
  
rescue => e
  puts "❌ Error scenario testing failed: #{e.message}"
end
# end

puts "\n📋 Test Summary:"
puts "- Authentication test"
puts "- Balance checking (get_balance and query_balance)"
puts "- Single SMS sending"
puts "- Bulk SMS sending"
puts "- Custom sender ID and priority"
puts "- Full response methods (query_send_sms)"
puts "- Error handling and validation"
puts "- Number validation"

# puts "\n💡 To run live tests:"
# puts "1. Uncomment the test sections in this file"
# puts "2. Ensure you have sufficient balance"
# puts "3. Update TEST_PHONE with a valid number"
# puts "4. Run: ruby spec/live_api_test.rb"

# puts "\n⚠️  Remember: Live tests consume actual SMS credits!"
# puts "Use sandbox environment for development testing."