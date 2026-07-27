package ru.bitvibe.waggy.data.repository

internal object VersionComparator {
    fun isNewer(candidate: String, current: String): Boolean {
        val candidateVersion = ParsedVersion.from(candidate) ?: return false
        val currentVersion = ParsedVersion.from(current) ?: return false

        val componentCount = maxOf(
            candidateVersion.components.size,
            currentVersion.components.size,
        )
        repeat(componentCount) { index ->
            val candidateComponent = candidateVersion.components.getOrElse(index) { 0 }
            val currentComponent = currentVersion.components.getOrElse(index) { 0 }
            if (candidateComponent != currentComponent) {
                return candidateComponent > currentComponent
            }
        }

        return comparePreRelease(
            candidateVersion.preRelease,
            currentVersion.preRelease,
        ) > 0
    }

    private fun comparePreRelease(candidate: String?, current: String?): Int {
        if (candidate == null && current == null) return 0
        if (candidate == null) return 1
        if (current == null) return -1

        val candidateParts = candidate.split('.')
        val currentParts = current.split('.')
        repeat(maxOf(candidateParts.size, currentParts.size)) { index ->
            val candidatePart = candidateParts.getOrNull(index) ?: return -1
            val currentPart = currentParts.getOrNull(index) ?: return 1
            val candidateNumber = candidatePart.toLongOrNull()
            val currentNumber = currentPart.toLongOrNull()
            val comparison = when {
                candidateNumber != null && currentNumber != null ->
                    candidateNumber.compareTo(currentNumber)

                candidateNumber != null -> -1
                currentNumber != null -> 1
                else -> candidatePart.compareTo(currentPart, ignoreCase = true)
            }
            if (comparison != 0) return comparison
        }
        return 0
    }

    private data class ParsedVersion(
        val components: List<Long>,
        val preRelease: String?,
    ) {
        companion object {
            fun from(value: String): ParsedVersion? {
                val normalized = value.trim()
                    .removePrefix("v")
                    .removePrefix("V")
                    .substringBefore('+')
                val versionAndPreRelease = normalized.split('-', limit = 2)
                val components = versionAndPreRelease.first()
                    .split('.')
                    .map { it.toLongOrNull() ?: return null }
                if (components.isEmpty()) return null

                return ParsedVersion(
                    components = components,
                    preRelease = versionAndPreRelease.getOrNull(1)?.takeIf { it.isNotBlank() },
                )
            }
        }
    }
}
