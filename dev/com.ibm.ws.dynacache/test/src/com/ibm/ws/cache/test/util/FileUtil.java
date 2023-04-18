/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.cache.test.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static File setupDir(String path) throws IOException {
        File dir = new File(path);
        FileUtil.ensureAbsent(dir);
        FileUtil.ensureExists(dir);
        return dir;
    }

    public static File ensureExists(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                return file;
            } else {
                throw new IOException("Target [ " + file.getAbsolutePath() + " ] exists as a simple file");
            }
        }

        file.mkdirs();

        if (!file.exists()) {
            throw new IOException("Target [ " + file.getAbsolutePath() + " ] could not be created as a directory");
        } else if (!file.isDirectory()) {
            throw new IOException("Target [ " + file.getAbsolutePath() + " ] re-appeared as a simple file");
        } else {
            return file;
        }
    }

    public static void ensureAbsent(File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        remove(file);
    }

    public static void remove(File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    remove(child);
                }
            }
        }

        file.delete();

        if (file.exists()) {
            throw new IOException("Failed to delete [ " + file.getAbsolutePath() + " ]");
        }
    }
}
