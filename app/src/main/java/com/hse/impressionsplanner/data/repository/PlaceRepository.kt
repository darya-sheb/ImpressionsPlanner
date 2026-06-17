package com.hse.impressionsplanner.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hse.impressionsplanner.data.Place
import com.hse.impressionsplanner.data.Route
import com.hse.impressionsplanner.data.samplePlaces
import com.hse.impressionsplanner.data.sampleRoutes
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class PlaceRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getPlaces(): List<Place> {
        return try {
            withTimeout(10_000L) {
                val snapshot = db.collection("places").get().await()
                val docs = snapshot.documents.mapNotNull { doc ->
                    Place(
                        id          = doc.id,
                        name        = doc.getString("name") ?: return@mapNotNull null,
                        description = doc.getString("description") ?: "",
                        category    = doc.getString("category") ?: "",
                        imageUrl    = doc.getString("imageUrl") ?: "",
                        address     = doc.getString("address") ?: "",
                        lat         = doc.getDouble("lat") ?: 0.0,
                        lon         = doc.getDouble("lon") ?: 0.0
                    )
                }

                val needsReseed = docs.size < samplePlaces.size
                    || docs.any { it.imageUrl.isBlank() || it.lat == 0.0 }
                if (needsReseed) {
                    seedPlaces()
                    samplePlaces
                } else {
                    docs
                }
            }
        } catch (e: Exception) {
            samplePlaces
        }
    }

    suspend fun getRoutes(): List<Route> {
        return try {
            withTimeout(10_000L) {
                val snapshot = db.collection("ready_routes").get().await()
                val docs = snapshot.documents.mapNotNull { doc ->
                    Route(
                        id          = doc.id,
                        name        = doc.getString("name") ?: return@mapNotNull null,
                        description = doc.getString("description") ?: "",
                        imageUrl    = doc.getString("imageUrl") ?: "",
                        duration    = doc.getString("duration") ?: "",
                        placeCount  = (doc.getLong("placeCount") ?: 0).toInt(),
                        sourceUrl   = doc.getString("sourceUrl") ?: "",
                        categories  = (doc.get("categories") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                }
                val needsReseed = docs.size < sampleRoutes.size
                    || docs.any { it.imageUrl.isBlank() }
                if (needsReseed) {
                    seedRoutes()
                    sampleRoutes
                } else {
                    docs
                }
            }
        } catch (e: Exception) {
            sampleRoutes
        }
    }

    private suspend fun seedPlaces() {
        samplePlaces.forEach { place ->
            db.collection("places").document(place.id).set(
                mapOf(
                    "name"        to place.name,
                    "description" to place.description,
                    "category"    to place.category,
                    "imageUrl"    to place.imageUrl,
                    "address"     to place.address,
                    "lat"         to place.lat,
                    "lon"         to place.lon
                )
            ).await()
        }
    }

    private suspend fun seedRoutes() {
        sampleRoutes.forEach { route ->
            db.collection("ready_routes").document(route.id).set(
                mapOf(
                    "name"        to route.name,
                    "description" to route.description,
                    "imageUrl"    to route.imageUrl,
                    "duration"    to route.duration,
                    "placeCount"  to route.placeCount,
                    "sourceUrl"   to route.sourceUrl,
                    "categories"  to route.categories
                )
            ).await()
        }
    }
}
