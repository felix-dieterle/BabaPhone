package com.example.babaphone

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Location/GPS Mock Test
 * 
 * This test demonstrates how to mock GPS/location data for testing location-based features.
 * While the current BabaPhone app doesn't use location tracking for monitoring,
 * this infrastructure is ready for future features like:
 * - Geofencing (alert if baby/device moves outside range)
 * - Location-based device pairing
 * - Distance-based audio quality adjustment
 */
@RunWith(AndroidJUnit4::class)
class LocationMockTest {
    
    private lateinit var locationManager: LocationManager
    
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    @Test
    fun mockLocation_CanCreateMockGPSLocation() {
        // Create a mock location
        val mockLocation = createMockLocation(
            latitude = 52.5200,  // Berlin coordinates
            longitude = 13.4050,
            altitude = 34.0,     // 34 meters above sea level
            provider = LocationManager.GPS_PROVIDER
        )
        
        // Verify the mock location has the correct values
        assertEquals(52.5200, mockLocation.latitude, 0.0001)
        assertEquals(13.4050, mockLocation.longitude, 0.0001)
        assertEquals(34.0, mockLocation.altitude, 0.1)
        assertEquals(LocationManager.GPS_PROVIDER, mockLocation.provider)
        assertTrue(mockLocation.hasAltitude())
    }
    
    @Test
    fun mockLocation_CanSimulateMovement() {
        // Simulate movement in 3D space (latitude, longitude, altitude)
        val positions = listOf(
            // Starting position (ground floor)
            Triple(52.5200, 13.4050, 0.0),
            // Moving up (1st floor)
            Triple(52.5200, 13.4050, 3.0),
            // Moving up (2nd floor)
            Triple(52.5200, 13.4050, 6.0),
            // Moving up (3rd floor)
            Triple(52.5200, 13.4050, 9.0),
            // Moving horizontally and up (different room, 3rd floor)
            Triple(52.5201, 13.4051, 9.0),
        )
        
        val locations = positions.map { (lat, lon, alt) ->
            createMockLocation(lat, lon, alt, LocationManager.GPS_PROVIDER)
        }
        
        // Verify we can track vertical movement (height changes)
        assertEquals(0.0, locations[0].altitude, 0.1)
        assertEquals(3.0, locations[1].altitude, 0.1)
        assertEquals(6.0, locations[2].altitude, 0.1)
        assertEquals(9.0, locations[3].altitude, 0.1)
        
        // Verify horizontal movement - at least one coordinate should change
        assertNotEquals(locations[3].latitude, locations[4].latitude)
    }
    
    @Test
    fun mockLocation_CanCalculateDistanceBetweenFloors() {
        // Create locations for different floors in a building
        val groundFloor = createMockLocation(52.5200, 13.4050, 0.0, LocationManager.GPS_PROVIDER)
        val thirdFloor = createMockLocation(52.5200, 13.4050, 9.0, LocationManager.GPS_PROVIDER)
        
        // Calculate vertical distance
        val verticalDistance = thirdFloor.altitude - groundFloor.altitude
        
        // A typical floor is about 3 meters high, so 3 floors = 9 meters
        assertEquals(9.0, verticalDistance, 0.1)
    }
    
    @Test
    fun mockLocation_SupportsNetworkProvider() {
        // Network provider can also provide altitude/height data
        val networkLocation = createMockLocation(
            latitude = 52.5200,
            longitude = 13.4050,
            altitude = 15.0,
            provider = LocationManager.NETWORK_PROVIDER
        )
        
        assertEquals(LocationManager.NETWORK_PROVIDER, networkLocation.provider)
        assertTrue(networkLocation.hasAltitude())
        assertEquals(15.0, networkLocation.altitude, 0.1)
    }
    
    @Test
    fun mockLocation_CanSimulate3DMovementPath() {
        // Simulate a realistic 3D movement path through a building
        // This could represent moving a baby monitor device through different rooms and floors
        val path = listOf(
            // Ground floor bedroom
            createMockLocation(52.5200, 13.4050, 0.0, LocationManager.GPS_PROVIDER),
            // Ground floor hallway
            createMockLocation(52.5201, 13.4050, 0.0, LocationManager.GPS_PROVIDER),
            // Stairs to 1st floor
            createMockLocation(52.5201, 13.4051, 1.5, LocationManager.GPS_PROVIDER),
            // 1st floor bedroom
            createMockLocation(52.5200, 13.4051, 3.0, LocationManager.GPS_PROVIDER),
            // 1st floor another room
            createMockLocation(52.5199, 13.4051, 3.0, LocationManager.GPS_PROVIDER),
        )
        
        // Verify the path makes sense
        assertEquals(5, path.size)
        
        // Verify altitude increases as we go upstairs
        assertTrue(path[2].altitude > path[1].altitude)
        assertTrue(path[3].altitude > path[2].altitude)
        
        // Verify horizontal movement on the same floor
        assertEquals(path[3].altitude, path[4].altitude, 0.1)
        assertNotEquals(path[3].latitude, path[4].latitude)
    }
    
    /**
     * Helper function to create a mock location
     * This can be used in future location-based features
     */
    private fun createMockLocation(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        provider: String
    ): Location {
        return Location(provider).apply {
            this.latitude = latitude
            this.longitude = longitude
            this.altitude = altitude
            this.time = System.currentTimeMillis()
            this.accuracy = 10.0f // 10 meter accuracy
            this.elapsedRealtimeNanos = android.os.SystemClock.elapsedRealtimeNanos()
        }
    }
}
