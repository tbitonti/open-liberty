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
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 *
 */
public class ContainerVerifier implements ContainerAdapter<String> {

    private String getIDForContainer(Container c) throws UnableToAdaptException {
        String totalPath = c.getPath();
        Container root = c.getRoot();
        Entry entryInParent = root.adapt(Entry.class);
        while (entryInParent != null) {
            totalPath = entryInParent.getPath() + "#" + totalPath;
            root = entryInParent.getRoot();
            entryInParent = root.adapt(Entry.class);
        }
        return totalPath;
    }

    /** {@inheritDoc} */
    @Override
    public String adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer artifactContainer, Container containerToAdapt) throws UnableToAdaptException {
        String totalPath = getIDForContainer(containerToAdapt);

        //tests that apply to all adaptable nodes.. 
        //(easy place to run them)

        if (containerToAdapt.isRoot() && !containerToAdapt.getPath().equals("/")) {
            return "FAIL: adaptable container passed claimed to be root, but had path other than '/' at " + totalPath;
        }

        if (!root.isRoot()) {
            return "FAIL: passed root that was not root! at path " + totalPath;
        }

        if (!root.getPath().equals("/")) {
            return "FAIL: passed root did not have path '/'" + totalPath;
        }

        if (!containerToAdapt.isRoot()) {
            Entry e = root.getEntry(containerToAdapt.getPath());
            if (e == null) {
                return "FAIL: unable to obtain entry via root using path [" + containerToAdapt.getPath() + "] for container being adapted at " + totalPath;
            }
        }

        //checks that rely on structure helper manipulation..
        String name = containerToAdapt.getName();
        if ("aa".equals(name) || "ab".equals(name) || "ba".equals(name) || "baa".equals(name)) {
            if (!containerToAdapt.isRoot()) {
                return "FAIL: container with name " + name + " at " + totalPath + " was expected to be root because of structure helper, and was not.";
            }
        }

        return "adapter verified container " + totalPath;
    }
}
