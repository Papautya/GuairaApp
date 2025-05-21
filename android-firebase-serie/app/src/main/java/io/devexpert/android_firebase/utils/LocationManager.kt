package io.devexpert.android_firebase.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class Coordinates(val latitude: Double, val longitude: Double)
data class AddressComponents(val city: String?, val adminArea: String?, val country: String?)

class LocationManager(
    private val fusedClient: FusedLocationProviderClient,
    private val context: Context
) {
    @SuppressLint("MissingPermission")
    suspend fun getLastCoordinates(): Coordinates? {
        // Intentamos con lastLocation
        fusedClient.lastLocation
            .addOnSuccessListener { /* nada, usamos await */ }
        val loc = fusedClient.lastLocation.await()
        if (loc != null) return Coordinates(loc.latitude, loc.longitude)
        // Si no hay, forzamos nueva petición
        val req = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(30_000).build()
        val cur = fusedClient.getCurrentLocation(req, null).await()
        return cur?.let { Coordinates(it.latitude, it.longitude) }
    }

    suspend fun reverseGeocode(coords: Coordinates): AddressComponents {
        return try {
            val geo = Geocoder(context, Locale.getDefault())
            val list = geo.getFromLocation(coords.latitude, coords.longitude, 1)
            if (!list.isNullOrEmpty()) {
                val a = list[0]
                AddressComponents(a.locality, a.adminArea, a.countryName)
            } else AddressComponents(null, null, null)
        } catch (e: Exception) {
            AddressComponents(null, null, null)
        }
    }
}
