package com.andrejmilanovic.asteroidradar.api

import com.andrejmilanovic.asteroidradar.PictureOfDay
import com.andrejmilanovic.asteroidradar.database.DatabaseAsteroid
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkAsteroid(
    val id: Long, val codename: String, val closeApproachDate: String,
    val absoluteMagnitude: Double, val estimatedDiameter: Double,
    val relativeVelocity: Double, val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean
)

/**
 * Convert Network results to database objects
 */
fun List<NetworkAsteroid>.asDatabaseModel(): Array<DatabaseAsteroid> {
    return this.map {
        DatabaseAsteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }.toTypedArray()
}

data class NetworkPicture(
    @Json(name = "media_type") val mediaType: String,
    val title: String,
    val url: String
)

fun NetworkPicture.asDomainModel(): PictureOfDay {
    return PictureOfDay(
        mediaType = this.mediaType,
        title = this.title,
        url = this.url
    )
}