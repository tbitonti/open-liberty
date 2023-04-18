/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.cache;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import com.ibm.ws.cache.test.stub.StubComponentContext;

public class CacheServiceImplThunk {
    public static final String CACHE_SERVICE_ID = "cacheService";

    public static CacheServiceImpl start() {
        CacheServiceImpl cs = new CacheServiceImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("id", CACHE_SERVICE_ID);

        start(cs, new StubComponentContext(), props);

        return cs;
    }

    public static void start(CacheService cacheService, ComponentContext context, Map<String, Object> props) {
        ((CacheServiceImpl) cacheService).start(context, props);
    }

    public static void stop(CacheService cacheService) {
        ((CacheServiceImpl) cacheService).stop();
    }
}
