/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.ras.instrument.internal.model;

import java.util.ArrayList;
import java.util.List;

public class TraceOptionsData {

    public TraceOptionsData() {
        super();

        this.traceGroups = new ArrayList<String>();
    }

    public TraceOptionsData(List<String> traceGroups, String messageBundle, boolean traceExceptionThrow, boolean traceExceptionHandling) {
        this.traceGroups = new ArrayList<String>();
        for ( String traceGroup : traceGroups ) {
        	this.addTraceGroup(traceGroup);
        }

        this.setMessageBundle(messageBundle);
        this.traceExceptionThrow = traceExceptionThrow;
        this.traceExceptionHandling = traceExceptionHandling;
    }

    @Override
    public boolean equals(Object object) {
        if ( this == object ) {
            return true;
        } else if ( object == null ) {
        	return false;
        } else if ( object.getClass() != TraceOptionsData.class ) {
        	return false;
        }
        
        TraceOptionsData traceOptions = (TraceOptionsData) object;

        if ( !traceGroups.equals(traceOptions.traceGroups) ) {
        	return false;
        }
        
        if ( messageBundle == null ) {
        	if ( traceOptions.messageBundle != null ) {
        		return false;
        	}
        } else {
        	if ( traceOptions.messageBundle == null ) {
        		return false;
        	} else if ( !messageBundle.equals(traceOptions.messageBundle) ) {
        		return false;
        	}
        }
        
        if ( traceExceptionThrow != traceOptions.traceExceptionThrow ) {
        	return false;
        } else if ( traceExceptionHandling != traceOptions.traceExceptionHandling ) {
        	return false;

        } else {
        	return true;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(";traceGroups=").append(traceGroups);
        sb.append(",messageBundle=").append(messageBundle);
        sb.append(",traceExceptionThrow=").append(traceExceptionThrow);
        sb.append(",traceExceptionHandling=").append(traceExceptionHandling);
        return sb.toString();
    }

    //

    private final List<String> traceGroups;
    
    public void addTraceGroup(String traceGroup) {
        if (traceGroup != null && !traceGroup.equals("") && !traceGroups.contains(traceGroup)) {
            traceGroups.add(traceGroup);
        }
    }
    
    public List<String> getTraceGroups() {
        return traceGroups;
    }

    private String messageBundle;
    
    public void setMessageBundle(String messageBundle) {
        if ( (messageBundle != null) && !messageBundle.isEmpty() ) {
            this.messageBundle = messageBundle;
        }
    }

    public String getMessageBundle() {
        return this.messageBundle;
    }
    
    private boolean traceExceptionThrow;
    private boolean traceExceptionHandling;

    public boolean isTraceExceptionHandling() {
        return traceExceptionHandling;
    }

    public void setTraceExceptionHandling(boolean traceExceptionHandling) {
        this.traceExceptionHandling = traceExceptionHandling;
    }

    public boolean isTraceExceptionThrow() {
        return traceExceptionThrow;
    }

    public void setTraceExceptionThrow(boolean traceExceptionThrow) {
        this.traceExceptionThrow = traceExceptionThrow;
    }
}
