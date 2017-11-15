/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2017
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat_bvt;

import com.ibm.ws.fat_bvt.utils.FATFeatureDef;
import com.ibm.ws.fat_bvt.utils.FATWebArchiveDef;

/**
 * Artifact BVT server definition.
 */
public class FATArtifactBVTServer {
    public static final String ARTIFACT_BVT_SERVER_NAME = "com.ibm.ws.artifact.fat_bvt";
    public static final String ARTIFACT_BVT_CONTEXT_ROOT = "/artifactapi";

    public static final String ARTIFACT_BVT_WAR_NAME = "artifactapi.war";
    public static final String ARTIFACT_BVT_PACKAGE_NAME = "com.ibm.ws.artifact.fat_bvt";

    public static final String ARTIFACT_BVT_ROOT_RESOURCES_PATH =
        "test-applications/" + ARTIFACT_BVT_WAR_NAME + "/resources";

    public static FATWebArchiveDef ARTIFACT_BVT_WAR_DEF =
        new FATWebArchiveDef(
            ARTIFACT_BVT_WAR_NAME,
            new String[] { ARTIFACT_BVT_PACKAGE_NAME },
            ARTIFACT_BVT_ROOT_RESOURCES_PATH,
            new String[] { },
            new String[] { "web.xml" }
    );

    public static final String ARTIFACT_BVT_FEATURE_MANIFEST_PATH =
        "features/artifactinternals-1.0.mf";
    public static final String ARTIFACT_BVT_FEATURE_JAR_PATH =    
        "artifactinternals.jar";

    public static FATFeatureDef ARTIFACT_BVT_FEATURE_DEF =
        new FATFeatureDef(
            ARTIFACT_BVT_FEATURE_MANIFEST_PATH,
            ARTIFACT_BVT_FEATURE_JAR_PATH);
}

