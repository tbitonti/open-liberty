/*
* IBM Confidential
*
* OCO Source Materials
*
* WLP Copyright IBM Corp. 2017
*
* The source code for this program is not published or otherwise divested 
* of its trade secrets, irrespective of what has been deposited with the 
* U.S. Copyright Office.
*/
package com.ibm.ws.fat_bvt.utils;

/**
 * Feature definition.  Used when preparing liberty servers.
 */
public class FATFeatureDef {
    public final String featureManifestPath;
    public final String featureJarPath;
    
    public FATFeatureDef(String featureManifestPath, String featureJarPath) {
        this.featureManifestPath = featureManifestPath;
        this.featureJarPath = featureJarPath;
    }
}
