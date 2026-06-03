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
                        id = doc.id,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        address = doc.getString("address") ?: ""
                    )
                }
                if (docs.size < samplePlaces.size) {
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
                        id = doc.id,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        duration = doc.getString("duration") ?: "",
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        placeCount = (doc.getLong("placeCount") ?: 0).toInt()
                    )
                }
                if (docs.size < sampleRoutes.size) {
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
                    "name" to place.name,
                    "description" to place.description,
                    "category" to place.category,
                    "imageUrl" to place.imageUrl,
                    "rating" to place.rating,
                    "address" to place.address
                )
            ).await()
        }
    }

    private suspend fun seedRoutes() {
        sampleRoutes.forEach { route ->
            db.collection("ready_routes").document(route.id).set(
                mapOf(
                    "name" to route.name,
                    "description" to route.description,
                    "imageUrl" to route.imageUrl,
                    "duration" to route.duration,
                    "rating" to route.rating,
                    "placeCount" to route.placeCount
                )
            ).await()
        }
    }
}
