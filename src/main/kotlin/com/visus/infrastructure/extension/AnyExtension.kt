/*  AnyExtension.kt
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
 *  Extension to Any to check if actual value is true (cannot throw ClassCastException as every class inherits from Any)
 *
 *  @return true if value can be converted to boolean, false otherwise
 */
internal fun Any.isTrue() = when (this) {
    is Boolean  -> this
    else        -> this.toString().toBoolean()
}
