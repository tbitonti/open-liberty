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
package com.ibm.ws.artifact.fat;

import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.adapters.EntryAdapter;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 * Simple adapter that converts an entry to an integer representing the number of chars in it's filename.
 */
public class TestEntryAdapter implements EntryAdapter<Integer> {

    /** {@inheritDoc} */
    @Override
    public Integer adapt(Container root, OverlayContainer rootOverlay, ArtifactEntry ae, Entry entryToAdapt) {
        return entryToAdapt.getName().length();
    }

}
