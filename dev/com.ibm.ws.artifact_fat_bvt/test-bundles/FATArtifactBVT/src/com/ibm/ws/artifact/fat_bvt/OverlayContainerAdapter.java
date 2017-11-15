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
package com.ibm.ws.artifact.fat_bvt;

import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 * Returns the root overlay container for the container being adapted.
 */
public class OverlayContainerAdapter implements ContainerAdapter<OverlayContainer> {

    /** {@inheritDoc} */
    @Override
    public OverlayContainer adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer artifactContainer, Container containerToAdapt) {
        return rootOverlay;
    }

}
