/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.archive;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test canonicalization.
 */
public class DirPatternCannonizationTest {
    public static String[][] TEST_DATA = new String[][] {
        { "C:\\", "C:/" },
        { "c:\\", "C:/" },
        
        { "C:\\dev", "C:/dev/" },
        { "c:\\dev", "C:/dev/" },

        { "C:\\junk", "C:/junk" },
        { "C:\\junk\\", "C:/junk" },
        { "c:\\junk", "C:/junk" },
        { "c:\\junk\\", "C:/junk" },

        { "C:\\dev\\repos-pub\\", "C:/dev/repos-pub/" },
        { "C:\\dev\\repos-pub", "C:/dev/repos-pub/" },
        { "c:\\dev\\repos-pub\\", "C:/dev/repos-pub/" },
        { "c:\\dev\\repos-pub", "C:/dev/repos-pub/" }
    };

    @Test
    public void testCannonizations() {
        String[] firstFailure = null;
        for ( String[] testData : TEST_DATA ) {
            String[] nextFailure = testCannonization(testData[0], testData[1]);
            if ( (nextFailure != null) && (firstFailure == null) ) {
                firstFailure = nextFailure;
            }
        }
        if ( firstFailure != null ) {
            Assert.fail("Failed canonicalization of [ " + firstFailure[0] + " ] expected [ " + firstFailure[1] + " ]; obtained [ " + firstFailure[2] + " ]");
        }
    }
    
    protected String[] testCannonization(String rawPath, String expectedNormPath) {
        String actualNormPath = DirPattern.cannonize( new File(rawPath) );
        boolean matches = expectedNormPath.equals(actualNormPath);

        System.out.println("Raw: [ " + rawPath + " ]");
        System.out.println("  Expected: [ " + expectedNormPath + " ]");
        System.out.println("  Actual  : [ " + actualNormPath + " ]");
        System.out.println("  Match   : [ " + matches + " ]");

        if ( !matches ) {
            return new String[] { rawPath, expectedNormPath, actualNormPath };  
        } else {
            return null;
        }
    }
}
