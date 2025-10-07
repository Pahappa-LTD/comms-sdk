# frozen_string_literal: true

require 'spec_helper'

RSpec.describe 'CommsSdk::V1 Models' do
  describe CommsSdk::V1::ApiResponseCode do
    describe '.from_json' do
      it 'returns OK for "ok" string' do
        expect(described_class.from_json("ok")).to eq(CommsSdk::V1::ApiResponseCode::OK)
      end

      it 'returns OK for "OK" string' do
        expect(described_class.from_json("OK")).to eq(CommsSdk::V1::ApiResponseCode::OK)
      end

      it 'returns FAILED for "failed" string' do
        expect(described_class.from_json("failed")).to eq(CommsSdk::V1::ApiResponseCode::FAILED)
      end

      it 'returns FAILED for "Failed" string' do
        expect(described_class.from_json("Failed")).to eq(CommsSdk::V1::ApiResponseCode::FAILED)
      end

      it 'raises error for unknown value' do
        expect {
          described_class.from_json("unknown")
        }.to raise_error(ArgumentError, /Unknown value: unknown/)
      end
    end
  end

  describe CommsSdk::V1::MessagePriority do
    describe 'constants' do
      it 'has correct values for all priorities' do
        expect(CommsSdk::V1::MessagePriority::HIGHEST.value).to eq("0")
        expect(CommsSdk::V1::MessagePriority::HIGH.value).to eq("1")
        expect(CommsSdk::V1::MessagePriority::MEDIUM.value).to eq("2")
        expect(CommsSdk::V1::MessagePriority::LOW.value).to eq("3")
        expect(CommsSdk::V1::MessagePriority::LOWEST.value).to eq("4")
      end
    end

    describe '.from_value' do
      it 'returns correct priority for each value' do
        expect(described_class.from_value("0")).to eq(CommsSdk::V1::MessagePriority::HIGHEST)
        expect(described_class.from_value("1")).to eq(CommsSdk::V1::MessagePriority::HIGH)
        expect(described_class.from_value("2")).to eq(CommsSdk::V1::MessagePriority::MEDIUM)
        expect(described_class.from_value("3")).to eq(CommsSdk::V1::MessagePriority::LOW)
        expect(described_class.from_value("4")).to eq(CommsSdk::V1::MessagePriority::LOWEST)
      end

      it 'raises error for unknown value' do
        expect {
          described_class.from_value("5")
        }.to raise_error(ArgumentError, /Unknown priority value: 5/)
      end
    end
  end

  describe CommsSdk::V1::UserData do
    let(:username) { "agabu-idaniel" }
    let(:apikey) { "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99" }
    let(:user_data) { described_class.new(username, apikey) }

    describe '#initialize' do
      it 'sets username and apikey' do
        expect(user_data.username).to eq(username)
        expect(user_data.apikey).to eq(apikey)
      end
    end

    describe '#to_hash' do
      it 'returns hash with correct structure' do
        expected = {
          "username" => username,
          "password" => apikey  # Note: apikey maps to "password" in JSON
        }
        
        expect(user_data.to_hash).to eq(expected)
      end
    end

    describe '#to_json' do
      it 'returns valid JSON string' do
        json_string = user_data.to_json
        parsed = JSON.parse(json_string)
        
        expect(parsed["username"]).to eq(username)
        expect(parsed["password"]).to eq(apikey)
      end
    end
  end

  describe CommsSdk::V1::MessageModel do
    let(:message_model) do
      described_class.new(
        number: "256772123456",
        message: "Test message",
        senderid: "TestSender",
        priority: "0"
      )
    end

    describe '#initialize' do
      it 'sets all attributes correctly' do
        expect(message_model.number).to eq("256772123456")
        expect(message_model.message).to eq("Test message")
        expect(message_model.senderid).to eq("TestSender")
        expect(message_model.priority).to eq("0")
      end
    end

    describe '#to_hash' do
      it 'returns hash with correct structure' do
        expected = {
          "number" => "256772123456",
          "message" => "Test message",
          "senderid" => "TestSender",
          "priority" => "0"
        }
        
        expect(message_model.to_hash).to eq(expected)
      end
    end

    describe '#to_json' do
      it 'returns valid JSON string' do
        json_string = message_model.to_json
        parsed = JSON.parse(json_string)
        
        expect(parsed["number"]).to eq("256772123456")
        expect(parsed["message"]).to eq("Test message")
        expect(parsed["senderid"]).to eq("TestSender")
        expect(parsed["priority"]).to eq("0")
      end
    end
  end

  describe CommsSdk::V1::ApiRequest do
    let(:user_data) { CommsSdk::V1::UserData.new("agabu-idaniel", "dcfa634d7936ec699a3b26f6cd924801b09b285a31949f99") }
    let(:message_model) do
      CommsSdk::V1::MessageModel.new(
        number: "256772123456",
        message: "Test",
        senderid: "Test",
        priority: "0"
      )
    end

    describe '#initialize' do
      context 'without message data' do
        let(:api_request) { described_class.new(method: "Balance", userdata: user_data) }

        it 'sets method and userdata' do
          expect(api_request.method).to eq("Balance")
          expect(api_request.userdata).to eq(user_data)
          expect(api_request.msgdata).to be_nil
        end
      end

      context 'with message data' do
        let(:api_request) do
          described_class.new(
            method: "SendSms",
            userdata: user_data,
            msgdata: [message_model]
          )
        end

        it 'sets all attributes' do
          expect(api_request.method).to eq("SendSms")
          expect(api_request.userdata).to eq(user_data)
          expect(api_request.msgdata).to eq([message_model])
        end
      end
    end

    describe '#to_hash' do
      context 'without message data' do
        let(:api_request) { described_class.new(method: "Balance", userdata: user_data) }

        it 'returns hash without msgdata' do
          hash = api_request.to_hash
          
          expect(hash["method"]).to eq("Balance")
          expect(hash["userdata"]).to eq(user_data.to_hash)
          expect(hash).not_to have_key("msgdata")
        end
      end

      context 'with message data' do
        let(:api_request) do
          described_class.new(
            method: "SendSms",
            userdata: user_data,
            msgdata: [message_model]
          )
        end

        it 'returns hash with msgdata' do
          hash = api_request.to_hash
          
          expect(hash["method"]).to eq("SendSms")
          expect(hash["userdata"]).to eq(user_data.to_hash)
          expect(hash["msgdata"]).to eq([message_model.to_hash])
        end
      end
    end
  end

  describe CommsSdk::V1::ApiResponse do
    describe '#initialize' do
      it 'sets all attributes when provided' do
        response = described_class.new(
          status: "OK",
          message: "Success",
          cost: 50,
          currency: "UGX",
          msg_follow_up_unique_code: "ABC123",
          balance: "100.50"
        )

        expect(response.status).to eq("OK")
        expect(response.message).to eq("Success")
        expect(response.cost).to eq(50)
        expect(response.currency).to eq("UGX")
        expect(response.msg_follow_up_unique_code).to eq("ABC123")
        expect(response.balance).to eq("100.50")
      end

      it 'handles nil values for optional attributes' do
        response = described_class.new(status: "OK")

        expect(response.status).to eq("OK")
        expect(response.message).to be_nil
        expect(response.cost).to be_nil
        expect(response.currency).to be_nil
        expect(response.msg_follow_up_unique_code).to be_nil
        expect(response.balance).to be_nil
      end
    end

    describe '.from_hash' do
      it 'creates instance from hash with all fields' do
        hash = {
          "Status" => "OK",
          "Message" => "Success",
          "Cost" => 50,
          "Currency" => "UGX",
          "MsgFollowUpUniqueCode" => "ABC123",
          "Balance" => "100.50"
        }

        response = described_class.from_hash(hash)

        expect(response.status).to eq("OK")
        expect(response.message).to eq("Success")
        expect(response.cost).to eq(50)
        expect(response.currency).to eq("UGX")
        expect(response.msg_follow_up_unique_code).to eq("ABC123")
        expect(response.balance).to eq("100.50")
      end

      it 'handles hash with missing fields' do
        hash = { "Status" => "OK" }

        response = described_class.from_hash(hash)

        expect(response.status).to eq("OK")
        expect(response.message).to be_nil
      end
    end

    describe '#to_hash' do
      it 'returns hash with all non-nil values' do
        response = described_class.new(
          status: "OK",
          message: "Success",
          cost: 50,
          balance: "100.50"
        )

        hash = response.to_hash

        expect(hash["Status"]).to eq("OK")
        expect(hash["Message"]).to eq("Success")
        expect(hash["Cost"]).to eq(50)
        expect(hash["Balance"]).to eq("100.50")
        expect(hash).not_to have_key("Currency")
        expect(hash).not_to have_key("MsgFollowUpUniqueCode")
      end
    end

    describe '#to_json' do
      it 'returns valid JSON string' do
        response = described_class.new(
          status: "OK",
          message: "Success",
          balance: "100.50"
        )

        json_string = response.to_json
        parsed = JSON.parse(json_string)

        expect(parsed["Status"]).to eq("OK")
        expect(parsed["Message"]).to eq("Success")
        expect(parsed["Balance"]).to eq("100.50")
      end
    end
  end
end