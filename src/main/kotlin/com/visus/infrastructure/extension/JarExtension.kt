/*  JarExtension.kt
 *
 *  Copyright (C) 2022, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.infrastructure.extension

import org.gradle.api.tasks.bundling.Jar

import com.visus.infrastructure.ManifestPlugin
import com.visus.infrastructure.exception.JarPatchingFailedException


/**
 *  Extension to Jar to patch the manifest attributes with additional ones
 *
 *  @param attributes the additional attributes to add to JAR archive artifact
 */
internal fun Jar.patchManifest(attributes: Map<String, String>) {
    // i) all necessary and reused variables
    val jarDir = "${this.project.buildDir}/libs"
    val jarFileName = this.archiveFileName.get()

    // ii) don't patch if JAR archive does not exist
    if (!this.project.file("$jarDir/$jarFileName").exists()) {
        this.project.logger.warn(
            "[${ManifestPlugin::class.simpleName} -> Jar.patchManifest : WARNING] The jar archive artifact at " +
            "'$jarDir/$jarFileName' does not exist, therefore skipping the patch process for this jar archive artifact!"
        )
        return
    }

    // iii) create META-INF directory if not already exists & create new MANIFEST.MF file
    this.project.mkdir("$jarDir/META-INF")

    val buffer = StringBuffer()
    attributes.forEach { (key, value) -> buffer.append("$key: $value\n") }
    this.project.file("$jarDir/META-INF/MANIFEST.MF").writeText(buffer.toString())

    // iv) patch existing JAR archive artefact using "jar" executable
    val exitCode = this.project.exec {
        isIgnoreExitValue = true
        workingDir = this@patchManifest.project.file(jarDir)
        commandLine = listOf("jar", "ufm", jarFileName, "META-INF/MANIFEST.MF")
    }.exitValue

    // v) delete META-INF/MANIFEST.MF for next JAR archive artifact
    this.project.delete("$jarDir/META-INF/MANIFEST.MF")

    // vi) fail if patching existing JAR archive artifact has failed
    if (exitCode != 0) {
        throw JarPatchingFailedException(
            "[${ManifestPlugin::class.simpleName}] Patching the JAR archive artifact '$jarDir/$jarFileName' failed " +
            "while running the 'jar' executable with exit code: $exitCode"
        )
    }
}
