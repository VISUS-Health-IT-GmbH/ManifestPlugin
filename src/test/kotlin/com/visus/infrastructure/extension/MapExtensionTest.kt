/*  MapExtensionTest.kt
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

import org.junit.Assert
import org.junit.Test


/**
 *  MapExtensionTest:
 *  ================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the Map extension methods defined
 */
open class MapExtensionTest {
    companion object {
        // example property key used in tests
        private val KEY_NAME = "test123"
    }


    /** 1) Check the "Map<*, *>.checkValueIsBlank" method */
    @Test fun test_checkValueIsBlank() {
        Assert.assertFalse(mapOf<Any, Any>().checkValueIsBlank(KEY_NAME))
        Assert.assertFalse(mapOf<Any, Any?>(KEY_NAME to null).checkValueIsBlank(KEY_NAME))
        Assert.assertFalse(mapOf<Any, Any?>(KEY_NAME to "null").checkValueIsBlank(KEY_NAME))
        Assert.assertTrue(mapOf<Any, Any?>(KEY_NAME to "").checkValueIsBlank(KEY_NAME))
        Assert.assertTrue(mapOf<Any, Any?>(KEY_NAME to " ").checkValueIsBlank(KEY_NAME))
    }


    /** 2) Check the "Map<*, *>.checkValueTrulyExists" method */
    @Test fun test_checkValueTrulyExists() {
        Assert.assertFalse(mapOf<Any, Any?>().checkValueTrulyExists(KEY_NAME))
        Assert.assertFalse(mapOf<Any, Any?>(KEY_NAME to null).checkValueTrulyExists(KEY_NAME))
        Assert.assertFalse(mapOf<Any, Any?>(KEY_NAME to "").checkValueTrulyExists(KEY_NAME))
        Assert.assertFalse(mapOf<Any, Any?>(KEY_NAME to " ").checkValueTrulyExists(KEY_NAME))
        Assert.assertTrue(mapOf<Any, Any?>(KEY_NAME to "null").checkValueTrulyExists(KEY_NAME))
    }


    /** 3) Check the "Map<*, *>?.checkValuePossiblyExists" method */
    @Test fun test_checkValuePossiblyExists() {
        val emptyMap: Map<*, *>? = null
        Assert.assertFalse(emptyMap.checkValuePossiblyExists(KEY_NAME))
        Assert.assertFalse(mapOf<Any, Any?>(KEY_NAME to null).checkValuePossiblyExists(KEY_NAME))
        Assert.assertTrue(mapOf<Any, Any?>(KEY_NAME to "null").checkValuePossiblyExists(KEY_NAME))
    }
}
