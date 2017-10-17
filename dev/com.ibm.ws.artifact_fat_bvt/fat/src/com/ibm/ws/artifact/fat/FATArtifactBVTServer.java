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
package com.ibm.ws.artifact.fat;

import com.ibm.ws.fat.utils.FATWebArchiveDef;

/**
 * Artifact BVT server definition.
 */
public class FATArtifactBVTServer {
    public static final String ARTIFACT_BVT_SERVER_NAME = "com.ibm.ws.artifact.bvt";
    public static final String ARTIFACT_BVT_CONTEXT_ROOT = "/artifactapi";

    public static final String ARTIFACT_BVT_WAR_NAME = "artifactapi.war";
    public static final String ARTIFACT_BVT_PACKAGE_NAME = "com.ibm.ws.artifact.fat";

    public static final String ARTIFACT_BVT_ROOT_RESOURCES_PATH =
        "test-applications/" + ARTIFACT_BVT_WAR_NAME + "/resources";

    public static FATWebArchiveDef ARTIFACT_BVT_WAR_PARAMS = new FATWebArchiveDef(
        ARTIFACT_BVT_WAR_NAME,
        new String[] { ARTIFACT_BVT_PACKAGE_NAME },
        ARTIFACT_BVT_ROOT_RESOURCES_PATH,
        new String[] { },
        new String[] { "web.xml" }
    );
}

