/*******************************************************************************
 * Copyright (c) 2010,2024 IBM Corporation and others.
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

package com.ibm.ws.config.admin.internal;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.admin.ConfigurationDictionary;
import com.ibm.ws.config.admin.ExtendedConfiguration;

/**
 * This represents a Configuration and implements Configuration.
 * It provides APIs to get configuration attributes, properties,
 * and to delete and update its configuration dictionary.
 *
 * In addition to the standard OSGi Configuration value type support,
 * this implementation also supports Map of Strings as one of the value types.
 */
class ExtendedConfigurationImpl implements ExtendedConfiguration {
    public ExtendedConfigurationImpl(ConfigAdminServiceFactory caFactory,
                                     String bundleLocation,
                                     String factoryPid,
                                     String pid,
                                     Dictionary<String, Object> properties,
                                     Set<ConfigID> references,
                                     Set<String> uniques) {

        this.caFactory = caFactory;
        this.bundleLocation = bundleLocation;
        this.factoryPid = factoryPid;
        this.pid = pid;

        this.setProperties(properties);
        this.attributes = new HashSet<ConfigurationAttribute>();

        this.references = references;
        this.uniqueVariables = uniques;

        this.addPidMapping();
        this.addReferences();
    }

    @Override
    @Trivial
    public String toString() {
        return this.getClass().getSimpleName()
               + "[pid=" + pid
               + ",factoryPid=" + factoryPid
               + ",boundBundle=" + boundBundle
               + ",bundleLocation=" + bundleLocation
               + "]";

    }

    private int hashCode;

    @Override
    @Trivial
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = pid.hashCode();
        }
        return hashCode;
    }

    @Override
    @Trivial
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Configuration)) {
            return false;
        }
        Configuration oConfig = (Configuration) o;
        String oPid = oConfig.getPid();
        if (pid == null) {
            return (oPid == null);
        } else if (oPid == null) {
            return false;
        } else {
            return pid.equals(oPid);
        }
    }

    //

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    @Trivial
    public void lock() {
        lock.lock();
    }

    @Override
    @Trivial
    public void unlock() {
        checkLocked();
        lock.unlock();
    }

    @Trivial
    protected void checkLocked() {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Thread not lock owner");
        }
    }

    //

    private boolean isDeleted;

    @Override
    @Trivial
    public boolean isDeleted() {
        return isDeleted;
    }

    @Trivial
    private void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Trivial
    private void assertNotDeleted() {
        if (isDeleted) {
            throw new IllegalStateException("Configuration pid " + pid + " was deleted.");
        }
    }

    //

    @Override
    @Trivial
    public void delete() throws IOException {
        delete(true);
    }

    @Override
    public void delete(boolean fireNotifications) {
        lock.lock();
        try {
            assertNotDeleted();
            setDeleted(true);

            if (fireNotifications) {
                fireConfigurationDeleted(null);
            }

            removePidMapping();
            removeReferences();
        } finally {
            lock.unlock();
        }

        caFactory.getConfigurationStore().removeConfiguration(pid);
    }

    //

    public static final String OVERRIDES_PROPERTY = "config.overrides";

    private boolean isOverride;

    @Override
    @Trivial
    public void setInOverridesFile(boolean isOverride) {
        lock.lock();
        try {
            this.isOverride = isOverride;
            if (properties != null) {
                if (this.isOverride) {
                    properties.put(OVERRIDES_PROPERTY, "true");
                } else {
                    properties.remove(OVERRIDES_PROPERTY);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Trivial
    public boolean isInOverridesFile() {
        lock.lock();
        try {
            assertNotDeleted();
            if (properties == null) {
                return isOverride;
            } else {
                return (properties.get(OVERRIDES_PROPERTY) != null);
            }
        } finally {
            lock.unlock();
        }
    }

    //

    private final AtomicLong changeCount = new AtomicLong();
    private volatile boolean isChanged;

    @Override
    @Trivial
    public long getChangeCount() {
        return changeCount.get();
    }

    private void recordChange() {
        changeCount.incrementAndGet();
        isChanged = true;
    }

    @Trivial
    private boolean clearChanged() {
        if (isChanged) {
            isChanged = false;
            return true;
        } else {
            return false;
        }
    }

    //

    private String bundleLocation;
    private Bundle boundBundle;

    @Override
    @Trivial
    public void setBundleLocation(String bundleLocation) {
        setBundleLocation(bundleLocation, true);
    }

    private void setBundleLocation(String bundleLocation, boolean checkPerm) {
        lock.lock();
        try {
            assertNotDeleted();
            if (checkPerm) {
                caFactory.checkConfigurationPermission();
            }
            this.bundleLocation = bundleLocation;
            boundBundle = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Trivial
    public String getBundleLocation() {
        return getBundleLocation(true);
    }

    protected String getBundleLocation(boolean checkPermission) {
        lock.lock();
        try {
            assertNotDeleted();
            if (checkPermission) {
                caFactory.checkConfigurationPermission();
            }
            if (bundleLocation != null) {
                return bundleLocation;
            } else if (boundBundle != null) {
                return boundBundle.getLocation();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Trivial
    protected boolean isUnbound() {
        return (boundBundle == null);
    }

    @Trivial
    protected boolean bind(Bundle bundle) {
        lock.lock();
        try {
            if ((boundBundle == null) &&
                ((bundleLocation == null) || bundleLocation.equals(bundle.getLocation()))) {
                boundBundle = bundle;
            }
            return (boundBundle == bundle);
        } finally {
            lock.unlock();
        }
    }

    @Trivial
    protected void unbind(Bundle bundle) {
        lock.lock();
        try {
            if (boundBundle == bundle) {
                boundBundle = null;
            }
        } finally {
            lock.unlock();
        }
    }

    // Identity ...

    private final ConfigAdminServiceFactory caFactory;
    private final String factoryPid;
    private final String pid;
    private ConfigID configId;

    @Trivial
    private void addPidMapping() {
        if ((properties != null) && (factoryPid != null) && (caFactory != null)) {
            caFactory.registerConfiguration(getFullId(), this);
        }
    }

    @Trivial
    private void removePidMapping() {
        if ((properties != null) && (factoryPid != null)) {
            caFactory.unregisterConfiguration(getFullId());
        }
    }

    @Trivial
    private void addReferences() {
        if ((properties != null) && (references != null)) {
            caFactory.addReferences(references, getFullId());
        }
    }

    @Trivial
    private void removeReferences() {
        if ((properties != null) && (references != null)) {
            caFactory.removeReferences(references, getFullId());
        }
    }

    @Trivial
    private void store() {
        caFactory.getConfigurationStore().save();
    }

    @Override
    public void fireConfigurationDeleted(Collection<Future<?>> futureList) {
        Future<?> caFuture = caFactory.notifyConfigurationDeleted(this, factoryPid != null);
        Future<?> configFuture = caFactory.dispatchEvent(ConfigurationEvent.CM_DELETED, factoryPid, pid);
        if (futureList != null) {
            futureList.add(caFuture);
            futureList.add(configFuture);
        }
    }

    //

    @Override
    @Trivial
    public String getFactoryPid() {
        return getFactoryPid(true);
    }

    public String getFactoryPid(boolean checkDeleted) {
        lock.lock();
        try {
            if (checkDeleted)
                assertNotDeleted();
            return this.factoryPid;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Trivial
    public String getPid() {
        return getPid(true);
    }

    public String getPid(boolean checkDeleted) {
        lock.lock();
        try {
            if (checkDeleted)
                assertNotDeleted();
            return this.pid;
        } finally {
            lock.unlock();
        }
    }

    //

    //

    @Override
    public Object getProperty(String key) {
        lock.lock();
        try {
            assertNotDeleted();
            // TODO: clone the value
            if (properties != null) {
                return properties.get(key);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        lock.lock();
        try {
            assertNotDeleted();
            if (this.properties == null)
                return null;

            Dictionary<String, Object> copy = properties.copy();
            return copy;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Trivial
    public Dictionary<String, Object> getReadOnlyProperties() {
        lock.lock();
        try {
            assertNotDeleted();
            return properties;
        } finally {
            lock.unlock();
        }
    }

    /*
     * The Configuration Admin service must first store the configuration
     * information and then call a configuration target's updated method: either the
     * ManagedService.updated or ManagedServiceFactory.updated method.
     */
    @Override
    public void update() throws IOException {
        lock.lock();
        try {
            assertNotDeleted();
            store();
            caFactory.notifyConfigurationUpdated(this, factoryPid != null);
        } finally {
            lock.unlock();
        }
    }

    /*
     * The Configuration Admin service must first store the configuration
     * information and then call a configuration target's updated method: either the
     * ManagedService.updated or ManagedServiceFactory.updated method.
     *
     * Also initiates an asynchronous call to all ConfigurationListeners with a
     * ConfigurationEvent.CM_UPDATED event.
     */
    @Override
    public void update(Dictionary<String, ?> useProperties) throws IOException {
        lock.lock();
        try {
            doUpdateProperties(useProperties);
            fireConfigurationUpdated(null);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateProperties(Dictionary<String, Object> useProperties) throws IOException {
        lock.lock();
        try {
            doUpdateProperties(useProperties);
            // Almost the same as 'update', but do not fire update events.
        } finally {
            lock.unlock();
        }
    }

    private void doUpdateProperties(Dictionary<String, ?> useProperties) {
        assertNotDeleted();
        setProperties(useProperties);
        store();
        recordChange();
    }

    @Override
    public void fireConfigurationUpdated(Collection<Future<?>> futureList) {
        if (!clearChanged()) {
            return;
        }

        Future<?> caFuture = caFactory.notifyConfigurationUpdated(this, factoryPid != null);
        Future<?> configFuture = caFactory.dispatchEvent(ConfigurationEvent.CM_UPDATED, factoryPid, pid);
        if (futureList != null) {
            if (caFuture != null) {
                futureList.add(caFuture);
            }
            if (configFuture != null) {
                futureList.add(configFuture);
            }
        }
    }

    @Override
    public void updateCache(Dictionary<String, Object> useProperties,
                            Set<ConfigID> useReferences,
                            Set<String> newUniques) throws IOException {

        lock.lock();
        try {
            removeReferences();

            setProperties(useProperties);
            setReferences(useReferences);
            setUniqueVariables(newUniques);

            store();

            addReferences();

            recordChange();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConfigID getFullId() {
        if (configId != null) {
            return configId;
        }

        if (factoryPid == null) {
            return new ConfigID(pid);
        } else {
            String id = (String) properties.get(ConfigAdminConstants.CFG_CONFIG_INSTANCE_ID);
            if (id == null) {
                return new ConfigID(factoryPid, null);
            } else {
                return ConfigID.deserialize(id);
            }
        }
    }

    @Override
    public void setFullId(ConfigID configId) {
        this.configId = configId;
    }

    @Override
    @Trivial
    public Set<ConfigID> getReferences() {
        lock.lock();
        try {
            return references;
        } finally {
            lock.unlock();
        }
    }

    // Raw state

    private ConfigurationDictionary properties;

    private final Set<ConfigurationAttribute> attributes;

    private Set<ConfigID> references;

    private Set<String> uniqueVariables;

    @Trivial
    protected boolean matchesFilter(Filter filter) {
        if (properties != null)
            return properties.matches(filter);
        return false;
    }

    @Trivial
    private void setReferences(Set<ConfigID> references) {
        this.references = references;
    }

    @Trivial
    private void setUniqueVariables(Set<String> uniqueVariables) {
        this.uniqueVariables = uniqueVariables;
    }

    private void setProperties(Dictionary<String, ?> props) {
        if (props == null) {
            this.properties = null;
            return;
        }

        ConfigurationDictionary newProps = new ConfigurationDictionary();

        Enumeration<String> keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (newProps.get(key) != null) {
                throw new IllegalArgumentException(key + " is already present or is a case variant.");
            }
            newProps.put(key, copyValue(props.get(key)));
        }

        if (factoryPid != null) {
            newProps.put(ConfigurationAdmin.SERVICE_FACTORYPID, factoryPid);
        }
        newProps.put(Constants.SERVICE_PID, pid);
        if (isOverride) {
            newProps.put(OVERRIDES_PROPERTY, "true");
        }

        this.properties = newProps;

        addPidMapping(); // Update the PID mapping in case the PID changed.
    }

    @Override
    @Trivial
    public Set<String> getUniqueVariables() {
        lock.lock();
        try {
            if (uniqueVariables == null)
                return Collections.emptySet();
            else
                return uniqueVariables;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<ConfigurationAttribute> getAttributes() {
        assertNotDeleted(); // TODO: Check security?

        return attributes;
    }

    @Override
    public void addAttributes(ConfigurationAttribute... attrs) throws IOException {
        assertNotDeleted(); // TODO: Check security?

        for (int i = 0; i < attrs.length; i++) {
            attributes.add(attrs[i]);
        }
    }

    @Override
    public void removeAttributes(ConfigurationAttribute... attrs) throws IOException {
        assertNotDeleted(); // TODO: Check security?

        for (int attrNo = 0; attrNo < attrs.length; attrNo++) {
            attributes.remove(attrs[attrNo]);
        }
    }

    //

    @Override
    public boolean updateIfDifferent(Dictionary<String, ?> useProperties) throws IOException {
        lock.lock();
        try {
            assertNotDeleted();
            if (equalConfigProperties(properties, useProperties)) {
                return false;
            } else {
                update(useProperties);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    /*
     * TODO: This is for config plugins, which we dont support...
     *
     * However, if this throws an illegal state exception then we get this exception:
     *
     * [11/19/18 14:55:51:340 EST] 00000024 LogService-13-com.ibm.ws.org.apache.felix.scr E CWWKE0701E: bundle
     * com.ibm.ws.org.apache.felix.scr:1.0.23.201811071104 (13)Error while loading components of bundle com.ibm.ws.event:1.0.23.201811021519 (15)
     * Bundle:com.ibm.ws.org.apache.felix.scr(id=13) java.lang.IllegalStateException: getProcessedProperties(ServiceReference<?> reference) in
     * ExtendedConfiguraitonImpl.java has not been implemented.
     * at com.ibm.ws.config.admin.internal.ExtendedConfigurationImpl.getProcessedProperties(ExtendedConfigurationImpl.java:789)
     * at org.apache.felix.scr.impl.manager.RegionConfigurationSupport.configureComponentHolder(RegionConfigurationSupport.java:211)
     *
     * So for now, we'll just return a copy of the current config which lets the server start up.
     */
    @Override
    public Dictionary<String, Object> getProcessedProperties(ServiceReference<?> reference) {
        lock.lock();
        try {
            assertNotDeleted();
            if (properties == null) {
                return null;
            } else {
                // TODO: This is not entirely safe:
                // Property values can be collections, which may be updated
                // without regard to the lock.
                return properties.copy();
            }
        } finally {
            lock.unlock();
        }
    }

    // Dictionary primitives ...

    private static boolean equalConfigProperties(Dictionary<String, ?> oldP,
                                                 Dictionary<String, ?> newP) {

        int oldSize = ((oldP == null) ? 0 : oldP.size());
        int newSize = ((newP == null) ? 0 : newP.size());
        if (oldSize != newSize) {
            return false;
        }

        Map<String, ?> oldMap = toMap(oldP);
        Map<String, ?> newMap = toMap(newP);

        for (Map.Entry<String, ?> oldEntry : oldMap.entrySet()) {
            String oldKey = oldEntry.getKey();
            if (!newMap.containsKey(oldKey)) {
                return false;
            } else if (!equalValues(oldEntry.getValue(), newMap.get(oldKey))) {
                return false;
            }
        }

        for (Map.Entry<String, ?> newEntry : newMap.entrySet()) {
            String newKey = newEntry.getKey();
            if (!oldMap.containsKey(newKey)) {
                return false;
            }
        }

        return true;
    }

    private static Map<String, ?> toMap(Dictionary<String, ?> d) {
        if ((d == null) || d.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String, Object> m = new HashMap<String, Object>(d.size());
        for (Enumeration<String> keys = d.keys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            m.put(key, d.get(key));
        }
        return m;
    }

    // Value primitives ...

    private static boolean equalValues(Object c1, Object c2) {
        if ((c1 instanceof String) && (c2 instanceof String)) {
            return c1.equals(c2);
        }

        if ((c1 instanceof String[]) && (c2 instanceof String[])) {
            return Arrays.equals((String[]) c1, (String[]) c2);
        }

        if ((c1 instanceof Map) && (c2 instanceof Map)) {
            return c1.equals(c2);
        }

        if (c1 != null) {
            return c1.equals(c2);
        } else {
            return (c2 == null);
        }
    }

    private static Object copyValue(Object v) {
        if (v.getClass().isArray()) {
            int arrayLength = Array.getLength(v);
            Object copyOfArray = Array.newInstance(v.getClass().getComponentType(), arrayLength);
            System.arraycopy(v, 0, copyOfArray, 0, arrayLength);
            return copyOfArray;
        } else if (v instanceof Collection) {
            return new Vector<Object>((Collection<?>) v);
        } else {
            return v;
        }
    }
}
