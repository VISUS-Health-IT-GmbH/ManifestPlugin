/*  MapExtension.kt
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


/**
 *  Extension to Map<*, *> to check if value accessed by key exists and is blank
 *
 *  @param key to access value
 *  @return true if value is not null but blank, false otherwise
 */
internal fun Map<*, *>.checkValueIsBlank(key: String) : Boolean {
    return this.containsKey(key) && this[key]?.toString()?.isBlank() ?: false
}


/**
 *  Extension to Map<*, *> to check if a value accessed by key exists and is filled with actual value
 *
 *  @param key to access value
 *  @return true if value is neither null nor blank, false otherwise
 */
internal fun Map<*, *>.checkValueTrulyExists(key: String) : Boolean {
    return this.containsKey(key) && this[key]?.toString()?.isNotBlank() ?: false
}


/**
 *  Extension to Map<*, *>? to check if a value accessed by key exists and is filled with actual value when nullable map
 *  is not null
 *
 *  @param key to access value
 *  @return true if value is neither null nor blank, false otherwise (also when map is null)
 */
internal fun Map<*, *>?.checkValuePossiblyExists(key: String) = this?.checkValueTrulyExists(key) ?: false
