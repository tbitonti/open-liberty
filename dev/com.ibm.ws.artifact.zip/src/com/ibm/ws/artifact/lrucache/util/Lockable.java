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

public interface Lockable {
    @FunctionalInterface
    public interface FailableProducer<V, E extends Exception> {
        V get() throws E;
    }
    
    @FunctionalInterface
    public interface Failable<E extends Exception> {
        void act() throws E;
    }    

    <E extends Exception> void blockAll(Failable<E> producer) throws E;
    <E extends Exception> void blockWrites(Failable<E> producer) throws E;

    <P, E extends Exception> P blockAll(FailableProducer<P, E> producer) throws E;
    <P, E extends Exception> P blockWrites(FailableProducer<P, E> producer) throws E;
}