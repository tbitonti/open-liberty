/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal.metatype;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.metatype.EquinoxAttributeDefinition;
import org.osgi.service.metatype.AttributeDefinition;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.xml.internal.XMLConfigConstants;
import com.ibm.ws.config.xml.internal.schema.AttributeDefinitionSpecification;

@Trivial
public class ExtendedAttributeDefinitionImpl implements ExtendedAttributeDefinition {

    public ExtendedAttributeDefinitionImpl(AttributeDefinition delegate) {
        this.delegate = delegate;

        int useType;

        if (delegate instanceof EquinoxAttributeDefinition) {
            EquinoxAttributeDefinition equinoxDelegate = (EquinoxAttributeDefinition) delegate;

            Map<String, String> metatypeExtensions = getMetatypeExtensions(equinoxDelegate);

            String typeText = metatypeExtensions.get(ATTRIBUTE_TYPE_NAME);
            if (typeText != null) {
                Integer typeValue = MetaTypeFactoryImpl.IBM_TYPES.get(typeText);
                if (typeValue != null) {
                    useType = typeValue.intValue();
                } else {
                    throw new IllegalArgumentException("Unrecognized type '" + typeText + "' in attribute definition " + equinoxDelegate.getID());
                }
            } else {
                useType = delegate.getType();
            }

            this.referencePid = metatypeExtensions.get(ATTRIBUTE_REFERENCE_NAME);

            this.isFinal = (metatypeExtensions.get(FINAL_ATTR_NAME) != null);
            this.isFlat = (metatypeExtensions.get(FLAT_ATTR_NAME) != null);
            this.obscure = metatypeExtensions.get(OBSCURE_NAME) != null;
            // A beta attribute *ONLY IF* explicitly enabled.
            this.beta = ExtendedAttributeDefinition.isTrueString(metatypeExtensions.get(BETA_NAME));
            // Resolve variables *UNLESS* explicitly disabled.
            this.resolveVariables = !(ExtendedAttributeDefinition.isFalseString(metatypeExtensions.get(VARIABLE_SUBSTITUTION_NAME)));

            this.isUnique = metatypeExtensions.get(UNIQUE_ATTR_NAME) != null;
            this.uniqueCategory = metatypeExtensions.get(UNIQUE_ATTR_NAME);

            this.copyOf = metatypeExtensions.get(COPY_OF_ATTR_NAME);
            this.rename = metatypeExtensions.get(RENAME_ATTR_NAME);
            this.variable = metatypeExtensions.get(VARIABLE_ATTR_NAME);

            this.service = metatypeExtensions.get(SERVICE);
            this.serviceFilter = metatypeExtensions.get(SERVICE_FILTER);

            Map<String, String> metatypeUIExtensions = getMetatypeUIExtensions(equinoxDelegate);

            this.group = metatypeUIExtensions.get(GROUP_ATTR_NAME);
            this.requiresFalse = metatypeUIExtensions.get(REQUIRES_FALSE_ATTR_NAME);
            this.requiresTrue = metatypeUIExtensions.get(REQUIRES_TRUE_ATTR_NAME);

            String uiReferenceValue = metatypeUIExtensions.get(UI_REFERENCE);
            if (uiReferenceValue != null) {
                this.uiReference = Arrays.asList(uiReferenceValue.split("[, ]+"));
            } else {
                this.uiReference = null;
            }

        } else if (delegate instanceof WSAttributeDefinitionImpl) {
            WSAttributeDefinitionImpl wsDelegate = (WSAttributeDefinitionImpl) this.delegate;

            useType = delegate.getType();

            this.referencePid = wsDelegate.getReferencePid();

            this.isFinal = wsDelegate.isFinal();
            this.isFlat = wsDelegate.isFlat();
            this.obscure = false;
            this.beta = false;
            this.resolveVariables = true;

            this.isUnique = wsDelegate.isUnique();
            this.uniqueCategory = wsDelegate.getUnique();

            this.copyOf = wsDelegate.getCopyOf();
            this.rename = null;
            this.variable = wsDelegate.getVariable();

            this.service = wsDelegate.getService();
            this.serviceFilter = wsDelegate.getServiceFilter();

            this.group = null;
            this.requiresFalse = null;
            this.requiresTrue = null;
            this.uiReference = null;

        } else {
            throw new IllegalArgumentException("Unknown attribute definition type [ " + delegate.getClass() + " ]");
        }

        this.delegateType = useType;
    }

    private static Map<String, String> getMetatypeExtensions(EquinoxAttributeDefinition equinoxDelegate) {
        Set<String> supportedExtensions = equinoxDelegate.getExtensionUris();

        if ((supportedExtensions != null) && supportedExtensions.contains(XMLConfigConstants.METATYPE_EXTENSION_URI)) {
            return equinoxDelegate.getExtensionAttributes(XMLConfigConstants.METATYPE_EXTENSION_URI);
        } else {
            return null;
        }
    }

    private static Map<String, String> getMetatypeUIExtensions(EquinoxAttributeDefinition equinoxDelegate) {
        Set<String> supportedExtensions = equinoxDelegate.getExtensionUris();

        if ((supportedExtensions != null) && supportedExtensions.contains(XMLConfigConstants.METATYPE_UI_EXTENSION_URI)) {
            return equinoxDelegate.getExtensionAttributes(XMLConfigConstants.METATYPE_UI_EXTENSION_URI);
        } else {
            return null;
        }
    }

    //

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof ExtendedAttributeDefinitionImpl) {
            return this.delegate == ((ExtendedAttributeDefinitionImpl) other).delegate;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + '[' + delegate.getID() + ']';
    }

    //

    private final AttributeDefinition delegate;
    private final int delegateType;

    @Override
    public AttributeDefinition getDelegate() {
        return delegate;
    }

    @Override
    public int getType() {
        return delegateType;
    }

    //

    @Override
    public String getID() {
        return delegate.getID();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public int getCardinality() {
        return delegate.getCardinality();
    }

    @Override
    public String[] getOptionValues() {
        return delegate.getOptionValues();
    }

    @Override
    public String[] getOptionLabels() {
        return delegate.getOptionLabels();
    }

    @Override
    public String validate(String value) {
        return delegate.validate(value);
    }

    @Override
    public String[] getDefaultValue() {
        return delegate.getDefaultValue();
    }

    //

    @Override
    public Set<String> getExtensionUris() {
        if (delegate instanceof EquinoxAttributeDefinition) {
            return ((EquinoxAttributeDefinition) delegate).getExtensionUris();
        } else {
            return Collections.<String> emptySet();
        }
    }

    @Override
    public Map<String, String> getExtensions(String extensionUri) {
        if (delegate instanceof EquinoxAttributeDefinition) {
            return ((EquinoxAttributeDefinition) delegate).getExtensionAttributes(extensionUri);
        } else {
            return Collections.<String, String> emptyMap();
        }
    }

    @Override
    public String getAttributeName() {
        if (delegate instanceof AttributeDefinitionSpecification) {
            return ((AttributeDefinitionSpecification) delegate).getAttributeName();
        } else {
            return delegate.getName();
        }
    }

    //

    private final String referencePid;

    private final boolean isFinal;
    private final boolean isFlat;
    private final boolean obscure;
    private final boolean resolveVariables;
    private final boolean beta;

    private final boolean isUnique;
    private final String uniqueCategory;

    private final String rename;
    private final String variable;
    private final String copyOf;

    private final String service;
    private final String serviceFilter;

    private final String group;
    private final String requiresFalse;
    private final String requiresTrue;
    private final List<String> uiReference;

    @Override
    public String getReferencePid() {
        return referencePid;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getServiceFilter() {
        return serviceFilter;
    }

    @Override
    public boolean isBeta() {
        return beta;
    }

    @Override
    public boolean isObscured() {
        return obscure;
    }

    @Override
    public String getCopyOf() {
        return copyOf;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String getVariable() {
        return variable;
    }

    @Override
    public boolean isUnique() {
        return isUnique;
    }

    @Override
    public boolean isFlat() {
        return isFlat;
    }

    @Override
    public String getUniqueCategory() {
        return uniqueCategory;
    }

    @Override
    public String getRename() {
        return rename;
    }

    @Override
    public boolean resolveVariables() {
        return resolveVariables;
    }

    //

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getRequiresTrue() {
        return requiresTrue;
    }

    @Override
    public String getRequiresFalse() {
        return requiresFalse;
    }

    @Override
    public List<String> getUIReference() {
        return uiReference;
    }
}
