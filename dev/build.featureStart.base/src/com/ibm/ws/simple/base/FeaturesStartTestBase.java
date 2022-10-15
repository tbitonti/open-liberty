/*******************************************************************************
 * Copyright (c) 2019, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.simple.base;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.function.ToLongFunction;

import com.ibm.websphere.simplicity.OperatingSystem;
import com.ibm.websphere.simplicity.log.Log;

import componenttest.custom.junit.runner.Mode.TestMode;
import componenttest.custom.junit.runner.TestModeFilter;
import componenttest.topology.impl.JavaInfo;
import componenttest.topology.impl.LibertyServer;

/**
 * Test to verify that Open Liberty can start with every valid
 * single feature.
 *
 * Split into buckets to enable shorter builds. Notably, Windows on FYRE hardware
 * does not run in under two hours, which is the maximum allowed time for a FAT
 * bucket.
 *
 * Currently split into four buckets. The last time too much time was taken the
 * number of buckets was two. The number has been increased to four to give us
 * extra running room before a new split is necessary.
 */
public class FeaturesStartTestBase {
    // TODO: A cleaner implementation would have the test base have
    //       instance state.  Keeping the use of static / class state
    //       since the test implementation evolved from a single FAT
    //       test class which had a static injected test server,
    //       and which relies on the static before/after class API.

    public static void setParameters(Class<?> c,
                                     LibertyServer server, String serverName,
                                     int numBuckets, int bucketNo, int sparsity) throws Exception {
        setParameters(c,
                      server, serverName,
                      numBuckets, bucketNo, sparsity,
                      null);
    }

    public static void setParameters(Class<?> c,
                                     LibertyServer server, String serverName,
                                     int numBuckets, int bucketNo, int sparsity,
                                     String singleFeature) throws Exception {
        FeaturesStartTestBase.c = c;

        FeaturesStartTestBase.setServer(server, serverName);

        // Setting a single feature will override all of the
        // feature bucket and feature selection calculations.

        FeaturesStartTestBase.singleFeature = singleFeature;

        // TODO: Not sure if having 'numBuckets' be set is best.
        //       All of the buckets should set the same value.
        FeaturesStartTestBase.numBuckets = numBuckets;
        FeaturesStartTestBase.bucketNo = bucketNo;
        FeaturesStartTestBase.sparsity = sparsity;
    }

    //

    public static Class<?> c;

    protected static void logInfo(String m, String msg) {
        Log.info(c, m, msg);
    }

    protected static void logError(String m, String msg) {
        Log.error(c, m, null, msg);
    }

    protected static void logError(String m, String msg, Throwable th) {
        Log.error(c, m, th, msg);
    }

    //

    public static LibertyServer server;

    public static String serverName;
    public static String serverConfigPath;

    public static boolean serverIsZOS;
    public static int serverJavaLevel;
    public static boolean isHealthCenterAvailable;

    public static void setServer(LibertyServer server, String serverName) throws Exception {
        FeaturesStartTestBase.server = server;
        FeaturesStartTestBase.serverName = serverName;
        FeaturesStartTestBase.serverConfigPath = server.getServerConfigurationPath();
        FeaturesStartTestBase.serverIsZOS = server.getMachine().getOperatingSystem().equals(OperatingSystem.ZOS);

        FeaturesStartTestBase.serverJavaLevel = JavaInfo.forServer(server).majorVersion();

        // TODO: This is incorrect: The setting is computed for the java running
        //       the FAT, not for the java running the server.  'JavaInfo' has no
        //       API which will perform this check on a target server.

        FeaturesStartTestBase.isHealthCenterAvailable = JavaInfo.isSystemClassAvailable("com.ibm.java.diagnostics.healthcenter.agent.mbean.HealthCenter");
    }

    /**
     * Kill the server which has the specified PID.
     *
     * @throws Exception Thrown if the attempt to kill the server failed.
     */
    protected static void killProcess(String pid) throws Exception {
        server.getMachine().killProcess(Integer.parseInt(pid));
    }

    /**
     * Answer the PID of the server. Answer null if a running
     * server is not detected.
     *
     * @return The PID of the running server. Null if the server
     *         is not running.
     */
    protected static String getPid() {
        String m = "getPid";
        try {
            return server.getPid();
        } catch (Exception e) {
            logError(m, "Failed to obtain PID", e);
            return null;
        }
    }

    /**
     * Set the server configuration to have exactly the one specified feature.
     *
     * @param lastShortName The previously configured feature.
     * @param shortName     The feature to set in the server configuration.
     * @param timingResult  Storage for timing data. (Not currently used.)
     *
     * @throws Exception Thrown if the update failed.
     */
    public static void setFeature(String lastShortName, String shortName, TimingResult timingResult) throws Exception {
        String m = "setFeature";

        logInfo(m, "Configuring server [ " + serverName + " ] for feature [ " + shortName + " ]");
        if (lastShortName != null) {
            logInfo(m, "Prior feature [ " + lastShortName + " ]");
        }

        // Performance notes: The original, unfixed 'updateServerConfiguration' took
        // just over 10s on Windows to perform an update.  That is because the
        // implementation attempted to rename the newly written configuration onto the
        // server configuration.  That failed, but was performed with full retries.
        //
        // With a fix to 'updateServerConfiguration' the update time is reduced to just
        // over 1s.  Much better, but not as good as possible.
        //
        // With either direct rewrite implementation ('changeFeatures' or 'FileRewriter')
        // the update time plummets to about 0.03s.

        // A direct rewrite API is already present in LibertyServer!  Since the server is
        // stopped for this update, a direct rewrite is usable.

        // This implementation mixes the read and write steps too tightly to
        // collect separate timings.
        server.changeFeatures(Collections.singletonList(shortName));

        // Alternate, rewrite implementation.  Used before 'changeFeatures' was discovered.

        // Initially, the server configuration has an empty feature manager
        // element.
        //
        // The first update adds a feature into that element.
        //
        // Subsequent updates replace the feature.

        // String matchLine;
        // Set<Integer> additions;
        // if (lastShortName == null) {
        //     matchLine = "<featureManager>";
        //     additions = Collections.singleton(Integer.valueOf(0));
        // } else {
        //     matchLine = "<feature>" + lastShortName + "</feature>";
        //     additions = Collections.emptySet();
        // }
        // String featureLine = "<feature>" + shortName + "</feature>";
        // String[] matchLines = new String[] { matchLine };
        // String[] updateLines = new String[] { featureLine };
        // FileRewriter.update(serverConfigPath, matchLines, updateLines, additions);

        // The original implementation, which uses XML serialization.

        // ServerConfiguration config = server.getServerConfiguration();
        // Set<String> features = config.getFeatureManager().getFeatures();
        // features.clear();
        // features.add(shortName);
        // server.updateServerConfiguration(config);
    }

    /**
     * Forcibly stop the server.
     *
     * First, if the server is set as having been started,
     * use {@link LibertyServer#stopServer} to stop the server.
     *
     * Second, if the server PID is available, attempt to kill
     * the server process.
     *
     * @param pid           The PID of the running server.
     * @param allowedErrors Errors allowed in the stop server command.
     * @param failures      Storage for recording failures.
     * @param timingResult  Storage for timing data.
     *
     * @return True or false telling if the stop was successful.
     */
    public static boolean forceStopServer(String shortName,
                                          String pid,
                                          String[] allowedErrors,
                                          Map<String, Failure> failures,
                                          TimingResult timingResult) {
        String m = "forceStopServer";

        String description = "Server [ " + serverName + " ]" +
                             " PID [ " + pid + " ]" +
                             " Feature [ " + shortName + " ]";

        boolean didStop;
        boolean didKill;

        try {
            if (server.isStarted()) {
                logInfo(m, "Stopping: " + description);
                if (allowedErrors != null) {
                    logInfo(m, "Allowed errors [ " + Arrays.toString(allowedErrors) + " ]");
                }

                long initialStopNs = timingResult.getTimeNs();
                Exception boundException;
                try {
                    server.stopServer(allowedErrors);
                    didStop = true;
                    boundException = null;
                } catch (Exception e) {
                    didStop = false;
                    boundException = e;
                } finally {
                    timingResult.setStopNsFromInitial(initialStopNs);
                }
                // Handle the result *outside* of the timing block.
                if (boundException == null) {
                    logInfo(m, "Stopped: " + description);
                } else {
                    addFailure(m, failures, shortName, "Stop", boundException);
                }
            } else {
                logInfo(m, "Not started: " + description);
                didStop = true;
            }

        } finally {
            if (pid != null) {
                logInfo(m, "Killing: " + description);
                long initialKillNs = timingResult.getTimeNs();
                Exception boundException;
                try {
                    killProcess(pid);
                    didKill = true;
                    boundException = null;
                } catch (Exception e) {
                    didKill = false;
                    boundException = e;
                } finally {
                    timingResult.setKillNsFromInitial(initialKillNs);
                }
                // Handle the result *outside* of the timing block.
                if (boundException == null) {
                    logInfo(m, "Killed: " + description);
                } else {
                    addFailure(m, failures, shortName, "Kill", boundException);
                }
            } else {
                logInfo(m, "Null PID: " + description);
                didKill = true;
            }
        }

        return (didStop && didKill);
    }

    // Test features within a bucket, with an assigned range within
    // the features list.
    //
    // Tests run by this class are conditioned entirely on the number
    // of buckets and the bucket number.
    //
    // The server name must be updated to match the bucket parameters.

    public static String singleFeature;

    public static int numBuckets;
    public static int bucketNo;

    // Control parameter: Must be 0 or greater.
    // If greater than 0, test a subset of features, 1 of every SPARSITY.
    //
    // For example, setting '10' means run every 10'th test.
    // (At least 1 test is always run.)
    //
    // Use this when testing to limit the number of features which are
    // started.

    public static int sparsity;

    public static int firstFeatureNo; // The first test to be run.
    public static int lastFeatureNo; // One past the last test to be run.

    // Feature filters:
    //
    // Some features require a minimum java level;
    // Do not test any features if the server java level is less than that
    // required level for the feature.
    //
    // Some features are stable.
    // Do not test those features when the test mode is LITE.
    //
    // Some features cannot be started individually.
    // Do not test those features.
    //
    // Other features have specific prerequisities.  See 'skipFeature'.

    public static final Map<String, Integer> requiredLevels = new HashMap<>();
    public static final Set<String> stableFeatures = new HashSet<>();
    public static final Map<String, String> unstartableFeatures = new HashMap<>();

    //

    // TODO: Should this perhaps be initialized from java code?  Having the
    // external properties file doesn't seem to add anything.

    public static final String REQUIRED_LEVELS_NAME = "/feature-levels.properties";

    /**
     * Read the table of minimum required java levels for features.
     *
     * Keys are feature names, all lower case. Values are integer values
     * representing a minimum java level.
     *
     * @throws IOException Thrown if the properties file could not be read.
     */
    public static void initRequiredLevels() throws IOException {
        Properties props = new Properties();
        try (InputStream input = FeaturesStartTestBase.class.getResourceAsStream(REQUIRED_LEVELS_NAME)) {
            props.load(input);
        }

        props.forEach((sName, reqLevel) -> {
            String shortName = ((String) sName).toLowerCase();
            Integer requiredLevel = Integer.valueOf((String) reqLevel);
            requiredLevels.put(shortName, requiredLevel);
        });
    }

    /**
     * Test if the current server java level is sufficient to start
     * a specified feature. Answer null if the java level is sufficient.
     * Answer the required level if the java level is not sufficient.
     *
     * Three results are possible:
     *
     * No minimum java level is specified. Answer null. The feature is
     * expected to start.
     *
     * A minimum java level is specified, and the server java level is
     * not less than that specified minimum level. Answer null. The
     * feature is expected to start.
     *
     * A minimum java level is specified, and the server java level is
     * less than that specified minimum level. Answer the specified
     * minimum level. The feature is not expected to start.
     *
     * @param shortName The short name of the feature which is to be tested.
     *
     * @return Null if the feature should be startable using the current
     *         server java. The required minimum java level if the current
     *         server java level is too low to start the feature.
     */
    public static Integer getMinimumJava(String shortName) {
        Integer minJavaLevel = requiredLevels.get(shortName.toLowerCase());
        if (minJavaLevel == null) {
            return null;
        } else if (serverJavaLevel >= minJavaLevel) {
            return null;
        } else {
            return minJavaLevel;
        }
    }

    public static final String STABLE_FEATURES_NAME = "/features-stable.txt";

    public static void initStableFeatures() throws IOException {
        List<String> features;
        try (InputStream input = FeaturesStartTestBase.class.getResourceAsStream(STABLE_FEATURES_NAME)) {
            features = FileRewriter.read(input);
        }
        for (String feature : features) {
            feature = feature.trim();
            if (!feature.isEmpty() && (feature.charAt(0) != '#')) {
                stableFeatures.add(feature.toLowerCase());
            }
        }
    }

    public static boolean isStable(String shortName) {
        return stableFeatures.contains(shortName);
    }

    //

    public static final String UNSTARTABLE_FEATURES_NAME = "/features-unstartable.properties";

    /**
     * Read the table of features which cannot run individually.
     *
     * Keys are feature names, all lower case. Values are descriptive
     * reasons for why the feature cannot be run.
     *
     * @throws IOException Thrown if the properties file could not be read.
     */
    public static void initUnstartableFeatures() throws IOException {
        Properties props = new Properties();
        try (InputStream input = FeaturesStartTestBase.class.getResourceAsStream(UNSTARTABLE_FEATURES_NAME)) {
            props.load(input);
        }
        props.forEach((sName, reason) -> {
            String shortName = ((String) sName).toLowerCase();
            unstartableFeatures.put(shortName, (String) reason);
        });
    }

    public static final List<String> features = new ArrayList<>();

    /**
     * Initialize the features which are to be tested.
     *
     * These are read from the features folder of the test server.
     *
     * This initialization step requires that the test server already be
     * injected by the FAT runner.
     *
     * @throws IOException Thrown if the feature directory could not be read,
     *                         or if no features are available.
     */
    public static void initFeatures() throws IOException {
        String m = "initFeatures";

        File featuresDir = new File(server.getInstallRoot() + "/lib/features/");
        String featuresPath = featuresDir.getAbsolutePath();

        if (!featuresDir.exists()) {
            throw new IOException("Folder [ " + featuresPath + " ] does not exist");
        }

        File[] featureManifests = featuresDir.listFiles();
        if (featureManifests == null) {
            throw new IOException("Folder [ " + featuresPath + " ] could not be listed");
        } else if (featureManifests.length == 0) {
            throw new IOException("Folder [ " + featuresPath + " ] is empty");
        }

        Arrays.sort(featureManifests, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        // 'parseShortName' filters on OSGI meta-information:
        // client features, test features, and non-public features are filtered.
        //
        // 'skipFeature' filters on feature name and external information, for
        // example, required java level, required platform, disallowed as singleton.

        List<String> testFeatures = new ArrayList<>();
        List<String> nonPublicFeatures = new ArrayList<>();
        List<String> stableFeatures = new ArrayList<>();
        Map<String, String> untestableFeatures = new HashMap<>();

        for (File featureManifest : featureManifests) {
            String shortName = parseShortName(featureManifest, nonPublicFeatures, testFeatures);
            if (shortName == null) {
                continue; // Not readable, or filtered.
            }

            String lcShortName = shortName.toLowerCase();

            if (isStable(lcShortName)) {
                stableFeatures.add(shortName);
                if (TestModeFilter.FRAMEWORK_TEST_MODE == TestMode.LITE) {
                    continue;
                }
            }

            String skipReason = unstartableFeatures.get(lcShortName);
            if (skipReason == null) {
                skipReason = skipFeature(lcShortName);
            }
            if (skipReason != null) {
                untestableFeatures.put(shortName, skipReason);
                continue;
            }

            features.add(shortName);
        }

        if (!nonPublicFeatures.isEmpty()) {
            logInfo(m, "Non-public features: [ " + nonPublicFeatures.size() + " ]");
            display(m, "    ", 80, nonPublicFeatures);
        }
        if (!testFeatures.isEmpty()) {
            logInfo(m, "Test features: [ " + testFeatures.size() + " ]");
            display(m, "    ", 80, testFeatures);
        }
        if (!untestableFeatures.isEmpty()) {
            untestableFeatures.forEach((sName, skipReason) -> {
                logInfo(m, "Untestable: [ " + sName + " ]: " + skipReason);
            });
        }

        if (!stableFeatures.isEmpty()) {
            if (TestModeFilter.FRAMEWORK_TEST_MODE == TestMode.LITE) {
                logInfo(m, "TestMode [ " + TestModeFilter.FRAMEWORK_TEST_MODE + " ]: Do not test stable features");
            } else {
                logInfo(m, "TestMode [ " + TestModeFilter.FRAMEWORK_TEST_MODE + " ]: Do test stable features");
            }
            logInfo(m, "Stable features: [ " + stableFeatures.size() + " ]");
            display(m, "    ", 80, stableFeatures);
        }

        // All tests *MUST* use the same features, in the same order.
        features.sort((n1, n2) -> n1.compareTo(n2));

        logInfo(m, "All testable features: [ " + features.size() + " ]:");
        display(m, "    ", 80, features);

        if (features.isEmpty()) {
            throw new IOException("Folder [ " + featuresPath + " ] has no testable features");
        }

        if (singleFeature != null) {
            logInfo(m, "Single feature request: Test only [ " + singleFeature + " ]");
            int singleFeatureNo = features.indexOf(singleFeature);
            if (singleFeatureNo == -1) {
                throw new IOException("Feature [ " + singleFeature + " ] is not testable");
            } else {
                firstFeatureNo = singleFeatureNo;
                lastFeatureNo = singleFeatureNo + 1;
            }
        } else {
            int[] range = getRangeForBucket(features.size(), numBuckets, bucketNo);
            firstFeatureNo = range[0];
            lastFeatureNo = range[1];
        }
    }

    /**
     * Parse the short name of a feature manifest.
     *
     * Answer null if the feature manifest is a directory, or is not actually a manifest.
     * Answer null if the feature manifest is missing a short name value, or is a client
     * feature, a test feature, or a non-public feature.
     *
     * @param featureFile The feature manifest file which is to be parsed.
     * @param nonPublic   Storage for non-public feature names.
     * @param test        Storage for test feature names.
     *
     * @return The short name from the feature file.
     *
     * @throws IOException Thrown if an error occurs while reading the feature file.
     */
    public static String parseShortName(File featureFile, List<String> nonPublic, List<String> test) throws IOException {
        String m = "parseShortName";

        // The features directory has additional, non-manifest files.
        // Ignore them.
        if (featureFile.isDirectory() || !featureFile.getName().endsWith(".mf")) {
            return null;
        }

        try (Scanner scanner = new Scanner(featureFile)) {
            String symbolicName = null;
            String shortName = null;
            String unusableReason = null;

            boolean isNonPublic = false;
            boolean isTest = false;

            // Keep looping after setting the reason, so to have the
            // short name to display.
            while (((unusableReason == null) || (shortName == null) || (symbolicName == null)) &&
                   scanner.hasNextLine()) {

                String line = scanner.nextLine();

                // TODO: This checking is approximate!
                // Metadata may be split across multiple lines.

                if ((shortName == null) && line.startsWith("IBM-ShortName:")) {
                    shortName = line.substring("IBM-ShortName:".length()).trim();
                    String upperShortName = shortName.toUpperCase();
                    if (upperShortName.contains("EECLIENT") || upperShortName.contains("SECURITYCLIENT")) {
                        unusableReason = "client-only";
                    }

                } else if (line.contains("IBM-Test-Feature:") && line.contains("true")) {
                    isTest = true;
                    unusableReason = "test";

                } else if (line.startsWith("Subsystem-SymbolicName:")) {
                    // A short name is not available for most non-public features.
                    // Read the symbolic name as an alternative name.

                    String tail = line.substring("Subsystem-SymbolicName:".length());
                    int semiPos = tail.indexOf(';');
                    if (semiPos == -1) {
                        symbolicName = tail.trim();
                    } else {
                        symbolicName = tail.substring(0, semiPos).trim();
                    }

                    if (!tail.contains("visibility:=public")) {
                        isNonPublic = true;
                        unusableReason = "non-public";
                    }
                }
            }

            // TODO: Do all public features have a short name?
            // At least one feature, 'configfatlibertyinternals-1.0', has no short name,
            // and is not filtered by any of the tests.

            if (unusableReason == null) {
                if (shortName == null) {
                    unusableReason = "No 'IBM-ShortName'";
                }
            }

            if (unusableReason != null) {
                if (isNonPublic || isTest) {
                    String useName;
                    if (shortName != null) {
                        useName = shortName;
                    } else if (symbolicName != null) {
                        useName = symbolicName;
                    } else {
                        useName = featureFile.getName();
                        logInfo(m, "Strange: [ " + featureFile.getAbsolutePath() + " ] has no symbolic name");
                    }
                    if (isNonPublic) {
                        nonPublic.add(useName);
                    } else {
                        test.add(useName);
                    }
                    // Log non-public and test features all together.  Otherwise,
                    // these bloat the log.
                } else {
                    logInfo(m, "Cannot test feature [ " + symbolicName + " ] [ " + shortName + " ]: " + unusableReason);
                }
                return null;
            } else {
                return shortName;
            }
        }
    }

    /**
     * Tell if a feature is to be skipped.
     *
     * @param shortName The short name of the feature which is to be tested.
     * @return Null if the feature is to be tested. A string message if the
     *         feature is not to be tested.
     */
    public static String skipFeature(String shortName) {
        // z/OS Connect is NOT a z/OS only feature.
        // All other features which start with "zos" are z/OS only features.
        if (!serverIsZOS) {
            if (((shortName.startsWith("zos") && !shortName.startsWith("zosconnect-")) ||
                 shortName.equalsIgnoreCase("batchSMFLogging-1.0"))) {
                return "z/OS only";
            }
        }

        // Only IBM JDK includes Health Center and IBM JDK 11+ (Semeru)
        // is based on Adopt JDK 11+, which does not include Health Center.
        if (shortName.equalsIgnoreCase("logstashCollector-1.0")) {
            if (!isHealthCenterAvailable) {
                return "Requires Health Center";
            } else if (serverIsZOS) {
                return "Requires the attach API, which is disabled on z/OS";
            }
        }

        return null;
    }

    /**
     * Compute a range for a bucket within a larger range, dividing the
     * larger range as evenly as possible. Any leftover elements are
     * allocated to the initial buckets.
     *
     * @param numElements The number of elements of the overal range.
     * @param numBuckets  The number of buckets.
     * @param bucketNo    The bucket number (one based).
     *
     * @return The range as a half open interval: The first offset of the range,
     *         then the last offset of the range plus one.
     */
    public static int[] getRangeForBucket(int numElements, int numBuckets, int bucketNo) {
        // A zero based bucket number is easier to compute with.
        int useBucketNo = bucketNo - 1;

        int bucketSize = numElements / numBuckets;
        int residue = numElements % numBuckets;

        // Assign the range assuming an even split (residue == 0).

        int firstFeatureNo = useBucketNo * bucketSize;
        int lastFeatureNo = firstFeatureNo + bucketSize;

        // But there may be leftover features.
        // Allocate these one per bucket, starting with the first bucket.

        // When there is a residue, 'bucketSize' is imprecise:
        //   Bucket numbers [ 0 .. residue - 1 ] have a bucket size one greater.
        //   Bucket numbers [ residue .. bucketNo - 1 ] have the computed bucket size

        if (residue != 0) {
            if (useBucketNo < residue) {
                // In effect, add one to the bucket size.
                firstFeatureNo += useBucketNo;
                lastFeatureNo += useBucketNo + 1;
            } else {
                // In effect, add one to the bucket size **for preceeding buckets**
                firstFeatureNo += residue;
                lastFeatureNo += residue;
            }
        }

        return new int[] { firstFeatureNo, lastFeatureNo };
    }

    // Set the range for this bucket ...
    // Distribute features as evenly as possible:
    // 12 features with 4 buckets: 0..3, 3..6, 6..9. 9..12: 3, 3, 3, 3: 12
    // 11 features with 4 buckets: 0..3, 3..6, 6..9, 9..11: 3, 3, 3, 2: 11
    //  9 features with 4 buckets: 0..3, 3..5, 5..7, 7..9 : 3, 2, 2, 2: 9
    //
    // static final int[] NUM_FEATURES_RANGE = { 12, 11, 10, 9, 8 };
    // static final int[] NUM_BUCKETS_RANGE = { 2, 3, 4 };
    //
    // public static void main(String[] args) {
    //     for (int numFeatures : NUM_FEATURES_RANGE) {
    //         for (int numBuckets : NUM_BUCKETS_RANGE) {
    //             for (int bucketNo = 1; bucketNo <= numBuckets; bucketNo++) {
    //                 int[] range = getRange(numFeatures, numBuckets, bucketNo);
    //             }
    //         }
    //     }
    // }

    public static final Map<String, String[]> allowedErrors = new HashMap<>();

    /**
     * Initialize the allowed failure messages.
     *
     * This is a table of error message codes, specified per feature short name.
     *
     * The table spans all buckets.
     */
    public static void initAllowedErrors() {
        // TODO: OpenAPI code needs to be reworked so that it
        // doesn't leave threads around when the server stops
        // before it has finished initializing. Once OpenAPI is
        // fixed, these QUISCE_FAILURES should be removed.
        // TODO: This might be fixed by now.
        String[] QUIESCE_FAILURES = new String[] { "CWWKE1102W", "CWWKE1107W" };
        allowedErrors.put("openapi-3.0", QUIESCE_FAILURES);
        allowedErrors.put("openapi-3.1", QUIESCE_FAILURES);
        allowedErrors.put("mpOpenApi-1.0", QUIESCE_FAILURES);

        allowedErrors.put("batchSMFLogging-1.0", new String[] { "CWWKE0702E: .* com.ibm.ws.jbatch.smflogging" });

        // requires binaryLogging-1.0 to be enabled via bootstrap.properties
        allowedErrors.put("logAnalysis-1.0", new String[] { "CWWKE0702E: .* com.ibm.ws.loganalysis" });

        // The Rtcomm service is not able to connect to tcp://localhost:1883.
        allowedErrors.put("rtcomm-1.0", new String[] { "CWRTC0002E" });
        // The Rtcomm service is not able to connect to tcp://localhost:1883.
        // The Rtcomm service - The following virtual hosts could not be found or are not correctly configured: [abcdefg].
        allowedErrors.put("rtcommGateway-1.0", new String[] { "CWRTC0002E", "SRVE9956W" });

        // lets the user now certain config attributes will be ignored depending on whether or not 'inboundPropagation' is configured
        allowedErrors.put("samlWeb-2.0", new String[] { "CWWKS5207W: .* inboundPropagation" });
        // pulls in the samlWeb-2.0 feature
        allowedErrors.put("wsSecuritySaml-1.1", new String[] { "CWWKS5207W: .* inboundPropagation" });

        // Ignore required config warnings for the 'collectiveMember-1.0' feature, and all features that include it
        String[] COLLECTIVE_MEMBER_WARNINGS = new String[] { "CWWKG0033W: .*collectiveTrust", "CWWKG0033W: .*serverIdentity" };
        allowedErrors.put("collectiveMember-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("collectiveController-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("clusterMember-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("dynamicRouting-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("healthAnalyzer-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("healthManager-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("scalingController-1.0", COLLECTIVE_MEMBER_WARNINGS);
        allowedErrors.put("scalingMember-1.0", COLLECTIVE_MEMBER_WARNINGS);
    }

    // The error which is expected when the java level is less than the
    // required java level.

    public static final String JAVA_LEVEL_ERROR = "CWWKF0032E";
    public static final String[] JAVA_LEVEL_ERRORS = { JAVA_LEVEL_ERROR };

    //

    /**
     * Determine the java level used by the test server.
     *
     * Read and filter features from the test server.
     *
     * Setup the table of allowed errors, per feature short name.
     *
     * @throws Exception Thrown if the class level initialization failed. This
     *                       would be most likely because of a failure to read feature data, or
     *                       because the build produced no testable features.
     */
    public static void setUp() throws Exception {
        if (server == null) {
            throw new IllegalStateException("Server is not set");
        }

        initStableFeatures();
        initFeatures(); // Uses the stable features.
        initRequiredLevels();
        initAllowedErrors(); // Static table of known allowable errors.
    }

    //

    public static class StartupResult {
        public final boolean attempted;
        public final boolean started;
        public final String pid;

        /**
         * Factory method: The startup was not even attempted.
         */
        public StartupResult() {
            this.attempted = false;
            this.started = false;
            this.pid = null;
        }

        public static final boolean DID_ATTEMPT = true;
        public static final boolean DID_START = true;

        /**
         * Fully parameterized factory method.
         *
         * 'started' should not be true if 'attempted' is false.
         *
         * 'pid' should be null if 'attempted' is false. 'pid' may be null
         * if 'started' is false. That indicates a startup attempt which left
         * a dangling server process, but which reported failure.
         *
         * @param attempted True or false, telling if the startup was attempted.
         * @param started   True or false telling if the started was successful.
         * @param pid       The PID of the server process.
         */
        public StartupResult(boolean attempted, boolean started, String pid) {
            this.attempted = attempted;
            this.started = started;
            this.pid = pid;
        }
    }

    // There was a discussion of time-limiting the essential steps.
    // However, on some of the very slow FYRE hardware, these can take
    // several minutes or more.
    //
    // Also, the entire FAT is time-limited.

    public static class TimingResult {
        public final String shortName;

        public long updateNs = UNSET_NS;
        // public long readNs = UNSET_NS; // Subset of 'update'; no longer used
        // public long writeNs = UNSET_NS; // Subset of 'update'; no longer used
        public long startNs = UNSET_NS;
        public long pidNs = UNSET_NS;
        public long stopNs = UNSET_NS;
        public long killNs = UNSET_NS;

        public TimingResult(String shortName) {
            this.shortName = shortName;
        }

        public long getTimeNs() {
            return System.nanoTime();
        }

        public long getTimeNs(long initialNs) {
            return getTimeNs() - initialNs;
        }

        public void setUpdateNsFromInitial(long initialNs) {
            updateNs = getTimeNs(initialNs);
        }

        // public void setReadNsFromInitial(long initialNs) {
        //     readNs = getTimeNs(initialNs);
        // }

        // public void setWriteNsFromInitial(long initialNs) {
        //     writeNs = getTimeNs(initialNs);
        // }

        public void setStartNsFromInitial(long initialNs) {
            startNs = getTimeNs(initialNs);
        }

        public void setPidNsFromInitial(long initialNs) {
            pidNs = getTimeNs(initialNs);
        }

        public void setStopNsFromInitial(long initialNs) {
            stopNs = getTimeNs(initialNs);
        }

        public void setKillNsFromInitial(long initialNs) {
            killNs = getTimeNs(initialNs);
        }

        public long totalNs() {
            return sum(updateNs, startNs, pidNs, stopNs, killNs);
        }
    }

    //

    /**
     * Main test: Attempt to start each of the features in the specified bucket.
     *
     * @throws Exception Currently unused. A thrown exception will be logged and
     *                       will cause the test to fail.
     */
    public static void testStartFeatures() throws Exception {
        String m = "testStartFeatures";

        logInfo(m, "Test server: " + serverName);
        logInfo(m, "Test server java: " + serverJavaLevel);

        if (features.isEmpty()) {
            banner(m);
            logInfo(m, "No features were selected");
            banner(m);
            return;
        }

        int numFeatures = lastFeatureNo - firstFeatureNo;

        banner(m);
        logInfo(m, "Bucket [ " + bucketNo + " ] of [ " + numBuckets + " ]");
        logInfo(m, "Features [ " + features.size() + " ]");
        if (singleFeature != null) {
            logInfo(m, "Feature [ " + singleFeature + " ]");
        } else {
            logInfo(m, "  Count [ " + numFeatures + " ]");
            logInfo(m, "  First [ " + firstFeatureNo + " ]: [ " + features.get(firstFeatureNo) + " ]");
            logInfo(m, "  Last  [ " + (lastFeatureNo - 1) + " ]: [ " + features.get(lastFeatureNo - 1) + " ]");

            if (sparsity > 1) {
                int numSparseFeatures = numFeatures / sparsity;
                if ((numSparseFeatures * sparsity) < numFeatures) {
                    numSparseFeatures++;
                }
                numFeatures = numSparseFeatures;
                logInfo(m, "  Sparsity [ " + sparsity + " ]");
                logInfo(m, "  Sparse count [ " + numFeatures + " ]");
            }
        }

        banner(m);

        List<String> skipped = new ArrayList<>();

        List<String> expectedSuccesses = new ArrayList<>();
        Map<String, Failure> unexpectedFailures = new LinkedHashMap<>();

        List<String> expectedFailures = new ArrayList<>();
        Map<String, String> unexpectedSuccesses = new HashMap<>();

        Map<String, TimingResult> timingResults = new HashMap<>(numFeatures);

        String lastShortName;
        String nextShortName = null;

        for (int featureNo = firstFeatureNo; featureNo < lastFeatureNo; featureNo++) {
            String shortName = features.get(featureNo);

            if (singleFeature == null) {
                if ((sparsity > 0) && (((featureNo - firstFeatureNo) % sparsity) != 0)) {
                    skipped.add(shortName);
                    logInfo(m, "Skipping [ " + shortName + " ]: Filtered by SPARSITY");
                    continue;
                }
            }

            lastShortName = nextShortName;
            nextShortName = shortName;

            Integer javaMinimum = getMinimumJava(nextShortName);

            TimingResult timingResult = new TimingResult(nextShortName);
            timingResults.put(nextShortName, timingResult);

            try {
                StartupResult startupResult = null;
                try {
                    startupResult = startFeature(lastShortName, nextShortName, unexpectedFailures, timingResult);

                } finally {
                    // A null result is only possible if 'startFeature' failed with a throwable.
                    // In this case, do our best to stop the server.
                    // Make a dummy result that looks like an attempted startup.

                    if (startupResult == null) {
                        startupResult = new StartupResult(StartupResult.DID_ATTEMPT, !StartupResult.DID_START, getPid());
                    }

                    if (startupResult.attempted) {
                        String[] useAllowedErrors;
                        if (javaMinimum != null) {
                            useAllowedErrors = JAVA_LEVEL_ERRORS;
                        } else {
                            useAllowedErrors = allowedErrors.get(nextShortName);
                        }

                        if (forceStopServer(nextShortName, startupResult.pid, useAllowedErrors, unexpectedFailures, timingResult)) {
                            if (!unexpectedFailures.containsKey(nextShortName)) {
                                if (javaMinimum != null) {
                                    List<String> levelErrors = server.findStringsInLogs(JAVA_LEVEL_ERROR);
                                    if (levelErrors.isEmpty()) {
                                        String msg = "Unexpected successful start [ " + nextShortName + " ]: Current java [ " + serverJavaLevel + " ]: Required java: [ "
                                                     + javaMinimum
                                                     + " ]";
                                        unexpectedSuccesses.put(nextShortName, msg);
                                        logError(m, msg);
                                    } else {
                                        expectedFailures.add(nextShortName);
                                    }
                                } else {
                                    expectedSuccesses.add(nextShortName);
                                }
                            }
                        }
                    }
                }

            } finally {
                display(m, timingResult);
            }
        }

        logInfo(m, "Expected successes [ " + expectedSuccesses.size() + " ]");
        if (!expectedSuccesses.isEmpty()) {
            display(m, "    ", 80, expectedSuccesses);
        }

        logInfo(m, "Expected failures [ " + expectedFailures.size() + " ]");
        if (!expectedFailures.isEmpty()) {
            display(m, "    ", 80, expectedFailures);
        }

        logInfo(m, "Unexpected failures [ " + unexpectedFailures.size() + " ]");
        if (!unexpectedFailures.isEmpty()) {
            display(m, "    ", 80, unexpectedFailures.keySet());
        }

        logInfo(m, "Unexpected successes [ " + unexpectedSuccesses.size() + " ]");
        if (!unexpectedSuccesses.isEmpty()) {
            display(m, "    ", 80, unexpectedSuccesses.keySet());
        }

        if (!skipped.isEmpty()) {
            logInfo(m, "Skipped [ " + skipped.size() + " ]");
            display(m, "    ", 80, skipped);
        }

        display(m, timingResults);

        if (!unexpectedFailures.isEmpty() || !unexpectedSuccesses.isEmpty()) {
            StringBuilder msgBuilder = new StringBuilder();
            if (!unexpectedFailures.isEmpty()) {
                msgBuilder.append("Unexpected feature start failures [ ");
                msgBuilder.append(unexpectedFailures.keySet());
                msgBuilder.append(" ]: ");
                msgBuilder.append(unexpectedFailures.values());
                msgBuilder.append(".\n");
            }
            if (!unexpectedSuccesses.isEmpty()) {
                if (msgBuilder.length() != 0) {
                    msgBuilder.append(" ");
                }
                msgBuilder.append("Unexpected feature start successes [ ");
                msgBuilder.append(unexpectedSuccesses.keySet());
                msgBuilder.append(" ]: ");
                msgBuilder.append(unexpectedSuccesses.values());
                msgBuilder.append(".\n");
            }

            msgBuilder.append("If the failing features are test-only features, add 'IBM-Test-Feature: true'\n");
            msgBuilder.append("  to the feature manifests.\n");

            msgBuilder.append("If a failing feature requires a higher Java level, update 'feature-levels.properties'\n");
            msgBuilder.append("  to indicate the required minimum Java level.\n");

            msgBuilder.append("If a feature cannot be started by itself, or always produces particular errors\n");
            msgBuilder.append("  when started by itself, this class must be updated with the relevant information.\n");

            msgBuilder.append("When a feature fails to start, the defect for the failure should be assigned to\n");
            msgBuilder.append("  the team which is most responsible for the feature, or which is most responsible\n");
            msgBuilder.append("  for the underlying function which failed.\n");

            msgBuilder.append("Consider carefully whether to create a new defect or to associate the failure with\n");
            msgBuilder.append("  an existing defect.  Usually, the exception information must match exactly for\n");
            msgBuilder.append("  the failure to be associated with an existing defect.");

            fail(msgBuilder.toString());
        }
    }

    /**
     * Attempt to start a feature. Set the feature as the single configured feature
     * then start the server.
     *
     * Do not stop the feature.
     *
     * If an attempt was made to start the server, answer the PID of the started server.
     * (This can be null if the attempt was made but failed.)
     *
     * @param lastShortName The last configured short name. May be null.
     * @param shortName     The short name of the feature which is to be started.
     * @param failures      Storage for features which failed to start.
     * @param timingResult  Storage for time recording.
     *
     * @return The PID of the started server. Null if the feature could not be
     *         configured, or if the server startup was attempted but failed.
     */
    public static StartupResult startFeature(String lastShortName, String shortName, Map<String, Failure> failures, TimingResult timingResult) {
        String m = "startFeature";

        long initialUpdateNs = timingResult.getTimeNs();

        try {
            setFeature(lastShortName, shortName, timingResult);

        } catch (Exception e) {
            addFailure(m, failures, shortName, "Set feature", e);

            // Complete failure: The start was not attempted.
            return new StartupResult(!StartupResult.DID_ATTEMPT, !StartupResult.DID_START, null);

        } finally {
            timingResult.setUpdateNsFromInitial(initialUpdateNs);
        }

        // 'attempted' is now true

        long initialStartNs = timingResult.getTimeNs();
        boolean started;
        try {
            // Default start: Pre-clean and clean the server.
            server.startServer(shortName + ".log");
            started = true;
        } catch (Exception e) {
            started = false;
            addFailure(m, failures, shortName, "Start", e);
        } finally {
            timingResult.setStartNsFromInitial(initialStartNs);
        }

        // The PID may or may not be available:
        // PID retrieval is performed even if 'started' is false, so to handle
        // the case of an apparently failed startup which left a dangline process.

        long initialPidNs = timingResult.getTimeNs();

        String pid = getPid();
        logInfo(m, "Server PID: " + pid);

        timingResult.setPidNsFromInitial(initialPidNs);

        return new StartupResult(StartupResult.DID_ATTEMPT, started, pid);
    }

    public static class Failure {
        public final String shortName;
        public final String activity;
        public final Throwable failure;

        public Failure(String shortName, String activity) {
            this(shortName, activity, null);
        }

        public Failure(String shortName, String activity, Throwable failure) {
            this.shortName = shortName;
            this.activity = activity;
            this.failure = failure;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Feature [ ");
            builder.append(shortName);
            builder.append(" ] Activity [ ");
            builder.append(activity);
            builder.append(" ]");
            if (failure != null) {
                builder.append(": ");
                builder.append(failure);
            }
            return builder.toString();
        }
    }

    public static void addFailure(String m, Map<String, Failure> failures, String shortName, String activity, Throwable th) {
        logError(m, "Failed to start feature [ " + shortName + " ]: " + activity, th);
        failures.put(shortName, new Failure(shortName, activity, th));
    }

    //

    public static void banner(String m) {
        logInfo(m, "**************************************************************************");
    }

    public static void display(String m, TimingResult timingResult) {
        StringBuilder builder = new StringBuilder();
        builder.append("Feature [ " + timingResult.shortName + " ]: ");

        builder.append(format("Update", timingResult.updateNs));
        builder.append(", ");

        builder.append(format("Start", timingResult.startNs));
        builder.append(", ");

        builder.append(format("PID", timingResult.pidNs));
        builder.append(", ");

        logInfo(m, builder.toString());
        builder.setLength(0);

        builder.append("    ");

        builder.append(format("Stop", timingResult.stopNs));
        builder.append(", ");

        builder.append(format("Kill", timingResult.killNs));
        builder.append(", ");

        builder.append(format("Total", timingResult.totalNs()));

        logInfo(m, builder.toString());
        builder.setLength(0);
    }

    public static void display(String m, Map<String, TimingResult> timingResults) {
        Map<String, TimingSummary> summaries = new LinkedHashMap<>();

        summaries.put("Update", statistics("Update", timingResults, (TimingResult result) -> result.updateNs));
        // summaries.put("Read", statistics("Read", timingResults, (TimingResult result) -> result.readNs));
        // summaries.put("Write", statistics("Write", timingResults, (TimingResult result) -> result.writeNs));
        summaries.put("Start", statistics("Start", timingResults, (TimingResult result) -> result.startNs));
        summaries.put("PID", statistics("PID", timingResults, (TimingResult result) -> result.pidNs));
        summaries.put("Stop", statistics("Stop", timingResults, (TimingResult result) -> result.stopNs));
        summaries.put("Kill", statistics("Kill", timingResults, (TimingResult result) -> result.killNs));
        summaries.put("Total", statistics("Total", timingResults, (TimingResult result) -> result.totalNs()));

        logInfo(m, "Timing Summary:");

        StringBuilder builder = new StringBuilder();
        summaries.forEach((description, summary) -> {
            builder.append("[ ");
            builder.append(description);
            builder.append(" ]: ");

            if (summary.count == 0) {
                builder.append("** NONE **");
                logInfo(m, builder.toString());
                builder.setLength(0);

            } else {
                builder.append(format("Avg", summary.avg) + " ( " + summary.count + " ): ");
                builder.append(format("Total", summary.sum));
                logInfo(m, builder.toString());
                builder.setLength(0);

                logInfo(m, "  " + formatStat("Min", summary.min, summary.minShort));
                logInfo(m, "  " + formatStat("Max", summary.max, summary.maxShort));
            }
        });
    }

    public static class TimingSummary {
        public final String description;

        public TimingSummary(String description) {
            this.description = description;
        }

        public int count = 0;
        public long sum = 0L;
        public long avg = UNSET_NS;

        public long min = UNSET_NS;
        public String minShort = null;

        public long max = UNSET_NS;
        public String maxShort = null;
    }

    public static TimingSummary statistics(String description, Map<String, TimingResult> timingResults, ToLongFunction<TimingResult> producer) {
        TimingSummary summary = new TimingSummary(description);

        for (Map.Entry<String, TimingResult> resultEntry : timingResults.entrySet()) {
            String shortName = resultEntry.getKey();
            TimingResult result = resultEntry.getValue();
            long stat = producer.applyAsLong(result);

            if (stat == UNSET_NS) {
                continue;
            }

            summary.count++;
            summary.sum += stat;

            if ((summary.min == UNSET_NS) || (stat < summary.min)) {
                summary.min = stat;
                summary.minShort = shortName;
            }
            if ((summary.max == UNSET_NS) || (stat > summary.max)) {
                summary.max = stat;
                summary.maxShort = shortName;
            }
        }

        if (summary.count != 0) {
            summary.avg = summary.sum / summary.count;
        }

        return summary;
    }

    public static final long NS_IN_SEC = 1000000000;
    public static final long UNSET_NS = -1L;

    public static long sum(long... toAdd) {
        long sum = 0L;
        for (long nextToAdd : toAdd) {
            if (nextToAdd != UNSET_NS) {
                sum += nextToAdd;
            }
        }
        return sum;
    }

    public static final String nsAsSec(long ns) {
        return String.format("%.4f", Float.valueOf(((float) ns) / NS_IN_SEC));
    }

    public static String format(String description, long ns) {
        String nsText;
        if (ns == UNSET_NS) {
            nsText = "**UNSET**";
        } else {
            nsText = nsAsSec(ns);
        }

        return description + " [ " + nsText + " ]";
    }

    public static String formatStat(String description, long stat, String shortName) {
        if (stat == UNSET_NS) {
            return description + " [ **UNSET** ]";
        } else {
            return description + " [ " + nsAsSec(stat) + " ] ( " + shortName + " )";
        }
    }

    /**
     * Display values as a comma-delimited list, with values split across lines
     * at the specified length.
     *
     * @param m      The method requesting the display.
     * @param prefix A prefix to display on each line.
     * @param length The length at which to wrap the emitted lines.
     * @param values The values which are to be displayed.
     */
    public static void display(String m, String prefix, int length, Collection<String> values) {
        StringBuilder builder = new StringBuilder();
        int valuesOnLine = 0;

        int numValues = values.size();
        int valueNo = 0;

        for (String value : values) {
            if (valuesOnLine > 0) {
                builder.append(','); // Always need a comma.

                int spaceNeeded = 1; // Room for a space.
                spaceNeeded += value.length(); // Room for the value.
                if (valueNo < numValues - 1) {
                    spaceNeeded++; // Room for a comma after the value.
                }

                if ((builder.length() + spaceNeeded) > length) {
                    logInfo(m, builder.toString());
                    builder.setLength(0);
                    valuesOnLine = 0;
                } else {
                    builder.append(' ');
                }
            }

            if (valuesOnLine == 0) {
                builder.append(prefix);
            }

            // The first value on each line is added without
            // checking the length.  That allows over-size values
            // to be displayed.

            builder.append(value);
            valuesOnLine++;

            valueNo++;
        }

        if (valuesOnLine > 0) {
            logInfo(m, builder.toString());
            builder.setLength(0);
        }
    }
}
