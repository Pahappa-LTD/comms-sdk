# frozen_string_literal: true

require 'spec_helper'
require 'webmock/rspec'

# Hits the real sandbox API (https://comms-test.pahappa.net) end-to-end, to catch
# real API drift that a fully-mocked spec can't (e.g. the walletType regression
# fixed elsewhere in this SDK). Skipped unless COMMS_SANDBOX_USERNAME and
# COMMS_SANDBOX_API_KEY are set in the environment, so it never fails (or runs)
# without sandbox credentials, and no real credential ever needs to live in source.
RSpec.describe 'CommsSdk live sandbox smoke test' do
  username = ENV['COMMS_SANDBOX_USERNAME']
  api_key = ENV['COMMS_SANDBOX_API_KEY']

  before do
    WebMock.allow_net_connect!
    CommsSdk::V1::CommsSDK.use_sandbox
  end

  after do
    WebMock.disable_net_connect!(allow_localhost: true)
  end

  def skip_unless_credentials!
    if username.nil? || username.empty? || api_key.nil? || api_key.empty?
      skip 'Set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run this test'
    end
  end

  it 'rejects wrong credentials against the sandbox' do
    # Always runs (costs nothing) - never a real credential, and use_sandbox is
    # already forced in the before block above so this can never hit production.
    # CommsSDK.authenticate raises rather than returning an unauthenticated
    # instance - matches the existing assertion in full_workflow_spec.rb.
    expect {
      CommsSdk::V1::CommsSDK.authenticate('invalid-user', 'invalid-key-00000000000000000000000000000000')
    }.to raise_error(RuntimeError, /Credentials validation failed/)
  end

  it 'authenticates and sends an SMS to a single number against the real sandbox' do
    skip_unless_credentials!

    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    expect(sdk.is_authenticated).to be true

    result = sdk.send_sms('256700000000', 'Test message from Ruby SDK live sandbox spec')
    expect(result).to be true
  end

  it 'sends an SMS to multiple numbers against the real sandbox' do
    skip_unless_credentials!

    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    expect(sdk.is_authenticated).to be true

    numbers = ['256700000000', '256700000001', '256700000002']
    result = sdk.send_sms(numbers, 'Bulk test message from Ruby SDK live sandbox spec')
    expect(result).to be true
  end

  it 'rejects a send with more than 1000 numbers without crashing' do
    skip_unless_credentials!

    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    expect(sdk.is_authenticated).to be true

    numbers = (0..1000).map { |i| format('256700%06d', i) }
    expect(numbers.length).to eq(1001)

    result = nil
    expect { result = sdk.query_send_sms(numbers, 'Oversized batch test from Ruby SDK live sandbox spec') }.not_to raise_error
    expect(result).not_to be_nil
    expect(result.status).to eq(CommsSdk::V1::ApiResponseCode::FAILED)
  end

  it 'queries the real sandbox balance' do
    skip_unless_credentials!

    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    expect(sdk.is_authenticated).to be true

    balance = sdk.get_balance
    expect(balance).to be_a(Numeric)
    expect(balance).to be >= 0

    response = sdk.query_balance
    expect(response.status).to eq(CommsSdk::V1::ApiResponseCode::OK)
  end
end
