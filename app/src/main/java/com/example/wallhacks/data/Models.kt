package com.example.wallhacks.data

/** EXIF metadata sent along with the raw picture (mocked for the prototype). */
data class PhotoExif(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val timestampMs: Long,
    val focalLengthMm: Float,
)

/** Camera pose estimated by the server by matching the picture against the stored scene. */
data class CameraPose(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    /** Compass heading, degrees clockwise from north. */
    val headingDeg: Double,
    val pitchDeg: Double,
    val horizontalFovDeg: Double,
)

/** Object of interest captured earlier by a drone. */
data class PointOfInterest(
    val id: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val heightM: Double,
    val inFieldOfView: Boolean,
)

data class LocalizationResult(
    val pose: CameraPose,
    val objects: List<PointOfInterest>,
)
