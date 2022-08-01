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

import java.util.List;
import java.util.Map;

import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

/**
 * Extended object class definition metadata.
 */
public interface ExtendedObjectClassDefinition extends ObjectClassDefinition {

    String ALIAS_ATTRIBUTE = "alias";

    /**
     * Return the alias value that should be used in the configuration to refer to this object
     * class definition. The alias value must be unique across all alias values defined in
     * metatype.
     *
     * @return a String containing the alias value or null if not used
     */
    String getAlias();

    //

    String CHILD_ALIAS_ATTRIBUTE = "childAlias";

    /**
     * Used ONLY for child-first nested elements.
     * Return the alias value that should be used if this object class definition represents an element
     * that can only exist when nested under another element. This is only valid when there is a value
     * returned from getParentPid. The alias value does not have to be unique.
     *
     * @return a String containing the alias value or null if not used.
     */
    String getChildAlias();

    //

    String EXTENDS_ATTRIBUTE = "extends";

    /**
     * Returns the name of the pid this object is extending.
     *
     * @return the ibm:extends String or null if the value does not exist
     */
    String getExtends();

    String EXTENDS_ALIAS_ATTRIBUTE = "extendsAlias";

    /**
     * Return the suffix for constructing nested element names of the form adId.suffix for OCDs that extend reference pids in a parent-first nesting.
     * Used ONLY for extending (ibm:extends) elements with an ancestor pid used as a reference pid in nested xml.
     *
     * @return a String containing the suffix for constructing element names or null if not used.
     */
    String getExtendsAlias();

    //

    String PARENT_PID_ATTRIBUTE = "parentPid";

    /**
     * Used ONLY for child-first nested elements.
     * Returns the pid or factory pid of the object class definition that this definition should
     * be nested under.
     *
     * @return a String containing the parent pid value or null if not used
     */
    String getParentPID();

    //

    String SUPPORTS_EXTENSIONS_ATTRIBUTE = "supportExtensions";

    /**
     * Used ONLY to support child-first nested elements under this element.
     *
     * Return true if this object class definition supports nested elements
     * not specified in metatype.
     *
     * @return True or false telling if nested elements are supported.
     */
    boolean supportsExtensions();

    String SUPPORTS_HIDDEN_EXTENSIONS_ATTRIBUTE = "supportHiddenExtensions";

    /**
     * Return true if this object class definition supports nested
     * elements not specified in metatype and these nested elements
     * are child first and should not appear in the parent at all.
     *
     * @return True or false telling if hidden nested elements are supported.
     */
    boolean supportsHiddenExtensions();

    //

    String METATYPE_EXTRA_PROPERTIES = "extraProperties";

    boolean hasExtraProperties();

    String METATYPE_ACTION_ATTRIBUTE = "action";

    String getAction();

    String LOCALIZATION_ATTRIBUTE = "localization";

    String getLocalization();

    String XSD_ANY_ATTRIBUTE = "any";

    int getXsdAny();

    String EXCLUDED_CHILDREN_ATTRIBUTE = "excludeChildren";

    String getExcludedChildren();

    String OBJECT_CLASS = "objectClass";

    List<String> getObjectClass();

    String BETA_ATTRIBUTE = "beta";

    boolean isBeta();

    //

    ObjectClassDefinition getDelegate();

    //

    /**
     * Computes a map keyed with attribute ID with values wrapped AttributeDefinitions.
     * USED ONLY BY SCHEMA BUILDER
     *
     * @return a map of attribute ID to wrapped attribute definition.
     */
    Map<String, ExtendedAttributeDefinition> getAttributeMap();

    //

    String REQUIRE_EXPLICIT_CONFIGURATION = "requireExplicitConfiguration";

    boolean hasAllRequiredDefaults();

    List<AttributeDefinition> getRequiredAttributes();
}
