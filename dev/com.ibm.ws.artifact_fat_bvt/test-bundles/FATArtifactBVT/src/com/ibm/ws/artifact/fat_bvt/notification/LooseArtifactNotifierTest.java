/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012, 2017
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat_bvt.notification;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.ArtifactNotifier;
import com.ibm.wsspi.artifact.DefaultArtifactNotification;
import com.ibm.wsspi.artifact.ArtifactNotifier.ArtifactListener;
import com.ibm.wsspi.artifact.ArtifactNotifier.ArtifactNotification;
import com.ibm.wsspi.artifact.factory.ArtifactContainerFactory;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainerFactory;

/**
 *
 */
public class LooseArtifactNotifierTest extends AbstractArtifactNotificationTest {

    private File baseDir = null;
    private File dataDir = null;
    private File data2Dir = null;
    private File cacheDir = null;
    private File overlayDir = null;
    private File overlayCacheDir = null;
    private File looseXml = null;
    private ArtifactContainer rootContainer = null;
    private OverlayContainer overlayContainer = null;
    private String id = null;
    private final int INTERVAL = 200;
    private final int SLEEP_DELAY = 30 * 1000;

    /** {@inheritDoc} */
    @Override
    public boolean setup(String id, File testDataBaseDir, ArtifactContainerFactory acf, OverlayContainerFactory ocf, PrintWriter out) {
        this.id = id;
        File base = createTempDir(id, out);
        if (base != null) {
            this.baseDir = base;
            //add a request to kill the dir when we're done.. 
            //we'll also tidy it up in tearDown, but this way makes setup easier.
            this.baseDir.deleteOnExit();

            this.dataDir = new File(baseDir, "DATA");
            this.data2Dir = new File(baseDir, "DATA2");
            this.cacheDir = new File(baseDir, "CACHE");
            this.overlayDir = new File(baseDir, "OVERLAY");
            this.overlayCacheDir = new File(baseDir, "OVERLAYCACHE");

            boolean mkdirsOk = true;
            mkdirsOk &= dataDir.mkdirs();
            mkdirsOk &= cacheDir.mkdirs();
            mkdirsOk &= overlayDir.mkdirs();
            mkdirsOk &= overlayCacheDir.mkdirs();

            if (mkdirsOk) {

                File jarFile = new File(dataDir, "test.jar");
                copyFile(new File(testDataBaseDir, "c/b.jar"), jarFile);

                File jar2File = new File(dataDir, "test2.jar");
                copyFile(new File(testDataBaseDir, "c/b.jar"), jar2File);

                File thingwar = new File(dataDir, "thing.jar");
                thingwar.mkdirs();

                //build loose xml for test case..
                try {
                    looseXml = new File(baseDir, "loose.xml");
                    FileWriter fw = new FileWriter(looseXml, false);
                    PrintWriter pw = new PrintWriter(fw);

                    //can't use the default xml from the other tests as it uses paths that would end 
                    //up being used by multiple test cases. 
                    String thing = thingwar.getAbsolutePath();
                    String dDir = dataDir.getAbsolutePath();
                    String dDir2 = data2Dir.getAbsolutePath();
                    String jar = jarFile.getAbsolutePath();
                    String jar2 = jar2File.getAbsolutePath();
                    String xmlData =
                                    "<?xml version=\"1.0\"?>" +
                                                    "<archive>" +
                                                    "        <dir targetInArchive=\"/nojar\" sourceOnDisk=\"" + dDir + "\" excludes=\"/*.jar\"/>" +
                                                    "        <dir targetInArchive=\"/nojar\" sourceOnDisk=\"" + dDir2 + "\" />" +
                                                    "        <archive targetInArchive=\"/DIRASJAR.jar\">" +
                                                    "            <dir targetInArchive=\"/\" sourceOnDisk=\"" + thing + "\" />" +
                                                    "        </archive>" +
                                                    "        <file targetInArchive=\"/mapped.jar\" sourceOnDisk=\"" + jar + "\" />" +
                                                    "        <file targetInArchive=\"/dir/subdir/mapped.jar\" sourceOnDisk=\"" + jar2 + "\" />" +
                                                    "</archive>";

                    pw.println(xmlData);
                    pw.flush();
                    pw.close();

                    this.rootContainer = acf.getContainer(this.cacheDir, looseXml);
                    if (rootContainer == null) {
                        out.println("FAIL: unable to create ArtifactContainer for looseXml in Loose Artifact Notifier testcase. (test case issue, not code issue)");
                        return false;
                    }
                    this.overlayContainer = ocf.createOverlay(OverlayContainer.class, rootContainer);
                    if (overlayContainer == null) {
                        out.println("FAIL: unable to create unable to create OverlayContainer for looseXml in for Loose Artifact Notifier testcase. (test case issue, not code issue)");
                        return false;
                    }
                    this.overlayContainer.setOverlayDirectory(overlayCacheDir, overlayDir);

                } catch (IOException io) {
                    out.println("FAIL: unable to create loose xml for test case (testcase issue, not code issue)");
                    io.printStackTrace(out);
                    return false;
                }

            } else {
                out.println("FAIL: unable to create dirs for File Artifact Notifier testcase. (test case issue, not code issue)");
                return false;
            }

            return true;
        } else {
            out.println("FAIL: unable to create temp dir for File Artifact Notifier testcase. (test case issue, not code issue)");
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean tearDown(PrintWriter out) {
        return removeFile(baseDir);
    }

    @Override
    public boolean testNoNotificationsAfterRemovalOfListener(PrintWriter out) {

        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wanted = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener test = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wanted, test);

        boolean pass = true;
        File createTest = null;
        try {
            //make a file
            createTest = new File(dataDir, "test");
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }
            //reset the sets
            invocable.add(test);
            test.getAdded().clear();
            test.getRemoved().clear();
            test.getModified().clear();
            invoked.clear();

            //clear us from the listener set..
            an.removeListener(test);

            //remove the file..
            if (!removeFile(createTest)) {
                out.println("FAIL: unable to remove file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was invoked as unexpectedly invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }
            //reset the sets
            invocable.add(test);
            test.getAdded().clear();
            test.getRemoved().clear();
            test.getModified().clear();
            invoked.clear();

            //try with 2 listeners.. 
            NotificationListener test2 = new NotificationListener(invocable, invoked);
            an.registerForNotifications(wanted, test2);
            an.registerForNotifications(wanted, test);

            //make sure 2 is in the set.
            invocable.add(test2);

            //create the file..
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listeners were not invoked as expected invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }
            //reset the sets for after removal..
            invocable.add(test);
            invocable.add(test2);
            test.getAdded().clear();
            test.getRemoved().clear();
            test.getModified().clear();
            test2.getAdded().clear();
            test2.getRemoved().clear();
            test2.getModified().clear();
            invoked.clear();

            //remove one of the listeners.. 
            an.removeListener(test2);

            //remove the file..
            if (!removeFile(createTest)) {
                out.println("FAIL: unable to remove file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }

            //deregister the last listener.. (just being clean here) 
            an.removeListener(test);

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        }
        //kill that test file if somehow it's still there. 
        removeFile(createTest);
        return pass;

    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleNonRootListenerTest(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/nojar/one"));
        ArtifactNotification wantedTwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/nojar/two"));
        //covers 61479 problem obtaining notifications from mapped entries in mapped subdirs. 
        ArtifactNotification wantedThree = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/dir"));

        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);
        an.registerForNotifications(wantedTwo, testtwo);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            invocable.clear();
            invoked.clear();

            //make a file
            nested = new File(createTest, "test");
            if (!nested.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            invocable.clear();
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            invocable.clear();
            invoked.clear();

            NotificationListener testthree = new NotificationListener(invocable, invoked);
            an.registerForNotifications(wantedThree, testthree);
            File jar2File = new File(dataDir, "test2.jar");
            if (!jar2File.delete()) {
                out.println("FAIL: could not delete jar2 file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected " + testthree);
                pass = false;
            }
            if (!verifyCounts(testthree, new String[] {}, new String[] { "/dir/subdir/mapped.jar" }, new String[] {}, out)) {
                out.println("FAIL: listener was not invoked correctly, expected deletion of /dir/subdir/mapped.jar " + testthree);
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        }
        //kill that test file if somehow it's still there. 
        removeFile(createTest);
        removeFile(nested);
        removeFile(other);
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleOverlappingNonRootListenerTest(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/nojar/one"));
        ArtifactNotification wantedTwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/nojar/one/two"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);
        an.registerForNotifications(wantedTwo, testtwo);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //make a file
            nested = new File(createTest, "two");
            if (!nested.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected [i] invoked " + invoked + " invocable " + invocable);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected [ii] invoked " + invoked + " invocable " + invocable);
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        }
        //kill that test file if somehow it's still there. 
        removeFile(createTest);
        removeFile(nested);
        removeFile(other);
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleOverlappingRootListenerTest(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        ArtifactNotification wantedTwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/nojar/one"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);
        an.registerForNotifications(wantedTwo, testtwo);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invoked.clear();

            //make a file
            nested = new File(createTest, "two");
            if (!nested.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        }
        //kill that test file if somehow it's still there. 
        removeFile(createTest);
        removeFile(nested);
        removeFile(other);
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleRootListenerTest(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        ArtifactNotification wantedTwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);
        an.registerForNotifications(wantedTwo, testtwo);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invoked.clear();

            //make a file
            nested = new File(createTest, "two");
            if (!nested.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        }
        //kill that test file if somehow it's still there. 
        removeFile(createTest);
        removeFile(nested);
        removeFile(other);
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runSingleNonRootListenerTest(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/nojar/one"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected for /nojar/one invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            //make a file
            nested = new File(createTest, "two");
            if (!nested.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected for /nojar/one/two invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was invoked unexpectedly for /nojar/two invoked:" + invoked + " invocable:" + invocable);
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {

            an.removeListener(testone);
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runSingleRootListenerTest(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            //make a file
            nested = new File(createTest, "two");
            if (!nested.createNewFile()) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //make a file in data2, obscured by data1
            //reset the sets
            invocable.add(testone);
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();
            invoked.clear();

            File d2 = new File(data2Dir, "one");
            if (!d2.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier d2 test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected zero, got " + testone);
                pass = false;
            }
            out.println("add of obscured folder " + testone);

            //reset the sets
            invocable.add(testone);
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();
            invoked.clear();

            if (!removeFile(createTest)) {
                out.println("FAIL: could not remove test file for File Notifier d2 test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            //the remove on /nojar/one/two is due to the tests above.. 
            if (!verifyCounts(testone, new String[] {}, new String[] { "/nojar/one/two" }, new String[] { "/nojar/one" }, out)) {
                pass = false;
            }
            out.println("remove of primary folder " + testone);

            //reset the sets
            invocable.add(testone);
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();
            invoked.clear();

            if (!createTest.mkdirs()) {
                out.println("FAIL: could not remove test file for File Notifier d2 test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            if (!verifyCounts(testone, new String[] {}, new String[] {}, new String[] { "/nojar/one" }, out)) {
                pass = false;
            }
            out.println("recreate of primary folder " + testone);

            //reset the sets
            invocable.add(testone);
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();
            invoked.clear();

            if (!removeFile(createTest)) {
                out.println("FAIL: could not remove test file for File Notifier d2 test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            if (!verifyCounts(testone, new String[] {}, new String[] {}, new String[] { "/nojar/one" }, out)) {
                pass = false;
            }
            out.println("remove of primary folder " + testone);

            //reset the sets
            invocable.add(testone);
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();
            invoked.clear();
            if (!removeFile(d2)) {
                out.println("FAIL: could not create test file for File Notifier d2 test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            if (!verifyCounts(testone, new String[] {}, new String[] { "/nojar/one" }, new String[] {}, out)) {
                pass = false;
            }
            out.println("remove of secondary folder " + testone);

            //reset the sets
            invocable.add(testone);
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();
            invoked.clear();

            if (!d2.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier d2 test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected zero, got " + testone);
                pass = false;
            }
            if (!verifyCounts(testone, new String[] { "/nojar/one" }, new String[] {}, new String[] {}, out)) {
                pass = false;
            }
            out.println("re-add of secondary folder " + testone);

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

            an.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testAddRemoveOfExistingViaOverlayBecomesModified(PrintWriter out) {
        ArtifactNotifier on = getNotifier(overlayContainer, out);
        if (on == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(overlayContainer, Collections.<String> singleton("/"));
        on.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        //don't register yet.. 

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {

            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            createTest = new File(dataDir, "other");
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }

            //register to listen against overlay
            on.registerForNotifications(wantedOne, testone);

            //override the file via the overlay.
            ArtifactEntry e = overlayContainer.getEntry("/nojar/other");
            if (e == null) {
                out.println("FAIL: unable to obtaine created entry via overlay (overlay issue, not notifier issue)");
                pass = false;
            } else {
                overlayContainer.addToOverlay(e, "/nojar/one", false); //set entry into overlay at path "/one"            

                if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                    out.println("FAIL: listener was not invoked as expected for addToOverlay");
                    pass = false;
                }

                if (!verifyCounts(testone, new String[] {}, new String[] {}, new String[] { "/nojar", "/nojar/one" }, out)) {
                    out.println("FAIL: add of existing did not become modified. " + testone);
                    pass = false;
                }

                //reset the sets
                invocable.add(testone);
                invoked.clear();
                testone.getAdded().clear();
                testone.getRemoved().clear();
                testone.getModified().clear();

                overlayContainer.removeFromOverlay("/nojar/one");
                if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                    out.println("FAIL: listener was not invoked as expected for removeFromOverlay");
                    pass = false;
                }

                if (!verifyCounts(testone, new String[] {}, new String[] {}, new String[] { "/nojar/one" }, out)) {
                    out.println("FAIL: remove of existing did not become modified. " + testone);
                    pass = false;
                }

            }
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

            on.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testBaseAndOverlaidContentWithMaskUnmask(PrintWriter out) {
        ArtifactNotifier on = getNotifier(overlayContainer, out);
        if (on == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(overlayContainer, Collections.<String> singleton("/"));
        on.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        //don't register yet.. 

        boolean pass = true;
        File createTest = null;
        try {

            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            createTest = new File(dataDir, "other");
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }

            //override the file via the overlay.
            ArtifactEntry e = rootContainer.getEntry("/nojar/other");
            if (e == null) {
                out.println("FAIL: unable to obtain created entry via overlay (overlay issue, not notifier issue)");
                pass = false;
            } else {
                overlayContainer.addToOverlay(e, "/nojar/one", false); //set entry into overlay at path "/one"

                //register to listen against overlay
                on.registerForNotifications(wantedOne, testone);

                overlayContainer.mask("/nojar/one");

                if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                    out.println("FAIL: listener was not invoked as expected");
                    pass = false;
                }

                if (!verifyCounts(testone, new String[] {}, new String[] { "/nojar/one" }, new String[] {}, out)) {
                    out.println("FAIL: mask of existing did not become removed. " + testone);
                    pass = false;
                } else if (!testone.getRemoved().get(0).getPaths().contains("/nojar/one")) {
                    out.println("FAIL: Incorrect path in notification, expected /nojar/one in removed. " + testone);
                    pass = false;
                }

                //reset the sets
                invocable.add(testone);
                invoked.clear();
                testone.getAdded().clear();
                testone.getRemoved().clear();
                testone.getModified().clear();

                overlayContainer.unMask("/nojar/one");

                if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                    out.println("FAIL: listener was not invoked as expected");
                    pass = false;
                }

                if (!verifyCounts(testone, new String[] { "/nojar/one" }, new String[] {}, new String[] {}, out)) {
                    out.println("FAIL: unmask of existing did not become added. " + testone);
                    pass = false;
                } else if (!testone.getAdded().get(0).getPaths().contains("/nojar/one")) {
                    out.println("FAIL: Incorrect path in notification, expected /nojar/one in added. " + testone);
                    pass = false;
                }

                //reset the sets
                invocable.add(testone);
                invoked.clear();
                testone.getAdded().clear();
                testone.getRemoved().clear();
                testone.getModified().clear();

                overlayContainer.mask("/nojar/other");

                if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                    out.println("FAIL: listener was not invoked as expected");
                    pass = false;
                }

                if (!verifyCounts(testone, new String[] {}, new String[] { "/nojar/other" }, new String[] {}, out)) {
                    out.println("FAIL: mask of existing did not become removed. " + testone);
                    pass = false;
                } else if (!testone.getRemoved().get(0).getPaths().contains("/nojar/other")) {
                    out.println("FAIL: Incorrect path in notification, expected /nojar/other in removed. " + testone);
                    pass = false;
                }

                //reset the sets
                invocable.add(testone);
                invoked.clear();
                testone.getAdded().clear();
                testone.getRemoved().clear();
                testone.getModified().clear();

                overlayContainer.unMask("/nojar/other");

                if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                    out.println("FAIL: listener was not invoked as expected");
                    pass = false;
                }

                if (!verifyCounts(testone, new String[] { "/nojar/other" }, new String[] {}, new String[] {}, out)) {
                    out.println("FAIL: unmask of existing did not become added. " + testone);
                    pass = false;
                } else if (!testone.getAdded().get(0).getPaths().contains("/nojar/other")) {
                    out.println("FAIL: Incorrect path in notification, expected /nojar/other in added. " + testone);
                    pass = false;
                }
            }
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            on.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testMultipleListenersViaOverlay(PrintWriter out) {
        ArtifactNotifier on = getNotifier(overlayContainer, out);
        if (on == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(overlayContainer, Collections.<String> singleton("/nojar/one"));
        ArtifactNotification wantedTwo = new DefaultArtifactNotification(overlayContainer, Collections.<String> singleton("/nojar/two"));
        ArtifactNotification wantedThree = new DefaultArtifactNotification(overlayContainer, Collections.<String> singleton("/nojar/one/test"));
        on.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);
        NotificationListener testthree = new NotificationListener(invocable, invoked);

        on.registerForNotifications(wantedOne, testone);
        on.registerForNotifications(wantedTwo, testtwo);
        on.registerForNotifications(wantedThree, testthree);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY) || !invoked.contains(testone)) {
                out.println("FAIL: listener was not invoked as expected [i] invoked " + invoked + " invocable " + invocable);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invocable.add(testthree);
            invoked.clear();

            //make a file
            nested = new File(createTest, "test");
            if (!nested.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY) || !invocable.contains(testtwo)) {
                out.println("FAIL: listener was not invoked as expected [ii] invoked " + invoked + " invocable " + invocable);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invocable.add(testtwo);
            invocable.add(testthree);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY) || !invoked.contains(testtwo)) {
                out.println("FAIL: listener was not invoked as expected [iii] invoked " + invoked + " invocable " + invocable);
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

            on.removeListener(testone);
            on.removeListener(testtwo);
            on.removeListener(testthree);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNestedChangeCausesEntryChange(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));

        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File other = null;
        File thingwar = new File(dataDir, "thing.jar");//built in setup.

        //make a file
        createTest = new File(thingwar, "one");
        if (!createTest.mkdirs()) {
            out.println("FAIL: could not create test file for File Notifier test (test case issue)");
            pass = false;
        }
        if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: listener was not invoked as expected invoked:" + invoked + " invocable" + invocable);
            pass = false;
        }

        if (!verifyCounts(testone, new String[] {}, new String[] {}, new String[] { "/DIRASJAR.jar" }, out)) {
            out.println("FAIL: nested change did not cause entry change for loose archive " + testone);
            pass = false;
        }

        //kill that test file if somehow it's still there. 
        removeFile(createTest);

        removeFile(other);
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonExistingUnderOverlay(PrintWriter out) {
        ArtifactNotifier on = getNotifier(overlayContainer, out);
        if (on == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(overlayContainer, Collections.<String> singleton("/"));
        on.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        on.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {

            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] { "/nojar/one" }, new String[] {}, new String[] {}, out)) {
                out.println("FAIL: Incorrect path in notification, expected /nojar/one in added. " + testone);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();

            if (!removeFile(createTest)) {
                out.println("FAIL: unable to delete file created to test overlay passthru (testcase issue, not a notification error)");
                pass = false;
            }

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] {}, new String[] { "/nojar/one" }, new String[] {}, out)) {
                out.println("FAIL: Incorrect path in notification, expected /nojar/one in removed. " + testone);
                pass = false;
            }

        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

            on.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveMixedNotification(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Arrays.asList(new String[] { "/", "!/" }));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            //make a file
            nested = new File(createTest, "two");
            if (!nested.createNewFile()) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during File Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

            an.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveNotificationAtNonRoot(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("!/nojar"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            out.println("One");
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for Loose Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. i expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            out.println("Two");
            //make a file
            nested = new File(createTest, "two");
            if (!nested.createNewFile()) {
                out.println("FAIL: could not create test file for Loose Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. ii expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            out.println("Three");
            //make a file
            other = new File(dataDir, "two");
            if (!other.createNewFile()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. iii expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }

            //end, dir should be clean now too.
        } catch (IOException io) {
            out.println("FAIL: io exception during Loose Notifier testcase");
            io.printStackTrace(out);
            pass = false;
        } finally {
            //kill that test file if somehow it's still there. 
            removeFile(createTest);
            removeFile(nested);
            removeFile(other);

            an.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveNotificationAtRoot(PrintWriter out) {
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("!/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        an.registerForNotifications(wantedOne, testone);

        boolean pass = true;
        File createTest = null;
        File nested = null;
        File other = null;
        try {
            out.println("negative test..");
            //make a file
            createTest = new File(dataDir, "one");
            if (!createTest.mkdirs()) {
                out.println("FAIL: could not create test file for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected none, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            out.println("resetting.. test..");

            //reset the sets
            invocable.add(testone);
            invoked.clear();

            out.println("removing xml test..");
            //remove the xml..
            if (!removeFile(looseXml)) {
                out.println("FAIL: could not remove xml for File Notifier test (test case issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
                pass = false;
            }
            out.println("end of test");

            //end, dir should be clean now too.
        } finally {
            try {
                //kill that test file if somehow it's still there. 
                removeFile(createTest);
                removeFile(nested);
                removeFile(other);

                an.removeListener(testone);
            } catch (Throwable t) {
                System.out.println("FAIL: caught throwable during finally ");
                t.printStackTrace(out);
            }
        }
        return pass;
    }
}
