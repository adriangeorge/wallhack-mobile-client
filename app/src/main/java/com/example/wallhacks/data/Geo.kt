package com.example.wallhacks.data

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val METERS_PER_DEG_LAT = 111_320.0

/** Point `distanceM` away from the origin along compass `bearingDeg` (flat-earth approximation, fine for < 1 km). */
fun offsetLatLon(lat: Double, lon: Double, bearingDeg: Double, distanceM: Double): Pair<Double, Double> {
    val b = Math.toRadians(bearingDeg)
    val north = distanceM * cos(b)
    val east = distanceM * sin(b)
    return Pair(
        lat + north / METERS_PER_DEG_LAT,
        lon + east / (METERS_PER_DEG_LAT * cos(Math.toRadians(lat))),
    )
}

/** East/north offset in meters of (lat, lon) relative to (originLat, originLon). */
fun toLocalMeters(originLat: Double, originLon: Double, lat: Double, lon: Double): Pair<Double, Double> {
    val north = (lat - originLat) * METERS_PER_DEG_LAT
    val east = (lon - originLon) * METERS_PER_DEG_LAT * cos(Math.toRadians(originLat))
    return Pair(east, north)
}

fun distanceM(east: Double, north: Double): Double = sqrt(east * east + north * north)
