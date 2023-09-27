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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ibm.ws.feature.utils.FeatureInfo;
import com.ibm.ws.feature.utils.FeatureRepo;

//@formatter:off

/**
 * Versionless feature reporter.
 *
 * Display several charts of public features in relation to versionless features.
 */
public class VersionlessTest {

    public static FeatureRepo getRepository() {
        return FeatureTest.getRepository();
    }

    public static FeatureInfo getFeature(String featureName) {
        return getRepository().getFeature(featureName);
    }

    //

    private static final String DOUBLE_DASHES = "============================================================";
    private static final String DASHES = "------------------------------------------------------------";

    protected static void printDoubleDashes() {
        System.out.println(DOUBLE_DASHES);
    }

    protected static void printDashes() {
        System.out.println(DASHES);
    }

    /**
     * Display all current cohorts.
     *
     * The cohorts table, as obtained from the feature repository,
     * is a mapping of feature base name to the collection of the
     * available versions of that feature.
     *
     * Only public features are listed.
     */
    @Test
    public void listCohorts() {
        Map<String, List<String>> cohorts = getRepository().getCohorts();

        List<String> baseNames = new ArrayList<>(cohorts.keySet());
        baseNames.sort(Comparator.comparing(String::toString));

        printDoubleDashes();

        System.out.println("All Cohorts:");

        printDashes();

        for (String baseName : baseNames) {
            List<String> cohort = cohorts.get(baseName);
            System.out.println("  [ " + baseName + " ] [ " + cohort + " ]");
        }

        printDoubleDashes();
    }

    /**
     * Display all of the selector cohorts.
     *
     * These are a subset of all cohorts:
     *
     * Each selector has an associated list of base names of platform
     * (convenience) features.
     *
     * Display each selector name, then display each of the
     * base names which are associated with the selector, then display
     * each of the versions of the selector element base names.
     */
    @Test
    public void listSelectorCohorts() {
        Map<String, List<String>> cohorts = getRepository().getCohorts();

        printDoubleDashes();

        System.out.println("Selectors:");

        printDashes();

        VersionlessData.getSelectorCohorts().forEach((String selector, List<String> sBaseNames) -> {
            System.out.println("  [ " + selector + " ]");
            for (String featureName : sBaseNames) {
                System.out.println("    [ " + featureName + " ] [ " + cohorts.get(featureName) + " ]");
            }
        });

        printDoubleDashes();
    }

    /**
     * Display selector details.
     *
     */
    @Test
    public void listSelectorDetails() {
        Map<String, List<String>> cohorts = getRepository().getCohorts();

        printDoubleDashes();

        System.out.println("Selector Details:");

        printDashes();

        VersionlessData.getSelectorCohorts().forEach((String selector, List<String> seBaseNames) -> {
            System.out.println("  [ " + selector + " ]");
            for (String seBaseName : seBaseNames) {
                List<String> seCohort = cohorts.get(seBaseName);
                System.out.println("    [ " + seBaseName + " ] [ " + seCohort + " ]");

                for (String seVersion : seCohort) {
                    String seName = seBaseName + "-" + seVersion;
                    FeatureInfo seInfo = getFeature(seName);
                    if (seInfo == null) {
                        System.out.println("      [ " + seName + " ** NOT FOUND ** ]");
                        continue;
                    } else {
                        System.out.println("      [ " + seInfo.getName() + " ]");
                    }

                    seInfo.forEachSortedDepName((String depName) -> {
                        FeatureInfo depInfo = getFeature(depName);
                        if (depInfo == null) {
                            System.out.println("        [ " + depName + " ** NOT FOUND ** ]");
                        } else if (depInfo.isPublic()) {
                            System.out.println("        [ " + depInfo.getName() + " ]");
                        } else {
                            // Ignore
                        }
                    });
                }
            }
        });

        printDoubleDashes();
    }

    /**
     * Append values to a string builder.
     *
     * @param builder The builder to which to append values.
     * @param values Values to append to the builder.
     */
    private static void append(StringBuilder builder, Object...values) {
        for ( Object value : values ) {
            builder.append(value);
        }
    }

    /**
     * Copy values into storage, then sort them using the base string
     * comparator.
     *
     * @param storage Storage for the sorted values.
     * @param values Values to put into storage and sorted.
     */
    protected static void strSort(List<String> storage, Collection<String> values) {
        storage.addAll(values);
        storage.sort(Comparator.comparing(String::toString));
    }

    /**
     * Copy values into storage, then sort them using the version string
     * comparator.  (See {@link FeatureRepo#compareVersions(String, String)}.
     *
     * @param storage Storage for the sorted values.
     * @param values Values to put into storage and sorted.
     */
    protected static void verSort(List<String> storage, Collection<String> values) {
        storage.addAll(values);
        storage.sort(FeatureRepo::compareVersions);
    }

    /**
     * Display a table of selectors.  For each selector, for each base name of dependencies
     * of the selector, display the table mapping selector element versions to dependency
     * versions.
     */
    @Test
    public void listSelectorTable() {
        Map<String, List<String>> cohorts = getRepository().getCohorts();
        Map<String, List<String>> useSelectorCohorts = VersionlessData.getSelectorCohorts();

        // selectorName -> depBaseName -> sceVersion -> depVersion
        Map<String, Map<String, Map<String, String>>> useCohortRanges = VersionlessData.getCohortRanges();

        StringBuilder depBuilder = new StringBuilder();

        Set<String> scVersionsSet = new HashSet<>();
        List<String> scVersions = new ArrayList<>();
        List<String> depNames = new ArrayList<>();
        Set<String> handledDeps = new HashSet<>();

        printDoubleDashes();

        useSelectorCohorts.forEach( (String selectorName, List<String> scBaseNames) -> {
            System.out.println("Selector [ " + selectorName + " ]");
            printDashes();

            for ( String scBaseName : scBaseNames ) {
                List <String> sVersions = cohorts.get(scBaseName);
                scVersionsSet.addAll(sVersions);

                System.out.println("  [ [ " + scBaseName + " ] [ " + sVersions + " ] ]");
            }
            verSort(scVersions, scVersionsSet);
            scVersionsSet.clear();

            Map<String, Map<String, String>> cohortRange = useCohortRanges.get(selectorName);

            strSort(depNames, cohortRange.keySet());

            // System.out.println("S [ " + selectorName + " ] [ " + depNames + " ]");

            int maxLength = 0;
            for ( String depName : depNames ) {
                int depLength = depName.length();
                if ( depLength > maxLength ) {
                    maxLength = depLength;
                }
            }

            for ( String depName : depNames ) {
                if ( handledDeps.contains(depName) ) {
                    continue;
                }

                String prefixBefore;
                String prefixAfter;
                String updatedName;

                if ( VersionlessData.isSpanning(depName) ) {
                    prefixBefore = SPAN_PREFIX;
                    prefixAfter = null;
                    updatedName = null;
                } else if ( VersionlessData.isAdded(depName)) {
                    prefixBefore = ADDED_PREFIX;
                    prefixAfter = null;
                    updatedName = null;
                } else if ( VersionlessData.isRetired(depName)) {
                    prefixBefore = RETIRED_PREFIX;
                    prefixAfter = null;
                    updatedName = null;
                } else {
                    updatedName = VersionlessData.getSharedName(depName);
                    if ( updatedName != null ) {
                        prefixBefore = SHARED_PREFIX_BEFORE;
                        prefixAfter = SHARED_PREFIX_AFTER;
                    } else {
                        updatedName = VersionlessData.getChangedName(depName);

                        if ( updatedName != null ) {
                            prefixBefore = CHANGED_PREFIX_BEFORE;
                            prefixAfter = CHANGED_PREFIX_AFTER;
                        } else {
                            prefixBefore = UNKNOWN_PREFIX;
                            prefixAfter = null;
                        }
                    }
                }

                printDepLine(depBuilder, maxLength, prefixBefore, depName, cohortRange.get(depName), scVersions);

                if ( updatedName != null ) {
                    handledDeps.add(updatedName);
                    printDepLine(depBuilder, maxLength, prefixAfter, updatedName, cohortRange.get(updatedName), scVersions);
                }
            }

            scVersions.clear();
            depNames.clear();
            handledDeps.clear();

            printDashes();
        });

        printDoubleDashes();
    }

    private static final String UNKNOWN_PREFIX = "???";

    private static final String SPAN_PREFIX = ">->";

    private static final String RETIRED_PREFIX = ">-X";
    private static final String ADDED_PREFIX = "X->";

    private static final String CHANGED_PREFIX_BEFORE = ">-*";
    private static final String CHANGED_PREFIX_AFTER = "*->";

    private static final String SHARED_PREFIX_BEFORE = ">--";
    private static final String SHARED_PREFIX_AFTER = "-->";

    private void printDepLine(StringBuilder depBuilder, int maxLength,
                              String prefix, String depName,
                              Map<String, String> depVersions,
                              List<String> scVersions) {

        append(depBuilder, prefix, " [ ", depName, " ]: ");

        int missing = maxLength - depName.length();
        for ( int missingNo = 0; missingNo < missing; missingNo++ ) {
            depBuilder.append(' ');
        }

        for ( String scVersion : scVersions ) {
            int scLength = scVersion.length();
            int scMissing = 4 - scLength;
            String scAdj = ( (scMissing == 1) ? " " : (scMissing == 2) ? "  " : "");

            String depVersion = depVersions.get(scVersion);
            if ( depVersion == null ) {
                append(depBuilder, " [ ", scAdj, scVersion, " - ", "XXXX", " ]");

            } else {
                int depLength = depVersion.length();
                int depMissing = 4 - depLength;
                String depAdj = ( (depMissing == 1) ? " " : (depMissing == 2) ? "  " : "");

                append(depBuilder, " [ ", scAdj, scVersion, " - ", depAdj, depVersion, " ]");
            }
        }

        System.out.println(depBuilder.toString());

        depBuilder.setLength(0);
    }
}
//@formatter:on
