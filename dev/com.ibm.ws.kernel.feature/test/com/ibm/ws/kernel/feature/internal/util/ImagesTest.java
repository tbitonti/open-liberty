/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.kernel.feature.internal.util;

import java.io.File;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;

public class ImagesTest {
    public static final String IMAGES_PROJECT_PATH = "../build.image";
    public static final String IMAGES_PATH = IMAGES_PROJECT_PATH + "/" + "profiles";

    public static final String IMAGES_OUTPUT_PATH = "./build/images/images.xml";

    @Test
    public void testPrint() throws Exception {
        File rootInputFile = new File(IMAGES_PATH);
        String rootInputPath = rootInputFile.getAbsolutePath();
        if (!rootInputFile.exists()) {
            Assert.fail("Images input [ " + rootInputPath + " ] does not exist");
        } else {
            System.out.println("Images input [ " + rootInputPath + " ]");
        }

        File imagesOutputFile = new File(IMAGES_OUTPUT_PATH);
        String imagesOutputPath = imagesOutputFile.getAbsolutePath();
        File outputParent = imagesOutputFile.getParentFile();
        String outputParentPath = outputParent.getAbsolutePath();
        outputParent.mkdirs();
        if (!outputParent.exists()) {
            Assert.fail("Images output [ " + outputParentPath + " ] does not exist");
        } else if (!outputParent.isDirectory()) {
            Assert.fail("Images output [ " + outputParentPath + " ] is not a directory");
        } else {
            System.out.println("Images output [ " + imagesOutputPath + " ]");
        }

        List<File[]> imageDirs = ImageReader.selectImageDirs(rootInputFile);
        System.out.println("Selected [ " + imageDirs.size() + " ] feature files");

        Images images = ImageReader.readImages(imageDirs);
        System.out.println("Read [ " + images.getImages().size() + " ] images from [ " + rootInputPath + " ]");

        ImageXML.write(imagesOutputFile, images);
        System.out.println("Wrote [ " + images.getImages().size() + " ] images to [ " + imagesOutputPath + " ]");
    }
}
