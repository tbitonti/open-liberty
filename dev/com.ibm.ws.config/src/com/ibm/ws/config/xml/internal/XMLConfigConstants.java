/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.xml.internal;

import java.util.regex.Pattern;

//@formatter:off
public interface XMLConfigConstants {
    String NLS_PROPS = "com.ibm.ws.config.internal.resources.ConfigMessages";
    String TR_GROUP = "config";

    String NLS_OPTIONS = "com.ibm.ws.config.internal.resources.ConfigOptions";

    //

    String FEATURE_CHANGING_TOPIC =
        "com/ibm/ws/kernel/feature/internal/FeatureManager/FEATURE_CHANGING";
    String FEATURE_CHANGE_TOPIC =
        "com/ibm/ws/kernel/feature/internal/FeatureManager/FEATURE_CHANGE";

    //

    String CFG_SERVICE_PREFIX = "service.";

    String CFG_SERVICE_PID = "service.pid";

    //

    String CFG_CONFIG_PREFIX = "config.";

    String CFG_CONFIG_SOURCE = "config.source";

    String CFG_CONFIG_INSTANCE_ID = "config.id";

    String CFG_CONFIG_REFERENCES = "config.references";

    String CFG_CONFIG_INSTANCE_DISPLAY_ID = "config.displayId";

    String CFG_PARENT_PID = "config.parentPID";

    String CFG_CONFIG_SOURCE_FILE = "file";

    String CFG_INSTANCE_ID = "id";

    String CFG_REFERENCE_SUFFIX = "Ref";

    char INSTANCE_DELIMITER = '-';

    String CFG_CONFIG_REF = "ref";

    String CONFIG_ENABLED_ATTRIBUTE = "configurationEnabled";

    /** This is the URI for the ibm: namespace we use for IBM extensions to metatype. */
    String METATYPE_EXTENSION_URI = "http://www.ibm.com/xmlns/appservers/osgi/metatype/v1.0.0";

    /** This is the URI for the ibmui: namespace we use for IBM UI extensions to metatype. */
    String METATYPE_UI_EXTENSION_URI = "http://www.ibm.com/xmlns/appservers/osgi/metatype/ui/v1.0.0";

    String DEFAULT_CONFIG_HEADER = "IBM-Default-Config";

    /** Prefix for the kernel bundle location. */
    String BUNDLE_LOC_KERNEL_TAG = "kernel@";

    /** Prefix for the feature bundle location. */
    String BUNDLE_LOC_FEATURE_TAG = "feature@";

    /** Prefix for the connector module location. */
    String BUNDLE_LOC_CONNECTOR_TAG = "ConnectorModuleMetatype@";

    /** Bundle location reference tag. */
    String BUNDLE_LOC_REFERENCE_TAG = "reference:";

    /** Bundle location product extension tag. */
    String BUNDLE_LOC_PROD_EXT_TAG = "productExtension:";

    String CORE_PRODUCT_NAME = "core";

    // Top level configuration elements:

    /** Name for configuration include elements.*/
    String INCLUDE = "include";
    /** Attribute name for setting the merge behavior for an include element. */
    String BEHAVIOR_ATTRIBUTE = "onConflict";

    /** Name for configuration variable elements. */
    String VARIABLE = "variable";
    /** Name for the name attribute of a variable element. */
    String VARIABLE_NAME = "name";
    /** Name for the default value attribute of a variable element. */
    String VARIABLE_DEFAULT_VALUE = "defaultValue";
    /** Name for the value attribute of a variable element. */
    String VARIABLE_VALUE = "value";

    /** Variable reference open text. */
    String VAR_OPEN = "${";
    /** Variable reference close text. */
    String VAR_CLOSE = "}";

    /** Pattern for recognizing a variable reference. */
    Pattern VAR_PATTERN = Pattern.compile("\\Q" + VAR_OPEN + "\\E(.+?)\\Q" + VAR_CLOSE + "\\E");
    /** Pattern for a list variable reference. */
    Pattern VAR_LIST_PATTERN = Pattern.compile("\\Q" + VAR_OPEN + "\\Elist\\((.+?)\\)\\Q" + VAR_CLOSE + "\\E");
    /** Pattern for parsing a comma delimited list. */

    Pattern COMMA_PATTERN = Pattern.compile("\\s*,\\s*");
    /** Pattern for parsing a semicolon delimited list. */
    Pattern PARENTHESIS_PATTERN = Pattern.compile("\\s*;\\s*");

    /** Pattern for a value assignment. */
    Pattern EQUALS_PATTERN = Pattern.compile("\\s=\\s*");
}
//@formatter:on