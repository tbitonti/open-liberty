/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package com.ibm.ws.cache.test.stub;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class StubBundle implements Bundle {
    @Override
    public int compareTo(Bundle arg0) {
        return 0;
    }

    @Override
    public <A> A adapt(Class<A> arg0) {
        return null;
    }

    @Override
    public Enumeration<URL> findEntries(String arg0, String arg1, boolean arg2) {
        return null;
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    @Override
    public long getBundleId() {
        return 0;
    }

    @Override
    public File getDataFile(String arg0) {
        return null;
    }

    @Override
    public URL getEntry(String arg0) {
        return null;
    }

    @Override
    public Enumeration<String> getEntryPaths(String arg0) {
        return null;
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        return null;
    }

    @Override
    public Dictionary<String, String> getHeaders(String arg0) {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public String getLocation() {
        return "";
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        return null;
    }

    @Override
    public URL getResource(String arg0) {
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String arg0) throws IOException {
        return null;
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        return null;
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int arg0) {
        return null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public String getSymbolicName() {
        return null;
    }

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public boolean hasPermission(Object arg0) {
        return false;
    }

    @Override
    public Class<?> loadClass(String arg0) throws ClassNotFoundException {
        return null;
    }

    @Override
    public void start() throws BundleException {
        // EMPTY
    }

    @Override
    public void start(int arg0) throws BundleException {
        // EMPTY
    }

    @Override
    public void stop() throws BundleException {
        // EMPTY
    }

    @Override
    public void stop(int arg0) throws BundleException {
        // EMPTY
    }

    @Override
    public void uninstall() throws BundleException {
        // EMPTY
    }

    @Override
    public void update() throws BundleException {
        // EMPTY
    }

    @Override
    public void update(InputStream arg0) throws BundleException {
        // EMPTY
    }
}
