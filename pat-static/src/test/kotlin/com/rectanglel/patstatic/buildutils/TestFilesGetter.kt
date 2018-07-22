package com.rectanglel.patstatic.buildutils

import java.io.File

/**
 * Works around the issue where JUnit from gradlew thinks the relative path is at base project level,
 * while running the Tests in Android Studio thinks the folder is at module-level.
 *
 * This isn't ideal, and I'd rather get a better solution to this...
 */
fun getTestFilesFolder() : File {
    val testFilesFolder = File("testFiles")
    if (testFilesFolder.exists()) {
        return testFilesFolder
    }
    return File("../testFiles")
}