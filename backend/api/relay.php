<?php
/**
 * BabaPhone Audio Relay API
 * 
 * Endpoint for relaying audio data between devices when direct P2P connection fails
 * This is a fallback mechanism - direct connection is preferred for better performance
 * 
 * POST /api/relay.php
 * {
 *   "from_device_id": "sender-device-id",
 *   "to_device_id": "receiver-device-id",
 *   "audio_data": "base64-encoded-audio-data"
 * }
 * 
 * GET /api/relay.php?device_id=receiver-device-id
 * Returns pending audio data for the device
 */

require_once '../config/config.php';
require_once '../config/database.php';

$db = new Database();

// Get request method
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    // Relay audio data
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        http_response_code(400);
        echo json_encode(['error' => 'Invalid JSON input']);
        exit();
    }
    
    // Validate required fields
    $requiredFields = ['from_device_id', 'to_device_id', 'audio_data'];
    foreach ($requiredFields as $field) {
        if (!isset($input[$field])) {
            http_response_code(400);
            echo json_encode(['error' => "Missing required field: $field"]);
            exit();
        }
    }
    
    // Verify devices are paired
    $fromDevice = $db->getDevice($input['from_device_id']);
    $toDevice = $db->getDevice($input['to_device_id']);
    
    if (!$fromDevice || !$toDevice) {
        http_response_code(404);
        echo json_encode(['error' => 'One or both devices not found']);
        exit();
    }
    
    // Store audio data temporarily for retrieval
    $audioPacket = [
        'from_device_id' => $input['from_device_id'],
        'to_device_id' => $input['to_device_id'],
        'audio_data' => $input['audio_data'],
        'timestamp' => microtime(true),
        'delivered' => false
    ];
    
    $packetId = md5($input['from_device_id'] . microtime(true));
    $db->saveAudioPacket($packetId, $audioPacket);
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'message' => 'Audio data relayed',
        'packet_id' => $packetId
    ]);
    
} elseif ($method === 'GET') {
    // Retrieve pending audio data for a device
    $deviceId = isset($_GET['device_id']) ? $_GET['device_id'] : null;
    
    if (!$deviceId) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing device_id parameter']);
        exit();
    }
    
    $packets = $db->getAudioPacketsForDevice($deviceId);
    
    // Mark packets as delivered and schedule for cleanup
    foreach ($packets as &$packet) {
        $db->markAudioPacketDelivered($packet['id']);
    }
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'count' => count($packets),
        'packets' => $packets
    ]);
    
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
