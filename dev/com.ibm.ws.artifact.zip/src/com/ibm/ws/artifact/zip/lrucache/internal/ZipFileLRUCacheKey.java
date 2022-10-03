/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.zip.lrucache.internal;

public final class ZipFileLRUCacheKey {
    public ZipFileLRUCacheKey(String archivePath, String entryPath) {
        this.archivePath = archivePath;
        this.entryPath = entryPath;
        this.hashCode = archivePath.hashCode() * 31 + entryPath.hashCode();
    }

    public final String archivePath;

    public String getArchivePath() {
        return archivePath;
    }

    public final String entryPath;

    public String getEntryPath() {
        return entryPath;
    }

    //

    public final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object other) {
        if ( other == null ) {
            return false;
        } else if ( other == this ) {
            return true;
        } else if ( !(other instanceof ZipFileLRUCacheKey) ) {
            return false;
        } else {
            ZipFileLRUCacheKey otherKey = (ZipFileLRUCacheKey) other;
            if ( !otherKey.archivePath.equals(this.archivePath) ) {
                return false;
            } else if ( !otherKey.entryPath.equals(this.entryPath) ) {
                return false;
            } else {
                return true;
            }
        }
    }
}
