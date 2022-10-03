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

import com.ibm.ws.artifact.lrucache.LRUCacheSource;
import com.ibm.ws.artifact.zip.cache.ZipCachingService;
import com.ibm.ws.artifact.zip.cache.ZipFileHandle;

public class ZipFileLRUCacheSource implements LRUCacheSource<ZipFileLRUCacheKey, byte[]> {

    public ZipFileLRUCacheSource(ZipCachingService zipCachingService) {
        this.zipCachingService = zipCachingService;
    }

    private final ZipCachingService zipCachingService;
    
    public ZipCachingService getZipCachingService() {
        return zipCachingService;
    }
    
    @Override
    public long getSize(ZipFileLRUCacheKey key, byte[] value) throws Exception {
        return value.length;
    }

    @Override
    public boolean contains(ZipFileLRUCacheKey key) throws Exception {
        ZipCachingService useService = getZipCachingService();

        ZipFileHandle zipHandle = useService.openZipFile(key.archivePath);
        
        try ( ZipFile zipFile = zipHandle.open() ) {
            return zipHandle.contains(zipFile, key.entryPath);
        }
    }

    @Override
    public byte[] acquire(ZipFileLRUCacheKey key) throws Exception {
        // TODO
    }

    @Override
    public void discard(ZipFileLRUCacheKey key, byte[] value) throws Exception {
        // NO-OP
    }
}
