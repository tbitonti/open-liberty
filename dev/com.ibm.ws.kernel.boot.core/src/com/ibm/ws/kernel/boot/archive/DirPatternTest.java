package com.ibm.ws.kernel.boot.archive;

import java.io.File;

public class DirPatternTest {
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

    public static void main(String[] parms) {
        for ( String[] testData : TEST_DATA ) {
            testCannonization(testData[0], testData[1]);
        }
    }
    
    public static void testCannonization(String rawPath, String expectedNormPath) {
        String actualNormPath = DirPattern.cannonize( new File(rawPath) );
        
        System.out.println("Raw: [ " + rawPath + " ]");
        System.out.println("  Expected: [ " + expectedNormPath + " ]");
        System.out.println("  Actual  : [ " + actualNormPath + " ]");
        System.out.println("  Match   : [ " + expectedNormPath.equals(actualNormPath) + " ]");
    }
}
