/*  AnyExtensionTest.kt
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
 *  AnyExtensionTest:
 *  ================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the Any extension methods defined
 */
open class AnyExtensionTest {
    /** 1) Check the "Any.isTrue" method */
    @Test fun test_isTrue() {
        Assert.assertTrue((true as Any).isTrue())
        Assert.assertTrue(("true" as Any).isTrue())
    }
}
