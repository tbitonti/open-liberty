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
package com.ibm.ws.artifact.fat.unableadapters;

import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 *
 */
public class BeforeUnableToAdaptContainerVerifier implements ContainerAdapter<Float> {
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
    public Float adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer artifactContainer, Container containerToAdapt) throws UnableToAdaptException {
        String totalPath = getIDForContainer(containerToAdapt);

        String name = containerToAdapt.getName();
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
