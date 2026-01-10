<?php
/**
 * BabaPhone Device Discovery API
 * 
 * Endpoint for discovering available devices
 * 
 * GET /api/discover.php?device_type=child
 * Returns list of available child or parent devices
 */

require_once '../config/config.php';
require_once '../config/database.php';

$db = new Database();

// Get request method
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    // Get query parameters
    $deviceType = isset($_GET['device_type']) ? $_GET['device_type'] : null;
    
    // Validate device type if provided
    if ($deviceType && !in_array($deviceType, ['parent', 'child'])) {
        http_response_code(400);
        echo json_encode(['error' => 'device_type must be either "parent" or "child"']);
        exit();
    }
    
    // Clean up old devices first
    $db->cleanupOldDevices();
    
    // Get devices
    if ($deviceType) {
        $devices = $db->getDevicesByType($deviceType);
    } else {
        $devices = $db->getActiveDevices();
    }
    
    http_response_code(200);
    echo json_encode([
        'status' => 'success',
        'count' => count($devices),
        'devices' => array_values($devices)
    ]);
    
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
