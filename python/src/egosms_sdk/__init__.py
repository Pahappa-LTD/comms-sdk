"""
EgoSMS Python SDK

A Python SDK for integrating with the EgoSMS API.
"""

__version__ = "0.1.0"
__author__ = "Pahappa Limited"
__email__ = "systems@pahappa.com"

from .v1 import EgoSmsSDK, MessagePriority

__all__ = ['EgoSmsSDK', 'MessagePriority']
