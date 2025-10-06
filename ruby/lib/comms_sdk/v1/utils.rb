# frozen_string_literal: true

require 'net/http'
require 'json'
require 'uri'
require 'set'

module CommsSdk
  module V1
    module Utils
      class NumberValidator
        REGEX = /^\+?(0|\d{3})\d{9}$/

        def self.validate_numbers(numbers)
          return [] if numbers.nil? || numbers.empty?

          cleansed = Set.new
          numbers.each do |number|
            next if number.nil? || number.strip.empty?

            cleaned_number = number.strip.gsub(/[-\s]/, '')
            if cleaned_number.match?(REGEX)
              if cleaned_number.start_with?('0')
                cleaned_number = '256' + cleaned_number[1..]
              elsif cleaned_number.start_with?('+')
                cleaned_number = cleaned_number[1..]
              end
              cleansed.add(cleaned_number)
            else
              puts "Number (#{number}) is not valid!"
            end
          end

          cleansed.to_a
        end
      end

      class Validator
        def self.validate_credentials(sdk)
          if sdk.api_key.nil? || sdk.api_key.empty? || sdk.user_name.nil? || sdk.user_name.empty?
            raise ArgumentError, "Either API Key or Username must be provided"
          end

          unless valid_credential?(sdk)
            puts <<~ASCII
                                                                    _                    
                /\\     _|_ |_   _  ._ _|_ o  _  _. _|_ o  _  ._    |_ _. o |  _   _| | | 
               /--\\ |_| |_ | | (/_ | | |_ | (_ (_|  |_ | (_) | |   | (_| | | (/_ (_| o o 
                                                                                         
            ASCII
            puts
            raise "Credentials validation failed"
          end

          puts "Credentials validated successfully."
          puts "Validated using basic auth"
          sdk.set_authenticated
          true
        end

        private

        def self.valid_credential?(sdk)
          api_request = ApiRequest.new(
            method: "Balance",
            userdata: UserData.new(sdk.user_name, sdk.api_key)
          )

          begin
            uri = URI(CommsSdk::V1::CommsSDK::API_URL)
            http = Net::HTTP.new(uri.host, uri.port)
            http.use_ssl = uri.scheme == 'https'
            
            request = Net::HTTP::Post.new(uri)
            request['Content-Type'] = 'application/json'
            request.body = api_request.to_json
            
            response = http.request(request)
            api_response = ApiResponse.from_hash(JSON.parse(response.body))

            if api_response.status == ApiResponseCode::OK
              puts "Credentials validated successfully."
              true
            else
              puts "Error validating credentials: #{api_response.message}"
              false
            end
          rescue => e
            puts "Error validating credentials: #{e.message}"
            false
          end
        end
      end
    end
  end
end