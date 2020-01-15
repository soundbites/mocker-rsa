package nl.jcraane.mocker.features.forwarding

import nl.jcraane.mocker.util.MockTimeProvider
import nl.jcraane.mocker.util.TimeProvider
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class FileWriterStrategyTest {
    @Test
    fun getFileNameIfNotExists() {
        val timeProvider = MockTimeProvider(LocalDateTime.of(2020, 1, 15, 10, 5, 15))

        assertEquals("rootFolder/Recorded.kt", MockFileWriterStrategy("rootFolder", "Recorded.kt", overwriteExistingFiles = true, fileExists = false, timeProvider = timeProvider).getUniqueFullPath(null))
        assertEquals("rootFolder/Recorded.kt", MockFileWriterStrategy("rootFolder", "Recorded.kt", overwriteExistingFiles = false, fileExists = false, timeProvider = timeProvider).getUniqueFullPath(null))
        assertEquals("rootFolder/Recorded_y2020M01d15h10m05s15.kt", MockFileWriterStrategy("rootFolder", "Recorded.kt", overwriteExistingFiles = false, fileExists = true, timeProvider = timeProvider).getUniqueFullPath(null))
        assertEquals("rootFolder/Recorded_y2020M01d15h10m05s15", MockFileWriterStrategy("rootFolder", "Recorded", overwriteExistingFiles = false, fileExists = true, timeProvider = timeProvider).getUniqueFullPath(null))
    }
}

class MockFileWriterStrategy(rootFolder: String, defaultFileName: String, overwriteExistingFiles: Boolean, private val fileExists: Boolean, timeProvider: TimeProvider) : FileWriterStrategy(rootFolder, defaultFileName, overwriteExistingFiles, timeProvider) {
    override fun fileExists(fullPath: String) = fileExists
}