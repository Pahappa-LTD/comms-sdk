# frozen_string_literal: true

require 'spec_helper'
require 'webmock/rspec'

RSpec.describe CommsSdk::V1::Utils::Validator do
  let(:mock_sdk) do
    double('CommsSdk',
      user_name: 'agabu-idaniel',
      api_key: 'dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99',
      set_authenticated: nil
    )
  end

  let(:success_response) do
    {
      "Status" => "OK",
      "Message" => "Success",
      "Balance" => "100.50"
    }
  end

  let(:failed_response) do
    {
      "Status" => "Failed",
      "Message" => "Invalid credentials"
    }
  end

  before do
    WebMock.disable_net_connect!(allow_localhost: true)
  end

  after do
    WebMock.reset!
  end

  describe '.validate_credentials' do
    context 'with valid credentials' do
      before do
        stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
          .with(
            body: hash_including("method" => "Balance"),
            headers: { 'Content-Type' => 'application/json' }
          )
          .to_return(
            status: 200,
            body: success_response.to_json,
            headers: { 'Content-Type' => 'application/json' }
          )
      end

      it 'returns true for valid credentials' do
        expect(described_class.validate_credentials(mock_sdk)).to be true
      end

      it 'calls set_authenticated on the SDK' do
        expect(mock_sdk).to receive(:set_authenticated)
        described_class.validate_credentials(mock_sdk)
      end

      it 'prints success messages' do
        expect { described_class.validate_credentials(mock_sdk) }
          .to output(/Credentials validated successfully.*Validated using basic auth/m).to_stdout
      end
    end

    context 'with invalid credentials' do
      before do
        stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
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

      it 'raises error for invalid credentials' do
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(RuntimeError, /Credentials validation failed/)
      end

      it 'prints ASCII art failure message' do
        expect {
          begin
            described_class.validate_credentials(mock_sdk)
          rescue RuntimeError
            # Ignore the error for this test
          end
        }.to output(/\/\\\s+_\|_/).to_stdout
      end
    end

    context 'with network errors' do
      before do
        stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
          .to_raise(StandardError.new("Network error"))
      end

      it 'raises error for network failures' do
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(RuntimeError, /Credentials validation failed/)
      end
    end

    context 'with missing credentials' do
      it 'raises ArgumentError for nil API key' do
        allow(mock_sdk).to receive(:api_key).and_return(nil)
        
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(ArgumentError, /Either API Key or Username must be provided/)
      end

      it 'raises ArgumentError for empty API key' do
        allow(mock_sdk).to receive(:api_key).and_return("")
        
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(ArgumentError, /Either API Key or Username must be provided/)
      end

      it 'raises ArgumentError for nil username' do
        allow(mock_sdk).to receive(:user_name).and_return(nil)
        
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(ArgumentError, /Either API Key or Username must be provided/)
      end

      it 'raises ArgumentError for empty username' do
        allow(mock_sdk).to receive(:user_name).and_return("")
        
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(ArgumentError, /Either API Key or Username must be provided/)
      end
    end

    context 'with malformed JSON response' do
      before do
        stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
          .to_return(
            status: 200,
            body: "invalid json",
            headers: { 'Content-Type' => 'application/json' }
          )
      end

      it 'raises error for malformed response' do
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(RuntimeError, /Credentials validation failed/)
      end
    end

    context 'with HTTP error status' do
      before do
        stub_request(:post, CommsSdk::V1::CommsSDK::API_URL)
          .to_return(status: 500, body: "Internal Server Error")
      end

      it 'raises error for HTTP errors' do
        expect {
          described_class.validate_credentials(mock_sdk)
        }.to raise_error(RuntimeError, /Credentials validation failed/)
      end
    end
  end
end