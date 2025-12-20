#!/bin/bash
rm -f *.gem
gem build comms_sdk.gemspec
GEM_HOST_API_KEY=rubygems_key gem push ./comms_sdk-*.gem