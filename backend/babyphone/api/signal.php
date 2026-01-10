<?php
/**
 * BabaPhone Signaling/Relay API
 * 
 * Endpoint for establishing connections between devices and relaying audio data
 * 
 * POST /api/signal.php
 * {
 *   "from_device_id": "sender-device-id",
 *   "to_device_id": "receiver-device-id",
 *   "signal_type": "offer|answer|ice_candidate|connect|disconnect",
 *   "data": {...}
 * }
 */

require_once '../config/config.php';
require_once '../config/database.php';

$db = new Database();

// Get request method
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    // Process signaling message
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        http_response_code(400);
        echo json_encode(['error' => 'Invalid JSON input']);
        exit();
    }
    
    // Validate required fields
    $requiredFields = ['from_device_id', 'to_device_id', 'signal_type'];
    foreach ($requiredFields as $field) {
        if (!isset($input[$field])) {
            http_response_code(400);
            echo json_encode(['error' => "Missing required field: $field"]);
            exit();
        }
    }
    
    // Validate devices exist
    $fromDevice = $db->getDevice($input['from_device_id']);
    $toDevice = $db->getDevice($input['to_device_id']);
    
    if (!$fromDevice || !$toDevice) {
        http_response_code(404);
        echo json_encode(['error' => 'One or both devices not found']);
        exit();
    }
    
    // Store the signal for retrieval by the target device
    $signal = [
        'from_device_id' => $input['from_device_id'],
        'to_device_id' => $input['to_device_id'],
        'signal_type' => $input['signal_type'],
        'data' => isset($input['data']) ? $input['data'] : null,
        'timestamp' => time(),
        'delivered' => false
    ];
    
    $signalId = md5($input['from_device_id'] . $input['to_device_id'] . microtime(true));
    $db->saveSignal($signalId, $signal);
    
    // Handle specific signal types
    switch ($input['signal_type']) {
        case 'connect':
            // Create pairing between devices
            $db->createPairing($input['from_device_id'], $input['to_device_id']);
            break;
            
        case 'disconnect':
            // Could implement pairing removal here if needed
            break;
    }
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'message' => 'Signal queued for delivery',
        'signal_id' => $signalId
    ]);
    
} elseif ($method === 'GET') {
    // Retrieve pending signals for a device
    $deviceId = isset($_GET['device_id']) ? $_GET['device_id'] : null;
    
    if (!$deviceId) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing device_id parameter']);
        exit();
    }
    
    $signals = $db->getSignalsForDevice($deviceId);
    
    // Mark signals as delivered
    foreach ($signals as $signal) {
        $db->markSignalDelivered($signal['id']);
    }
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'count' => count($signals),
        'signals' => $signals
    ]);
    
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
