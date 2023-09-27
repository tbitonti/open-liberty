/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.feature.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.ws.feature.utils.FeatureInfo;
import com.ibm.ws.feature.utils.FeatureRepo;

/**
 * Data for versionless tests.
 */
public class VersionlessData {
    public static FeatureRepo getRepository() {
        return FeatureTest.getRepository();
    }

    public static FeatureInfo getFeature(String featureName) {
        return getRepository().getFeature(featureName);
    }

    /**
     * Base selector data:
     *
     * Each selector links to platform / umbrella features
     * by their base names.
     */
    private static final String[][] SELECTOR_ELEMENTS =
        { { "jakarta",
            "com.ibm.websphere.appserver.javaee",
                "com.ibm.websphere.appserver.jakartaee",
                "io.openliberty.jakartaee",
            "com.ibm.websphere.appserver.webProfile",
                "io.openliberty.webProfile",
            "com.ibm.websphere.appserver.javaeeClient",
            "io.openliberty.jakartaeeClient" },
          { "microProfile",
            "com.ibm.websphere.appserver.microProfile",
                "io.openliberty.microProfile" } };

    /** Table of cohorts associated with each selector. */
    private static final Map<String, List<String>> selectorCohorts;

    /**
     * Table of cohorts associated with each selector.
     *
     * Keys are selector names.  Values are platform feature short names.
     *
     * @return The selector cohorts table.
     */
    public static Map<String, List<String>> getSelectorCohorts() {
        return selectorCohorts;
    }

    static {
        Map<String, List<String>> useCohorts = new LinkedHashMap<>();

        for ( String[] selectorData : SELECTOR_ELEMENTS ) {
            put(useCohorts, selectorData);
        }

        selectorCohorts = useCohorts;
    }

    /**
     * Populate the selector cohorts table: The first data element
     * is a selector. The remaining data elements the base names of
     * cohort elements.
     *
     * @param storage Storage for the selector cohort table.
     * @param data Data to put in the table.
     */
    private static void put(Map<String, List<String>> storage, String... data) {
        String key = data[0];

        List<String> values = new ArrayList<>(data.length - 1);
        for (int datumNo = 1; datumNo < data.length; datumNo++) {
            values.add(data[datumNo]);
        }

        storage.put(key, values);
    }

    // selectorName -> depBaseName -> sceVersion -> depVersion

    /** Table of cohort element versions. */
    private static final Map<String, Map<String, Map<String, String>>> cohortRanges;

    /**
     * Table of cohort element versions.
     *
     * The table has a complex structure:
     *
     * The top two layers are keyed by selector name and by dependent
     * feature base names.
     *
     * The bottom two layers map selector element versions to dependent feature
     * versions.
     *
     * Each entry of the bottom layer tells what version of a dependency feature
     * is brought in by the selector element that corresponds with the selector
     * version.
     *
     * @return The cohort element versions table.
     */
    public static Map<String, Map<String, Map<String, String>>> getCohortRanges() {
        return cohortRanges;
    }

    static {
        Map<String, List<String>> cohorts = getRepository().getCohorts();

        Map<String, Map<String, Map<String, String>>> useRanges = new HashMap<>();

        getSelectorCohorts().forEach((String selectorName, List<String> scBaseNames) -> {
            // System.out.println("S [ " + selectorName + " ]");
            Map<String, Map<String, String>> cohortRange =
                useRanges.computeIfAbsent(selectorName,
                                          (String name) -> new HashMap<String, Map<String, String>>());

            for (String scBaseName : scBaseNames) {
                List<String> sceVersions = cohorts.get(scBaseName);

                // System.out.println("S [ " + selectorName + " ] SB [ " + scBaseName + " ] [ " + sceVersions + " ]");

                for (String sceVersion : sceVersions) {
                    String sceName = scBaseName + "-" + sceVersion;
                    FeatureInfo sceInfo = getFeature(sceName);
                    if (sceInfo == null) {
                        // System.out.println("Not found [ " + sceName + " ]");
                        continue;
                    }

                    sceInfo.forEachSortedDepName((String depName) -> {
                        FeatureInfo depInfo = getFeature(depName);
                        if ( depInfo == null ) {
                            // System.out.println("Not found [ " + depName + " ]");
                            return;
                        } else if ( !depInfo.isPublic() ) {
                            // System.out.println("Not public [ " + depName + " ]");
                            return;
                        }

                        String depBaseName = depInfo.getBaseName();
                        String depVersion = depInfo.getVersion();

                        Map<String, String> depRange =
                            cohortRange.computeIfAbsent(depBaseName,
                                                        (String name) -> new HashMap<String, String>());

                        depRange.put(sceVersion, depVersion);

                        // System.out.println(
                        //     "S [ " + selectorName + " ] SB [ " + scBaseName + " ]" +
                        //     " D [ " + depName + " ]: [ " + sceVersion + " : " + depVersion + " ]");
                    });
                }
            }
        });

        cohortRanges = useRanges;
    }

    /** Table of feature base names which span all current versions. */

    public static final Set<String> spanningBaseNames;

    public static boolean isSpanning(String baseName) {
        return spanningBaseNames.contains(baseName);
    }

    static {
           Set<String> useNames = new HashSet<>();

           useNames.add("com.ibm.websphere.appserver.jdbc");
           useNames.add("com.ibm.websphere.appserver.jndi");
           useNames.add("com.ibm.websphere.appserver.servlet");

           spanningBaseNames = useNames;
    }

    /**
     * Table of feature base names which span all current versions,
     * changing the prefix but keeping the simple name.
     */
    public static final Map<String, String> sharedBaseNames;

    public static final boolean isShared(String baseName) {
        return sharedBaseNames.containsKey(baseName);
    }

    public static String getSharedName(String baseName) {
        return sharedBaseNames.get(baseName);
    }

    static {
           Map<String, String> useMapping = new HashMap<>();

           useMapping.put("com.ibm.websphere.appserver.appClientSupport", "io.openliberty.appClientSupport");
           useMapping.put("com.ibm.websphere.appserver.appSecurity", "io.openliberty.appSecurity");
           useMapping.put("com.ibm.websphere.appserver.batch", "io.openliberty.batch");
           useMapping.put("com.ibm.websphere.appserver.beanValidation", "io.openliberty.beanValidation");
           useMapping.put("com.ibm.websphere.appserver.cdi", "io.openliberty.cdi");
           useMapping.put("com.ibm.websphere.appserver.concurrent", "io.openliberty.concurrent");
           useMapping.put("com.ibm.websphere.appserver.jsonb", "io.openliberty.jsonb");
           useMapping.put("com.ibm.websphere.appserver.jsonp", "io.openliberty.jsonp");
           useMapping.put("com.ibm.websphere.appserver.managedBeans", "io.openliberty.managedBeans");
           useMapping.put("com.ibm.websphere.appserver.webProfile", "io.openliberty.webProfile");
           useMapping.put("com.ibm.websphere.appserver.websocket", "io.openliberty.websocket");

           useMapping.put("com.ibm.websphere.appserver.mpConfig", "io.openliberty.mpConfig");
           useMapping.put("com.ibm.websphere.appserver.mpFaultTolerance", "io.openliberty.mpFaultTolerance");
           useMapping.put("com.ibm.websphere.appserver.mpHealth", "io.openliberty.mpHealth");
           useMapping.put("com.ibm.websphere.appserver.mpJwt", "io.openliberty.mpJwt");
           useMapping.put("com.ibm.websphere.appserver.mpMetrics", "io.openliberty.mpMetrics");
           useMapping.put("com.ibm.websphere.appserver.mpOpenAPI", "io.openliberty.mpOpenAPI");
           useMapping.put("com.ibm.websphere.appserver.mpOpenTracing", "io.openliberty.mpOpenTracing");
           useMapping.put("com.ibm.websphere.appserver.mpRestClient", "io.openliberty.mpRestClient");

           sharedBaseNames = useMapping;
    }

    /** Table of feature base names which changed both the prefix and the simple name. */
    public static final Map<String, String> changedBaseNames;

    public static final boolean isChanged(String baseName) {
        return changedBaseNames.containsKey(baseName);
    }

    public static String getChangedName(String baseName) {
        return changedBaseNames.get(baseName);
    }

    static {
           Map<String, String> useMapping = new HashMap<>();

           useMapping.put("com.ibm.websphere.appserver.ejb", "io.openliberty.enterpriseBeans");
           useMapping.put("com.ibm.websphere.appserver.ejbLite", "io.openliberty.enterpriseBeansLite");
           useMapping.put("com.ibm.websphere.appserver.el", "io.openliberty.expressionLanguage");
           useMapping.put("com.ibm.websphere.appserver.jacc", "io.openliberty.appAuthorization");
           useMapping.put("com.ibm.websphere.appserver.jaspic", "io.openliberty.appAuthentication");
           useMapping.put("com.ibm.websphere.appserver.javaMail", "io.openliberty.mail");
           useMapping.put("com.ibm.websphere.appserver.jaxb", "io.openliberty.xmlBinding");
           useMapping.put("com.ibm.websphere.appserver.jaxws", "io.openliberty.xmlWS");
           useMapping.put("com.ibm.websphere.appserver.jca", "io.openliberty.connectors");
           useMapping.put("com.ibm.websphere.appserver.jcaInboundSecurity", "io.openliberty.connectorsInboundSecurity");
           useMapping.put("com.ibm.websphere.appserver.jms", "io.openliberty.messaging");
           useMapping.put("com.ibm.websphere.appserver.jpa", "io.openliberty.persistence");
           useMapping.put("com.ibm.websphere.appserver.jsf", "io.openliberty.faces");
           useMapping.put("com.ibm.websphere.appserver.jsp", "io.openliberty.pages");
           useMapping.put("com.ibm.websphere.appserver.jaxrs", "io.openliberty.restfulWS");
           useMapping.put("com.ibm.websphere.appserver.jaxrsClient", "io.openliberty.restfulWSClient");
           useMapping.put("com.ibm.websphere.appserver.wasJmsClient", "io.openliberty.messagingClient");
           useMapping.put("com.ibm.websphere.appserver.wasJmsSecurity", "io.openliberty.messagingSecurity");
           useMapping.put("com.ibm.websphere.appserver.wasJmsServer", "io.openliberty.messagingServer");

           changedBaseNames = useMapping;
    }

    /** Table of base feature names which were retired. */
    public static final Set<String> retiredBaseNames;

    public static boolean isRetired(String baseName) {
        return retiredBaseNames.contains(baseName);
    }

    static {
        Set<String> useNames = new HashSet<>();

        useNames.add("com.ibm.websphere.appserver.j2eeManagement");
        useNames.add("com.ibm.websphere.appserver.restConnector");

        retiredBaseNames = useNames;
    }

    /** Table of base feature names which have been added. */
    public static final Set<String> addedBaseNames;

    public static boolean isAdded(String baseName) {
        return addedBaseNames.contains(baseName);
    }

    static {
        Set<String> useNames = new HashSet<>();

        useNames.add("io.openliberty.data");
        useNames.add("io.openliberty.mpTelemetry");

        addedBaseNames = useNames;
    }
}