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
package com.ibm.ws.config.xml.internal.metatype;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.metatype.EquinoxObjectClassDefinition;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.xml.internal.XMLConfigConstants;

//@formatter:off
public class ExtendedObjectClassDefinitionImpl implements ExtendedObjectClassDefinition {

    // backwards "compatible".  These strings were inadvertently exposed to users as ibm:filter targets.
    // We convert them to ibm:objectClass silently so they can be seen as targets of ibm:service.

    private static final List<String> EXPOSED_FILTERS =
        Arrays.asList( "com.ibm.ws.zos.connect.interceptorType",
                       "com.ibm.ws.zos.connect.dataXformType",
                       "com.ibm.ws.zos.connect.serviceType" );


    /**
     * Prefixes the alias with the product extension name if there is a product extension
     * associated to this OCD.
     *
     * @param alias          The alias name to process.
     * @param bundleLocation bundle location to analyze to provide prefix for the alias
     * @return The new alias possibly including a prefix based on the bundle location.
     */
    private static String getAliasName(String alias, String bundleLocation) {
        if ( (alias == null) || alias.isEmpty() ) {
            return alias;
        }

        if ( (bundleLocation == null) || bundleLocation.isEmpty() ) {
            return alias;
        }

        // "kernel@", "ConnectorModuleMetatype@"
        if ( bundleLocation.startsWith(XMLConfigConstants.BUNDLE_LOC_KERNEL_TAG) ||
             bundleLocation.startsWith(XMLConfigConstants.BUNDLE_LOC_CONNECTOR_TAG) ) {
            return alias; // nothing to do. The alias is returned.

        // "feature@"
        } else if ( !bundleLocation.startsWith(XMLConfigConstants.BUNDLE_LOC_FEATURE_TAG) ) {
            // Unknown location. Ignore the alias. If bundles are installed through fileInstall,
            // bundle resolution should happen through the pid or factoryPid.
            return null;

        } else {
            // "productExtension:"
            int startIndex = bundleLocation.indexOf(XMLConfigConstants.BUNDLE_LOC_PROD_EXT_TAG);
            if ( startIndex == -1 ) {
                return alias;
            } else {
                // Expecting, for example:
                //     "feature@productExtension:testproduct:" +
                //     "reference:file:/C:/test/test.prod.extensions_1.0.0.jar";

                startIndex += XMLConfigConstants.BUNDLE_LOC_PROD_EXT_TAG.length();

                int endIndex = bundleLocation.indexOf(':', startIndex);
                if ( endIndex == -1 ) {
                    throw new IllegalArgumentException("Non-valid bundle location: " + bundleLocation);
                }

                String productName = bundleLocation.substring(startIndex, endIndex);
                return productName + "_" + alias;
            }
        }
    }

    @Trivial
    public static ExtendedObjectClassDefinitionImpl newExtendedObjectClassDefinition(ObjectClassDefinition ocd, String bundleLocation) {
        if ( ocd instanceof EquinoxObjectClassDefinition ) {
            return new ExtendedObjectClassDefinitionImpl((EquinoxObjectClassDefinition) ocd, bundleLocation);
        } else if ( ocd instanceof WSObjectClassDefinitionImpl ) {
            return new ExtendedObjectClassDefinitionImpl((WSObjectClassDefinitionImpl) ocd, bundleLocation);
        } else {
            return new ExtendedObjectClassDefinitionImpl(ocd);
        }
    }

    private static List<String> add(List<String> storage, String value) {
        if ( storage == null ) {
            storage = Collections.singletonList(value);
        } else {
            if ( storage.size() == 1 ) {
                storage = new ArrayList<String>(storage);
            }
            storage.add(value);
        }
        return storage;
    }

    private ExtendedObjectClassDefinitionImpl(EquinoxObjectClassDefinition extendedOcd, String bundleLocation) {
        this.delegate = extendedOcd;

        Set<String> supportedExtensions = extendedOcd.getExtensionUris();

        Map<String, String> extensions;
        if ( (supportedExtensions != null) && supportedExtensions.contains(XMLConfigConstants.METATYPE_EXTENSION_URI) ) {
            extensions = extendedOcd.getExtensionAttributes(XMLConfigConstants.METATYPE_EXTENSION_URI);
        } else {
            extensions = Collections.emptyMap();
        }

        Map<String, String> uiExtensions;
        if ( (supportedExtensions != null) && supportedExtensions.contains(XMLConfigConstants.METATYPE_UI_EXTENSION_URI) ) {
            uiExtensions = extendedOcd.getExtensionAttributes(XMLConfigConstants.METATYPE_UI_EXTENSION_URI);
        } else {
            uiExtensions = Collections.emptyMap();
        }

        this.parentPid = extensions.get(PARENT_PID_ATTRIBUTE);
        this.alias = getAliasName(extensions.get(ALIAS_ATTRIBUTE), bundleLocation);
        this.action = extensions.get(METATYPE_ACTION_ATTRIBUTE);

        this.supportsHiddenExtensions =
            ( extensions.get(SUPPORTS_HIDDEN_EXTENSIONS_ATTRIBUTE) != null );
        this.supportsExtensions = this.supportsHiddenExtensions ||
            ( extensions.get(SUPPORTS_EXTENSIONS_ATTRIBUTE) != null );

        this.extendsAlias = extensions.get(EXTENDS_ALIAS_ATTRIBUTE);
        this.extendsAttribute = extensions.get(EXTENDS_ATTRIBUTE);
        this.childAlias = extensions.get(CHILD_ALIAS_ATTRIBUTE);
        this.excludedChildren = extensions.get(EXCLUDED_CHILDREN_ATTRIBUTE);
        this.requireExplicitConfiguration =
            ExtendedAttributeDefinition.isTrueString( extensions.get(REQUIRE_EXPLICIT_CONFIGURATION) );
        this.beta =
            ExtendedAttributeDefinition.isTrueString( extensions.get(BETA_ATTRIBUTE) );

        List<String> objectClasses = null;

        for ( AttributeDefinition ad : extendedOcd.getAttributeDefinitions(ALL) ) {
            String attributeID = ad.getID();
            if ( EXPOSED_FILTERS.contains(attributeID) ) {
                objectClasses = add(objectClasses, attributeID);
            }
        }

        String extensionObjectClass = extensions.get(OBJECT_CLASS);
        if ( extensionObjectClass != null ) {
            String[] extensionObjectClasses = extensionObjectClass.split("[, ]+");
            for ( String extensionClass : extensionObjectClasses ) {
                if ( (objectClasses == null) || !objectClasses.contains(extensionClass) ) {
                    objectClasses = add(objectClasses, extensionClass);
                }
            }
        }

        this.objectClass = objectClasses;

        int useAnyCount;
        String anyVal = extensions.get(XSD_ANY_ATTRIBUTE);
        if ( anyVal != null ) {
            try {
                useAnyCount = Integer.parseInt(anyVal);
            } catch ( NumberFormatException nfe ) {
                useAnyCount = 0; // Ignore and assign 0.
            }
        } else {
            useAnyCount = 0;
        }
        this.anyCount = useAnyCount;

        this.extraProperties =
            ExtendedAttributeDefinition.isTrueString( uiExtensions.get(METATYPE_EXTRA_PROPERTIES) );
        this.localization = uiExtensions.get(LOCALIZATION_ATTRIBUTE);
    }

    private ExtendedObjectClassDefinitionImpl(WSObjectClassDefinitionImpl delegate, String bundleLocation) {
        this.delegate = delegate;

        this.parentPid = delegate.getParentPID();
        this.alias = getAliasName(delegate.getAlias(), bundleLocation);
        this.extraProperties = false;
        this.supportsExtensions = delegate.supportsExtensions() || delegate.supportsHiddenExtensions();
        this.supportsHiddenExtensions = delegate.supportsHiddenExtensions();
        this.childAlias = delegate.getChildAlias();
        this.extendsAlias = delegate.getExtendsAlias();
        this.extendsAttribute = delegate.getExtends();
        this.objectClass = delegate.getObjectClass();

        if ( delegate instanceof ExtendedObjectClassDefinition ) {
            this.anyCount = ((ExtendedObjectClassDefinition) delegate).getXsdAny();
            this.excludedChildren = ((ExtendedObjectClassDefinition) delegate).getExcludedChildren();
            this.action = ((ExtendedObjectClassDefinition) delegate).getAction();
            this.requireExplicitConfiguration = !((ExtendedObjectClassDefinition) delegate).hasAllRequiredDefaults();
            this.beta = ((ExtendedObjectClassDefinition) delegate).isBeta();
        } else {
            this.anyCount = 0;
            this.excludedChildren = null;
            this.action = null;
            this.requireExplicitConfiguration = false;
            this.beta = false;
        }

        this.localization = null;
    }

    /**
     * Basic constructor: Set the delegate.  Default all other values.
     *
     * @param delegate The delegate of the new extended class definition.
     */
    private ExtendedObjectClassDefinitionImpl(ObjectClassDefinition delegate) {
        this.delegate = delegate;

        this.parentPid = null;
        this.alias = null;
        this.extraProperties = false;
        this.localization = null;
        this.supportsExtensions = false;
        this.supportsHiddenExtensions = false;
        this.extendsAlias = null;
        this.extendsAttribute = null;
        this.childAlias = null;
        this.excludedChildren = null;
        this.requireExplicitConfiguration = false;

        this.objectClass = null;
        this.action = null;
        this.anyCount = 0;
        this.beta = false;
    }

    @Override
    public String toString() {
        return super.toString() + '[' + delegate.getID() + ']';
    }

    //

    // HOPEFULLY TEMPORARY!

    private String pid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    //

    private final ObjectClassDefinition delegate;

    @Override
    @Trivial
    public ObjectClassDefinition getDelegate() {
        return delegate;
    }

    //

    @Override
    @Trivial
    public String getID() {
        return delegate.getID();
    }

    @Override
    @Trivial
    public String getName() {
        return delegate.getName();
    }

    @Override
    @Trivial
    public InputStream getIcon(int size) throws IOException {
        return delegate.getIcon(size);
    }

    @Override
    @Trivial
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    @Trivial
    public AttributeDefinition[] getAttributeDefinitions(int filter) {
        return delegate.getAttributeDefinitions(filter);
    }

    //

    @Override
    @Trivial
    public List<AttributeDefinition> getRequiredAttributes() {
        return Arrays.asList(getAttributeDefinitions(REQUIRED));

    }

    //ONLY USED BY SCHEMA WRITER
    @Override
    @Trivial
    /**
     * Create and return a table of all attributes of this extended class definition.
     *
     * Keys are attribute IDs.  Values are extended attributes, one for each of
     * the attributes of the delegate.  See {@link ObjectClassDefinition#getAttributeDefinitions}.
     *
     * @return A table of attributes of this extended class definition.
     */
    public Map<String, ExtendedAttributeDefinition> getAttributeMap() {
        AttributeDefinition[] attrDefs = getAttributeDefinitions(ObjectClassDefinition.ALL);
        if (attrDefs == null) {
            return Collections.emptyMap();
        }

        Map<String, ExtendedAttributeDefinition> attributeMap =
            new HashMap<String, ExtendedAttributeDefinition>();
        for ( AttributeDefinition attrDef : attrDefs ) {
            attributeMap.put(attrDef.getID(), new ExtendedAttributeDefinitionImpl(attrDef));
        }
        return attributeMap;
    }

    private final boolean requireExplicitConfiguration;

    /**
     * Tell if this extended class definition has defaults for all of its attributes.
     *
     * Answer false if metadata is configured to require explicit configuration.
     * (This makes the method is badly named.  A better name would be
     * "requiresExplicitConfiguration".)
     *
     * Answer false if any required attribute does not have any default values.
     *
     * Answer true if all required attributes have a default value and at least one
     * attribute has a default value.  (This is an odd but meaningful check: At least
     * one attribute of the class definition will be present.)
     *
     * @return True or false telling if the class definition does not require an
     *     explicit configuration.  This is false if explicitly configured, or if
     *     any required attribute does not have a default, or if no attributes has
     *     a default.
     */
    @Override
    @Trivial
    public boolean hasAllRequiredDefaults() {
        if ( requireExplicitConfiguration ) {
            return false;
        }

        AttributeDefinition[] requiredAttributes = getAttributeDefinitions(ObjectClassDefinition.REQUIRED);
        if ( requiredAttributes != null ) {
            for ( AttributeDefinition attrDef : requiredAttributes ) {
                String[] defaultValues = attrDef.getDefaultValue();
                if ( defaultValues == null ) {
                    return false;
                }
            }
        }

        AttributeDefinition[] attrDefs = getAttributeDefinitions(ObjectClassDefinition.ALL);
        if ( attrDefs != null ) {
            for ( AttributeDefinition attrDef : attrDefs ) {
                String[] defaultValues = attrDef.getDefaultValue();
                if ( (defaultValues != null) && (defaultValues.length > 0) ) {
                    return true;
                }
            }
        }

        return false;
    }

    //

    private final String alias;

    @Override
    @Trivial
    public String getAlias() {
        return alias;
    }

    private final List<String> objectClass;

    @Override
    @Trivial
    public List<String> getObjectClass() {
        return objectClass;
    }

    private final String parentPid;

    @Override
    @Trivial
    public String getParentPID() {
        return parentPid;
    }

    private final boolean extraProperties;

    @Override
    @Trivial
    public boolean hasExtraProperties() {
        return extraProperties;
    }

    private final String localization;

    @Override
    @Trivial
    public String getLocalization() {
        return localization;
    }

    private final String extendsAlias;

    @Override
    public String getExtendsAlias() {
        return extendsAlias;
    }

    private final String extendsAttribute;

    @Override
    @Trivial
    public String getExtends() {
        return extendsAttribute;
    }

    private final String childAlias;

    @Override
    @Trivial
    public String getChildAlias() {
        return childAlias;
    }

    private final boolean supportsExtensions;
    private final boolean supportsHiddenExtensions;

    @Override
    @Trivial
    public boolean supportsExtensions() {
        return supportsExtensions;
    }

    @Override
    @Trivial
    public boolean supportsHiddenExtensions() {
        return supportsHiddenExtensions;
    }

    private final int anyCount;

    @Override
    public int getXsdAny() {
        return anyCount;
    }

    private final String excludedChildren;

    @Override
    public String getExcludedChildren() {
        return excludedChildren;
    }

    private final String action;

    @Override
    public String getAction() {
        return action;
    }

    private final boolean beta;

    @Override
    public boolean isBeta() {
        return beta;
    }
}
//@formatter:on