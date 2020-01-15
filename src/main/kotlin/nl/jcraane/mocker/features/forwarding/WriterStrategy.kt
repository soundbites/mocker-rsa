package nl.jcraane.mocker.features.forwarding

import nl.jcraane.mocker.prependIfMissing
import nl.jcraane.mocker.util.RealTimeProvider
import nl.jcraane.mocker.util.TimeProvider
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.format.DateTimeFormatter

interface WriterStrategy {
    val rootFolder: String
    fun write(contents: String, fileName: String? = null)
    fun write(contents: ByteArray, fileName: String? = null)
}

/**
 * Writes the given contents using the write method to file
 *
 * @param rootFolder base path of where the file is written, absolute path for example: /mymachine/projects/mocker/src/main/kotlin
 * @param subFolders Optional subfolders. If present and the folders do not exist, those folders are created.
 * @param defaultFileName If this writer should always write to the same file (which overrides the fileName passed in the write method (which is optional)).
 *
 * Open class so testcases can override methods for easier testing with files (we could use an abstraction or a something like jimfs but this suffices for now).
 */
open class FileWriterStrategy(
    override val rootFolder: String,
    private val defaultFileName: String? = null,
    private val overwriteExistingFiles: Boolean = false,
    private val timeProvider: TimeProvider = RealTimeProvider()) : WriterStrategy {

    private var firstWrite = true
    private var uniqueFilename = ""

    override fun write(contents: String, fileName: String?) {
        File(rootFolder).mkdirs()
        uniqueFilename = getUniqueFullPath(fileName)
        FileWriter(uniqueFilename, false).use {
            it.write(contents)
            it.flush()
            firstWrite = false
        }
    }

    override fun write(contents: ByteArray, fileName: String?) {
        File(rootFolder).mkdirs()
        uniqueFilename = getUniqueFullPath(fileName)
        FileOutputStream(uniqueFilename).use {
            it.write(contents)
            it.flush()
        }
    }

    /**
     * Returns the full path of the file to be written. If the file already exists, a number is appended to the end of
     * the file name (this is an incrementing number).
     */
    fun getUniqueFullPath(fileName: String?): String {
        val name = this.defaultFileName ?: fileName

        if (name == null || name.isEmpty()) {
            throw IllegalArgumentException("No fileName specified, the defaultFileName is null or empty and the fileName is null or empty.")
        }

        val fullPath = "$rootFolder${name.prependIfMissing("/")}"

        return if (overwriteExistingFiles || !firstWrite) {
            fullPath
        } else {
            if (!fileExists(fullPath)) {
                fullPath
            } else {
                val nameWithTimeStamp = appendTimeStamp(name)
                "$rootFolder${nameWithTimeStamp?.prependIfMissing("/")}"
            }
        }
    }

    open fun fileExists(fullPath: String) = File(fullPath).exists()

    private fun appendTimeStamp(fileName: String): String {
        val extensionSeparator = "."
        val indexOfDot = fileName.lastIndexOf(extensionSeparator)
        val timestamp = timeProvider.localDate.format(DateTimeFormatter.ofPattern("'y'yyyy'M'MM'd'dd'h'HH'm'mm's'ss"))
        return if (indexOfDot != -1) {
            // Extension found, append the timestap before the dot.
            val name = fileName.substringBeforeLast(extensionSeparator)
            val extension = fileName.substringAfterLast(extensionSeparator)
            "${name}_$timestamp$extensionSeparator$extension"
        } else {
            // No extension found, append the timestamp at the end
            "${fileName}_$timestamp"
        }
    }
}
