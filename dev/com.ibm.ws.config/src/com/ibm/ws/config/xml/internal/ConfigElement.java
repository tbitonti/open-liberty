/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.xml.internal.XMLConfigParser.MergeBehavior;

@Trivial
abstract class ConfigElement {
    public static Comparator<String> CASE_INSENSITIVE = new Comparator<String>() {
        @Override
        @Trivial
        public int compare(String s1, String s2) {
            if (s1 == s2) {
                return 0;
            } else if (s1 == null) {
                return 1;
            } else if (s2 == null) {
                return -1;
            } else {
                return s1.compareToIgnoreCase(s2);
            }
        }
    };

    @Trivial
    static class GroupHashMap extends TreeMap<String, Object> {
        private static final long serialVersionUID = 3632130989008067578L;

        public GroupHashMap() {
            super(CASE_INSENSITIVE);
        }

        public GroupHashMap(GroupHashMap map) {
            super(CASE_INSENSITIVE);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> values = (List<Object>) value;
                    value = new ArrayList<Object>(values);
                }
                put(entry.getKey(), value);
            }
        }
    }

    @Trivial
    public static class ConfigElementComparator implements Comparator<ConfigElement> {
        public static final ConfigElementComparator INSTANCE = new ConfigElementComparator();

        @Override
        public int compare(ConfigElement e1, ConfigElement e2) {
            return (e1.getSequenceId() - e2.getSequenceId());
        }
    }

    //

    protected ConfigElement(String nodeName, int sequenceId,
                            List<String> docLocationStack,
                            List<MergeBehavior> behaviorStack) {

        if (nodeName == null) {
            throw new NullPointerException("Node name must be specified");
        }

        this.nodeName = nodeName;
        this.attributes = new GroupHashMap();
        this.operations = new HashMap<String, MERGE_OP>();

        this.sequenceId = sequenceId;

        this.docLocationStack = new ArrayList<>(docLocationStack);

        int numDocs = docLocationStack.size();
        this.location = docLocationStack.get(numDocs - 1);
        this.parentLocation = ((numDocs > 1) ? docLocationStack.get(numDocs - 2) : null);

        this.behaviorStack = new ArrayList<>(behaviorStack);
        this.mergeBehavior = behaviorStack.get(behaviorStack.size() - 1);
    }

    protected ConfigElement(String nodeName) {
        if (nodeName == null) {
            throw new NullPointerException("Node name must be specified");
        }
        this.nodeName = nodeName;
        this.attributes = new GroupHashMap();
        this.operations = new HashMap<String, MERGE_OP>();

        this.sequenceId = 0;

        this.docLocationStack = null;
        this.location = null;
        this.parentLocation = null;

        this.behaviorStack = null;
        this.mergeBehavior = null;
    }

    public ConfigElement(ConfigElement element) {
        this.nodeName = element.nodeName;
        this.attributes = new GroupHashMap(element.attributes);

        this.parent = element.parent;
        this.childAttributeName = element.childAttributeName;

        // TODO: The children are **NOT** cloned!
        //       Reassigning the parents breaks the relationship
        //       of the children with the original parent.

        this.children = element.children;
        for (ConfigElement child : children) {
            child.setParent(this);
        }

        this.sequenceId = 0;
        // this.sequenceId = element.sequenceId; TFB: Why don't we copy this.

        this.docLocationStack = element.docLocationStack;
        this.location = element.location;
        this.parentLocation = element.parentLocation;

        this.behaviorStack = element.behaviorStack;
        this.mergeBehavior = element.mergeBehavior;

        // this.overrideLocation = element.overrideLocation; TFB: Why don't we copy this?

        this.operations = new HashMap<String, MERGE_OP>(element.operations);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getName());
        builder.append("[").append(getFullId()).append("]");
        return builder.toString();
    }

    private void appendId(StringBuilder builder) {
        ConfigElement useParent = getParent();
        if (useParent != null) {
            useParent.appendId(builder);
            builder.append('/');
        }

        builder.append(getNodeDisplayName());

        String useId = getId();
        if (useId != null) {
            builder.append('[');
            builder.append(useId);
            builder.append(']');
        }
    }

    public String getDisplayId() {
        StringBuilder displayId = new StringBuilder();
        appendId(displayId);
        return displayId.toString();
    }

    private final boolean safeEquals(Object v1, Object v2) {
        return ((v1 == null && v2 == null) ||
                (v1 != null && !v1.equals(v2)));
    }

    // @formatter:off
    boolean equalsIgnoreIdAttr(ConfigElement other) {
        if ( this == other ) {
            return true;
        }

        if ( !safeEquals(nodeName, other.nodeName) ) {
            return false;
        }

        if ( sequenceId != other.sequenceId ) {
            return false;
        }
        if ( !safeEquals(location, other.location) ) {
            return false;
        }

        if (isTextOnly != other.isTextOnly) {
            return false;
        }

        if ( !safeEquals(elementValue, other.elementValue) ) {
            return false;
        }

        for ( Map.Entry<String, Object> entry : attributes.entrySet() ) {
            String key = entry.getKey();
            if ( "id".equals(key) ) {
                continue;
            }
            if ( !safeEquals( entry.getValue(), other.attributes.get(key) ) ) {
                return false;
            }
        }

        for ( Map.Entry<String, Object> otherEntry : other.attributes.entrySet() ) {
            String otherKey = otherEntry.getKey();
            if ( "id".equals(otherKey) ) {
                continue;
            }
            if ( !safeEquals( otherEntry.getValue(), attributes.get(otherKey) ) ) {
                return false;
            }
        }

        return ( safeEquals(children, other.children) &&
                 safeEquals( getId(), other.getId() ) &&
                 safeEquals(operations, other.operations) );
    }
    // @formatter:on

    //

    private final String nodeName;

    public String getNodeName() {
        return nodeName;
    }

    /**
     * The original name from the XML text. This is intended
     * to be used in messages.
     *
     * @return The original name from the XML text.
     */
    protected String getNodeDisplayName() {
        return nodeName;
    }

    public abstract String getId();

    /**
     * Answer the print string of the configuration ID of this element.
     *
     * See {@link #getConfigID()}.
     *
     * @return The print string of the configuration ID of this element.
     */
    public String getFullId() {
        return getConfigID().toString();
    }

    /**
     * Answer a configuration ID for this element. Use the already
     * assigned ID of this element within the configuration ID.
     * The ID should not be null.
     *
     * Include the node name in the configuration ID. If the element
     * has a parent, include the the parent configuration ID and the
     * child attribute name in the configuration name.
     *
     * @return A configuration ID for this element.
     */
    public ConfigID getConfigID() {
        return getConfigID(getNodeName(), getId());
    }

    /**
     * Answer a configuration ID for this element. Use the supplied
     * PID and ID within the configuration ID. The ID of this element is
     * expected to be null.
     *
     * If the element has a parent, include the the parent configuration
     * ID and the child attribute name in the configuration name.
     *
     * @return A configuration ID for this element.
     */
    public ConfigID getConfigID(String usePid, String useId) {
        // We can't unconditionally use the child attribute name:
        // ConfigComparator.buildConfiguration assigns a child attribute name
        // but may not assign a parent.

        ConfigElement useParent = getParent();
        ConfigID parentID;
        String useChildName;
        if (useParent == null) {
            parentID = null;
            useChildName = null;
        } else {
            parentID = useParent.getConfigID();
            useChildName = childAttributeName;
        }

        return new ConfigID(parentID, usePid, useId, useChildName);
    }

    //

    /**
     * Tell if this is a child element or a collection element.
     *
     * If it has attributes or child elements (isTextOnly() false),
     * it's definitely a child element. If it is text only but has no
     * content, it can't be a collection attribute and must be a child
     * element.
     *
     * @return True or false telling if this a child or a collection element.
     */
    public boolean isChildElement() {
        return (!isEmpty() || !isTextOnly() || "".equals(getElementValue()));
    }

    //

    private GroupHashMap attributes;

    protected GroupHashMap setAttributes() {
        if (attributes != null) {
            return attributes;
        } else {
            return (attributes = new GroupHashMap());
        }
    }

    public Map<String, Object> getAttributes() {
        return ((attributes == null) ? Collections.emptyMap() : attributes);
    }

    public boolean isEmpty() {
        return ((attributes == null) || attributes.isEmpty());
    }

    public boolean containsAttribute(String name) {
        return ((attributes != null) && attributes.containsKey(name));
    }

    public Object getAttribute(String name) {
        return ((attributes == null) ? null : attributes.get(name));
    }

    public void setAttribute(String name, Object value) {
        setAttributes().put(name, value);
    }

    private void checkType(String name, Class<?> type) {
        Object attributeValue = getAttribute(name);
        if (attributeValue == null) {
            return;
        }

        if (!type.isInstance(attributeValue)) {
            String message = "Incompatible types for attribute " + name + ":" +
                             " Attempted to assign " + type.getName() +
                             " but type " + attributeValue.getClass().getName() +
                             " is already assigned.";
            throw new IllegalArgumentException(message);
        }
    }

    public void addAttribute(String name, String value) {
        checkType(name, String.class);
        setAttributes().put(name, value);
    }

    public void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    /**
     * Answer the value of a collection type attribute.
     *
     * Create and assign an empty collection as the attribute value
     * if none already exists.
     *
     * Thrown a runtime exception if the named attribute already has
     * a non-collection type value.
     *
     * @param name An attribute name.
     * @return The value of the named collection attribute.
     */
    private List<Object> getCollectionAttribute(String name) {
        checkType(name, List.class);

        @SuppressWarnings("unchecked")
        List<Object> attributeValue = (List<Object>) getAttribute(name);
        if (attributeValue == null) {
            setAttributes().put(name, attributeValue = new ArrayList<Object>(1));
        }
        return attributeValue;
    }

    public void addCollectionAttribute(String name, Object value) {
        getCollectionAttribute(name).add(value);
    }

    @Trivial
    public static class Reference extends ConfigID {
        private static final long serialVersionUID = 6931519568553134827L;

        public Reference(String pid, String id) {
            super(pid, id);
        }
    }

    public void addReference(String name, String id) {
        addCollectionAttribute(name, new Reference(name, id));
    }

    public String getRefAttr() {
        Object attributeValue = getAttribute(XMLConfigConstants.CFG_CONFIG_REF);
        if ((attributeValue != null) && (attributeValue instanceof String)) {
            return (String) attributeValue;
        } else {
            return null;
        }
    }

    public void setIdAttribute() {
        String useId = getId();
        if (useId == null) {
            return;
        }

        if (useId.startsWith("default-")) {
            useId = getFullId();
        }

        setAttributes().put(XMLConfigConstants.CFG_INSTANCE_ID, getFullId());
    }

    public boolean isEnabled() {
        Object attributeValue = getAttribute(XMLConfigConstants.CONFIG_ENABLED_ATTRIBUTE);
        if ((attributeValue instanceof String) && "false".equalsIgnoreCase((String) attributeValue)) {
            return false;
        } else {
            return true;
        }
    }

    //

    private String elementValue = "";

    public String getElementValue() {
        return elementValue;
    }

    public void setElementValue(String elementValue) {
        this.elementValue = elementValue;
    }

    //

    private boolean isTextOnly = true; //assume non-nested config by default

    public boolean isTextOnly() {
        return isTextOnly;
    }

    public void setTextOnly(boolean isTextOnly) {
        this.isTextOnly = isTextOnly;
    }

    //

    private ConfigElement parent;
    protected String childAttributeName;

    void setParent(ConfigElement parent) {
        this.parent = parent;
    }

    public ConfigElement getParent() {
        return parent;
    }

    protected void setChildAttributeName(String childAttributeName) {
        this.childAttributeName = childAttributeName;
    }

    public String getChildAttributeName() {
        return childAttributeName;
    }

    //

    private Set<ConfigElement> children;

    public abstract boolean isSimple();

    public boolean hasNestedElements() {
        return ((children != null) && !children.isEmpty());
    }

    public Set<ConfigElement> getChildren() {
        return ((children == null) ? Collections.emptySet() : children);
    }

    protected void addChild(ConfigElement newChild) {
        if (children == null) {
            children = new HashSet<ConfigElement>();
        }
        children.add(newChild);
    }

    protected void addChildren(Set<ConfigElement> newChildren) {
        if ((newChildren == null) || newChildren.isEmpty()) {
            return;
        }

        if (children == null) {
            children = new HashSet<ConfigElement>(newChildren.size());
        }
        children.addAll(newChildren);
    }

    //

    public void addChildConfigElement(String name, SimpleElement configElement) {
        List<Object> attributeValue = getCollectionAttribute(name);
        attributeValue.add(configElement);

        addChild(configElement);
        configElement.setParent(this);
        configElement.setChildAttributeName(name);
    }

    //

    private final int sequenceId;

    public int getSequenceId() {
        return sequenceId;
    }

    private final List<String> docLocationStack;
    private String location;
    private final String parentLocation;

    public void setDocumentLocation(String location) {
        this.location = location;
    }

    public String getDocumentLocation() {
        return location;
    }

    public Object getParentDocumentLocation() {
        return parentLocation;
    }

    private final List<MergeBehavior> behaviorStack;
    protected MergeBehavior mergeBehavior;

    protected void setMergeBehavior(MergeBehavior mergeBehavior) {
        this.mergeBehavior = mergeBehavior;
    }

    public MergeBehavior getMergeBehavior() {
        return mergeBehavior;
    }

    //

    private String overrideLocation;

    public String getMergedLocation() {
        if ((overrideLocation == null) || overrideLocation.isEmpty()) {
            return location;
        } else {
            return overrideLocation;
        }
    }

    //

    public enum MERGE_OP {
        APPEND, SET
    };

    private final Map<String, MERGE_OP> operations;

    public void setMergeOperation(String name, MERGE_OP mergeOperation) {
        MERGE_OP operation = operations.put(name, mergeOperation);
        if (operation != mergeOperation) {
            // TODO: log warning: two different operations specified for the
            // same attribute
        }
    }

    private MERGE_OP getMergeOperation(String name) {
        return operations.get(name);
    }

    //

    /**
     * The logic for merge has, unfortunately, gotten a bit unwieldy. Previously we were able to simply override in order
     * of the sequence number to support a "last one wins" policy. We kept the same logic when we added onConflict values
     * of REPLACE/IGNORE. Everything was fine with that as long as you're only dealing with one level of includes. If you
     * had multiple levels, a nested 'REPLACE' or 'MERGE' could replace a top level element even though the nested include's
     * parent was included with 'IGNORE'.
     *
     * So, now we have complicated logic. First we flatten all conflict subtrees in the list of elements using the following rules:
     *
     * 1. If the element preceding the current element in the conflict list is from the same file, merge the current element with
     * the previous element.
     *
     * 2. If the element preceding the current element in the conflict list is the current element's direct parent, we use the merge
     * behavior specified on the element to emerge the previous and current elements.
     *
     * 3. If the preceding element is an ancestor of the current element, we use the merge behavior from the ancestor's child to merge the
     * current element with the preceding element.
     *
     * 4. Otherwise, the two elements are not related, so no flattening is done. We add the current element to the flattened list of elements.
     *
     * After everything has been flattened, we can go through each element in the list and call override.
     *
     * There are probably better ways to handle this, but they would require restructuring the way we parse and store configuration.
     */
    protected void merge(List<? extends ConfigElement> elements) {
        if (elements.size() > 1) {
            Collections.sort(elements, ConfigElementComparator.INSTANCE);
        }

        ConfigElement[] elementArray = elements.toArray(new ConfigElement[elements.size()]);

        LinkedList<ConfigElement> flattened = new LinkedList<ConfigElement>();
        flattened.add(elementArray[elements.size() - 1]);
        for (int i = (elements.size() - 1); i > 0; i--) {
            ConfigElement element = new SimpleElement(elementArray[i - 1]);
            ConfigElement previous = flattened.getLast();
            if (element.getDocumentLocation().equals(previous.getDocumentLocation())) {
                // Same document, just merge
                element.override(previous);
                flattened.removeLast();
                flattened.add(element);
            } else if (previous.getParentDocumentLocation() != null && previous.getParentDocumentLocation().equals(element.getDocumentLocation())) {
                // 'element' is in the immediate parent document of 'previous'. Just override using the merge behavior that's already specified
                // on 'previous'
                element.override(previous);
                flattened.removeLast();
                flattened.add(element);
            } else if (previous.docLocationStack.contains(element.getDocumentLocation())) {
                // If 'element' is in the hierarchy that included 'previous', 'previous' needs to use the merge behavior specified
                // in the include statement in the document that contains 'element'.
                int idx = previous.docLocationStack.indexOf(element.getDocumentLocation());
                previous.mergeBehavior = previous.behaviorStack.get(idx + 1);
                element.override(previous);
                flattened.removeLast();
                flattened.add(element);
            } else {
                // Conflicting element not in stack, add for later merging
                flattened.add(element);

            }
        }

        for (int i = flattened.size(); i > 0; i--) {
            override(flattened.get(i - 1));
        }
    }

    protected void override(ConfigElement in) {
        MERGE_OP defaultOperation = MERGE_OP.APPEND;
        boolean sameLocation = in.getDocumentLocation().equals(getDocumentLocation());

        if (!sameLocation) {
            if (in.mergeBehavior == MergeBehavior.IGNORE) {
                return;
            } else if (in.mergeBehavior == MergeBehavior.REPLACE) {
                defaultOperation = MERGE_OP.SET;
            }
        }

        if (in.attributes != null) {
            for (Map.Entry<String, Object> entry : in.attributes.entrySet()) {
                MERGE_OP operation = in.getMergeOperation(entry.getKey());
                if (operation == null)
                    operation = defaultOperation;
                Object value = entry.getValue();
                if (operation == MERGE_OP.APPEND && value instanceof List) {
                    List<Object> attributeValues = getCollectionAttribute(entry.getKey());
                    @SuppressWarnings("unchecked")
                    List<Object> values = (List<Object>) value;
                    attributeValues.addAll(values);
                } else {
                    setAttribute(entry.getKey(), value);
                }
            }
        }

        parent = in.getParent();
        childAttributeName = in.childAttributeName;

        addChildren(in.getChildren());

        overrideLocation = in.location;
    }
}
