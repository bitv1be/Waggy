package ru.bitvibe.waggy.domain.usecase

import ru.bitvibe.waggy.domain.models.AppUpdate
import ru.bitvibe.waggy.domain.repository.AppUpdateRepository
import javax.inject.Inject

class CheckForAppUpdateUseCase @Inject constructor(
    private val repository: AppUpdateRepository,
) : UseCase<String, AppUpdate?> {
    override suspend fun invoke(args: String): AppUpdate? {
        return repository.getLatestUpdate(args)
    }
}
