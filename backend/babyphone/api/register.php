<?php
/**
 * BabaPhone Device Registration API
 * 
 * Endpoint for registering devices (parent or child) with the signaling server
 * 
 * POST /api/register.php
 * {
 *   "device_id": "unique-device-id",
 *   "device_type": "parent|child",
 *   "device_name": "Device Name"
 * }
 */

require_once '../config/config.php';
require_once '../config/database.php';

$db = new Database();

// Get request method
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    // Register a new device
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        http_response_code(400);
        echo json_encode(['error' => 'Invalid JSON input']);
        exit();
    }
    
    // Validate required fields
    $requiredFields = ['device_id', 'device_type', 'device_name'];
    foreach ($requiredFields as $field) {
        if (!isset($input[$field]) || empty($input[$field])) {
            http_response_code(400);
            echo json_encode(['error' => "Missing required field: $field"]);
            exit();
        }
    }
    
    // Validate device type
    if (!in_array($input['device_type'], ['parent', 'child'])) {
        http_response_code(400);
        echo json_encode(['error' => 'device_type must be either "parent" or "child"']);
        exit();
    }
    
    // Get client IP address
    $ipAddress = $_SERVER['REMOTE_ADDR'];
    if (isset($_SERVER['HTTP_X_FORWARDED_FOR'])) {
        $ipAddress = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR'])[0];
    }
    
    // Register the device
    $device = $db->registerDevice(
        $input['device_id'],
        $input['device_type'],
        $input['device_name'],
        $ipAddress
    );
    
    http_response_code(201);
    echo json_encode([
        'status' => 'success',
        'message' => 'Device registered successfully',
        'device' => $device
    ]);
    
} elseif ($method === 'PUT') {
    // Update device (heartbeat)
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input || !isset($input['device_id'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing device_id']);
        exit();
    }
    
    $device = $db->updateDeviceLastSeen($input['device_id']);
    
    if (!$device) {
        http_response_code(404);
        echo json_encode(['error' => 'Device not found']);
        exit();
    }
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'message' => 'Device updated',
        'device' => $device
    ]);
    
} elseif ($method === 'DELETE') {
    // Unregister a device
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input || !isset($input['device_id'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing device_id']);
        exit();
    }
    
    $result = $db->removeDevice($input['device_id']);
    
    if (!$result) {
        http_response_code(404);
        echo json_encode(['error' => 'Device not found or already removed']);
        exit();
    }
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'message' => 'Device unregistered successfully'
    ]);
    
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
