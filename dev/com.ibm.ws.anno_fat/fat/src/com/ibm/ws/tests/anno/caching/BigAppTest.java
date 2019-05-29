/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package com.ibm.ws.tests.anno.caching;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import componenttest.custom.junit.runner.Mode;

/**
 * Comprehensive test of caching options, including collection of
 * timing data.
 */
@Mode(Mode.TestMode.FULL)
public class BigAppTest extends AnnoCachingTest {
    private static final int ITERATIONS = 3;

    public static class TimedStart {
        public int iterations;
        public long sum, min, max, avg;

        public void add(long newTime) {
            if ( iterations == 0 ) {
                min = newTime;
                max = newTime;
            } else if ( newTime < min ) {
                min = newTime;
            } else if ( newTime > max ) {
                max = newTime;
            }

            iterations++;
            sum = newTime;
            avg = sum / iterations;
        }
    }

    public static final boolean CACHE_DISABLED = false;
    public static final boolean CACHE_ENABLED = true;

    public static enum StartCase {
        DISABLED_CLEAN(CACHE_DISABLED, ServerStartType.CLEAN, "Disabled Clean"),

        DISABLED_DIRTY(CACHE_DISABLED, "Disabled Dirty"),
        DISABLED_DIRTY_ONE_SCAN(CACHE_DISABLED, "Disabled Dirty One Scan Thread"),
        DISABLED_DIRTY_UNLIMITED_SCAN(CACHE_DISABLED, "Disabled Dirty Unlimited Scan Threads"),

        ENABLED_UNPOPULATED(CACHE_ENABLED, "Enabled Unpopulated"),
        ENABLED_POPULATED(CACHE_ENABLED, "Enabled Populated"),
        ENABLED_POPULATED_ALWAYS_VALID(CACHE_ENABLED, "Enabled Populated Assumed Valid"),

        ENABLED_UNPOPULATED_ONE_SCAN(CACHE_ENABLED, "Enabled Unpopulated One Scan Thread"),
        ENABLED_UNPOPULATED_UNLIMITED_SCAN(CACHE_ENABLED, "Enabled Unpopulated Unlimited Scan Threads"),

        ENABLED_UNPOPULATED_ONE_WRITE(CACHE_ENABLED, "Enabled Unpopulated One Write Thread"),
        ENABLED_UNPOPULATED_UNLIMITED_WRITE(CACHE_ENABLED, "Enabled Unpopulated Unlimited Write Threads"),

        DISABLED_UPDATE(CACHE_DISABLED, "Disabled Application Update"),
        ENABLED_UPDATE(CACHE_ENABLED, "Enabled Application Update");

        private StartCase(boolean enabled, String description) {
        	this(enabled, ServerStartType.DIRTY, description);
        }

        private StartCase(boolean enabled, ServerStartType startType, String description) {
            this.enabled = enabled;
            this.startType = startType;
            this.description = description;
        }

        public final boolean enabled;

        public boolean getEnabled() {
            return enabled;
        }

        public final ServerStartType startType;
        
        public ServerStartType getStartType() {
        	return startType;
        }

        public final String description;

        public String getDescription() {
            return description;
        }
    }

    private static EnumMap<StartCase, TimedStart> timingData =
        new EnumMap<StartCase, TimedStart>(StartCase.class);

    static {
        for ( StartCase startCase : StartCase.values() ) {
            timingData.put( startCase, new TimedStart() );
        }
    }

    public static void add(StartCase startCase, long time) {
        timingData.get(startCase).add(time);

        info(startCase.description + ": Time: " + nsToMs(time) + "");
    }

    public static void log(StartCase startCase, TimedStart timedStart) {
        info(startCase.description);
        info("  MAX = " + nsToMs(timedStart.max));
        info("  MIN = " + nsToMs(timedStart.min));
        info("  AVG = " + nsToMs(timedStart.avg));
    }

    private static void logChange(StartCase initialCase, StartCase finalCase) {
        TimedStart initialData = timingData.get(initialCase);
        TimedStart finalData = timingData.get(finalCase);

        long initialAvg = initialData.avg;
        long finalAvg = finalData.avg;

        long diff = finalAvg - initialAvg; 
        float ratio = ( ( initialAvg == 0 ) ? 0.0f : ( ((float) diff) / initialAvg ) );

        info("Different: " + initialCase.description + " to: " + finalCase.description);
        info("  Initial [ " + format(initialAvg) + " ] Final [ " + format(finalAvg) + " ]");
        info("  Diff    [ " + format(diff) +       " ] Ratio [ " + format(ratio) + " ]");
    }

    public enum ServerStartType { 
        NONE, CLEAN, DIRTY; 
    }

    /**
     * Collect timing data for server starts.  The server is not
     * cleared between starts.
     *
     * @param startCase The case to which to record the timing data.
     *
     * @throws Exception Thrown if the server start fails.
     */
    private static void collect(StartCase startCase) throws Exception {
        info("Collect: " + startCase + ": iterations: " + Integer.toString(ITERATIONS));

        for ( int startNo = 0; startNo < ITERATIONS; startNo++ ) {
            long time = cycleServer(startCase);
            add(startCase, time);
        }
    }

    /**
     * Collect timing data for server starts.  Do a clean start with
     * the cache disabled before each timed server start.
     * 
     * @param startCase The case to which to record the timing data.
     * @param jvmOptions JVM options file to use for the timed server start.
     * 
     * @throws Exception Thrown if the server start fails.
     */
    private static void collect(StartCase startCase, String jvmOptions) throws Exception {
        info("Collect: " + startCase + ": with: " + jvmOptions + ": iterations: " + Integer.toString(ITERATIONS));

        for ( int startNo = 0; startNo < ITERATIONS; startNo++ ) {
            cleanupServer();

            installJvmOptions(jvmOptions);
            long time = cycleServer(startCase);
            add(startCase, time);
        }
    }

    //

    public static final String EAR_NAME = "big-cdi-meetings.ear";

    @BeforeClass
    public static void setUp() throws Exception {
        info("setUp ENTER BigAppTest");

        setSharedServer();
        info("setup: Server: " + getServerName());

        installJvmOptions("JvmOptions_Enabled.txt");

        String sourceAppsDirName = "test-applications";

        File destAppsDir = getInstalledAppsDir();
        String destAppsDirName = destAppsDir.getName();

        info("setUp: Copy: " + EAR_NAME + " from: " + sourceAppsDirName + " to: " + destAppsDirName); 

        getLibertyServer().copyFileToLibertyServerRoot(sourceAppsDirName, destAppsDirName, EAR_NAME);

        setEarName(EAR_NAME);
        installServerXml("big-cdi-meetings_server.xml"); 

        info("setUp RETURN BigAppTest");
    }

    public static final String THICK_BANNER = "==================================================";
    public static final String THIN_BANNER  = "--------------------------------------------------";

    @AfterClass
    public static void cleanUp() throws Exception {
        info("cleanUp ENTER");

        info(THICK_BANNER);

        info(THIN_BANNER);
        info("Iterations: " + ITERATIONS);
        info(THIN_BANNER);
        for ( Map.Entry<StartCase, TimedStart> startData : timingData.entrySet() ) {
            log( startData.getKey(), startData.getValue() );
        }
        info(THIN_BANNER);

        info(THICK_BANNER);

        info("Comparisons:");
        info(THIN_BANNER);

        logChange(StartCase.DISABLED_CLEAN, StartCase.DISABLED_DIRTY);
        logChange(StartCase.DISABLED_DIRTY, StartCase.DISABLED_DIRTY_ONE_SCAN );
        logChange(StartCase.DISABLED_DIRTY, StartCase.DISABLED_DIRTY_UNLIMITED_SCAN );
        info(THIN_BANNER);

        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_POPULATED);
        logChange(StartCase.ENABLED_POPULATED,  StartCase.ENABLED_POPULATED_ALWAYS_VALID);
        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_POPULATED_ALWAYS_VALID);
        info(THIN_BANNER);

        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_UNPOPULATED_ONE_SCAN );
        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_UNPOPULATED_UNLIMITED_SCAN );
        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_UNPOPULATED_ONE_WRITE);
        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_UNPOPULATED_UNLIMITED_WRITE);
        info(THIN_BANNER);

        logChange(StartCase.DISABLED_DIRTY, StartCase.ENABLED_UNPOPULATED);
        logChange(StartCase.ENABLED_UNPOPULATED,  StartCase.ENABLED_POPULATED);
        logChange(StartCase.DISABLED_DIRTY, StartCase.ENABLED_POPULATED);
        info(THIN_BANNER);

        logChange( StartCase.DISABLED_UPDATE, StartCase.ENABLED_UPDATE );
        info(THIN_BANNER);

        info(THICK_BANNER);

        info("cleanUp RETURN");
    }

    private static long cycleServer(StartCase startCase)
        throws Exception {

        long lStartTime = System.nanoTime();

        if ( startCase.startType == ServerStartType.CLEAN ) {
            startServerClean();
        } else {
            startServerDirty();
        }

        long lEndTime = System.nanoTime();
        long elapsed = lEndTime - lStartTime;

        logBlock(startCase.description); 
        info("Server started: " + nsToMs(elapsed) + " (ms)");

        stopServer();

        return elapsed;
    }

    private static void cleanupServer() throws Exception {
        installJvmOptions("JvmOptions_Disabled.txt");
        startServerClean();
        stopServer();
    }

    private static void populateServer() throws Exception {
        installJvmOptions("JvmOptions_Enabled.txt");
        startServerClean();
        stopServer();
    }

    //

    @Test
    public void bigApp_testDisabledClean() throws Exception {
        cleanupServer();

        installJvmOptions("JvmOptions_Disabled.txt");
        collect(StartCase.DISABLED_CLEAN);
    }

    @Test
    public void bigApp_testDisabledDirty() throws Exception {
        cleanupServer();

        installJvmOptions("JvmOptions_Disabled.txt");
        collect(StartCase.DISABLED_DIRTY);
    }

    @Test
    public void bigApp_testDisabledDirtyOneScanThread() throws Exception {
        cleanupServer();

        installJvmOptions("JvmOptions_Disabled_ScanThreads_1.txt");
        collect(StartCase.DISABLED_DIRTY_ONE_SCAN);
    }

    @Test
    public void bigApp_testDisabledDirtyUnlimitedScanThreads() throws Exception {
        cleanupServer();

        installJvmOptions("JvmOptions_Disabled_ScanThreads_-1.txt");
        collect(StartCase.DISABLED_DIRTY_UNLIMITED_SCAN);
    }

    //

    @Test
    public void bigApp_testEnabledUnpopulated() throws Exception {
        collect(StartCase.ENABLED_UNPOPULATED, "JvmOptions_Enabled.txt"); 
    }

    @Test
    public void bigApp_testEnabledPopulated() throws Exception {
        populateServer();

        installJvmOptions("JvmOptions_Enabled.txt");
        collect(StartCase.ENABLED_POPULATED);
    }

    @Test
    public void bigApp_testEnabledPopulatedValid() throws Exception {
        populateServer();

        installJvmOptions("JvmOptions_Enabled_AlwaysValid.txt");
        collect(StartCase.ENABLED_POPULATED_ALWAYS_VALID);
    }

    //

    @Test
    public void bigApp_testEnabledUnpopulatedOneWriteThread() throws Exception {
        collect(StartCase.ENABLED_UNPOPULATED_ONE_WRITE, "JvmOptions_Enabled_WriteThreads_1.txt"); 
    }

    @Test
    public void bigApp_testEnabledUnpopulatedUnlimitedWriteThreads() throws Exception {
        collect(StartCase.ENABLED_UNPOPULATED_UNLIMITED_WRITE, "JvmOptions_Enabled_WriteThreads_-1.txt");
    }

    @Test
    public void bigApp_testEnabledUnpopulatedOneScanThread() throws Exception {
        collect(StartCase.ENABLED_UNPOPULATED_ONE_SCAN, "JvmOptions_Enabled_ScanThreads_1.txt");
    }

    @Test
    public void bigApp_testEnabledUnpopulatedUnlimitedScanThreads() throws Exception {
        collect(StartCase.ENABLED_UNPOPULATED_UNLIMITED_SCAN, "JvmOptions_Enabled_ScanThreads_-1.txt");
    }

    //

    @Test
    public void bigApp_testDisabledAppUpdate() throws Exception { 
        installJvmOptions("JvmOptions_Disabled.txt");
        startServerClean();

        StartCase startCase = StartCase.DISABLED_UPDATE;

        for ( int startNo = 0; startNo < ITERATIONS; startNo++ ) {
            long removeTime = timedRemoveJar("pmt.jar");
            add(startCase, removeTime);

            long addTime = timedAddJar("pmt.jar");
            add(startCase, addTime);
        }

        stopServer();
    }

    @Test
    public void bigApp_testEnabledAppUpdate() throws Exception { 
        installJvmOptions("JvmOptions_Enabled.txt");
        startServerClean();

        StartCase startCase = StartCase.ENABLED_UPDATE;

        for ( int startNo = 0; startNo < ITERATIONS; startNo++ ) {
            long removeTime = timedRemoveJar("pmt.jar");
            add(startCase, removeTime);

            long addTime = timedAddJar("pmt.jar");
            add(startCase, addTime);
        }

        stopServer();
    }

    public long timedRemoveJar(String jarName) throws Exception {
        logBlock("Removing jar: " + jarName);
        renameJarFileInApplication("big-cdi-meetings.war", jarName, jarName + "_backup");
        return waitForAppUpdate();
    }

    public long timedAddJar(String jarName) throws Exception {
        logBlock("Adding jar: " + jarName);
        renameJarFileInApplication("big-cdi-meetings.war", jarName + "_backup", jarName);
        return waitForAppUpdate();
    }
}
