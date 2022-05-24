package com.andrejmilanovic.asteroidradar.api

import android.annotation.SuppressLint
import android.os.Build
import com.andrejmilanovic.asteroidradar.Constants
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

fun parseAsteroidsJsonResult(jsonResult: JSONObject): ArrayList<NetworkAsteroid> {
    val nearEarthObjectsJson = jsonResult.getJSONObject("near_earth_objects")

    val asteroidList = ArrayList<NetworkAsteroid>()

    val nextSevenDaysFormattedDates = getNextSevenDaysFormattedDates()
    for (formattedDate in nextSevenDaysFormattedDates) {
        val dateAsteroidJsonArray = nearEarthObjectsJson.getJSONArray(formattedDate)

        for (i in 0 until dateAsteroidJsonArray.length()) {
            val asteroidJson = dateAsteroidJsonArray.getJSONObject(i)
            val id = asteroidJson.getLong("id")
            val codename = asteroidJson.getString("name")
            val absoluteMagnitude = asteroidJson.getDouble("absolute_magnitude_h")
            val estimatedDiameter = asteroidJson.getJSONObject("estimated_diameter")
                .getJSONObject("kilometers").getDouble("estimated_diameter_max")

            val closeApproachData = asteroidJson
                .getJSONArray("close_approach_data").getJSONObject(0)
            val relativeVelocity = closeApproachData.getJSONObject("relative_velocity")
                .getDouble("kilometers_per_second")
            val distanceFromEarth = closeApproachData.getJSONObject("miss_distance")
                .getDouble("astronomical")
            val isPotentiallyHazardous = asteroidJson
                .getBoolean("is_potentially_hazardous_asteroid")

            val asteroid = NetworkAsteroid(
                id, codename, formattedDate, absoluteMagnitude,
                estimatedDiameter, relativeVelocity, distanceFromEarth, isPotentiallyHazardous
            )
            asteroidList.add(asteroid)
        }
    }

    return asteroidList
}

private fun getNextSevenDaysFormattedDates(): ArrayList<String> {
    val formattedDateList = ArrayList<String>()

    val calendar = Calendar.getInstance()
    for (i in 0..Constants.DEFAULT_END_DATE_DAYS) {
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        formattedDateList.add(dateFormat.format(currentTime))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return formattedDateList
}

fun getCurrentDate(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(Constants.API_QUERY_DATE_FORMAT)
        current.format(formatter)
    } else {
        SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault()).format(Date())
            .toString()
    }
}

// Function that gives a date that is 7 days after start/current date
@SuppressLint("SimpleDateFormat")
fun getEndDate(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val endDate = LocalDateTime.now().plusDays(Constants.DEFAULT_END_DATE_DAYS.toLong())
        val formatter = DateTimeFormatter.ofPattern(Constants.API_QUERY_DATE_FORMAT)
        endDate.format(formatter)
    } else {
        val currentDate = getCurrentDate()
        val sdf = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT)
        val calendar = Calendar.getInstance()
        sdf.parse(currentDate)?.let { calendar.time = it }
        calendar.add(Calendar.DATE, Constants.DEFAULT_END_DATE_DAYS) // Number of days to add
        sdf.format(calendar.time) // Return end date
    }
}