/*******************************************************************************
 * Copyright (c) 1997,2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.cache.test.util;

import com.ibm.websphere.cache.ChangeEvent;
import com.ibm.websphere.cache.InvalidationEvent;
import com.ibm.ws.cache.util.SerializationUtility;

//@formatter:off
public class CacheEventData {
    public CacheEventData(ChangeEvent ce) {
        this(ce.getCacheName(), ce.getId(), ce.getValue(),
             ce.getCauseOfChange(), ce.getSourceOfChange());
    }

    public CacheEventData(InvalidationEvent ie) {
        this(ie.getCacheName(), ie.getId(), ie.getValue(),
             ie.getCauseOfInvalidation(), ie.getSourceOfInvalidation());
    }

    public CacheEventData(String cacheName,
                          Object id, Object value,
                          int cause, int source) {

        this.cacheName = cacheName;
        this.id = id;
        this.value = value;

        this.cause = cause;
        this.source = source;
    }

    private String printString;

    @Override
    public String toString() {
        if ( printString == null ) {
            printString = " Cache [ " + cacheName + " ] id [ " + id + " ]" + " value [ " + value + " ]" +
                          " cause [ " + cause + " ] source [ " + source + " ]";
        }
        return printString;
    }


    private final String cacheName;

    public String getCacheName() {
        return cacheName;
    }

    private final Object id;
    private final Object value;

    public Object getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    // Constant value from either ChangeEvent or InvalidationEvent.
    private final int source;
    private final int cause;

    public int getSource() {
        return source;
    }

    public int getCause() {
        return cause;
    }

    //

    protected static final boolean USE_EQUALS = true;

    protected String compareValues(String tag, Object actual, Object expected, boolean useEquals) {
        if ( (actual == null) ) {
            if ( expected == null ) {
                return null;
            } else {
                // null != !null
            }
        } else if ( expected != null ) {
            if ( (useEquals && actual.equals(expected)) || (!useEquals && (actual == expected)) ) {
                return null;
            } else {
                // actual.equals(expected) || (actual == expected)
            }
        } else {
            // !null != null
        }

        return tag + ": Expected [ " + expected + " ]; received [ " + actual + " ]";
    }

    protected Object deserialize(Object value) {
        if ( value instanceof byte[] ) {
            try {
                value = SerializationUtility.deserialize((byte[]) value, null);
            } catch (Exception e) {
                // ignore
            }
        }
        return value;
    }

    public String compare(CacheEventData expected) {
        String cmp = compareValues("CacheName", this.cacheName, expected.cacheName, USE_EQUALS);
        if ( cmp != null ) {
            return cmp;
        }

        cmp = compareValues("ID", this.id, expected.id, USE_EQUALS);
        if ( cmp != null ) {
            return cmp;
        }

        Object actualValue = deserialize(this.value);
        Object expectedValue = deserialize(expected.value);
        cmp = compareValues("Value", actualValue, expectedValue, USE_EQUALS);
        if ( cmp != null ) {
            return cmp;
        }

        cmp = compareValues("Cause", this.cause, expected.cause, !USE_EQUALS);
        if ( cmp != null ) {
            return cmp;
        };

        cmp = compareValues("Source", this.source, expected.source, !USE_EQUALS);
        if ( cmp != null ) {
            return cmp;
        }

        return "";
    }
}
//@formatter:on