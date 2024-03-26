/*******************************************************************************
 * Copyright (c) 2013,2024 IBM Corporation and others.
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
package com.ibm.ws.config.admin;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.Future;

import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.Configuration.ConfigurationAttribute;

public interface ExtendedConfiguration extends Configuration {

    void lock();

    void unlock();

    void fireConfigurationDeleted(Collection<Future<?>> futureList);

    void fireConfigurationUpdated(Collection<Future<?>> futureList);

    void delete(boolean fireNotifications);

    Object getProperty(String key);

    Dictionary<String, Object> getReadOnlyProperties();

    void updateCache(Dictionary<String, Object> properties, Set<ConfigID> references, Set<String> newUniques) throws IOException;

    void updateProperties(Dictionary<String, Object> properties) throws IOException;

    Set<ConfigID> getReferences();

    void setInOverridesFile(boolean inOverridesFile);

    boolean isInOverridesFile();

    Set<String> getUniqueVariables();

    void setFullId(ConfigID id);

    ConfigID getFullId();

    boolean isDeleted();

    // R7 Upgrade

    Set<ConfigurationAttribute> getAttributes();

    void addAttributes(ConfigurationAttribute... attrs) throws IOException;

    void removeAttributes(ConfigurationAttribute... attrs) throws IOException;

    boolean updateIfDifferent(Dictionary<String, ?> properties) throws IOException;

    Dictionary<String, Object> getProcessedProperties(ServiceReference<?> reference);
}
