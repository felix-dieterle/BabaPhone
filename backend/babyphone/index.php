<?php
/**
 * BabaPhone Backend - API Index
 * 
 * Welcome page and API documentation
 */

require_once 'config/config.php';

?>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BabaPhone Backend API</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            max-width: 900px;
            margin: 50px auto;
            padding: 20px;
            line-height: 1.6;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            border-bottom: 3px solid #4CAF50;
            padding-bottom: 10px;
        }
        h2 {
            color: #555;
            margin-top: 30px;
        }
        .endpoint {
            background: #f9f9f9;
            padding: 15px;
            margin: 10px 0;
            border-left: 4px solid #4CAF50;
            border-radius: 4px;
        }
        .method {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 3px;
            font-weight: bold;
            margin-right: 10px;
        }
        .post { background: #4CAF50; color: white; }
        .get { background: #2196F3; color: white; }
        .put { background: #FF9800; color: white; }
        .delete { background: #f44336; color: white; }
        code {
            background: #e0e0e0;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
        }
        pre {
            background: #263238;
            color: #aed581;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
        }
        .status {
            padding: 10px;
            background: #e8f5e9;
            border-left: 4px solid #4CAF50;
            margin: 20px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📱 BabaPhone Backend API</h1>
        
        <div class="status">
            <strong>Status:</strong> Server is running ✅<br>
            <strong>Version:</strong> 1.0.0<br>
            <strong>Server Time:</strong> <?php echo date('Y-m-d H:i:s'); ?>
        </div>

        <h2>API Endpoints</h2>

        <div class="endpoint">
            <span class="method post">POST</span>
            <strong>/api/register.php</strong><br>
            <p>Register a device (parent or child) with the server.</p>
            <pre>{
  "device_id": "unique-device-id",
  "device_type": "parent|child",
  "device_name": "Device Name"
}</pre>
        </div>

        <div class="endpoint">
            <span class="method put">PUT</span>
            <strong>/api/register.php</strong><br>
            <p>Update device heartbeat (keep-alive).</p>
            <pre>{
  "device_id": "unique-device-id"
}</pre>
        </div>

        <div class="endpoint">
            <span class="method delete">DELETE</span>
            <strong>/api/register.php</strong><br>
            <p>Unregister a device.</p>
            <pre>{
  "device_id": "unique-device-id"
}</pre>
        </div>

        <div class="endpoint">
            <span class="method get">GET</span>
            <strong>/api/discover.php?device_type=child</strong><br>
            <p>Discover available devices. Optional parameter: <code>device_type</code> (parent|child)</p>
        </div>

        <div class="endpoint">
            <span class="method post">POST</span>
            <strong>/api/signal.php</strong><br>
            <p>Send signaling data between devices (connection establishment).</p>
            <pre>{
  "from_device_id": "sender-id",
  "to_device_id": "receiver-id",
  "signal_type": "connect|disconnect|offer|answer",
  "data": {...}
}</pre>
        </div>

        <div class="endpoint">
            <span class="method get">GET</span>
            <strong>/api/signal.php?device_id=your-device-id</strong><br>
            <p>Retrieve pending signals for your device.</p>
        </div>

        <div class="endpoint">
            <span class="method post">POST</span>
            <strong>/api/relay.php</strong><br>
            <p>Relay audio data (fallback when P2P connection fails).</p>
            <pre>{
  "from_device_id": "sender-id",
  "to_device_id": "receiver-id",
  "audio_data": "base64-encoded-audio"
}</pre>
        </div>

        <div class="endpoint">
            <span class="method get">GET</span>
            <strong>/api/relay.php?device_id=your-device-id</strong><br>
            <p>Retrieve pending audio packets for your device.</p>
        </div>

        <h2>Usage Flow</h2>
        <ol>
            <li><strong>Register Device:</strong> Both parent and child devices register with the server</li>
            <li><strong>Discovery:</strong> Parent device discovers available child devices</li>
            <li><strong>Signaling:</strong> Devices exchange connection information</li>
            <li><strong>Connection:</strong> Devices attempt direct P2P connection</li>
            <li><strong>Fallback Relay:</strong> If P2P fails, audio is relayed through the server</li>
            <li><strong>Heartbeat:</strong> Devices send periodic heartbeats to stay registered</li>
        </ol>

        <h2>Security Notes</h2>
        <ul>
            <li>Always use HTTPS in production</li>
            <li>Implement proper authentication</li>
            <li>Audio data is not stored permanently</li>
            <li>Old data is automatically cleaned up</li>
        </ul>

        <h2>Configuration</h2>
        <p>Edit <code>config/config.php</code> to configure:</p>
        <ul>
            <li>Server ports</li>
            <li>Timeout values</li>
            <li>CORS settings</li>
            <li>Security options</li>
        </ul>
    </div>
</body>
</html>
