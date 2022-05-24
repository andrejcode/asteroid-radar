package com.andrejmilanovic.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.andrejmilanovic.asteroidradar.Asteroid
import com.andrejmilanovic.asteroidradar.BuildConfig
import com.andrejmilanovic.asteroidradar.api.*
import com.andrejmilanovic.asteroidradar.database.AsteroidsDatabase
import com.andrejmilanovic.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AsteroidRepository(private val database: AsteroidsDatabase) {
    /**
     * A list of asteroids that can be shown on the screen.
     */
    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroidsSortByDate()) {
            it.asDomainModel()
        }

    /**
     * Get asteroids from API and store them in database
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     */
    suspend fun getAsteroids() {
        withContext(Dispatchers.IO) {
            val asteroidResponse = AsteroidApi.retrofitService.getAsteroids(
                getCurrentDate(),
                getEndDate(),
                BuildConfig.NASA_API_KEY
            )
            val parsedAsteroids = parseAsteroidsJsonResult(JSONObject(asteroidResponse))
            val networkAsteroid = parsedAsteroids.map {
                NetworkAsteroid(
                    id = it.id,
                    codename = it.codename,
                    closeApproachDate = it.closeApproachDate,
                    absoluteMagnitude = it.absoluteMagnitude,
                    estimatedDiameter = it.estimatedDiameter,
                    relativeVelocity = it.relativeVelocity,
                    distanceFromEarth = it.distanceFromEarth,
                    isPotentiallyHazardous = it.isPotentiallyHazardous
                )
            }
            database.asteroidDao.insertAll(*networkAsteroid.asDatabaseModel())
        }
    }

    /**
     * Delete asteroids from database
     */
    suspend fun deleteAsteroids() {
        withContext(Dispatchers.IO) {
            database.asteroidDao.deleteAsteroids(getCurrentDate())
        }
    }
}