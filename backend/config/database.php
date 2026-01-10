<?php
/**
 * BabaPhone Database Handler
 * 
 * Simple file-based storage for device registration and session management
 * Can be replaced with a proper database for production use
 */

class Database {
    private $dataDir;
    
    public function __construct() {
        $this->dataDir = __DIR__ . '/../data';
        if (!file_exists($this->dataDir)) {
            mkdir($this->dataDir, 0750, true);
        }
    }
    
    /**
     * Register a device
     */
    public function registerDevice($deviceId, $deviceType, $deviceName, $ipAddress) {
        $device = [
            'device_id' => $deviceId,
            'device_type' => $deviceType, // 'parent' or 'child'
            'device_name' => $deviceName,
            'ip_address' => $ipAddress,
            'registered_at' => time(),
            'last_seen' => time()
        ];
        
        $this->saveData('devices/' . $deviceId . '.json', $device);
        return $device;
    }
    
    /**
     * Get device information
     */
    public function getDevice($deviceId) {
        return $this->loadData('devices/' . $deviceId . '.json');
    }
    
    /**
     * Update device last seen timestamp
     */
    public function updateDeviceLastSeen($deviceId) {
        $device = $this->getDevice($deviceId);
        if ($device) {
            $device['last_seen'] = time();
            $this->saveData('devices/' . $deviceId . '.json', $device);
            return $device;
        }
        return null;
    }
    
    /**
     * Get all active devices
     */
    public function getActiveDevices($timeout = DEVICE_TIMEOUT) {
        $devicesDir = $this->dataDir . '/devices';
        if (!file_exists($devicesDir)) {
            return [];
        }
        
        $devices = [];
        $files = glob($devicesDir . '/*.json');
        $currentTime = time();
        
        foreach ($files as $file) {
            $device = $this->loadData('devices/' . basename($file));
            if ($device && ($currentTime - $device['last_seen']) < $timeout) {
                $devices[] = $device;
            }
        }
        
        return $devices;
    }
    
    /**
     * Get devices by type
     */
    public function getDevicesByType($deviceType, $timeout = DEVICE_TIMEOUT) {
        $allDevices = $this->getActiveDevices($timeout);
        return array_filter($allDevices, function($device) use ($deviceType) {
            return $device['device_type'] === $deviceType;
        });
    }
    
    /**
     * Remove device
     */
    public function removeDevice($deviceId) {
        $filePath = $this->dataDir . '/devices/' . $deviceId . '.json';
        if (file_exists($filePath)) {
            return unlink($filePath);
        }
        return false;
    }
    
    /**
     * Create a pairing between parent and child devices
     */
    public function createPairing($parentId, $childId) {
        $pairing = [
            'parent_id' => $parentId,
            'child_id' => $childId,
            'created_at' => time(),
            'status' => 'active'
        ];
        
        $pairingId = md5($parentId . $childId . time());
        $this->saveData('pairings/' . $pairingId . '.json', $pairing);
        return $pairing;
    }
    
    /**
     * Get pairing for a device
     */
    public function getPairingForDevice($deviceId) {
        $pairingsDir = $this->dataDir . '/pairings';
        if (!file_exists($pairingsDir)) {
            return null;
        }
        
        $files = glob($pairingsDir . '/*.json');
        foreach ($files as $file) {
            $pairing = $this->loadData('pairings/' . basename($file));
            if ($pairing && ($pairing['parent_id'] === $deviceId || $pairing['child_id'] === $deviceId)) {
                return $pairing;
            }
        }
        
        return null;
    }
    
    /**
     * Save data to file
     */
    private function saveData($relativePath, $data) {
        $filePath = $this->dataDir . '/' . $relativePath;
        $dir = dirname($filePath);
        
        if (!file_exists($dir)) {
            mkdir($dir, 0750, true);
        }
        
        return file_put_contents($filePath, json_encode($data, JSON_PRETTY_PRINT));
    }
    
    /**
     * Load data from file
     */
    private function loadData($relativePath) {
        $filePath = $this->dataDir . '/' . $relativePath;
        
        if (!file_exists($filePath)) {
            return null;
        }
        
        $content = file_get_contents($filePath);
        return json_decode($content, true);
    }
    
    /**
     * Clean up old devices
     */
    public function cleanupOldDevices($timeout = DEVICE_TIMEOUT) {
        $devicesDir = $this->dataDir . '/devices';
        if (!file_exists($devicesDir)) {
            return 0;
        }
        
        $files = glob($devicesDir . '/*.json');
        $currentTime = time();
        $removed = 0;
        
        foreach ($files as $file) {
            $device = $this->loadData('devices/' . basename($file));
            if ($device && ($currentTime - $device['last_seen']) > $timeout) {
                unlink($file);
                $removed++;
            }
        }
        
        return $removed;
    }
    
    /**
     * Save a signal for delivery
     */
    public function saveSignal($signalId, $signal) {
        $signal['id'] = $signalId;
        return $this->saveData('signals/' . $signalId . '.json', $signal);
    }
    
    /**
     * Get pending signals for a device
     */
    public function getSignalsForDevice($deviceId) {
        $signalsDir = $this->dataDir . '/signals';
        if (!file_exists($signalsDir)) {
            return [];
        }
        
        $signals = [];
        $files = glob($signalsDir . '/*.json');
        
        foreach ($files as $file) {
            $signal = $this->loadData('signals/' . basename($file));
            if ($signal && $signal['to_device_id'] === $deviceId && !$signal['delivered']) {
                $signals[] = $signal;
            }
        }
        
        return $signals;
    }
    
    /**
     * Mark signal as delivered
     */
    public function markSignalDelivered($signalId) {
        $signal = $this->loadData('signals/' . $signalId . '.json');
        if ($signal) {
            $signal['delivered'] = true;
            $signal['delivered_at'] = time();
            return $this->saveData('signals/' . $signalId . '.json', $signal);
        }
        return false;
    }
    
    /**
     * Clean up old signals
     */
    public function cleanupOldSignals($timeout = 600) {
        $signalsDir = $this->dataDir . '/signals';
        if (!file_exists($signalsDir)) {
            return 0;
        }
        
        $files = glob($signalsDir . '/*.json');
        $currentTime = time();
        $removed = 0;
        
        foreach ($files as $file) {
            $signal = $this->loadData('signals/' . basename($file));
            if ($signal && ($currentTime - $signal['timestamp']) > $timeout) {
                unlink($file);
                $removed++;
            }
        }
        
        return $removed;
    }
    
    /**
     * Save audio packet for relay
     */
    public function saveAudioPacket($packetId, $packet) {
        $packet['id'] = $packetId;
        return $this->saveData('audio/' . $packetId . '.json', $packet);
    }
    
    /**
     * Get audio packets for a device
     */
    public function getAudioPacketsForDevice($deviceId) {
        $audioDir = $this->dataDir . '/audio';
        if (!file_exists($audioDir)) {
            return [];
        }
        
        $packets = [];
        $files = glob($audioDir . '/*.json');
        
        foreach ($files as $file) {
            $packet = $this->loadData('audio/' . basename($file));
            if ($packet && $packet['to_device_id'] === $deviceId && !$packet['delivered']) {
                $packets[] = $packet;
            }
        }
        
        return $packets;
    }
    
    /**
     * Mark audio packet as delivered
     */
    public function markAudioPacketDelivered($packetId) {
        $packet = $this->loadData('audio/' . $packetId . '.json');
        if ($packet) {
            $packet['delivered'] = true;
            $packet['delivered_at'] = microtime(true);
            return $this->saveData('audio/' . $packetId . '.json', $packet);
        }
        return false;
    }
    
    /**
     * Clean up old audio packets (should be aggressive - only keep recent ones)
     */
    public function cleanupOldAudioPackets($timeout = 30) {
        $audioDir = $this->dataDir . '/audio';
        if (!file_exists($audioDir)) {
            return 0;
        }
        
        $files = glob($audioDir . '/*.json');
        $currentTime = microtime(true);
        $removed = 0;
        
        foreach ($files as $file) {
            $packet = $this->loadData('audio/' . basename($file));
            if ($packet && ($currentTime - $packet['timestamp']) > $timeout) {
                unlink($file);
                $removed++;
            }
        }
        
        return $removed;
    }
}
?>
