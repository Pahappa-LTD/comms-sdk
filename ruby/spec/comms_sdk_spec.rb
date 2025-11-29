# frozen_string_literal: true

require "spec_helper"

RSpec.describe CommsSdk do
  it "has a version number" do
    expect(CommsSdk::VERSION).not_to be nil
  end

  describe '.authenticate' do
    it 'provides convenience method to access V1 SDK' do
      allow(CommsSdk::V1::CommsSDK).to receive(:authenticate)
        .with("username", "api_key")
        .and_return("mock_sdk")
      
      result = CommsSdk.authenticate("username", "api_key")
      expect(result).to eq("mock_sdk")
    end
  end
end
