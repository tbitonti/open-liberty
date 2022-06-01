/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.ras.instrument.internal.model;

public class PackageInfo {

    public PackageInfo() {
    	this(null, false, null);
    }

    public PackageInfo(String packageName, boolean trivial, TraceOptionsData traceOptionsData) {
    	this.setPackageName(packageName);
        this.setTrivial(trivial);
        this.setTraceOptionsData(traceOptionsData);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append(";packageName=").append(packageName);
        sb.append(",trivial=").append(trivial);
        sb.append(",traceOptionsData=").append(traceOptionsData);
        return sb.toString();
    }

    //

    private String packageName;
    private String internalPackageName;

    public void setPackageName(String packageName) {
        this.packageName = ((packageName == null) ? null : packageName.replace('/', '.'));
        this.internalPackageName = ((packageName == null) ? null : packageName.replace('.', '/'));
    }

    public String getPackageName() {
        return packageName;
    }

    public String getInternalPackageName() {
        return internalPackageName;
    }

    //

    private boolean trivial;
    
    public void setTrivial(boolean trivial) {
        this.trivial = trivial;
    }

    public boolean isTrivial() {
        return trivial;
    }

    //

    private TraceOptionsData traceOptionsData;

    public void setTraceOptionsData(TraceOptionsData traceOptionsData) {
        this.traceOptionsData =
        	( (traceOptionsData != null) ? traceOptionsData : new TraceOptionsData() ); 
    }
    
    public TraceOptionsData getTraceOptionsData() {
        return traceOptionsData;
    }
}
