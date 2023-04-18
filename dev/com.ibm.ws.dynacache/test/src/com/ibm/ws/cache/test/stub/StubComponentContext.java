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
package com.ibm.ws.cache.test.stub;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

// TODO: This should be a Mock.

public class StubComponentContext implements ComponentContext {

    private final BundleContext bundleContext = new StubBundleContext();

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    //

    @Override
    public void disableComponent(String arg0) {
        // EMPTY
    }

    @Override
    public void enableComponent(String arg0) {

    }

    @Override
    public ComponentInstance getComponentInstance() {
        return null;
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        return null;
    }

    @Override
    public ServiceReference<?> getServiceReference() {
        return null;
    }

    @Override
    public Bundle getUsingBundle() {
        return null;
    }

    @Override
    public Object locateService(String arg0) {
        return null;
    }

    @Override
    public <S> S locateService(String arg0, ServiceReference<S> arg1) {
        return null;
    }

    @Override
    public Object[] locateServices(String arg0) {
        return null;
    }
}
