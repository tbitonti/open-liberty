/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.admin;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.Future;

import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;

//@formatter:off
public interface ExtendedConfiguration extends Configuration {
    void lock();
    void unlock();

    void setInOverridesFile(boolean inOverridesFile);
    boolean isInOverridesFile();

    void setFullId(ConfigID id);
    ConfigID getFullId();

    Object getProperty(String key);
    Dictionary<String, Object> getReadOnlyProperties();
    Set<String> getUniqueVariables();
    Set<ConfigID> getReferences();

    void updateProperties(Dictionary<String, Object> properties) throws IOException;

    @Override
    boolean updateIfDifferent(Dictionary<String, ?> properties) throws IOException;

    @Override
    Dictionary<String, Object> getProcessedProperties(ServiceReference<?> reference);

    @Override
    Set<ConfigurationAttribute> getAttributes();

    @Override
    void addAttributes(Configuration.ConfigurationAttribute... attrs) throws IOException;

    @Override
    void removeAttributes(Configuration.ConfigurationAttribute... attrs) throws IOException;

    void delete(boolean fireNotifications);
    boolean isDeleted();

    void fireConfigurationDeleted(Collection<Future<?>> futures);
    void fireConfigurationUpdated(Collection<Future<?>> futures);

    void updateCache(Dictionary<String, Object> properties, Set<ConfigID> references, Set<String> newUniques) throws IOException;
}
//@formatter:on