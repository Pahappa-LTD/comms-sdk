"""
Pahappa Comms platform Python SDK

A Python SDK for integrating with the Comms platform API.
"""

__version__ = "0.1.0"
__author__ = "Pahappa Limited"
__email__ = "systems@pahappa.com"

from .v1 import CommsSDK, MessagePriority

__all__ = ['CommsSDK', 'MessagePriority']
