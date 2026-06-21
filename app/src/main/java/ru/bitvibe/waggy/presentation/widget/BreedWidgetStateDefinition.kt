package ru.bitvibe.waggy.presentation.widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream


object BreedWidgetStateDefinition : GlanceStateDefinition<BreedWidgetState> {
    private const val DATA_STORE_FILENAME_PREFIX = "breed_widget_state_"
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<BreedWidgetState> = DataStoreFactory.create(
        serializer = BreedWidgetStateSerializer,
        produceFile = { getLocation(context, fileKey) }
    )

    override fun getLocation(
        context: Context,
        fileKey: String
    ): File = context.dataStoreFile(DATA_STORE_FILENAME_PREFIX + fileKey.lowercase())
}

object BreedWidgetStateSerializer : Serializer<BreedWidgetState> {
    override val defaultValue: BreedWidgetState =
        BreedWidgetState.Loading

    override suspend fun readFrom(input: InputStream): BreedWidgetState = try {
        Json.decodeFromString(
            BreedWidgetState.serializer(),
            input.readBytes().decodeToString()
        )
    } catch (exception: SerializationException) {
        throw CorruptionException("Could not read widget state: ${exception.message}")
    }

    override suspend fun writeTo(
        t: BreedWidgetState,
        output: OutputStream
    ) {
        output.use {
            it.write(
                Json.encodeToString(BreedWidgetState.serializer(), t).encodeToByteArray()
            )
        }
    }

}