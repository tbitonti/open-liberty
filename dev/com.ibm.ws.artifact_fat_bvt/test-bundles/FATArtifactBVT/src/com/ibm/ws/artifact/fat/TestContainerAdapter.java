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
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 * Converts a container to the number of chars in it's path..
 */
public class TestContainerAdapter implements ContainerAdapter<Integer> {

    /** {@inheritDoc} */
    @Override
    public Integer adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer ac, Container containerToAdapt) {
        //System.out.println("Returning " + containerToAdapt.getPath().length() + " for path " + containerToAdapt.getPath());
        return containerToAdapt.getPath().length();
    }

}
