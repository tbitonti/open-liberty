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
import java.io.PrintWriter;

import com.ibm.wsspi.artifact.factory.ArtifactContainerFactory;
import com.ibm.wsspi.artifact.overlay.OverlayContainerFactory;

/**
 *
 */
public interface ArtifactNotificationTest {
    public boolean setup(String id, File testDataBaseDir, ArtifactContainerFactory acf, OverlayContainerFactory ocf, PrintWriter out);

    /**
     * Test non recursive notification at root
     */
    public boolean testNonRecursiveNotificationAtRoot(PrintWriter out);

    /**
     * Test non recursive notification at non root
     */
    public boolean testNonRecursiveNotificationAtNonRoot(PrintWriter out);

    /**
     * Test non recursive notification at root
     */
    public boolean testNonRecursiveMixedNotification(PrintWriter out);

    /**
     * Test that notifications stop after the listener is removed..
     */
    public boolean testNoNotificationsAfterRemovalOfListener(PrintWriter out);

    /**
     * Test a single listener with path of "/" with add/remove/modified
     * 
     * @param out
     * @return true if passed
     */
    public boolean runSingleRootListenerTest(PrintWriter out);

    /**
     * Test multiple listeners with path of "/" with add/remove/modified
     * 
     * @param out
     * @return true if passed
     */
    public boolean runMultipleRootListenerTest(PrintWriter out);

    /**
     * Test a single listener with path deeper than "/" with add/remove/modified
     * 
     * @param out
     * @return true if passed
     */
    public boolean runSingleNonRootListenerTest(PrintWriter out);

    /**
     * Test multiple listeners with (non-overlapping) paths deeper than "/" with add/remove/modified
     * 
     * @param out
     * @return true if passed
     */
    public boolean runMultipleNonRootListenerTest(PrintWriter out);

    /**
     * Test a multiple listeners with (overlapping) paths where at least one path is "/" with add/remove/modified
     * 
     * @param out
     * @return true if passed
     */
    public boolean runMultipleOverlappingRootListenerTest(PrintWriter out);

    /**
     * Test a multiple listeners with (overlapping) paths deeper than "/" with add/remove/modified
     * 
     * @param out
     * @return true if passed
     */
    public boolean runMultipleOverlappingNonRootListenerTest(PrintWriter out);

    /**
     * Test that adding/removing to an overlay of content present in the base, becomes a modified notificaton
     * 
     * @param out
     * @return
     */
    public boolean testAddRemoveOfExistingViaOverlayBecomesModified(PrintWriter out);

    /**
     * Test that add/remove/modify under an overlay are passed through, with the container becoming the overlay.
     * 
     * @param out
     * @return
     */
    public boolean testNonExistingUnderOverlay(PrintWriter out);

    /**
     * Test multiple listeners via overlay (some overlapping, some non-overlapping paths).
     * 
     * @param out
     * @return
     */
    public boolean testMultipleListenersViaOverlay(PrintWriter out);

    /**
     * Test nested change causes entry change
     */
    public boolean testNestedChangeCausesEntryChange(PrintWriter out);

    /**
     * Test mask/unmask with base & overlaid content.
     * 
     * mask paths within the overlay, and confirm add remove for existing content, and nothing for non-existing
     * confirm masks causes events on mask/unmask
     * confirm masks affect events for affected paths after mask/unmask
     * confirm masks affect events for overlay-only, base-only, and both, content.
     * 
     * @param out
     * @return
     */
    public boolean testBaseAndOverlaidContentWithMaskUnmask(PrintWriter out);

    public boolean tearDown(PrintWriter out);
}
