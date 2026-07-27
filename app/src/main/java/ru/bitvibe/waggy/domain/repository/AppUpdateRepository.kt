package ru.bitvibe.waggy.domain.repository

import ru.bitvibe.waggy.domain.models.AppUpdate

interface AppUpdateRepository {
    suspend fun getLatestUpdate(currentVersionName: String): AppUpdate?
}
