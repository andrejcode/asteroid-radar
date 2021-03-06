package com.andrejmilanovic.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.andrejmilanovic.asteroidradar.Asteroid
import com.andrejmilanovic.asteroidradar.BuildConfig
import com.andrejmilanovic.asteroidradar.PictureOfDay
import com.andrejmilanovic.asteroidradar.api.AsteroidApi
import com.andrejmilanovic.asteroidradar.api.asDomainModel
import com.andrejmilanovic.asteroidradar.database.getDatabase
import com.andrejmilanovic.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import java.lang.Exception

enum class AsteroidApiStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<AsteroidApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    // MutableLiveData to handle navigation to the selected asteroid
    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()
    val navigateToSelectedAsteroid: LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    // List of asteroids that will be shown on screen
    val asteroids = asteroidsRepository.asteroids

    /**
     * Request a snackBar to display a string.
     *
     * This variable is private because we don't want to expose MutableLiveData
     *
     * MutableLiveData allows anyone to set a value, and MainViewModel is the only
     * class that should be setting values.
     */
    private val _snackbar = MutableLiveData<String?>()
    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackbar

    /**
     * Init is called immediately when ViewModel is created
     * Get asteroids and picture of the day
     */
    init {
        viewModelScope.launch {
            try {
                _status.value = AsteroidApiStatus.LOADING
                asteroidsRepository.getAsteroids()
                _pictureOfDay.postValue(
                    AsteroidApi.retrofitService.getPictureOfDay(BuildConfig.NASA_API_KEY)
                        .asDomainModel()
                )
            } catch (e: Exception) {
                _status.value = AsteroidApiStatus.ERROR
                _snackbar.value = e.message
            } finally {
                _status.value = AsteroidApiStatus.DONE
            }
        }
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    /**
     * When asteroid is clicked, set the [_navigateToSelectedAsteroid] to that asteroid
     */
    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    // After the navigation has taken place, make sure navigateToSelectedAsteroid is set to null
    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    /**
     * Factory for constructing MainViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}