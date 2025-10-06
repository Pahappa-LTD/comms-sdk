# frozen_string_literal: true

require 'net/http'
require 'json'
require 'uri'
require_relative 'models'
require_relative 'utils'

module CommsSdk
  module V1
    class CommsSDK
      API_URL = "http://176.58.101.43:8080/communications/api/v1/json/"

      attr_reader :api_key, :user_name, :sender_id, :is_authenticated

      def initialize
        @api_key = nil
        @user_name = nil
        @sender_id = "EgoSMS"
        @is_authenticated = false
      end

      def self.authenticate(user_name, api_key)
        sdk = new
        sdk.instance_variable_set(:@user_name, user_name)
        sdk.instance_variable_set(:@api_key, api_key)
        Utils::Validator.validate_credentials(sdk)
        sdk
      end

      def self.use_sandbox
        remove_const(:API_URL) if const_defined?(:API_URL)
        const_set(:API_URL, "http://176.58.101.43:8080/communications/api/v1/json")
      end

      def self.use_live_server
        remove_const(:API_URL) if const_defined?(:API_URL)
        const_set(:API_URL, "http://176.58.101.43:8080/communications/api/v1/json")
      end

      def set_authenticated
        @is_authenticated = true
      end

      def with_sender_id(sender_id)
        @sender_id = sender_id
        self
      end

      def send_sms(numbers, message, sender_id: nil, priority: MessagePriority::HIGHEST)
        numbers = [numbers] if numbers.is_a?(String)
        
        api_response = query_send_sms(numbers, message, sender_id || @sender_id, priority)
        
        if api_response.nil?
          puts "Failed to get a response from the server."
          return false
        end
        
        case api_response.status
        when ApiResponseCode::OK
          puts "SMS sent successfully."
          puts "MessageFollowUpUniqueCode: #{api_response.msg_follow_up_unique_code}" if api_response.msg_follow_up_unique_code
          true
        when ApiResponseCode::FAILED
          puts "Failed: #{api_response.message}"
          false
        else
          raise "Unexpected response status: #{api_response.status}"
        end
      end

      def query_send_sms(numbers, message, sender_id, priority)
        return nil if sdk_not_authenticated?
        
        raise ArgumentError, "Numbers list cannot be empty" if numbers.nil? || numbers.empty?
        raise ArgumentError, "Message cannot be empty" if message.nil? || message.empty?
        raise ArgumentError, "Message cannot be a single character" if message.length == 1
        
        sender_id = @sender_id if sender_id.nil? || sender_id.strip.empty?
        
        if sender_id.length > 11
          puts "Warning: Sender ID length exceeds 11 characters. Some networks may truncate or reject messages."
        end
        
        numbers = Utils::NumberValidator.validate_numbers(numbers)
        if numbers.empty?
          $stderr.puts "No valid phone numbers provided. Please check inputs."
          return nil
        end
        
        message_models = numbers.map do |num|
          MessageModel.new(number: num, message: message, senderid: sender_id, priority: priority.value)
        end
        
        api_request = ApiRequest.new(
          method: "SendSms",
          userdata: UserData.new(@user_name, @api_key),
          msgdata: message_models
        )
        
        begin
          response = make_http_request(api_request)
          ApiResponse.from_hash(JSON.parse(response.body))
        rescue => e
          $stderr.puts "Failed to send SMS: #{e.message}"
          begin
            $stderr.puts "Request: #{api_request.to_hash}"
          rescue
            # Ignore serialization errors
          end
          nil
        end
      end

      def query_balance
        return nil if sdk_not_authenticated?
        
        api_request = ApiRequest.new(
          method: "Balance",
          userdata: UserData.new(@user_name, @api_key)
        )
        
        begin
          response = make_http_request(api_request)
          ApiResponse.from_hash(JSON.parse(response.body))
        rescue => e
          raise "Failed to get balance: #{e.message}"
        end
      end

      def get_balance
        response = query_balance
        return nil if response.nil? || response.balance.nil?
        
        begin
          Float(response.balance)
        rescue ArgumentError
          nil
        end
      end

      def to_s
        "SDK(#{@user_name} => #{@api_key})"
      end

      private

      def sdk_not_authenticated?
        unless @is_authenticated
          $stderr.puts "SDK is not authenticated. Please authenticate before performing actions."
          $stderr.puts "Attempting to re-authenticate with provided credentials..."
          return !Utils::Validator.validate_credentials(self)
        end
        false
      end

      def make_http_request(api_request)
        uri = URI(API_URL)
        http = Net::HTTP.new(uri.host, uri.port)
        http.use_ssl = uri.scheme == 'https'
        
        request = Net::HTTP::Post.new(uri)
        request['Content-Type'] = 'application/json'
        request.body = api_request.to_json
        
        http.request(request)
      end
    end
  end
end