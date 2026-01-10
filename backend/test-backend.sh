#!/bin/bash
# BabaPhone Backend - Test Script
# This script tests the backend API endpoints

BASE_URL="${1:-http://localhost:8080}"

echo "🔍 Testing BabaPhone Backend at $BASE_URL"
echo ""

# Check if server is running
echo "1️⃣ Checking server status..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL)
if [ "$HTTP_CODE" -eq 200 ]; then
    echo "   ✅ Server is running"
else
    echo "   ❌ Server is not responding (HTTP $HTTP_CODE)"
    exit 1
fi
echo ""

# Register child device
echo "2️⃣ Registering child device..."
RESPONSE=$(curl -s -X POST $BASE_URL/api/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "test-child-device",
    "device_type": "child",
    "device_name": "Test Baby Phone"
  }')
echo "   Response: $RESPONSE"
echo ""

# Register parent device
echo "3️⃣ Registering parent device..."
RESPONSE=$(curl -s -X POST $BASE_URL/api/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "test-parent-device",
    "device_type": "parent",
    "device_name": "Test Parent Phone"
  }')
echo "   Response: $RESPONSE"
echo ""

# Discover child devices
echo "4️⃣ Discovering child devices..."
RESPONSE=$(curl -s "$BASE_URL/api/discover.php?device_type=child")
echo "   Response: $RESPONSE"
echo ""

# Send heartbeat
echo "5️⃣ Sending heartbeat..."
RESPONSE=$(curl -s -X PUT $BASE_URL/api/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "test-child-device"
  }')
echo "   Response: $RESPONSE"
echo ""

# Send signal
echo "6️⃣ Sending connection signal..."
RESPONSE=$(curl -s -X POST $BASE_URL/api/signal.php \
  -H "Content-Type: application/json" \
  -d '{
    "from_device_id": "test-parent-device",
    "to_device_id": "test-child-device",
    "signal_type": "connect"
  }')
echo "   Response: $RESPONSE"
echo ""

# Retrieve signals
echo "7️⃣ Retrieving signals for child device..."
RESPONSE=$(curl -s "$BASE_URL/api/signal.php?device_id=test-child-device")
echo "   Response: $RESPONSE"
echo ""

# Cleanup - unregister devices
echo "8️⃣ Cleaning up - unregistering devices..."
curl -s -X DELETE $BASE_URL/api/register.php \
  -H "Content-Type: application/json" \
  -d '{"device_id": "test-child-device"}' > /dev/null
  
curl -s -X DELETE $BASE_URL/api/register.php \
  -H "Content-Type: application/json" \
  -d '{"device_id": "test-parent-device"}' > /dev/null
echo "   ✅ Cleanup complete"
echo ""

echo "✅ All tests completed!"
echo ""
echo "To test with your own server, run:"
echo "  ./test-backend.sh http://your-server.com"
