package nl.jcraane.mocker.features.forwarding

import nl.jcraane.mocker.prependIfMissing
import java.io.File
import java.io.FileWriter

interface WriterStrategy {
    val rootFolder: String
    fun write(contents: String, fileName: String? = null)
}

/**
 * Writes the given contents using the write method to file
 *
 * @param rootFolder base path of where the file is written, absolute path for example: /mymachine/projects/mocker/src/main/kotlin
 * @param subFolders Optional subfolders. If present and the folders do not exist, those folders are created.
 * @param defaultFileName If this writer should always write to the same file (which overrides the fileName passed in the write method (which is optional)).
 */
class FileWriterStrategy(
    override val rootFolder: String,
    private val defaultFileName: String? = null) : WriterStrategy {

    override fun write(contents: String, fileName: String?) {
        File(rootFolder).mkdirs()
        val name = (this.defaultFileName ?: fileName)?.prependIfMissing("/")
        val fullPath = "$rootFolder$name"

        FileWriter(fullPath, false).use {
            it.write(contents)
            it.flush()
        }
    }
}