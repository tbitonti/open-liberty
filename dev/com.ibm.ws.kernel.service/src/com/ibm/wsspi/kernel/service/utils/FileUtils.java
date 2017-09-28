/*******************************************************************************
 * Copyright (c) 2015, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wsspi.kernel.service.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.kernel.security.thread.ThreadIdentityManager;

/**
 * A set of utilities for working with Files
 */
public class FileUtils {
    /**
     * Invoke {@link File#isFile()} from within a {@link PrivilegedAction}.
     */
    public static boolean fileIsFile(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.isFile() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#isDirectory()} from within a {@link PrivilegedAction}.
     */
    public static boolean fileIsDirectory(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.isDirectory() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#exists()} from within a {@link PrivilegedAction}.
     */
    public static synchronized boolean fileExists(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.exists() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#length()} from within a {@link PrivilegedAction}.
     */
    public static long fileLength(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Long>() {
                @Override
                public Long run() {
                    return Long.valueOf( target.length() );
                }
            }).longValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#listFiles()} from within a {@link PrivilegedAction}.
     */
    public static File[] listFiles(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                @Override
                public File[] run() {
                    return target.listFiles();
                }
            });
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#list()} from within a {@link PrivilegedAction}.
     */
    public static String[] list(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<String[]>() {
                @Override
                public String[] run() {
                    return target.list();
                }
            });
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Create a new {@link InputStream} for the file within a {@link PrivilegedAction}.
     */
    @FFDCIgnore({PrivilegedActionException.class})
    public static InputStream getInputStream(final File target) throws FileNotFoundException {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                @Override
                public InputStream run() throws FileNotFoundException {
                    return new FileInputStream(target);
                }
            });
        } catch (PrivilegedActionException e) {
            Exception e2 = e.getException();
            if (e2 instanceof FileNotFoundException)
                throw (FileNotFoundException) e2;
            if (e2 instanceof RuntimeException)
                throw (RuntimeException) e2;
            throw new UndeclaredThrowableException(e);
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Create a new {@link FileOutputStream} for the file within a {@link PrivilegedAction}.
     */
    @FFDCIgnore({PrivilegedActionException.class})
    public static FileOutputStream getFileOutputStream(final File target) throws FileNotFoundException {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<FileOutputStream>() {
                @Override
                public FileOutputStream run() throws FileNotFoundException {
                    return new FileOutputStream(target);
                }
            });
        } catch (PrivilegedActionException e) {
            Exception e2 = e.getException();
            if (e2 instanceof FileNotFoundException)
                throw (FileNotFoundException) e2;
            if (e2 instanceof RuntimeException)
                throw (RuntimeException) e2;
            throw new UndeclaredThrowableException(e);
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#lastModified()} from within a {@link PrivilegedAction}.
     */
    public static long fileLastModified(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Long>() {
                @Override
                public Long run() {
                    return Long.valueOf( target.lastModified() );
                }
            }).longValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#setLastModified()} from within a {@link PrivilegedAction}.
     */
    public static boolean setLastModified(final File target, final long lastModified) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.setLastModified(lastModified) );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#canRead()} from within a {@link PrivilegedAction}.
     */
    public static boolean fileCanRead(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.canRead() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#canWrite()} from within a {@link PrivilegedAction}.
     */
    public static boolean fileCanWrite(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.canWrite() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#mkdirs} from within a {@link PrivilegedAction}.
     *
     * TODO: This is synchronized!  Why?  The need for synchronization should
     *       be explained.
     */
    public static synchronized boolean fileMkDirs(final File target) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( target.mkdirs() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#delete} from within a {@link PrivilegedAction}.
     */
    public static boolean fileDelete(final File file) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf( file.delete() );
                }
            }).booleanValue();
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }

    /**
     * Invoke {@link File#mkdirs()}, then, if that failed, invoke
     * {@link File#exists()}, both with a {@link PrivilegedAction}.
     */
    public static boolean ensureDirExists(File dir) {
        return (fileMkDirs(dir) || fileExists(dir));
    }

    /**
     * Invoke {@link Closeable#close()} within {@link ThreadIdentityManager#runAsServer()}.
     */
    public static boolean tryToClose(Closeable closeable) {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            if (closeable != null) {
                try {
                    closeable.close();
                    return true;
                } catch (IOException e) {
                    // FFDC
                }
            }
        } finally {
            ThreadIdentityManager.reset(token);
        }
        return false;
    }

    static private class SetFilePermsAction implements PrivilegedAction<Boolean> {
        private final File file;

        SetFilePermsAction(File file) {
            this.file = file;
        }

        /**
         * Set the permissions of the bound file to user read and user write.
         *
         * This is a best effort for windows, which does not handle file
         * permissions very well.
         */
        @Override
        public Boolean run() {
            // Set the file as 000
            file.setReadable(false, false);
            file.setWritable(false, false);
            file.setExecutable(false, false);

            // Set the file as 600
            file.setReadable(true, true);
            file.setWritable(true, true);

            return Boolean.TRUE;
        }
    }

    /**
     * Invoke {@link SetFilePermsAction#run}, which attempts to set the permissions
     * of the file to user read and user write, within a {@link PrivilegedAction}.
     */
    public static boolean setUserReadWriteOnly(final File file) {
        return ( AccessController.doPrivileged( new SetFilePermsAction(file) ).booleanValue() );
    }

    /**
     * Invoke {@link File#createNewFile} within a {@link PrivilegedExceptionAction}.
     */
    @FFDCIgnore({PrivilegedActionException.class})
    public static Boolean fileCreate(final File target) throws IOException {
        Object token = ThreadIdentityManager.runAsServer();
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws IOException {
                    return Boolean.valueOf( target.createNewFile() );
                }
            });
        } catch (PrivilegedActionException e) {
            Exception e2 = e.getException();
            if (e2 instanceof IOException)
                throw (IOException) e2;
            if (e2 instanceof RuntimeException)
                throw (RuntimeException) e2;
            throw new UndeclaredThrowableException(e);
        } finally {
            ThreadIdentityManager.reset(token);
        }
    }
}