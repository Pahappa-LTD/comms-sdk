# frozen_string_literal: true

require 'json'

module CommsSdk
  module V1
    class ApiResponseCode
      OK = "OK"
      FAILED = "Failed"

      def self.from_json(json_string)
        case json_string.downcase
        when "ok"
          OK
        when "failed"
          FAILED
        else
          raise ArgumentError, "Unknown value: #{json_string}"
        end
      end
    end

    class MessagePriority
      attr_reader :value

      def initialize(value)
        @value = value
      end

      def self.from_value(text)
        case text
        when "0"
          HIGHEST
        when "1"
          HIGH
        when "2"
          MEDIUM
        when "3"
          LOW
        when "4"
          LOWEST
        else
          raise ArgumentError, "Unknown priority value: #{text}"
        end
      end

      HIGHEST = new("0")
      HIGH = new("1")
      MEDIUM = new("2")
      LOW = new("3")
      LOWEST = new("4")
    end

    class UserData
      attr_reader :username, :apikey

      def initialize(username, apikey)
        @username = username
        @apikey = apikey
      end

      def to_hash
        {
          "username" => @username,
          "password" => @apikey  # Maps to "password" in JSON
        }
      end

      def to_json(*args)
        to_hash.to_json(*args)
      end
    end

    class MessageModel
      attr_reader :number, :message, :senderid, :priority

      def initialize(number:, message:, senderid:, priority:)
        @number = number
        @message = message
        @senderid = senderid
        @priority = priority
      end

      def to_hash
        {
          "number" => @number,
          "message" => @message,
          "senderid" => @senderid,
          "priority" => @priority
        }
      end

      def to_json(*args)
        to_hash.to_json(*args)
      end
    end

    class ApiRequest
      attr_reader :method, :userdata, :msgdata

      def initialize(method:, userdata:, msgdata: nil)
        @method = method
        @userdata = userdata
        @msgdata = msgdata
      end

      def to_hash
        hash = {
          "method" => @method,
          "userdata" => @userdata.to_hash
        }
        hash["msgdata"] = @msgdata.map(&:to_hash) if @msgdata
        hash
      end

      def to_json(*args)
        to_hash.to_json(*args)
      end
    end

    class ApiResponse
      attr_reader :status, :message, :cost, :currency, :msg_follow_up_unique_code, :balance

      def initialize(status:, message: nil, cost: nil, currency: nil, msg_follow_up_unique_code: nil, balance: nil)
        @status = status
        @message = message
        @cost = cost
        @currency = currency
        @msg_follow_up_unique_code = msg_follow_up_unique_code
        @balance = balance
      end

      def self.from_hash(hash)
        new(
          status: hash["Status"],
          message: hash["Message"],
          cost: hash["Cost"],
          currency: hash["Currency"],
          msg_follow_up_unique_code: hash["MsgFollowUpUniqueCode"],
          balance: hash["Balance"]
        )
      end

      def to_hash
        {
          "Status" => @status,
          "Message" => @message,
          "Cost" => @cost,
          "Currency" => @currency,
          "MsgFollowUpUniqueCode" => @msg_follow_up_unique_code,
          "Balance" => @balance
        }.compact
      end

      def to_json(*args)
        to_hash.to_json(*args)
      end
    end
  end
end