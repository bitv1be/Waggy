package ru.bitvibe.waggy.domain.usecase

interface UseCase<in Params, out Result> {
    suspend operator fun invoke(args: Params): Result

    data object None
}