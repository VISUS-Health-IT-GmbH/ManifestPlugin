/*  ManifestPluginException.kt
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

import org.gradle.api.InvalidUserDataException


/**
 *  Base extension for every extension thrown by this plugin
 *
 *  @author Tobias Hahnen
 */
internal open class ManifestPluginException(message: String) : InvalidUserDataException(message)


/** Exception thrown when no Java plugin applied to root project */
internal class JavaPluginMissingException(message: String) : ManifestPluginException(message)
