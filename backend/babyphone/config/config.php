<?php
/**
 * BabaPhone Backend Configuration
 * 
 * This file contains configuration settings for the BabaPhone signaling server
 */

// Database configuration (if needed for persistent storage)
define('DB_HOST', getenv('DB_HOST') ?: 'localhost');
define('DB_NAME', getenv('DB_NAME') ?: 'babaphone');
define('DB_USER', getenv('DB_USER') ?: 'babaphone_user');
define('DB_PASS', getenv('DB_PASS') ?: '');

// Server configuration
define('SERVER_PORT', getenv('SERVER_PORT') ?: 8080);
define('WEBSOCKET_PORT', getenv('WEBSOCKET_PORT') ?: 8081);

// Security
define('REQUIRE_HTTPS', getenv('REQUIRE_HTTPS') ?: false);
define('API_KEY_REQUIRED', getenv('API_KEY_REQUIRED') ?: false);
define('API_KEY', getenv('API_KEY') ?: '');

// Session timeouts (in seconds)
define('DEVICE_TIMEOUT', 300); // 5 minutes
define('CONNECTION_TIMEOUT', 600); // 10 minutes

// CORS settings
define('ALLOWED_ORIGINS', getenv('ALLOWED_ORIGINS') ?: '*');

// Error reporting
error_reporting(E_ALL);
ini_set('display_errors', '1');

// Timezone
date_default_timezone_set('Europe/Berlin');

// Enable CORS for API endpoints
if (strpos($_SERVER['REQUEST_URI'], '/api/') !== false) {
    header('Access-Control-Allow-Origin: ' . ALLOWED_ORIGINS);
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization');
    header('Content-Type: application/json; charset=utf-8');
}

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}
?>
