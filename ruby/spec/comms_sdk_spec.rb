# frozen_string_literal: true

require "spec_helper"

RSpec.describe CommsSdk do
  it "has a version number" do
    expect(CommsSdk::VERSION).not_to be nil
  end

  describe '.authenticate' do
    it 'provides convenience method to access V1 SDK' do
      allow(CommsSdk::V1::CommsSDK).to receive(:authenticate)
        .with("agabu-idaniel", "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99")
        .and_return("mock_sdk")
      
      result = CommsSdk.authenticate("agabu-idaniel", "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99")
      expect(result).to eq("mock_sdk")
    end
  end
end
