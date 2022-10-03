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
package com.ibm.ws.artifact.lrucache.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class LockableImpl implements Lockable {
    private final ReentrantReadWriteLock storageLock;
    private final WriteLock writeLock;
    private final ReadLock readLock;

    public LockableImpl() {
        this.storageLock = new ReentrantReadWriteLock();
        this.writeLock = storageLock.writeLock();
        this.readLock = storageLock.readLock();
    }   

    @Override
    public <P, E extends Exception> P blockAll(FailableProducer<P, E> producer) throws E {
        writeLock.lock();
        try {
            return producer.get();
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public <P, E extends Exception> P blockWrites(FailableProducer<P, E> producer) throws E {
        storageLock.readLock().lock();
        try {
            return producer.get();
        } finally {
            storageLock.readLock().unlock();
        }
    }    
    
    @Override
    public <E extends Exception> void blockAll(Failable<E> producer) throws E {
        writeLock.lock();
        try {
            producer.act();
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public <E extends Exception> void blockWrites(Failable<E> producer) throws E {
        readLock.lock();
        try {
            producer.act();
        } finally {
            readLock.unlock();
        }
    }
}
