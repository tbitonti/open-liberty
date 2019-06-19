/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.annocache.targets.cache.internal;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.wsspi.annocache.targets.cache.TargetCache_Options;

public class TargetCacheImpl_Options implements TargetCache_Options {

    public TargetCacheImpl_Options(
        boolean disabled,
        String dir,
        boolean readOnly, boolean alwaysValid, // boolean validate,
        int writeThreads, int writeLimit,
        boolean omitJandexWrite, boolean separateContainers, boolean useJandexFormat) {

        this.disabled = disabled;

        this.dir = dir;
        this.readOnly = readOnly;
        this.alwaysValid = alwaysValid;
        // this.validate = validate;

        this.writeThreads = writeThreads;
        this.writeLimit = writeLimit;

        this.omitJandexWrite = omitJandexWrite;
        this.separateContainers = separateContainers;
        this.useJandexFormat = useJandexFormat;
    }

    //
    
    @Override
    @Trivial
    public String toString() {
        return super.toString() +
            "(" +
                " Disabled " + Boolean.toString(disabled) + "," +
                " Directory " + dir + "," +
                " ReadOnly " + Boolean.toString(readOnly) + "," +
                " AlwaysValid " + Boolean.toString(alwaysValid) + "," +
                " WriteThreads " + Integer.toString(writeThreads) + "," +
                " WriteLimit " + Integer.toString(writeLimit) + "," +
                " OmitJandexWrite " + Boolean.toString(omitJandexWrite) + "," +
                " SeparateContainers " + Boolean.toString(separateContainers) +
                " UseJandexFormat " + Boolean.toString(useJandexFormat) +
            ")";
    }

    //

    private boolean disabled;

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    @Trivial
    public boolean getDisabled() {
        return disabled;
    }

    //

    private String dir;

    @Override
    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    @Trivial
    public String getDir() {
        return dir;
    }

    //

    private boolean readOnly;

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    @Trivial
    public boolean getReadOnly() {
        return readOnly;
    }

    //

    private boolean alwaysValid;

    @Override
    public void setAlwaysValid(boolean alwaysValid) {
        this.alwaysValid = alwaysValid;
    }

    @Override
    @Trivial
    public boolean getAlwaysValid() {
        return alwaysValid;
    }

    //

    // private boolean validate;

    // @Override
    // public void setValidate(boolean validate) {
    //     this.validate = validate;
    // }

    // @Override
    // @Trivial
    // public boolean getValidate() {
    //     return validate;
    // }

    //

    private int writeThreads;

    @Override
    @Trivial
    public int getWriteThreads() {
        return writeThreads;
    }

    @Override
    public void setWriteThreads(int writeThreads) {
        this.writeThreads = writeThreads;
    }
    
    //
    
    private int writeLimit;

    @Override
    @Trivial
    public int getWriteLimit() {
        return writeLimit;
    }

    @Override
    public void setWriteLimit(int writeLimit) {
        this.writeLimit = writeLimit;
    }
    
    //
    
    private boolean omitJandexWrite;

    @Override
    public void setOmitJandexWrite(boolean omitJandexWrite) {
        this.omitJandexWrite = omitJandexWrite;
    }

    @Override
    @Trivial
    public boolean getOmitJandexWrite() {
        return omitJandexWrite;
    }
    

    //

    private boolean separateContainers;

    @Override
    public void setSeparateContainers(boolean separateContainers) {
        this.separateContainers = separateContainers;
    }

    @Override
    @Trivial
    public boolean getSeparateContainers() {
        return separateContainers;
    }

    //

    private boolean useJandexFormat;

    @Override
    public void setUseJandexFormat(boolean useJandexFormat) {
        this.useJandexFormat = useJandexFormat;
    }

    @Override
    @Trivial
    public boolean getUseJandexFormat() {
        return useJandexFormat;
    }
}
