package com.example.wallhacks.data

import kotlinx.coroutines.delay
import java.io.File

/** What the real backend will do: take a picture + EXIF, return the camera pose and nearby objects of interest. */
interface WallhackerServer {
    suspend fun localize(photo: File, exif: PhotoExif): LocalizationResult
}

/** Mock backend: ignores the picture and returns hardcoded data after a fake network delay. */
class FakeWallhackerServer : WallhackerServer {

    override suspend fun localize(photo: File, exif: PhotoExif): LocalizationResult {
        delay(1_500)

        val pose = CameraPose(
            latitude = BASE_LAT,
            longitude = BASE_LON,
            altitudeM = 85.0,
            headingDeg = 45.0,
            pitchDeg = 0.0,
            horizontalFovDeg = 66.0,
        )

        // Objects are placed by bearing/distance so "in field of view" is consistent with the pose above
        // (FOV covers heading 45 +/- 33 deg = 12..78 deg).
        fun poi(id: String, label: String, bearing: Double, dist: Double, height: Double, inFov: Boolean): PointOfInterest {
            val (lat, lon) = offsetLatLon(pose.latitude, pose.longitude, bearing, dist)
            return PointOfInterest(id, label, lat, lon, height, inFov)
        }

        return LocalizationResult(
            pose = pose,
            objects = listOf(
                poi("poi-1", "Vehicle", bearing = 30.0, dist = 60.0, height = 1.8, inFov = true),
                poi("poi-2", "Person", bearing = 68.0, dist = 110.0, height = 1.7, inFov = true),
                poi("poi-3", "Antenna mast", bearing = 200.0, dist = 90.0, height = 12.0, inFov = false),
            ),
        )
    }

    companion object {
        // Roughly UPB campus, Bucharest.
        const val BASE_LAT = 44.4355
        const val BASE_LON = 26.0510

        /** Stand-in for reading the EXIF of the captured JPEG. */
        fun mockExif() = PhotoExif(
            latitude = BASE_LAT + 0.00002,
            longitude = BASE_LON - 0.00002,
            altitudeM = 84.0,
            timestampMs = System.currentTimeMillis(),
            focalLengthMm = 4.2f,
        )
    }
}
