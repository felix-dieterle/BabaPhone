package de.felixdieterle.babaphone.network

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for DeviceInfo data class
 */
class DeviceInfoTest {
    
    @Test
    fun deviceInfo_construction_withAllParameters() {
        val name = "Test Device"
        val address = "192.168.1.100"
        val port = 8080
        val deviceId = "device-123"
        
        val device = DeviceInfo(name, address, port, deviceId)
        
        assertEquals(name, device.name)
        assertEquals(address, device.address)
        assertEquals(port, device.port)
        assertEquals(deviceId, device.deviceId)
    }
    
    @Test
    fun deviceInfo_construction_withDefaultDeviceId() {
        val name = "Test Device"
        val address = "192.168.1.100"
        val port = 8080
        
        val device = DeviceInfo(name, address, port)
        
        assertEquals(name, device.name)
        assertEquals(address, device.address)
        assertEquals(port, device.port)
        assertEquals("", device.deviceId)
    }
    
    @Test
    fun deviceInfo_equality_sameValues() {
        val device1 = DeviceInfo("Device1", "192.168.1.1", 8080, "id1")
        val device2 = DeviceInfo("Device1", "192.168.1.1", 8080, "id1")
        
        assertEquals(device1, device2)
        assertEquals(device1.hashCode(), device2.hashCode())
    }
    
    @Test
    fun deviceInfo_equality_differentValues() {
        val device1 = DeviceInfo("Device1", "192.168.1.1", 8080, "id1")
        val device2 = DeviceInfo("Device2", "192.168.1.2", 8081, "id2")
        
        assertNotEquals(device1, device2)
    }
    
    @Test
    fun deviceInfo_equality_differentName() {
        val device1 = DeviceInfo("Device1", "192.168.1.1", 8080, "id1")
        val device2 = DeviceInfo("Device2", "192.168.1.1", 8080, "id1")
        
        assertNotEquals(device1, device2)
    }
    
    @Test
    fun deviceInfo_equality_differentAddress() {
        val device1 = DeviceInfo("Device1", "192.168.1.1", 8080, "id1")
        val device2 = DeviceInfo("Device1", "192.168.1.2", 8080, "id1")
        
        assertNotEquals(device1, device2)
    }
    
    @Test
    fun deviceInfo_equality_differentPort() {
        val device1 = DeviceInfo("Device1", "192.168.1.1", 8080, "id1")
        val device2 = DeviceInfo("Device1", "192.168.1.1", 8081, "id1")
        
        assertNotEquals(device1, device2)
    }
    
    @Test
    fun deviceInfo_toString_containsAllFields() {
        val device = DeviceInfo("Test Device", "192.168.1.100", 8080, "device-123")
        val stringRep = device.toString()
        
        assertTrue(stringRep.contains("Test Device"))
        assertTrue(stringRep.contains("192.168.1.100"))
        assertTrue(stringRep.contains("8080"))
        assertTrue(stringRep.contains("device-123"))
    }
    
    @Test
    fun deviceInfo_validIPAddress_formats() {
        // Test various valid IP formats
        val device1 = DeviceInfo("Device", "192.168.1.1", 8080)
        val device2 = DeviceInfo("Device", "10.0.0.1", 8080)
        val device3 = DeviceInfo("Device", "172.16.0.1", 8080)
        
        assertTrue(device1.address.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")))
        assertTrue(device2.address.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")))
        assertTrue(device3.address.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")))
    }
    
    @Test
    fun deviceInfo_validPort_range() {
        // Test common port ranges
        val device1 = DeviceInfo("Device", "192.168.1.1", 80)
        val device2 = DeviceInfo("Device", "192.168.1.1", 8080)
        val device3 = DeviceInfo("Device", "192.168.1.1", 65535)
        
        assertTrue(device1.port in 1..65535)
        assertTrue(device2.port in 1..65535)
        assertTrue(device3.port in 1..65535)
    }
}
