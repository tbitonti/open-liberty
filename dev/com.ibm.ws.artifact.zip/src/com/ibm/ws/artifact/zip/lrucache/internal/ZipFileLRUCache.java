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

import com.ibm.ws.artifact.lrucache.LRUCacheImpl;
import com.ibm.ws.artifact.lrucache.LRUCachePolicy;
import com.ibm.ws.artifact.lrucache.LRUCacheSource;

public class ZipFileLRUCache extends LRUCacheImpl<ZipFileLRUCacheKey, byte[]>{

    public ZipFileLRUCache(String description,
                           LRUCachePolicy<ZipFileLRUCacheKey, byte[]> policy,
                           LRUCacheSource<ZipFileLRUCacheKey, byte[]> source) {

        super(description, policy, source);
    }

}
