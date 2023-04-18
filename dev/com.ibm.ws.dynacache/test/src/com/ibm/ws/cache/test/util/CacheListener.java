/*******************************************************************************
 * Copyright (c) 2017,2023 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import com.ibm.websphere.cache.ChangeEvent;
import com.ibm.websphere.cache.ChangeListener;
import com.ibm.websphere.cache.InvalidationEvent;
import com.ibm.websphere.cache.InvalidationListener;

public class CacheListener implements InvalidationListener, ChangeListener {
    protected static long getTime() {
        return System.currentTimeMillis();
    }

    public CacheListener(Object listenerId) {
        this.listenerId = listenerId;

        this.isStarted = false;
        this.captured = new ArrayList<>();
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public void start() {
        if (!isStarted) {
            startMs = getTime();
            isStarted = true;
        }
    }

    public void stop() {
        if (isStarted) {
            isStarted = false;
            stopMs = getTime();
        }
    }

    public void stopClear() {
        stop();
        clearEvents();
    }

    public List<? extends CacheEventData> stopConsume() {
        stop();
        return consumeEvents();
    }

    public void restart() {
        stop();
        start();
    }

    public void restartClear() {
        stop();
        clearEvents();
        start();
    }

    public List<? extends CacheEventData> restartConsume() {
        stop();
        List<? extends CacheEventData> useEventData = consumeEvents();
        start();
        return useEventData;
    }

    //

    private final Object listenerId;

    public Object getListenerId() {
        return listenerId;
    }

    //

    private int eventType;

    public int getEventType() {
        return eventType;
    }

    private boolean isStarted;

    public boolean isStarted() {
        return isStarted;
    }

    private long startMs;

    public long getStart() {
        return startMs;
    }

    private long stopMs;

    public long getStop() {
        return stopMs;
    }

    //

    public List<? extends CacheEventData> copyEvents() {
        List<CacheEventData> useCaptured = new ArrayList<CacheEventData>(captured.size());
        useCaptured.addAll(captured);
        return useCaptured;
    }

    public List<? extends CacheEventData> consumeEvents() {
        List<CacheEventData> useCaptured = new ArrayList<CacheEventData>(captured.size());
        useCaptured.addAll(captured);

        captured.clear();

        return useCaptured;
    }

    public void clearEvents() {
        captured.clear();
    }

    //

    private final List<CacheEventData> captured;

    public List<? extends CacheEventData> getEvents() {
        return captured;
    }

    protected void capture(CacheEventData eventData) {
        captured.add(eventData);
    }

    protected boolean capture(InvalidationEvent ie) {
        if (!isStarted()) {
            return false;
        }

        if (ie.getCauseOfInvalidation() == getEventType()) {
            capture(new CacheEventData(ie));
            return true;
        } else {
            return false;
        }
    }

    protected boolean capture(ChangeEvent ce) {
        if (!isStarted()) {
            return false;
        }

        if (ce.getCauseOfChange() == getEventType()) {
            capture(new CacheEventData(ce));
            return true;
        } else {
            return false;
        }
    }

    protected String captureText(boolean captured) {
        return (captured ? "CAPTURED" : "DISCARDED");
    }

    @Override
    public void fireEvent(InvalidationEvent ie) {
        String capturedText = captureText(capture(ie));
        System.out.println("** Listener [ " + getListenerId() + " ] at [ " + ie.getTimeStamp() + " ]:" +
                           " [ " + ie.getValue() + " ] [ " + capturedText + " ]");
    }

    @Override
    public void cacheEntryChanged(ChangeEvent ce) {
        String capturedText = captureText(capture(ce));
        System.out.println("** Listener [ " + getListenerId() + " ] at [ " + ce.getTimeStamp() + " ]:" +
                           " [ " + ce.getValue() + " ] [ " + capturedText + " ]");
    }
}
