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
package com.ibm.ws.artifact.fat_bvt.bundle.unableadapters;

import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.EntryAdapter;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 *
 */
public class BeforeUnableToAdaptEntryVerifier implements EntryAdapter<Float> {
    private String getIDForContainer(Container c) throws UnableToAdaptException {
        String totalPath = c.getPath();
        Entry entryInParent = c.adapt(Entry.class);
        while (entryInParent != null) {
            totalPath = entryInParent.getPath() + "#" + totalPath;

            Container parentContainer = entryInParent.getEnclosingContainer();
            while (!parentContainer.isRoot()) {
                parentContainer = parentContainer.getEnclosingContainer();
            }

            entryInParent = parentContainer.adapt(Entry.class);
        }
        return totalPath;
    }

    /** {@inheritDoc} */
    @Override
    public Float adapt(Container root, OverlayContainer rootOverlay, ArtifactEntry artifactEntry, Entry entryToAdapt) throws UnableToAdaptException {
        String totalPath = null;
        //all root entries have a parent of / which _ends_ in / therefore we don't need the extra / added.
        if (entryToAdapt.getEnclosingContainer().isRoot()) {
            totalPath = getIDForContainer(entryToAdapt.getEnclosingContainer()) + entryToAdapt.getName();
        } else {
            totalPath = getIDForContainer(entryToAdapt.getEnclosingContainer()) + "/" + entryToAdapt.getName();
        }
        String name = entryToAdapt.getName();
        if (name.equals("ab") || name.equals("bb")) {
            //let all through to the next adapter.
            return null;
        } else if (name.equals("aa")) {
            return new Float(10);
        } else {
            return new Float(1);
        }
    }
}
