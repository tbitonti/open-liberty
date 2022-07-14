/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.admin.ConfigID;

@Trivial
class ConfigurationList<T extends ConfigElement> {

    private static String generateId(int index) {
        return "default-" + index;
    }

    //

    public ConfigurationList() {
        this.configElements = new ArrayList<T>();
        this.idCount = 0;
    }

    //

    private final List<T> configElements;

    public boolean isEmpty() {
        return configElements.isEmpty();
    }

    public List<T> collectElements(List<T> collectedElements) {
        if (configElements.isEmpty()) {
            return collectedElements;
        }

        if (collectedElements == null) {
            collectedElements = new ArrayList<T>();
        }
        collectedElements.addAll(configElements);
        return collectedElements;
    }

    public List<T> collectElementsWithId(String id, List<T> collectedElements) {
        if (configElements.isEmpty()) {
            return collectedElements;
        }

        int index = 0;
        for (T element : configElements) {
            String elementId = element.getId();
            if (elementId == null) {
                elementId = generateId(index++);
            }
            if (elementId.equals(id)) {
                if (collectedElements == null) {
                    collectedElements = new ArrayList<>();
                }
                collectedElements.add(element);
            }
        }
        return collectedElements;
    }

    //

    private int idCount;

    /**
     * Tell if elements of this list have IDs. Answer true or false
     * based on whether any element has an ID.
     *
     * @return True or false, telling if any element has an ID.
     */
    public boolean hasId() {
        return (hasElementWithId());
    }

    /**
     * Tell if any element has an ID.
     *
     * @return True or false telling if any element has an ID.
     */
    private boolean hasElementWithId() {
        return (idCount > 0);
    }

    //

    public void add(T element) {
        configElements.add(element);
        if (element.getId() != null) {
            idCount++;
        }
    }

    public void add(ConfigurationList<T> elements) {
        for (T element : elements.configElements) {
            add(element);
        }
    }

    public void remove(T element) {
        if (configElements.remove(element)) {
            if (element.getId() != null) {
                idCount--;
            }
        }
    }

    public void remove(ConfigurationList<T> elements) {
        for (T element : elements.configElements) {
            remove(element);
        }
    }

    /**
     * Remove all elements which have the specified ID.
     * If the specified ID is null, remove all elements.
     *
     * See {@link SimpleElement#getId}.
     *
     * @param id The target ID.
     *
     * @return True or false telling if any elements were removed.
     */
    public boolean remove(String id) {
        if (id == null) {
            if (configElements.isEmpty()) {
                return false;
            } else {
                configElements.clear();
                idCount = 0;
                return true;
            }

        } else {
            boolean removed = false;

            Iterator<T> useElements = configElements.iterator();
            while (useElements.hasNext()) {
                T element = useElements.next();

                if (!id.equals(element.getId())) {
                    continue;
                }

                useElements.remove();
                idCount--;
                removed = true;
            }

            return removed;
        }
    }

    /**
     * Partition this configuration list by configuration ID.
     *
     * Impute configuration IDs for elements which do not have one yet assigned.
     *
     * Imputed configuration IDs use either the specified ID, or, if the specified ID
     * is null, use a sequence of default IDs. See {@link #generateId(int)}.
     *
     * @param collectedBuckets A table of collected buckets.
     * @param defaultID
     * @return The elements partitioned by configuration ID. The collected elements
     *         parameter if this list is empty.
     */
    public Map<ConfigID, List<T>> collectElementsById(Map<ConfigID, List<T>> collectedBuckets, String id, String usePid) {
        if (configElements.isEmpty()) {
            return collectedBuckets;
        }

        if (collectedBuckets == null) {
            collectedBuckets = new HashMap<ConfigID, List<T>>();
        }

        int index = 0;
        for (T element : configElements) {
            // Assign an ID if necessary.
            // Supplying a PID seems strange, and possibly unnecessary.
            String useId = element.getId();
            if (useId == null) {
                if (id != null) {
                    useId = id;
                } else {
                    useId = generateId(index++);
                }
            }
            ConfigID configID = element.getConfigID(usePid, useId);

            List<T> bucketForId = collectedBuckets.get(configID);
            if (bucketForId == null) {
                bucketForId = new ArrayList<T>();
                collectedBuckets.put(configID, bucketForId);
            }
            bucketForId.add(element);
        }

        return collectedBuckets;
    }
}
