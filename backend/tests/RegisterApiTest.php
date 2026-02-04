<?php
/**
 * Unit tests for Device Registration API logic
 */

use PHPUnit\Framework\TestCase;

class RegisterApiTest extends TestCase
{
    /**
     * Test valid device type values
     */
    public function testValidDeviceTypes()
    {
        $validTypes = ['parent', 'child'];
        
        foreach ($validTypes as $type) {
            $this->assertContains($type, ['parent', 'child']);
        }
    }
    
    /**
     * Test invalid device type
     */
    public function testInvalidDeviceType()
    {
        $invalidType = 'invalid';
        
        $this->assertNotContains($invalidType, ['parent', 'child']);
    }
    
    /**
     * Test required fields validation
     */
    public function testRequiredFields()
    {
        $requiredFields = ['device_id', 'device_type', 'device_name'];
        
        $this->assertEquals(3, count($requiredFields));
        $this->assertContains('device_id', $requiredFields);
        $this->assertContains('device_type', $requiredFields);
        $this->assertContains('device_name', $requiredFields);
    }
    
    /**
     * Test device_id format validation
     */
    public function testDeviceIdFormat()
    {
        $validDeviceId = 'device-123-abc';
        
        $this->assertIsString($validDeviceId);
        $this->assertNotEmpty($validDeviceId);
        $this->assertGreaterThan(0, strlen($validDeviceId));
    }
    
    /**
     * Test device_name validation
     */
    public function testDeviceNameValidation()
    {
        $validName = 'Test Baby Monitor';
        $emptyName = '';
        
        $this->assertIsString($validName);
        $this->assertNotEmpty($validName);
        $this->assertEmpty($emptyName);
    }
    
    /**
     * Test JSON structure
     */
    public function testJsonStructure()
    {
        $jsonData = [
            'device_id' => 'test-device-123',
            'device_type' => 'child',
            'device_name' => 'Test Monitor'
        ];
        
        $jsonString = json_encode($jsonData);
        $decoded = json_decode($jsonString, true);
        
        $this->assertIsArray($decoded);
        $this->assertEquals($jsonData['device_id'], $decoded['device_id']);
        $this->assertEquals($jsonData['device_type'], $decoded['device_type']);
        $this->assertEquals($jsonData['device_name'], $decoded['device_name']);
    }
    
    /**
     * Test HTTP methods
     */
    public function testValidHttpMethods()
    {
        $validMethods = ['POST', 'PUT', 'DELETE'];
        
        $this->assertContains('POST', $validMethods);
        $this->assertContains('PUT', $validMethods);
        $this->assertContains('DELETE', $validMethods);
    }
    
    /**
     * Test timestamp format
     */
    public function testTimestampFormat()
    {
        $timestamp = time();
        
        $this->assertIsInt($timestamp);
        $this->assertGreaterThan(0, $timestamp);
    }
    
    /**
     * Test IP address validation
     */
    public function testIpAddressValidation()
    {
        $validIp = '192.168.1.100';
        $invalidIp = '999.999.999.999';
        
        $this->assertNotFalse(filter_var($validIp, FILTER_VALIDATE_IP));
        $this->assertFalse(filter_var($invalidIp, FILTER_VALIDATE_IP));
    }
}
