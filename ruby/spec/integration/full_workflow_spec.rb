# frozen_string_literal: true

require 'spec_helper'
require 'webmock/rspec'

RSpec.describe 'Full SDK Workflow Integration Test' do
  let(:username) { "agabu-idaniel" }
  let(:api_key) { "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99" }
  let(:phone_number) { "+256772123456" }
  let(:message) { "Hello from Ruby SDK!" }

  before do
    WebMock.disable_net_connect!(allow_localhost: true)
    CommsSdk::V1::CommsSDK.use_sandbox
  end

  after do
    WebMock.reset!
  end

  it 'completes full SMS sending workflow' do
    # Mock authentication
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(
        body: hash_including("method" => "Balance"),
        headers: { 'Content-Type' => 'application/json' }
      )
      .to_return(
        status: 200,
        body: {
          "Status" => "OK",
          "Message" => "Success",
          "Balance" => "100.50"
        }.to_json,
        headers: { 'Content-Type' => 'application/json' }
      )

    # Mock SMS sending
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(
        body: hash_including("method" => "SendSms"),
        headers: { 'Content-Type' => 'application/json' }
      )
      .to_return(
        status: 200,
        body: {
          "Status" => "OK",
          "Message" => "SMS sent successfully",
          "MsgFollowUpUniqueCode" => "ABC123",
          "Cost" => 50,
          "Currency" => "UGX"
        }.to_json,
        headers: { 'Content-Type' => 'application/json' }
      )

    # Step 1: Authenticate
    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    expect(sdk).to be_a(CommsSdk::V1::CommsSDK)
    expect(sdk.is_authenticated).to be true

    # Step 2: Set custom sender ID
    sdk.with_sender_id("RubySDK")
    expect(sdk.sender_id).to eq("RubySDK")

    # Step 3: Check initial balance
    initial_balance = sdk.get_balance
    expect(initial_balance).to eq(100.50)

    # Step 4: Send SMS
    success = sdk.send_sms(phone_number, message)
    expect(success).to be true

    # Step 5: Get full SMS response
    response = sdk.query_send_sms([phone_number], message, "RubySDK", CommsSdk::V1::MessagePriority::HIGHEST)
    expect(response).to be_a(CommsSdk::V1::ApiResponse)
    expect(response.status).to eq("OK")
    expect(response.msg_follow_up_unique_code).to eq("ABC123")

    # Step 6: Verify all requests were made
    expect(WebMock).to have_requested(:post, CommsSdk::V1::CommsSDK::API_URL).times(4) # 2 for auths, 2 for failed auth retry
  end

  it 'handles multiple numbers workflow' do
    numbers = ["+256772123456", "0772123457", "256772123458"]
    
    # Mock authentication
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(body: hash_including("method" => "Balance"))
      .to_return(
        status: 200,
        body: { "Status" => "OK", "Balance" => "200.00" }.to_json
      )

    # Mock SMS sending
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(body: hash_including("method" => "SendSms"))
      .to_return(
        status: 200,
        body: {
          "Status" => "OK",
          "Message" => "Bulk SMS sent successfully",
          "MsgFollowUpUniqueCode" => "BULK123"
        }.to_json
      )

    # Authenticate and send bulk SMS
    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    success = sdk.send_sms(numbers, "Bulk message from Ruby SDK!")
    
    expect(success).to be true
  end

  it 'handles error scenarios gracefully' do
    # Mock failed authentication
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(body: hash_including("method" => "Balance"))
      .to_return(
        status: 200,
        body: { "Status" => "Failed", "Message" => "Invalid credentials" }.to_json
      )

    expect {
      CommsSdk::V1::CommsSDK.authenticate("invalid", "credentials")
    }.to raise_error(RuntimeError, /Credentials validation failed/)
  end

  it 'validates phone numbers correctly' do
    # Mock authentication
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(body: hash_including("method" => "Balance"))
      .to_return(
        status: 200,
        body: { "Status" => "OK", "Balance" => "100.00" }.to_json
      )

    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)

    # Test with invalid numbers (should not make API call)
    expect {
      sdk.send_sms(["123", "invalid"], "Test message")
    }.not_to raise_error

    # Verify no SMS API call was made (only auth call)
    expect(WebMock).to have_requested(:post, CommsSdk::V1::CommsSDK::API_URL).once
  end

  it 'demonstrates method chaining' do
    # Mock authentication
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(body: hash_including("method" => "Balance"))
      .to_return(
        status: 200,
        body: { "Status" => "OK", "Balance" => "100.00" }.to_json
      )

    # Mock SMS sending
    stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
      .with(body: hash_including("method" => "SendSms"))
      .to_return(
        status: 200,
        body: { "Status" => "OK", "MsgFollowUpUniqueCode" => "CHAIN123" }.to_json
      )

    # Demonstrate method chaining
    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    success = sdk.with_sender_id("ChainTest").send_sms(phone_number, "Chained call!")
    
    expect(success).to be true
    expect(sdk.sender_id).to eq("ChainTest")
  end
end