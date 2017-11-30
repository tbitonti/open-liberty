/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.archive;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.common.SharedOutputManager;

import com.ibm.ws.artifact.loose.internal.LooseContainerFactoryHelper;
import com.ibm.wsspi.artifact.ArtifactContainer;

/**
 * Tests for the Loose Config implementation of the artifact API
 */
public class LooseConfigTest {

    private static SharedOutputManager outputMgr;

    /**
     * Capture stdout/stderr output to the manager.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // There are variations of this constructor: 
        // e.g. to specify a log location or an enabled trace spec. Ctrl-Space for suggestions
        outputMgr = SharedOutputManager.getInstance();
        outputMgr.captureStreams();
    }

    /**
     * Final teardown work when class is exiting.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Make stdout and stderr "normal"
        outputMgr.restoreStreams();
    }

    /**
     * Individual teardown after each test.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        // Clear the output generated after each method invocation
        outputMgr.resetStreams();
    }

    /**
     * Test null returned for invalid loose XML. Done for defect 52399.
     */
    @Test
    public void testInvalidXml() {
        // Try to parse invalid XML and make sure we get null back
        LooseContainerFactoryHelper factory = new LooseContainerFactoryHelper();
        File invalidXmlFile = new File("resources/InvalidXml.xml");
        Assert.assertTrue("Test XML file does not exist", invalidXmlFile.exists());
        //passing null as the cachedir is seriously discouraged.. 
        //but since we don't actually plan to use the Container.. it'll do. 
        ArtifactContainer container = factory.createContainer(null, invalidXmlFile);
        Assert.assertNull("Able to create a container even with invalid XML", container);

    }
}
