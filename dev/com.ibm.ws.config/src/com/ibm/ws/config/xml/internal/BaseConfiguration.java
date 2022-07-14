/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.xml.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Sensitive;
import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.xml.LibertyVariable;
import com.ibm.ws.config.xml.internal.XMLConfigParser.MergeBehavior;
import com.ibm.ws.config.xml.internal.variables.ConfigVariable;
import com.ibm.wsspi.kernel.service.location.WsResource;

//@formatter:off
class BaseConfiguration {
    private static final TraceComponent tc =
        Tr.register(BaseConfiguration.class,
                    XMLConfigConstants.TR_GROUP, XMLConfigConstants.NLS_PROPS);

    //

    public BaseConfiguration() {
        // EMPTY
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("configurations: ").append(configurationMap);
        return builder.toString();
    }

    //

    private String description;

    void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    //

    private final List<WsResource> includes = new ArrayList<WsResource>();

    public List<WsResource> getIncludes() {
        return includes;
    }

    //

    protected long lastModified = -1;

    void updateLastModified(long lastModified) {
        if (lastModified > this.lastModified) {
            this.lastModified = lastModified;
        }
    }

    public long getLastModified() {
        return lastModified;
    }

    //

    protected final Map<String, List<ConfigVariable>> variables =
        new HashMap<String, List<ConfigVariable>>();

    public void addVariable(@Sensitive ConfigVariable variable) {
        getVariableEntry(variable.getName()).add(variable);
    }

    private List<ConfigVariable> getVariableEntry(String name) {
        List<ConfigVariable> variableList = variables.get(name);
        if (variableList == null) {
            variableList = new ArrayList<ConfigVariable>(1);
            variables.put(name, variableList);
        }
        return variableList;
    }

    @Sensitive
    public Map<String, LibertyVariable> getVariables() {
        HashMap<String, LibertyVariable> variableMap = new HashMap<String, LibertyVariable>();

        variables.forEach( (varName, varValues) -> {
            if (varValues.isEmpty()) {
                return; // continue the forEach
            }

            // Merge variables based on merge behavior.
            //
            // 'MERGE' is treated the same as 'REPLACE':
            // Variable values are all treated as atomic values.
            //

            // REPLACE ("assign always"),
            // REPLACE_WHEN_EXISTS ("assign only if existing")
            // IGNORE ("assign only if missing")
            // == REPLACE_WHEN_MISSING("assign only if missing")
            //
            // MERGE ("merge always"),
            // MERGE_WHEN_EXISTS ("merge only if exists"),
            // MERGE_WHEN_MISSING; ("merge only if missing")
            //
            // MERGE ("merge always")
            // ==> REPLACE ("assign always")
            // ==> REPLACE
            // MERGE_WHEN_EXISTS ("merge only if exists")
            // ==> REPLACE_WHEN_EXISTS ("assign only if exists")
            // ==> REPLACE_WHEN_EXISTS
            // MERGE_WHEN_MISSING ("merge only if missing")
            // ==> REPLACE_WHEN_MISSING ("assign only if missing")
            // ==> IGNORE
            //
            // (REPLACE || MERGE): Assign
            // (null && (IGNORE || MERGE_WHEN_MISSING)): Assign
            //   (null && (MERGE_WHEN_EXISTS): Do not assign
            // (!null && MERGE_WHEN_EXISTS): Assign
            //   (!null && (IGNORE || MERGE_WHEN_MISSING)): Do not assign

            ConfigVariable activeValue = null;

            for (ConfigVariable varValue : varValues) {
                MergeBehavior varBehavior = varValue.getMergeBehavior();

                if ( ( (varBehavior == MergeBehavior.REPLACE) || (varBehavior == MergeBehavior.MERGE) ) ||
                     ( (activeValue == null) && ((varBehavior == MergeBehavior.IGNORE) || (varBehavior == MergeBehavior.MERGE_WHEN_MISSING) ) ) ||
                     ( (activeValue != null) && (varBehavior == MergeBehavior.MERGE_WHEN_EXISTS) ) ) {
                    activeValue = varValue;
                }
            }

            if (activeValue != null) {
                variableMap.put(varName, activeValue);
            }
        } );

        return variableMap;
    }

    //

    // Only SimpleElements (corresponding directly to server xml elements) are stored here
    protected final Map<String, ConfigurationList<SimpleElement>> configurationMap =
        new ConcurrentHashMap<String, ConfigurationList<SimpleElement>>();

    void getConfigurationNames(Set<String> names) {
        configurationMap.forEach( (name, elements) -> {
            if ( !elements.isEmpty() ) {
                names.add(name);
            }
        });
    }

    protected ConfigurationList<SimpleElement> getConfigurationList(String name) {
        return configurationMap.computeIfAbsent(name, newName ->  new ConfigurationList<SimpleElement>() );
    }

    public void addConfigElement(SimpleElement element) {
        getConfigurationList( element.getNodeName() ).add(element);
    }

    public boolean remove(String name, String id) {
        // TODO: Is this meaningful??  Configuration lists contain configuration elements.
        return getConfigurationList(name).remove(id);
    }

    public void append(BaseConfiguration baseConfiguration) {
        add(baseConfiguration);
    }

    public void add(BaseConfiguration baseConfiguration) {
        if ( baseConfiguration == null ) {
            return;
        }

        baseConfiguration.configurationMap.forEach( (name, elements) -> getConfigurationList(name).add(elements) );
        baseConfiguration.variables.forEach( (key, value) -> getVariableEntry(key).addAll(value) );
    }

    public void remove(BaseConfiguration baseConfiguration) {
        if ( baseConfiguration == null ) {
            return;
        }

        // TODO: Is this correct for keys not present in the receiving configuration?
        //       The result is the creation of empty buckets for those keys.
        //
        // TODO: Do we want to trim out elements which are empty?

        baseConfiguration.configurationMap.forEach( (key, value) -> getConfigurationList(key).remove(value) );
        baseConfiguration.variables.forEach( (key, value) -> getVariableEntry(key).removeAll(value) );
    }

    void getDefaultConfigurationNames(Set<String> names) {
        // no-op unless overridden in subclass
    }

    Map<ConfigID, List<SimpleElement>> defaultConfigurationFactories(String pid, String alias, String defaultId) {
        return Collections.emptyMap();
    }

    public Set<String> getConfigurationNames() {
        Set<String> names = new HashSet<String>();
        getConfigurationNames(names);
        getDefaultConfigurationNames(names);
        return names;
    }

    public boolean hasId(String pid) {
        ConfigurationList<SimpleElement> list = configurationMap.get(pid);
        if (list == null || !list.hasId()) {
            return defaultConfigurationHasId(pid);
        }
        return true;
    }

    boolean defaultConfigurationHasId(String pid) {
        // no-op unless overridden in subclass
        return false;
    }

    // Depends on there not being an id specified on the singleton. There are no
    // guarantees here, so this should really go away.
    void getSingletonNames(Set<String> singletons) {
        for (Map.Entry<String, ConfigurationList<SimpleElement>> entry : configurationMap.entrySet()) {
            ConfigurationList<SimpleElement> list = entry.getValue();
            if (!list.isEmpty() && !list.hasId()) {
                singletons.add(entry.getKey());
            }
        }
    }

    void getFactoryNames(Set<String> singletons) {
        for (Map.Entry<String, ConfigurationList<SimpleElement>> entry : configurationMap.entrySet()) {
            ConfigurationList<SimpleElement> list = entry.getValue();
            if (!list.isEmpty() && list.hasId()) {
                singletons.add(entry.getKey());
            }
        }
    }

    List<SimpleElement> getSingletonElements(String pid, String alias) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }

        List<SimpleElement> elements = null;

        ConfigurationList<SimpleElement> list = null;
        list = configurationMap.get(pid);
        if (list != null) {
            elements = list.collectElements(elements);
        }
        if (alias != null) {
            list = configurationMap.get(alias);
            if (list != null) {
                elements = list.collectElements(elements);
            }
        }

        return (elements == null) ? Collections.<SimpleElement> emptyList() : elements;
    }

    public SingletonElement getSingleton(String pid, String alias) throws ConfigMergeException {
        return getSingleton(pid, alias, true);
    }

    public SingletonElement getSingleton(String pid, String alias, boolean includeOverrides) throws ConfigMergeException {
        List<SimpleElement> elements = getSingletonElements(pid, alias);
        if (elements.isEmpty()) {
            return null;
        } else {
            if (!includeOverrides) {
                // New behavior -- if we find *any* default instance that is not marked merge_when_exists, return everything
                boolean found = false;
                for (SimpleElement element : elements) {
                    if (element.mergeBehavior != MergeBehavior.MERGE_WHEN_EXISTS) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return null;
            }

            return new SingletonElement(elements, pid);
        }
    }

    List<SimpleElement> getFactoryElements(String pid, String alias, String id) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        List<SimpleElement> elements = null;

        ConfigurationList<SimpleElement> list = null;
        list = configurationMap.get(pid);
        if (list != null) {
            elements = list.collectElementsWithId(id, elements);
        }
        if (alias != null) {
            list = configurationMap.get(alias);
            if (list != null) {
                elements = list.collectElementsWithId(id, elements);
            }
        }

        return (elements == null) ? Collections.<SimpleElement> emptyList() : elements;
    }

    public FactoryElement getFactoryInstance(String pid, String alias, String id) throws ConfigMergeException {
        return getFactoryInstance(pid, alias, id, true);
    }

    public FactoryElement getFactoryInstance(String pid, String alias, String id, boolean includeOverrides) throws ConfigMergeException {
        List<SimpleElement> elements = getFactoryElements(pid, alias, id);
        if (elements.isEmpty()) {
            return null;
        } else {
            if (!includeOverrides) {
                // New behavior -- if we find *any* default instance that is not marked merge_when_exists, return everything
                boolean found = false;
                for (SimpleElement element : elements) {
                    if (element.mergeBehavior != MergeBehavior.MERGE_WHEN_EXISTS) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return null;
            }

            return new FactoryElement(elements, pid, id);
        }
    }

    Map<ConfigID, List<SimpleElement>> getAllFactoryElements(String pid, String alias, String defaultId) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }

        Map<ConfigID, List<SimpleElement>> map = null;

        ConfigurationList<SimpleElement> list = null;
        list = configurationMap.get(pid);
        if (list != null) {
            map = list.collectElementsById(map, defaultId, pid);
        }
        if (alias != null) {
            list = configurationMap.get(alias);
            if (list != null) {
                map = list.collectElementsById(map, defaultId, pid);
            }
        }

        return (map == null) ? Collections.<ConfigID, List<SimpleElement>> emptyMap() : map;
    }

    public Map<ConfigID, FactoryElement> getFactoryInstances(String pid, String alias) throws ConfigMergeException {
        return getFactoryInstancesUsingDefaultId(pid, alias, null);
    }

    /**
     * Get a Map of FactoryElements indexed by ConfigID
     *
     * @param pid       The full pid value (must not be null)
     * @param alias     The alias value (may be null)
     * @param defaultId If not null, elements in the configuration that don't have an ID will use this
     * @return a Map of FactoryElement instances indexed by ConfigID
     * @throws ConfigMergeException
     */
    public Map<ConfigID, FactoryElement> getFactoryInstancesUsingDefaultId(String pid, String alias, String defaultId) throws ConfigMergeException {
        Map<ConfigID, List<SimpleElement>> defaultFactories = defaultConfigurationFactories(pid, alias, defaultId);
        Map<ConfigID, List<SimpleElement>> factories = getAllFactoryElements(pid, alias, defaultId);

        // process factory instances
        Map<ConfigID, FactoryElement> mergedMap = new HashMap<ConfigID, FactoryElement>();
        for (Map.Entry<ConfigID, List<SimpleElement>> entry : factories.entrySet()) {
            ConfigID elementId = entry.getKey();

            // If there are defaults, create a FactoryElement using them and then override with the
            // specified values. Otherwise, create a FactoryElement from the configured values.
            List<SimpleElement> defaultElements = defaultFactories.remove(elementId);
            if (defaultElements == null) {
                FactoryElement merged = new FactoryElement(entry.getValue(), pid, elementId.getId());
                mergedMap.put(elementId, merged);
            } else {
                // We have factory elements, so first remove any default configuration that is marked "merge if doesn't exist".
                Iterator<SimpleElement> iter = defaultElements.iterator();
                while (iter.hasNext()) {
                    SimpleElement element = iter.next();
                    if (element.mergeBehavior == MergeBehavior.MERGE_WHEN_MISSING) {
                        iter.remove();
                    }
                }

                if (!defaultElements.isEmpty()) {
                    FactoryElement merged = new FactoryElement(defaultElements, pid, elementId.getId());
                    // Remove the ID from the list of attributes if its using the non-default ID and
                    // the element its merging with is using a default ID.
                    // If it's specified, it will be added back by the configured values.
                    if (defaultElements.get(0).isUsingNonDefaultId() == true && entry.getValue().get(0).isUsingNonDefaultId() == false) {
                        merged.removeAttribute(XMLConfigConstants.CFG_INSTANCE_ID);
                        if (tc.isDebugEnabled()) {
                            Tr.debug(tc, "Removing default id from list of attributes");
                        }
                    }
                    merged.merge(entry.getValue());
                    mergedMap.put(elementId, merged);
                } else {
                    FactoryElement merged = new FactoryElement(entry.getValue(), pid, elementId.getId());
                    mergedMap.put(elementId, merged);
                }

            }

        }
        // process remaining default factory instances. These instances do not match up to configurations, so don't
        // create them if the merge behavior is merge_when_exists
        for (Map.Entry<ConfigID, List<SimpleElement>> entry : defaultFactories.entrySet()) {
            ConfigID elementId = entry.getKey();
            List<SimpleElement> elements = entry.getValue();
            boolean found = false;
            for (SimpleElement element : elements) {
                if (element.mergeBehavior != MergeBehavior.MERGE_WHEN_EXISTS)
                    found = true;
            }

            if (found) {
                FactoryElement merged = new FactoryElement(elements, pid, elementId.getId());
                mergedMap.put(elementId, merged);
            }
        }

        return mergedMap;
    }
}
//@formatter:on