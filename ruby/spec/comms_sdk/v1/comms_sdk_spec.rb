# frozen_string_literal: true

require 'spec_helper'
require 'webmock/rspec'

RSpec.describe CommsSdk::V1::CommsSDK do
  let(:test_username) { "agabu-idaniel" }
  let(:test_api_key) { "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99" }
  let(:test_phone) { "+256772123456" }
  let(:test_message) { "Test message" }
  
  # Mock successful authentication response
  let(:auth_success_response) do
    {
      "Status" => "OK",
      "Message" => "Success",
      "Balance" => "100.50"
    }
  end
  
  # Mock successful SMS response
  let(:sms_success_response) do
    {
      "Status" => "OK",
      "Message" => "SMS sent successfully",
      "MsgFollowUpUniqueCode" => "ABC123",
      "Cost" => 50,
      "Currency" => "UGX"
    }
  end
  
  # Mock failed response
  let(:failed_response) do
    {
      "Status" => "Failed",
      "Message" => "Invalid credentials"
    }
  end

  before do
    WebMock.disable_net_connect!(allow_localhost: true)
    # Use sandbox for all tests
    described_class.use_sandbox
  end

  after do
    WebMock.reset!
  end

  describe '.authenticate' do
    context 'with valid credentials' do
      before do
        stub_request(:post, described_class::API_URL)
          .with(
            body: hash_including("method" => "Balance"),
            headers: { 'Content-Type' => 'application/json' }
          )
          .to_return(
            status: 200,
            body: auth_success_response.to_json,
            headers: { 'Content-Type' => 'application/json' }
          )
      end

      it 'returns an authenticated SDK instance' do
        sdk = described_class.authenticate(test_username, test_api_key)
        
        expect(sdk).to be_a(described_class)
        expect(sdk.user_name).to eq(test_username)
        expect(sdk.api_key).to eq(test_api_key)
        expect(sdk.is_authenticated).to be true
      end
    end

    context 'with invalid credentials' do
      before do
        stub_request(:post, described_class::API_URL)
          .with(
            body: hash_including("method" => "Balance"),
            headers: { 'Content-Type' => 'application/json' }
          )
          .to_return(
            status: 200,
            body: failed_response.to_json,
            headers: { 'Content-Type' => 'application/json' }
          )
      end

      it 'raises an error for invalid credentials' do
        expect {
          described_class.authenticate("invalid_user", "invalid_key")
        }.to raise_error(RuntimeError, /Credentials validation failed/)
      end
    end

    context 'with empty credentials' do
      it 'raises ArgumentError for empty username' do
        expect {
          described_class.authenticate("", test_api_key)
        }.to raise_error(ArgumentError, /Either API Key or Username must be provided/)
      end

      it 'raises ArgumentError for empty API key' do
        expect {
          described_class.authenticate(test_username, "")
        }.to raise_error(ArgumentError, /Either API Key or Username must be provided/)
      end
    end
  end

  describe '#send_sms' do
    let(:sdk) { create_authenticated_sdk }

    context 'with single number' do
      before do
        stub_sms_request(sms_success_response)
      end

      it 'sends SMS successfully' do
        result = sdk.send_sms(test_phone, test_message)
        expect(result).to be true
      end

      it 'handles string number input' do
        result = sdk.send_sms(test_phone, test_message)
        expect(result).to be true
      end
    end

    context 'with multiple numbers' do
      let(:numbers) { ["+256772123456", "0772123457"] }
      
      before do
        stub_sms_request(sms_success_response)
      end

      it 'sends SMS to multiple numbers successfully' do
        result = sdk.send_sms(numbers, test_message)
        expect(result).to be true
      end
    end

    context 'with custom sender ID' do
      before do
        stub_sms_request(sms_success_response)
      end

      it 'sends SMS with custom sender ID' do
        result = sdk.send_sms(test_phone, test_message, sender_id: "MySenderID")
        expect(result).to be true
      end
    end

    context 'with custom priority' do
      before do
        stub_sms_request(sms_success_response)
      end

      it 'sends SMS with custom priority' do
        result = sdk.send_sms(
          test_phone, 
          test_message, 
          priority: CommsSdk::V1::MessagePriority::LOW
        )
        expect(result).to be true
      end
    end

    context 'with invalid input' do
      it 'raises error for empty numbers' do
        expect {
          sdk.send_sms([], test_message)
        }.to raise_error(ArgumentError, /Numbers list cannot be empty/)
      end

      it 'raises error for empty message' do
        expect {
          sdk.send_sms(test_phone, "")
        }.to raise_error(ArgumentError, /Message cannot be empty/)
      end

      it 'raises error for single character message' do
        expect {
          sdk.send_sms(test_phone, "A")
        }.to raise_error(ArgumentError, /Message cannot be a single character/)
      end
    end

    context 'with short/invalid number' do
      before do
        # Mock the request but expect it to fail validation before making the request
        allow(CommsSdk::V1::Utils::NumberValidator).to receive(:validate_numbers).and_return([])
      end

      it 'returns nil for invalid numbers' do
        result = sdk.query_send_sms(["123"], test_message, "EgoSMS", CommsSdk::V1::MessagePriority::HIGHEST)
        expect(result).to be_nil
      end
    end

    context 'when API returns failure' do
      before do
        stub_sms_request(failed_response)
      end

      it 'returns false for failed SMS' do
        result = sdk.send_sms(test_phone, test_message)
        expect(result).to be false
      end
    end
  end

  describe '#query_send_sms' do
    let(:sdk) { create_authenticated_sdk }

    before do
      stub_sms_request(sms_success_response)
    end

    it 'returns full API response' do
      response = sdk.query_send_sms(
        [test_phone], 
        test_message, 
        "EgoSMS", 
        CommsSdk::V1::MessagePriority::HIGHEST
      )
      
      expect(response).to be_a(CommsSdk::V1::ApiResponse)
      expect(response.status).to eq("OK")
      expect(response.msg_follow_up_unique_code).to eq("ABC123")
    end
  end

  describe '#get_balance' do
    let(:sdk) { create_authenticated_sdk }

    before do
      stub_request(:post, described_class::API_URL)
        .with(
          body: hash_including("method" => "Balance"),
          headers: { 'Content-Type' => 'application/json' }
        )
        .to_return(
          status: 200,
          body: auth_success_response.to_json,
          headers: { 'Content-Type' => 'application/json' }
        )
    end

    it 'returns balance as float' do
      balance = sdk.get_balance
      expect(balance).to eq(100.50)
    end

    it 'returns nil for invalid balance' do
      stub_request(:post, described_class::API_URL)
        .with(
          body: hash_including("method" => "Balance"),
          headers: { 'Content-Type' => 'application/json' }
        )
        .to_return(
          status: 200,
          body: { "Status" => "OK", "Balance" => nil }.to_json,
          headers: { 'Content-Type' => 'application/json' }
        )

      balance = sdk.get_balance
      expect(balance).to be_nil
    end
  end

  describe '#query_balance' do
    let(:sdk) { create_authenticated_sdk }

    before do
      stub_request(:post, described_class::API_URL)
        .with(
          body: hash_including("method" => "Balance"),
          headers: { 'Content-Type' => 'application/json' }
        )
        .to_return(
          status: 200,
          body: auth_success_response.to_json,
          headers: { 'Content-Type' => 'application/json' }
        )
    end

    it 'returns full API response' do
      response = sdk.query_balance
      
      expect(response).to be_a(CommsSdk::V1::ApiResponse)
      expect(response.status).to eq("OK")
      expect(response.balance).to eq("100.50")
    end
  end

  describe '#with_sender_id' do
    let(:sdk) { create_authenticated_sdk }

    it 'sets sender ID and returns self' do
      result = sdk.with_sender_id("CustomSender")
      
      expect(result).to eq(sdk)
      expect(sdk.sender_id).to eq("CustomSender")
    end

    it 'allows method chaining' do
      expect(sdk.with_sender_id("Test").sender_id).to eq("Test")
    end
  end

  describe '#to_s' do
    let(:sdk) { create_authenticated_sdk }

    it 'returns formatted string representation' do
      expect(sdk.to_s).to eq("SDK(#{test_username} => #{test_api_key})")
    end
  end

  describe 'balance check after sending SMS' do
    let(:sdk) { create_authenticated_sdk }
    let(:balance_before) { 100.50 }
    let(:balance_after) { 99.50 }

    it 'shows decreased balance after sending SMS' do
      # Mock initial balance check
      stub_request(:post, described_class::API_URL)
        .with(
          body: hash_including("method" => "Balance"),
          headers: { 'Content-Type' => 'application/json' }
        )
        .to_return(
          status: 200,
          body: { "Status" => "OK", "Balance" => balance_before.to_s }.to_json,
          headers: { 'Content-Type' => 'application/json' }
        ).then
        .to_return(
          status: 200,
          body: { "Status" => "OK", "Balance" => balance_after.to_s }.to_json,
          headers: { 'Content-Type' => 'application/json' }
        )

      # Mock SMS sending
      stub_sms_request(sms_success_response)

      initial_balance = sdk.get_balance
      sdk.send_sms(test_phone, test_message)
      final_balance = sdk.get_balance

      expect(final_balance).to be < initial_balance
    end
  end

  private

  def create_authenticated_sdk
    sdk = described_class.new
    sdk.instance_variable_set(:@user_name, test_username)
    sdk.instance_variable_set(:@api_key, test_api_key)
    sdk.instance_variable_set(:@is_authenticated, true)
    sdk
  end

  def stub_sms_request(response_body)
    stub_request(:post, described_class::API_URL)
      .with(
        body: hash_including("method" => "SendSms"),
        headers: { 'Content-Type' => 'application/json' }
      )
      .to_return(
        status: 200,
        body: response_body.to_json,
        headers: { 'Content-Type' => 'application/json' }
      )
  end
end