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
package com.ibm.ws.artifact.fat_bvt.bundle.notification;

import java.io.File;
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
public class JarArtifactNotifierTest extends AbstractArtifactNotificationTest {
    private File testDataBaseDir = null;
    private File baseDir = null;
    private File cacheDir = null;
    private File overlayDir = null;
    private File jarFile = null;
    private File overlayCacheDir = null;
    private ArtifactContainerFactory acf;
    private OverlayContainerFactory ocf;
    @SuppressWarnings("unused")
    private String id = null;
    private final int INTERVAL = 200;
    private final int SLEEP_DELAY = 30 * 1000;

    /** {@inheritDoc} */
    @Override
    public boolean setup(String id, File testDataBaseDir, ArtifactContainerFactory acf, OverlayContainerFactory ocf, PrintWriter out) {
        this.id = id;
        this.testDataBaseDir = testDataBaseDir;
        this.acf = acf;
        this.ocf = ocf;
        File base = createTempDir(id, out);
        if (base != null) {
            this.baseDir = base;
            //add a request to kill the dir when we're done.. 
            //we'll also tidy it up in tearDown, but this way makes setup easier.
            this.baseDir.deleteOnExit();

            this.cacheDir = new File(baseDir, "CACHE");
            this.overlayDir = new File(baseDir, "OVERLAY");
            this.overlayCacheDir = new File(baseDir, "OVERLAYCACHE");

            this.jarFile = new File(baseDir, "TEST.JAR");
            boolean copyOk = copyFile(new File(this.testDataBaseDir, "c/b.jar"), this.jarFile);
            if (!copyOk) {
                out.println("FAIL: unable to copy test jar from " + new File(this.testDataBaseDir, "c/b.jar").getAbsolutePath() + " to " + this.jarFile.getAbsolutePath()
                            + " (test case issue, not code issue)");
                return false;
            }

            boolean mkdirsOk = true;
            mkdirsOk &= cacheDir.mkdirs();
            mkdirsOk &= overlayDir.mkdirs();
            mkdirsOk &= overlayCacheDir.mkdirs();

            if (!mkdirsOk) {
                out.println("FAIL: unable to create dirs for Jar Artifact Notifier testcase. (test case issue, not code issue)");
                return false;
            }

            return true;
        } else {
            out.println("FAIL: unable to create temp dir for Jar Artifact Notifier testcase. (test case issue, not code issue)");
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean tearDown(PrintWriter out) {
        //kill our test directory
        return removeFile(baseDir);
    }

    @Override
    public boolean testNoNotificationsAfterRemovalOfListener(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wanted = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener test = new NotificationListener(invocable, invoked);

        boolean pass = true;

        //add then remove the listener.. 
        an.registerForNotifications(wanted, test);
        an.removeListener(test);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 0, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: removed listener was invoked unexpectedly!");
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleNonRootListenerTest(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/ba"));
        ArtifactNotification wantedtwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/bb"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);
        an.registerForNotifications(wantedtwo, testtwo);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: non root listener was not invoked!");
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleOverlappingNonRootListenerTest(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/ba"));
        ArtifactNotification wantedtwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/ba/baa"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);
        an.registerForNotifications(wantedtwo, testtwo);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for multiple listener invocation.. expected both, got " + invoked.size() + " one? " + invoked.contains(testone) + " two? "
                        + invoked.contains(testtwo));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleOverlappingRootListenerTest(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        ArtifactNotification wantedtwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/ba"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);
        an.registerForNotifications(wantedtwo, testtwo);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for multiple listener invocation.. expected both, got " + invoked.size() + " one? " + invoked.contains(testone) + " two? "
                        + invoked.contains(testtwo));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleRootListenerTest(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        ArtifactNotification wantedtwo = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);
        an.registerForNotifications(wantedtwo, testtwo);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for multiple listener invocation.. expected both, got " + invoked.size() + " one? " + invoked.contains(testone) + " two? "
                        + invoked.contains(testtwo));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runSingleNonRootListenerTest(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/ba"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runSingleRootListenerTest(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testAddRemoveOfExistingViaOverlayBecomesModified(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        OverlayContainer overlay = ocf.createOverlay(OverlayContainer.class, rootContainer);
        overlay.setOverlayDirectory(this.overlayCacheDir, this.overlayDir);
        ArtifactNotifier an = getNotifier(overlay, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(overlay, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;
        try {
            an.registerForNotifications(wantedone, testone);

            ArtifactEntry ba = overlay.getEntry("/ba");
            overlay.addToOverlay(ba, "/bb", false);

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected for addToOverlay");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] { "/bb/baa", "/bb/baa/baa1.txt", "/bb/baa/baa2.txt" }, new String[] {}, new String[] { "/bb" }, out)) {
                out.println("FAIL: add of existing did not become modified. " + testone);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();

            overlay.removeFromOverlay("/bb");
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected for removeFromOverlay");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] {}, new String[] { "/bb/baa", "/bb/baa/baa1.txt", "/bb/baa/baa2.txt" }, new String[] { "/bb" }, out)) {
                out.println("FAIL: add of existing did not become modified. " + testone);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();

            if (!removeFile(jarFile)) {
                out.println("FAIL: Unable to remove file for root notifier removal (test issue not code issue)");
                pass = false;
            }
            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected for removeFromOverlay");
                pass = false;
            }

        } finally {
            an.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testBaseAndOverlaidContentWithMaskUnmask(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        OverlayContainer overlay = ocf.createOverlay(OverlayContainer.class, rootContainer);
        overlay.setOverlayDirectory(this.overlayCacheDir, this.overlayDir);
        ArtifactNotifier an = getNotifier(overlay, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(overlay, Collections.<String> singleton("/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;
        try {
            ArtifactEntry ba = overlay.getEntry("/ba");
            overlay.addToOverlay(ba, "/bb", false);

            //register to listen against overlay
            an.registerForNotifications(wantedone, testone);

            overlay.mask("/ba");

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] {}, new String[] { "/ba/baa/baa2.txt", "/ba/baa", "/ba", "/ba/baa/baa1.txt" }, new String[] {}, out)) {
                out.println("FAIL: mask of existing did not become removed. " + testone);
                pass = false;
            } else if (!testone.getRemoved().get(0).getPaths().contains("/ba")) {
                out.println("FAIL: Incorrect path in notification, expected /ba in removed. " + testone);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();

            overlay.unMask("/ba");

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] { "/ba/baa/baa2.txt", "/ba/baa", "/ba", "/ba/baa/baa1.txt" }, new String[] {}, new String[] {}, out)) {
                out.println("FAIL: unmask of existing did not become added. " + testone);
                pass = false;
            } else if (!testone.getAdded().get(0).getPaths().contains("/ba")) {
                out.println("FAIL: Incorrect path in notification, expected /ba in added. " + testone);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();

            overlay.mask("/bb");

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] {}, new String[] { "/bb/baa", "/bb/a.jar", "/bb/baa/baa1.txt", "/bb", "/bb/baa/baa2.txt" }, new String[] {}, out)) {
                out.println("FAIL: mask of existing did not become removed. " + testone);
                pass = false;
            } else if (!testone.getRemoved().get(0).getPaths().contains("/bb")) {
                out.println("FAIL: Incorrect path in notification, expected /bb in removed. " + testone);
                pass = false;
            }

            //reset the sets
            invocable.add(testone);
            invoked.clear();
            testone.getAdded().clear();
            testone.getRemoved().clear();
            testone.getModified().clear();

            overlay.unMask("/bb");

            if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

            if (!verifyCounts(testone, new String[] { "/bb/baa", "/bb/a.jar", "/bb/baa/baa1.txt", "/bb", "/bb/baa/baa2.txt" }, new String[] {}, new String[] {}, out)) {
                out.println("FAIL: unmask of existing did not become added. " + testone);
                pass = false;
            } else if (!testone.getAdded().get(0).getPaths().contains("/bb")) {
                out.println("FAIL: Incorrect path in notification, expected /bb in added. " + testone);
                pass = false;
            }

        } finally {
            an.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testMultipleListenersViaOverlay(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        OverlayContainer overlay = ocf.createOverlay(OverlayContainer.class, rootContainer);
        overlay.setOverlayDirectory(this.overlayCacheDir, this.overlayDir);
        ArtifactNotifier an = getNotifier(overlay, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone =
            new DefaultArtifactNotification(overlay, Collections.<String> singleton("/"));

        @SuppressWarnings("unused")
        ArtifactNotification wantedtwo =
            new DefaultArtifactNotification(overlay, Collections.<String> singleton("/"));

        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);
        NotificationListener testtwo = new NotificationListener(invocable, invoked);

        boolean pass = true;
        try {
            ArtifactEntry ba = overlay.getEntry("/ba");
            overlay.addToOverlay(ba, "/addedtooverlay", false);

            an.registerForNotifications(wantedone, testone);
            an.registerForNotifications(wantedone, testtwo);

            overlay.removeFromOverlay("/addedtooverlay");
            if (!waitForInvoked(invoked, 2, INTERVAL, SLEEP_DELAY)) {
                out.println("FAIL: listener was not invoked as expected");
                pass = false;
            }

        } finally {
            an.removeListener(testone);
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNestedChangeCausesEntryChange(PrintWriter out) {
        //nested content for a jar/zip are ZipEntries within the zip structure,
        //which cannot be altered except by deleting the entire jar, which causes
        //notifications against all entries inside.
        //this means that the only way to change a nested container is to delete the 
        //current zip, which is already tested above.
        out.println("Nested Causes Entry Change (No-Op for Jar)");
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonExistingUnderOverlay(PrintWriter out) {
        //the only tests that can modify existing under overlay are removes, which have been
        //driven by the other overlay tests within this class, so this test case can be a no-op.
        out.println("Non Existing Under Overlay (No-Op for Jar)");
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveMixedNotification(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedOne = new DefaultArtifactNotification(rootContainer, Arrays.asList(new String[] { "/", "!/" }));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedOne, testone);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveNotificationAtNonRoot(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("/bb"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
            pass = false;
        }
        return pass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveNotificationAtRoot(PrintWriter out) {
        ArtifactContainer rootContainer = acf.getContainer(cacheDir, jarFile);
        ArtifactNotifier an = getNotifier(rootContainer, out);
        if (an == null)
            return false;

        ArtifactNotification wantedone = new DefaultArtifactNotification(rootContainer, Collections.<String> singleton("!/"));
        an.setNotificationOptions(INTERVAL, false);

        Set<ArtifactListener> invocable = Collections.synchronizedSet(new HashSet<ArtifactListener>());
        Set<ArtifactListener> invoked = Collections.synchronizedSet(new HashSet<ArtifactListener>());

        NotificationListener testone = new NotificationListener(invocable, invoked);

        boolean pass = true;

        an.registerForNotifications(wantedone, testone);

        //kill the jar file 
        removeFile(jarFile);
        if (!waitForInvoked(invoked, 1, INTERVAL, SLEEP_DELAY)) {
            out.println("FAIL: incorrect count for single listener invocation.. expected one, got " + invoked.size() + " one? " + invoked.contains(testone));
            pass = false;
        }
        boolean found = false;
        for (ArtifactNotification z : testone.getRemoved()) {
            if (z.getPaths().contains("/")) {
                found = true;
            }
        }
        if (!found) {
            out.println("FAIL: delete of root did not cause / notifiy for !/ listener");
            pass = false;
        }
        return pass;
    }

}
