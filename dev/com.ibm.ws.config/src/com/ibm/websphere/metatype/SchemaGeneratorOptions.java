/*******************************************************************************
 * Copyright (c) 2011,2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.websphere.metatype;

import java.util.Locale;
import java.util.Set;

import org.osgi.framework.Bundle;

import com.ibm.websphere.ras.annotation.Trivial;

@Trivial
public class SchemaGeneratorOptions {

    private Bundle[] bundles;

    public Bundle[] getBundles() {
        return bundles;
    }

    public void setBundles(Bundle[] bundles) {
        this.bundles = bundles;
    }

    //

    private boolean isRuntime;

    public boolean isRuntime() {
        return this.isRuntime;
    }

    public void setIsRuntime(boolean value) {
        this.isRuntime = value;
    }

    private Locale locale;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private String encoding;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private Set<String> ignoredPids;

    public Set<String> getIgnoredPids() {
        return ignoredPids;
    }

    public void setIgnoredPids(Set<String> ignoredPids) {
        this.ignoredPids = ignoredPids;
    }

    private SchemaVersion schemaVersion;

    @Deprecated
    public String getSchemaVersion() {
        return schemaVersion.toString();
    }

    public SchemaVersion schemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = SchemaVersion.getEnum(schemaVersion);
    }

    public void setSchemaVersion(SchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    private OutputVersion outputVersion;

    @Deprecated
    public String getOutputVersion() {
        return outputVersion.toString();
    }

    public OutputVersion outputVersion() {
        return outputVersion;
    }

    public void setOutputVersion(String outputVersion) {
        this.outputVersion = OutputVersion.getEnum(outputVersion);
    }

    public void setOutputVersion(OutputVersion outputVersion) {
        this.outputVersion = outputVersion;
    }
}
