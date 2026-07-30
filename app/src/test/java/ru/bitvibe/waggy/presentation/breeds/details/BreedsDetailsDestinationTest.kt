package ru.bitvibe.waggy.presentation.breeds.details

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BreedsDetailsDestinationTest {
    @Test
    fun recommendedBreed_roundTripsRecommendationReason() {
        val destination = BreedsDetailsDestination(
            name = "retriever",
            recommendationReason = "Friendly, trainable, and compatible with your favorites.",
        )

        val restored = Json.decodeFromString<BreedsDetailsDestination>(
            Json.encodeToString(destination),
        )

        assertEquals(destination, restored)
    }

    @Test
    fun regularBreed_hasNoRecommendationReason() {
        assertNull(BreedsDetailsDestination(name = "hound").recommendationReason)
    }
}
