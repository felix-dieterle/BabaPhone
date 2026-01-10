<?php
/**
 * BabaPhone Backend - Cleanup Script
 * 
 * Run this periodically (e.g., via cron) to clean up old data
 * Example cron: */5 * * * * php /path/to/backend/cleanup.php
 */

require_once 'config/config.php';
require_once 'config/database.php';

$db = new Database();

echo "Running cleanup...\n";

// Clean up old devices (inactive for more than 5 minutes)
$removedDevices = $db->cleanupOldDevices(DEVICE_TIMEOUT);
echo "Removed $removedDevices inactive devices\n";

// Clean up old signals (older than 10 minutes)
$removedSignals = $db->cleanupOldSignals(600);
echo "Removed $removedSignals old signals\n";

// Clean up old audio packets (older than 30 seconds)
$removedPackets = $db->cleanupOldAudioPackets(30);
echo "Removed $removedPackets old audio packets\n";

echo "Cleanup complete.\n";
?>
