package me.vavra.dive.common

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Define the DataStore instance at the top level, tied to the Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Storage(private val context: Application) {

    private companion object {
        val RUN_ID_KEY = stringPreferencesKey("run_id")
    }

    /**
     * Internal Flow to observe the runId. Emits null if no runId is stored.
     */
    private val runIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[RUN_ID_KEY]
        }

    /**
     * Suspended function to get the current runId.
     * Returns the stored runId, or null if not set.
     */
    suspend fun getRunId(): String {
        return checkNotNull(runIdFlow.first())
    }

    /**
     * Saves the runId to DataStore.
     */
    suspend fun saveRunId(runId: String) {
        context.dataStore.edit { settings ->
            settings[RUN_ID_KEY] = runId
        }
    }
}
