/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011, 2017
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat_bvt.bundle;

import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.EntryAdapter;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 *
 */
public class EntryVerifier implements EntryAdapter<String> {
    private String getIDForContainer(Container c) throws UnableToAdaptException {
        String totalPath = c.getPath();
        Container root = c.getRoot();
        Entry entryInParent = root.adapt(Entry.class);
        while (entryInParent != null) {
            totalPath = entryInParent.getPath() + "#" + totalPath;
            Container parentContainer = entryInParent.getRoot();
            entryInParent = parentContainer.adapt(Entry.class);
        }
        return totalPath;
    }

    /** {@inheritDoc} */
    @Override
    public String adapt(Container root, OverlayContainer rootOverlay, ArtifactEntry artifactEntry, Entry entryToAdapt) throws UnableToAdaptException {
        String totalPath = null;
        //all root entries have a parent of / which _ends_ in / therefore we don't need the extra / added.
        if (entryToAdapt.getEnclosingContainer().isRoot()) {
            totalPath = getIDForContainer(entryToAdapt.getEnclosingContainer()) + entryToAdapt.getName();
        } else {
            totalPath = getIDForContainer(entryToAdapt.getEnclosingContainer()) + "/" + entryToAdapt.getName();
        }

        //tests that apply to all adaptable nodes.. 
        //(easy place to run them)
        Entry e = root.getEntry(entryToAdapt.getPath());
        if (e == null) {
            return "FAIL: unable to obtain entry via root using path [" + entryToAdapt.getPath() + "] for entry being adapted at " + totalPath;
        }

        if (!e.getPath().equals(entryToAdapt.getPath())) {
            return "FAIL: entry obtained via getEntry from root had path mismatch to entryToAdapt at " + totalPath + " expected[" + entryToAdapt.getPath() + "] got ["
                   + e.getPath() + "]";
        }

        return "adapter verified entry " + totalPath;
    }
}
