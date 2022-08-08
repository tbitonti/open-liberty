/*******************************************************************************
 * Copyright (c) 2010, 2022 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.websphere.metatype.MetaTypeFactory;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.xml.internal.MetaTypeRegistry.RegistryEntry;
import com.ibm.ws.config.xml.internal.metatype.ExtendedAttributeDefinition;
import com.ibm.ws.config.xml.internal.metatype.ExtendedObjectClassDefinition;
import com.ibm.ws.config.xml.internal.variables.ConfigVariableRegistry;
import com.ibm.wsspi.kernel.service.location.WsResource;

class ConfigValidator {

    private static final String LINE_SEPARATOR = ConfigUtil.getSystemProperty("line.separator");

    // The value to display for a secure/password attribute.
    // The value is selected to be exactly the same as used by
    // com.ibm.websphere.ras.ProtectedString.toString().
    private static final String SECURE_VALUE = "*****";

    private static final TraceComponent tc = Tr.register(ConfigValidator.class, XMLConfigConstants.TR_GROUP, XMLConfigConstants.NLS_PROPS);

    private final MetaTypeRegistry metatypeRegistry;

    private ServerXMLConfiguration configuration;
    private String configPath;

    private final ConfigVariableRegistry variableRegistry;

    ConfigValidator(MetaTypeRegistry metatypeRegistry, ConfigVariableRegistry variableRegistry) {
        this.metatypeRegistry = metatypeRegistry;
        this.variableRegistry = variableRegistry;
    }

    public void setConfiguration(ServerXMLConfiguration configuration) {
        this.configuration = configuration;

        if (configuration != null) {
            WsResource configDir = configuration.getConfigDir();
            if (configDir != null) {
                configPath = configDir.asFile().toURI().toString();
                configPath = variableRegistry.resolveRawString(configPath);
            }
        }
    }

    protected String relativeLocation(String location) {
        Tr.debug(tc, "Configuration path [ " + configPath + " ]");
        Tr.debug(tc, "Location [ " + location + " ]");

        if ((configPath == null) || !location.startsWith(configPath)) {
            return location;
        } else {
            return location.substring(configPath.length());
        }
    }

    public boolean validateSingleton(String pid, String alias) {
        String name = (alias == null) ? pid : alias;
        return validate(name, null, configuration.getConfiguration().getSingletonElements(pid, alias));
    }

    public boolean validateFactoryInstance(String pid, String alias, ConfigID id) {
        String name = (alias == null) ? pid : alias;
        return validate(name, id, configuration.getConfiguration().getFactoryElements(pid, alias, id.getId()));
    }

    public void validate(Set<RegistryEntry> entries) {
        // validate singletons
        for (RegistryEntry registry : entries) {
            if (registry.isSingleton()) {
                String alias;
                String name;

                alias = registry.getAlias() == null ? registry.getChildAlias() : registry.getAlias();
                name = (alias == null) ? registry.getPid() : alias;
                validate(registry, name, null, configuration.getConfiguration().getSingletonElements(registry.getPid(), alias));
            } else {
                String alias;
                String name;
                String defaultId;
                alias = registry.getAlias() == null ? registry.getChildAlias() : registry.getAlias();
                name = (alias == null) ? registry.getPid() : alias;
                defaultId = registry.getDefaultId();
                Map<ConfigID, List<SimpleElement>> instances = configuration.getConfiguration().getAllFactoryElements(registry.getPid(), alias, defaultId);
                for (Map.Entry<ConfigID, List<SimpleElement>> entry : instances.entrySet()) {
                    validate(registry, name, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Answer the definition of an attribute. Answer null if a definition
     * is not available.
     *
     * @param registryEntry The registry entry from which to retrieve the attribute definition.
     * @param attributeName The name of the attribute to locate.
     *
     * @return The definition of the attribute. Null if no definition is available for the attribute.
     */
    private ExtendedAttributeDefinition getAttributeDefinition(RegistryEntry registryEntry, String attributeName) {
        if (registryEntry == null) {
            return null;
        }

        ExtendedObjectClassDefinition definition = registryEntry.getObjectClassDefinition();
        if (definition == null) {
            return null;
        }

        Map<String, ExtendedAttributeDefinition> attributeMap = definition.getAttributeMap();
        ExtendedAttributeDefinition attributeDefinition = attributeMap.get(attributeName);
        return attributeDefinition;
    }

    /**
     * Test if an attribute is a secured / password type attribute.
     *
     * This is tested using the attribute type, as obtained from {@link ExtendedAttributeDefinition#getType()}.
     *
     * Attribute types {@link MetaTypeFactory#PASSWORD_TYPE} and {@link MetaTypeFactory#HASHED_PASSWORD_TYPE} are secured. All other attribute types are not secured.
     *
     * Answer false the attribute type cannot be obtained, either because the
     * registry entry was null, had no class definition, or had no definition
     * for the attribute.
     *
     * @param registryEntry The registry entry from which to obtain the attribute type.
     * @param attributeName The name of the attribute which is to be tested.
     *
     * @return True or false telling if the attribute is a secured/password type attribute.
     */
    private boolean isSecureAttribute(RegistryEntry registryEntry, String attributeName) {
        ExtendedAttributeDefinition attributeDefinition = getAttributeDefinition(registryEntry, attributeName);
        if (attributeDefinition == null) {
            return false; // No available definition; default to false.
        }

        // @formatter:off
        int attributeType = attributeDefinition.getType();
        return (attributeDefinition.isObscured()  || (attributeType == MetaTypeFactory.PASSWORD_TYPE) ||
                (attributeType == MetaTypeFactory.HASHED_PASSWORD_TYPE));
        // @formatter:on
    }

    private void logRegistryEntry(RegistryEntry registryEntry) {
        if (!tc.isDebugEnabled()) {
            return;
        }

        if (registryEntry == null) {
            Tr.debug(tc, "Registry Entry [ null ]");
            return;
        }

        Tr.debug(tc, "Registry Entry [ " + registryEntry.getPid() + " ]");
        Tr.debug(tc, "  Bundle [ " + registryEntry.getBundleId() + " : " + registryEntry.getBundleName() + " ]");
        Tr.debug(tc, "  Alias [ " + registryEntry.getAlias() + " ]");
        Tr.debug(tc, "  Child Alias [ " + registryEntry.getChildAlias() + " ]");
        Tr.debug(tc, "  Extends : [ " + registryEntry.getExtends() + " ]");

        ExtendedObjectClassDefinition definition = registryEntry.getObjectClassDefinition();
        Tr.debug(tc, "  Definition: [ " + definition + " ]");
        if (definition == null) {
            return;
        }

        Map<String, ExtendedAttributeDefinition> attributeMap = definition.getAttributeMap();
        for (Map.Entry<String, ExtendedAttributeDefinition> attributeEntry : attributeMap.entrySet()) {
            String attributeName = attributeEntry.getKey();
            ExtendedAttributeDefinition attributeDefinition = attributeEntry.getValue();

            Tr.debug(tc, "    [ " + attributeName + " ] [ " + attributeDefinition + " ]");

            if (attributeDefinition != null) {
                Tr.debug(tc, "      Type [ " + attributeDefinition.getType() + " ]");
                Tr.debug(tc, "      Card [ " + attributeDefinition.getCardinality() + " ]");
                Tr.debug(tc, "      Desc [ " + attributeDefinition.getDescription() + " ]");
            }
        }
    }

    /**
     * Valid configuration elements. If a validation problem is found, generate a validation
     * message and emit this as a trace warning. A validation message, if generated, will
     * usually be a multi-line message, with a half-dozen lines or more per problem which
     * is detected.
     *
     * @param pid      TBD
     * @param id       TBD
     * @param elements The configuration elements which are to be validated.
     *
     * @return True or false telling if the configuration elements are valid.
     */
    public boolean validate(String pid, ConfigID id, List<? extends ConfigElement> elements) {
        RegistryEntry registryEntry = metatypeRegistry.getRegistryEntryByPidOrAlias(pid);
        return validate(registryEntry, pid, id, elements);
    }

    /**
     * Valid configuration elements. If a validation problem is found, generate a validation
     * message and emit this as a trace warning. A validation message, if generated, will
     * usually be a multi-line message, with a half-dozen lines or more per problem which
     * is detected.
     *
     * @param registryEntry The registry entry associated with the PID of the configuration elements.
     * @param pid           TBD
     * @param id            TBD
     * @param elements      The configuration elements which are to be validated.
     *
     * @return True or false telling if the configuration elements are valid.
     */
    public boolean validate(RegistryEntry registryEntry, String pid, ConfigID id, List<? extends ConfigElement> elements) {
        Map<String, ConfigElementList> conflictedElementLists = generateConflictMap(registryEntry, elements);
        if (conflictedElementLists.isEmpty()) {
            return true;
        }

        logRegistryEntry(registryEntry);
        String validationMessage = generateCollisionMessage(pid, id, registryEntry, conflictedElementLists);
        Tr.audit(tc, "info.config.multiple.values", validationMessage);

        return false;
    }

    /**
     * Look for conflicts between single-valued attributes. No metatype registry
     * entry is available.
     *
     * @param elements The configuration elements to test.
     *
     * @return A table of conflicts between the configuration elements.
     */
    protected Map<String, ConfigElementList> generateConflictMap(List<? extends ConfigElement> list) {
        return generateConflictMap(null, list);
    }

    protected Map<String, ConfigElementList> generateConflictMap(ConfigElement... elements) {
        return generateConflictMap(null, Arrays.asList(elements));
    }

    /**
     * Look for conflicts between single-valued attributes of a list of configuration elements.
     *
     * Match attributes by name.
     *
     * Conflicts are keyed by attribute name.
     *
     * @param registryEntry The registry entry for the configuration elements.
     * @param elements      The configuration elements to test.
     *
     * @return A mapping of conflicts detected across the configuration elements.
     */
    protected Map<String, ConfigElementList> generateConflictMap(RegistryEntry registryEntry, List<? extends ConfigElement> elements) {
        if (elements.size() <= 1) {
            return Collections.emptyMap();
        }

        boolean foundConflict = false;

        Map<String, ConfigElementList> conflictMap = new HashMap<String, ConfigElementList>();
        for (ConfigElement element : elements) {
            for (Map.Entry<String, Object> entry : element.getAttributes().entrySet()) {
                String attributeName = entry.getKey();
                Object attributeValue = entry.getValue();

                // Do NOT add null attributes to the list.
                // 'ConfigElementList.add' does not handle null values.

                if (attributeValue == null) {
                    continue;
                }
                // consider single-values attributes only
                if (!(attributeValue instanceof String)) {
                    continue;
                }

                ConfigElementList configList = conflictMap.get(attributeName);
                if (configList == null) {
                    configList = new ConfigElementList(attributeName);
                    conflictMap.put(attributeName, configList);
                }

                // Note: A conflict is registered any time the value
                // is different, even if the merge policy for that
                // attribute is "IGNORE".
                if (configList.add(element)) { // Validation occurs within 'add'.
                    foundConflict = true;
                }
            }
        }

        if (!foundConflict) {
            return Collections.emptyMap();
        } else {
            return conflictMap;
        }
    }

    private void append(StringBuilder builder, String prefix, String msgId, Object... msgArgs) {
        builder.append(prefix);
        builder.append(Tr.formatMessage(tc, msgId, msgArgs));
        builder.append(LINE_SEPARATOR);
    }

    private boolean isUnset(Object attributeValue) {
        return ((attributeValue == null) ||
                (attributeValue instanceof String) && ((String) attributeValue).isEmpty());
    }

    /**
     * Emit a message for all detected conflicting elements.
     *
     * Message lines are generated for each attribute which has a conflict, and
     * for each value of each conflicted attribute.
     *
     * The conflict map should never be empty.
     *
     * Do not emit values for secure attributes. See {@link #isSecureAttribute(RegistryEntry, String)}.
     */
    // Public: Test entry
    public String generateCollisionMessage(String pid, ConfigID id, RegistryEntry registryEntry,
                                           Map<String, ConfigElementList> conflictMap) {

        String useId = ((id == null) ? null : id.getId());

        StringBuilder builder = new StringBuilder();

        String bannerMsg;
        if (id == null) {
            bannerMsg = "config.validator.multiple.values.singleton";
        } else {
            bannerMsg = "config.validator.multiple.values.instance";
        }
        append(builder, "", bannerMsg, pid, useId);

        for (Map.Entry<String, ConfigElementList> entry : conflictMap.entrySet()) {
            String attributeName = entry.getKey();

            ConfigElementList conflictList = entry.getValue();
            if (!conflictList.hasConflict()) {
                continue;
            }

            boolean isSecure = isSecureAttribute(registryEntry, attributeName);
            String activeLoc = conflictList.getActiveElement().getMergedLocation();

            append(builder, "  ", "config.validator.attribute", attributeName);

            for (ConfigElement element : conflictList) {
                String docLocation = element.getDocumentLocation();
                boolean inUse = docLocation.equals(activeLoc);

                docLocation = relativeLocation(docLocation);

                Object attributeValue;
                String valueMsg;

                if (isSecure) {
                    attributeValue = null;
                    valueMsg = inUse ? "config.validator.attribute.value.secure.inuse" : "config.validator.attribute.value.secure";
                } else {
                    attributeValue = element.getAttribute(attributeName);
                    if (isUnset(attributeValue)) {
                        attributeValue = null;
                        valueMsg = inUse ? "config.validator.attribute.value.unset.inuse" : "config.validator.attribute.value.unset";
                    } else {
                        valueMsg = inUse ? "config.validator.attribute.value.inuse" : "config.validator.attribute.value";
                    }
                }

                if (attributeValue == null) {
                    append(builder, "    ", valueMsg, docLocation);
                } else {
                    append(builder, "    ", valueMsg, docLocation, attributeValue);
                }
            }
        }

        return builder.toString();
    }

    /**
     * Configuration element list. This list replicates configuration merge
     * processing. Three APIs are defined:
     *
     * First, an element list is specified for a single attribute. Conflict
     * detection is performed on the values of that attribute.
     *
     * Second, when adding elements, the attribute value is tested, and the
     * list records whether it has multiple values. The meaning of
     * {@link ConfigElementList#add} is redefined to tell whether the list
     * has multiple values after the addition.
     *
     * Third, the list may be queried to tell if there are multiple values
     * {@link #hasConflict()}), and the active element and value may be
     * retrieved ({@link #getActiveElement()} and {@link #getActiveValue()}).
     *
     * Retrieval of the active element and active value must not be performed
     * before adding elements. An early attempt will result in a runtime
     * exception.
     */
    protected class ConfigElementList extends ArrayList<ConfigElement> {
        private static final long serialVersionUID = -8472291303190806069L;

        private final String attributeName;

        private String firstValue;
        private boolean hasConflict;
        private ConfigElement activeElement;

        public ConfigElementList(String attributeName) {
            this.attributeName = attributeName;

            this.firstValue = null;
            this.hasConflict = false;
            this.activeElement = null;
        }

        /**
         * Retrieve the value of the specified attribute from the element.
         *
         * Perform string resolution on the value. See
         * {@link ConfigVariableRegistry#resolveRawString}.
         *
         * @param element A configuration element.
         *
         * @return The value of the attribute of the element.
         */
        public String getAttribute(ConfigElement element) {
            return variableRegistry.resolveRawString((String) element.getAttribute(attributeName));
        }

        /**
         * Add an element to the list. The element must not have a null
         * attribute value.
         *
         * Tell if the list has multiple values after the addition.
         *
         * Note that this changes the meaning of 'add': {@link ArrayList#add}
         * answers true or false telling if the element was added, which is
         * always true for array lists.
         *
         * Note also: The value which is returned is not indicative of whether
         * the newly added element introduced a new value. The value returned
         * is a composite telling if any elements have different values.
         *
         * @param element The element which is to be added.
         *
         * @return True or false telling if the list has multiple attribute
         *         values.
         */
        @Override
        public boolean add(ConfigElement element) {
            if (hasConflict) {
                // Nothing to do: No checking is necessary once a conflict is detected.

            } else {
                String nextValue = getAttribute(element);
                if (isEmpty()) {
                    firstValue = nextValue;
                } else {
                    if (firstValue == null) {
                        hasConflict = (nextValue != null);
                    } else {
                        hasConflict = !firstValue.equals(nextValue);
                    }
                }
            }

            super.add(element);

            return hasConflict;
        }

        /**
         * Answer the attribute value of the last element of this list.
         *
         * A runtime exception will be thrown if no elements are present in the
         * list. See {@link ArrayList#get(int)}.
         *
         * @return The attribute value of the last element of this list.
         */
        @Deprecated
        public Object getLastValue() {
            return get(size() - 1).getAttribute(attributeName);
        }

        /**
         * Tell if the elements of this list have more than one attribute value.
         *
         * Attribute values are compared after performing variable resolution.
         * Element with different raw attribute values might resolve to the
         * same value, which would not detect as a conflict.
         *
         * @return True or false telling if the elements of this list have more
         *         than one attribute value.
         */
        public boolean hasConflict() {
            return hasConflict;
        }

        /**
         * Answer the active element of this list.
         *
         * Null will never be returned: If this list is empty,
         * the attempt to retrieve the active element results
         * in an runtime exception.
         *
         * See also {@link #computeActiveElement()}.
         *
         * @return The active element of this list.
         */
        protected ConfigElement getActiveElement() {
            if (activeElement == null) {
                activeElement = computeActiveElement();
            }
            return activeElement;
        }

        /**
         * Compute the active element of this list.
         *
         * This is a merge of the elements, which always starts
         * as a copy of the element which has the first sequence
         * number.
         *
         * Null will never be returned: If this list is empty,
         * the attempt to compute the active element results in
         * an runtime exception.
         *
         * That a copied element is always answered is a historical
         * artifact. That this is necessary is not clear.
         *
         * @return The active element of this list.
         */
        protected ConfigElement computeActiveElement() {
            ConfigElement firstElement = get(0);

            int useSize = size();
            if (useSize == 0) {
                throw new IllegalStateException("Elements must be added first");

            } else if (useSize == 1) {
                return new SimpleElement(firstElement);

            } else if (useSize == 2) {
                ConfigElement secondElement = get(1);

                // Always merge onto the sequentially first element.
                if (firstElement.getSequenceId() > secondElement.getSequenceId()) {
                    ConfigElement temp = firstElement;
                    firstElement = secondElement;
                    secondElement = temp;
                }

                firstElement = new SimpleElement(firstElement);
                firstElement.override(secondElement);
                return firstElement;

            } else {
                // A copy must be supplied: 'merge' sorts the elements by sequence number.
                // One less than the whole: The merge target doesn't need to be in the
                // list.
                List<ConfigElement> elements = new ArrayList<ConfigElement>(useSize - 1);

                // Always merge onto the sequentially first element.
                int firstSeq = firstElement.getSequenceId();
                for (int elementNo = 1; elementNo < useSize; elementNo++) {
                    ConfigElement nextElement = get(elementNo);
                    int nextSeq = nextElement.getSequenceId();
                    if (nextSeq < firstSeq) {
                        elements.add(firstElement);
                        firstElement = nextElement;
                        firstSeq = nextSeq;
                    } else {
                        elements.add(nextElement);
                    }
                }

                firstElement.merge(elements);
            }

            return firstElement;
        }

        // Updated by defect 172453:
        //
        // Previously, the active attribute value was obtained by overriding
        // the initial configuration element with all of the following
        // configuration elements.
        //
        // The update changes that to merge all elements of the list onto a
        // copy of the first list element.

        /**
         * Answer the value of the target attribute which results
         * from the merging of the elements of this list.
         *
         * @return The merged attribute value for this element list.
         */
        public Object getActiveValue() {
            return getActiveElement().getAttribute(attributeName);
        }
    }
}
