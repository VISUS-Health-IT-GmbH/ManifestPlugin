/*  JarExtensionException.kt
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
package com.visus.infrastructure.exception


/**
 *  Base exception for every exception thrown by the extension methods of org.gradle.api.tasks.bundling.Jar
 *
 *  @author Tobias Hahnen
 */
internal sealed class JarExtensionException(message: String) : ManifestPluginException(message)


/** Exception thrown pathing a JAR archive artifact manifest attributes failed */
internal class JarPatchingFailedException(message: String) : JarExtensionException(message)
