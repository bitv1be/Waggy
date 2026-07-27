package ru.bitvibe.waggy.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test
    fun isNewer_comparesNumericComponents() {
        assertTrue(VersionComparator.isNewer("1.0.10", "1.0.9"))
        assertTrue(VersionComparator.isNewer("2.0", "1.99.99"))
        assertFalse(VersionComparator.isNewer("1.0.9", "1.0.10"))
        assertFalse(VersionComparator.isNewer("1.0.0", "1.0"))
    }

    @Test
    fun isNewer_treatsStableVersionAsNewerThanPreRelease() {
        assertTrue(VersionComparator.isNewer("1.2.0", "1.2.0-rc.1"))
        assertTrue(VersionComparator.isNewer("1.2.0-rc.2", "1.2.0-rc.1"))
        assertFalse(VersionComparator.isNewer("1.2.0-beta.2", "1.2.0-rc.1"))
    }

    @Test
    fun isNewer_rejectsUnparseableVersions() {
        assertFalse(VersionComparator.isNewer("latest", "1.0.0"))
        assertFalse(VersionComparator.isNewer("1.0.1", "development"))
    }
}
