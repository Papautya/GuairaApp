package io.devexpert.android_firebase.utils

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import org.json.JSONObject

data class WeatherData(val temperature: Double, val humidity: Double)

class WeatherManager(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    suspend fun fetchWeather(coords: Coordinates): WeatherData? {
        val url = "https://api.openweathermap.org/data/2.5/weather" +
                "?lat=${coords.latitude}&lon=${coords.longitude}" +
                "&appid=$apiKey&units=metric&lang=es"
        return try {
            val resp: String = client.get(url).body()
            val main = JSONObject(resp).getJSONObject("main")
            WeatherData(
                temperature = main.getDouble("temp"),
                humidity    = main.getDouble("humidity")
            )
        } catch (e: Exception) {
            Log.e("WeatherManager", "Error fetching weather", e)
            null
        }
    }
}
