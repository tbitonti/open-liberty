/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wsspi.kernel.service.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.ibm.ws.kernel.security.thread.ThreadIdentityManager;

/**
 * A set of utilities for accessing the java system.
 */
public class SystemUtils {

    public static void addShutdownHook(final Thread shutdownThread) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    Runtime.getRuntime().addShutdownHook(shutdownThread);
                    return null;
                }
            });
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    public static String getProperty(final String propertyName) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(propertyName);
                }
            });
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    public static long getNanoTime() {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Long>() {
                @Override
                public Long run() {
                    return Long.valueOf( System.nanoTime() );
                }
            }).longValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }
}
