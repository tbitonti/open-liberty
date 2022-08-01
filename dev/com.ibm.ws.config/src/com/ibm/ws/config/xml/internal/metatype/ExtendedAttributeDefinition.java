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
import java.util.Set;

import org.osgi.service.metatype.AttributeDefinition;

// @formatter: off
public interface ExtendedAttributeDefinition extends AttributeDefinition {
    @Deprecated
    String FALSE = "false";

    static boolean isTrueString(String value) {
        return ((value != null) && value.equalsIgnoreCase("true"));
    }

    static boolean isFalseString(String value) {
        return ((value != null) && value.equalsIgnoreCase("false"));
    }

    //

    String ATTRIBUTE_TYPE_NAME = "type";

    //

    String SERVICE = "service";

    String SERVICE_FILTER = "serviceFilter";

    String getService();

    String getServiceFilter();

    //

    /**
     * Answer the group name of the this attribute. This may
     * be null.
     *
     * @return the ibm:group String or null if the value does not exist
     */
    String getGroup();

    String ATTRIBUTE_REFERENCE_NAME = "reference";

    /**
     * Answer the PID or factory PID of values referenced by this attribute.
     * Answer null unless this is a <code>PID_TYPE</code> type attribute.
     *
     * @return The PID or factory PID of values referenced by this attribute.
     */
    String getReferencePid();

    String FINAL_ATTR_NAME = "final";

    /**
     * Returns true if the value of this attribute can not be specified in the configuration.
     *
     * @return true if the value can not be overridden, false otherwise
     */
    boolean isFinal();

    String VARIABLE_ATTR_NAME = "variable";

    /**
     * Gets the value of a system variable that should be used prior to any default values.
     *
     * @return the variable name or null if not specified
     */
    String getVariable();

    String UNIQUE_ATTR_NAME = "unique";

    /**
     * Returns true if values for this AttributeDefinition should be unique. If the attribute definition
     * is marked unique, no other elements in the configuration may use the same value.
     *
     * @return true if the value of the AttributeDefinition should be unique
     */
    boolean isUnique();

    /**
     * Returns the category to be used for unique value checking. For example, if the unique
     * category is jndiName, the value of this attribute must be unique across all attributes
     * that have a unique category value of jndiName.
     *
     * @return the category String or null if the value is not specified
     */
    String getUniqueCategory();

    String FLAT_ATTR_NAME = "flat";

    /**
     * Tell if nested elements should be flattened.
     *
     * @return True or false telling if nested elements should be flattened.
     */
    boolean isFlat();

    String COPY_OF_ATTR_NAME = "copyOf";

    /**
     * The value of this attribute is a copy of the value of another
     * attribute. Answer the name of that attribute.
     *
     * @return The name of the attribute which is copied to obtain the
     *         value of this attribute. Answer null if this attribute is
     *         not a copy.
     */
    String getCopyOf();

    AttributeDefinition getDelegate();

    /**
     * Get a Set of all the ExtensionUris associated with the Attribute.
     *
     * @return A Set of Strings containing the extension Uris.
     */
    Set<String> getExtensionUris();

    /**
     * Returns a Map of the extensions for the current extension URI
     *
     * @param extensionUri - The Uri that you want the extensions for.
     * @return - A Map of the extensions for the selected extension Uri.
     */
    Map<String, String> getExtensions(String extensionUri);

    String RENAME_ATTR_NAME = "rename";

    /**
     * Returns the id of the attribute on the supertype that should be renamed.
     * The id on this attribute definition will be used instead.
     *
     * @return the ibm:rename String or null if the value does not exist
     */
    String getRename();

    String VARIABLE_SUBSTITUTION_NAME = "variableSubstitution";

    /**
     * Tell if variable resolution is to be performed on the value
     * of this attribute.
     *
     * Default to true.
     *
     * @return True or false telling if variable resolution is to be
     *         performed on this attribute.
     */
    boolean resolveVariables();

    String BETA_NAME = "beta";

    /**
     * Tell if this is a beta attribute.
     *
     * @return True or false telling if this is a beta attribute.
     */
    boolean isBeta();

    String OBSCURE_NAME = "obscure";

    /**
     * Tell if the value of this attribute is to be obscured.
     *
     * @return True or false telling if the value of this attribute is to
     *         be obscured.
     */
    boolean isObscured();

    //

    String getAttributeName();

    String GROUP_ATTR_NAME = "group";

    //

    String REQUIRES_TRUE_ATTR_NAME = "requiresTrue";

    String REQUIRES_FALSE_ATTR_NAME = "requiresFalse";

    /**
     * Conditionally enable this attribute: Answer the name of an attribute of
     * the same element which must be true for this attribute to be enabled.
     * Answer null if no such attribute is required to enable this attribute.
     *
     * See also {@link #getRequiresFalse()}.
     *
     * @return The name of an attribute (of the same element) which must be
     *         true to enable this attribute.
     */
    String getRequiresTrue();

    /**
     * Conditionally enable this attribute: Answer the name of an attribute of
     * the same element which must be false for this attribute to be enabled.
     * Answer null if no such attribute is required to enable this attribute.
     *
     * See also {@link #getRequiresTrue()}.
     *
     * @return The name of an attribute (of the same element) which must be
     *         false to enable this attribute.
     */
    String getRequiresFalse();

    //

    String UI_REFERENCE = "uiReference";

    /**
     * Answer the PIDs reference values which are held by this attribute.
     */
    List<String> getUIReference();
}
//@formatter: on