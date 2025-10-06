# frozen_string_literal: true

require_relative "comms_sdk/version"
require_relative "comms_sdk/v1/comms_sdk"

module CommsSdk
  class Error < StandardError; end
  
  # Convenience method to access the V1 SDK
  def self.authenticate(user_name, api_key)
    V1::CommsSDK.authenticate(user_name, api_key)
  end
end