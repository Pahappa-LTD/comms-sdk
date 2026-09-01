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

  it 'authenticates and sends an SMS against the real sandbox' do
    if username.nil? || username.empty? || api_key.nil? || api_key.empty?
      skip 'Set COMMS_SANDBOX_USERNAME and COMMS_SANDBOX_API_KEY to run this test'
    end

    sdk = CommsSdk::V1::CommsSDK.authenticate(username, api_key)
    expect(sdk.is_authenticated).to be true

    result = sdk.send_sms('256700000000', 'Test message from Ruby SDK live sandbox spec')
    expect(result).to be true
  end
end
