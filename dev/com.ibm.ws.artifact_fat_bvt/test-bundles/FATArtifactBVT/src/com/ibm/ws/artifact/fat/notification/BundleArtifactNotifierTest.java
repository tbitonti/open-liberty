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
package com.ibm.ws.artifact.fat.notification;

import java.io.File;
import java.io.PrintWriter;

import com.ibm.wsspi.artifact.factory.ArtifactContainerFactory;
import com.ibm.wsspi.artifact.overlay.OverlayContainerFactory;

/**
 *
 */
public class BundleArtifactNotifierTest extends AbstractArtifactNotificationTest {
    @Override
    public boolean testNoNotificationsAfterRemovalOfListener(PrintWriter out) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleNonRootListenerTest(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleOverlappingNonRootListenerTest(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleOverlappingRootListenerTest(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runMultipleRootListenerTest(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runSingleNonRootListenerTest(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean runSingleRootListenerTest(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean setup(String id, File testDataBaseDir, ArtifactContainerFactory acf, OverlayContainerFactory ocf, PrintWriter out) {
        out.println("Bundle Artifact Notifier NO-OP: " + id);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean tearDown(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testAddRemoveOfExistingViaOverlayBecomesModified(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testBaseAndOverlaidContentWithMaskUnmask(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testMultipleListenersViaOverlay(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNestedChangeCausesEntryChange(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonExistingUnderOverlay(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveMixedNotification(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveNotificationAtNonRoot(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean testNonRecursiveNotificationAtRoot(PrintWriter out) {
        // TODO Auto-generated method stub
        return true;
    }

}
