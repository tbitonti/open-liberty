/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.xml.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.websphere.config.ConfigParserException;
import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.xml.internal.ConfigValidator.ConfigElementList;
import com.ibm.ws.config.xml.internal.XMLConfigParser.MergeBehavior;
import com.ibm.ws.config.xml.internal.variables.ConfigVariableRegistry;
import com.ibm.ws.kernel.service.location.internal.VariableRegistryHelper;
import com.ibm.wsspi.kernel.service.location.WsLocationAdmin;

import test.common.SharedLocationManager;
import test.common.SharedOutputManager;
import test.utils.SharedConstants;

//@formatter:off
public class ConfigValidatorTest {

    private static SharedOutputManager outputMgr;

    private static void setupOutput() {
        outputMgr = SharedOutputManager.getInstance();
        // outputMgr.captureStreams(); // Redirect streams to a capture logger.
    }

    private static void tearDownOutput() {
        // outputMgr.restoreStreams(); // Remove stream redirects.
    }

    //

    private static final String DEFAULT_PROFILE_NAME = "default";

    private static MetaTypeRegistry metatypeRegistry;
    private static WsLocationAdmin locationAdmin;
    private static ConfigVariableRegistry variableRegistry;

    private static void setupConfig() {
        metatypeRegistry = new MetaTypeRegistry();
        locationAdmin = (WsLocationAdmin) SharedLocationManager.createDefaultLocations(SharedConstants.SERVER_XML_INSTALL_ROOT, DEFAULT_PROFILE_NAME);
        variableRegistry = new ConfigVariableRegistry(new VariableRegistryHelper(), new String[0], null, locationAdmin);
    }

    private static void tearDownConfig() {
        SharedLocationManager.resetWsLocationAdmin(); // Restore locations.
    }

    private static ConfigElement parseConfiguration(String docLocation, MergeBehavior mergeBehavior, String xmlText) throws ConfigParserException {
        XMLConfigParser configParser = new XMLConfigParser(locationAdmin, variableRegistry);
        return configParser.parseConfigElement(docLocation, mergeBehavior, xmlText);
    }

    private static Map<String, ConfigElementList> generateConflictMap(ConfigElement... elements) {
        displayElements(elements);

        ConfigValidator validator = new ConfigValidator(metatypeRegistry, variableRegistry);
        Map<String, ConfigElementList> conflictMap = validator.generateConflictMap(elements);

        displayConflicts(validator, conflictMap);

        return conflictMap;
    }

    private static void displayElements(ConfigElement... elements) {
        System.out.println("Configuration elements:");
        for ( ConfigElement element : elements ) {
            System.out.println("  [ " + element + " ]");
        }
    }

    private static void displayConflicts(ConfigValidator validator, Map<String, ConfigElementList> conflictMap) {
        if ( conflictMap.isEmpty() ) {
            System.out.println("No conflicts were found");
        } else {
            String conflictMessage = validator.generateCollisionMessage("TEST_PID", new ConfigID("TEST_ID"), null, conflictMap);
            System.out.println(conflictMessage);
        }
    }

    //

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setupOutput();
        setupConfig();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        tearDownConfig();
        tearDownOutput();
    }

    //

    @Before
    public void setUp() throws Exception {
        // EMPTY
    }

    @After
    public void tearDown() throws Exception {
        outputMgr.resetStreams(); // Give each test clear streams.
    }

    //

    private static void append(StringBuilder builder, String attrName, Object attrValue) {
        if ( attrValue != null ) {
            builder.append(' ');
            builder.append(attrName);
            builder.append("=\"");
            builder.append(attrValue);
            builder.append('\"');
        }
    }

    private static final String NO_ID = null;
    private static final Boolean NO_CLIENT_AUTH = null;
    private static final String NO_LOG_NAME = null;
    private static final Boolean NO_SSL_ENABLE = null;

    private static String httpConnector(String id, Boolean clientAuth, String logName, Boolean sslEnable) {
        StringBuilder builder = new StringBuilder("<httpConnector");

        append(builder, "id", id);
        append(builder, "clientAuth", clientAuth);
        append(builder, "logFile", logName);
        append(builder, "sslEnable", sslEnable);

        builder.append("/>");

        return builder.toString();
    }

    private static final String HTTP_CONNECTOR_1_F_X_X = httpConnector("1", false, NO_LOG_NAME, NO_SSL_ENABLE);
    private static final String HTTP_CONNECTOR_1_T_X_X = httpConnector("1", false, NO_LOG_NAME, NO_SSL_ENABLE);

    private static final String HTTP_CONNECTOR_1_X_A_X = httpConnector("1", NO_CLIENT_AUTH, "a", NO_SSL_ENABLE);
    private static final String HTTP_CONNECTOR_1_X_B_X = httpConnector("1", NO_CLIENT_AUTH, "a", NO_SSL_ENABLE);

    private static final String HTTP_CONNECTOR_1_X_X_F = httpConnector("1", NO_CLIENT_AUTH, NO_LOG_NAME, false);
    private static final String HTTP_CONNECTOR_1_X_X_T = httpConnector("1", NO_CLIENT_AUTH, NO_LOG_NAME, true);

    //

    private static final String HTTP_CONNECTOR_X_F_A_X = httpConnector(NO_ID, false, "a", NO_SSL_ENABLE);
    private static final String HTTP_CONNECTOR_1_F_A_X = httpConnector("1", false, "a", NO_SSL_ENABLE);

    private static final String HTTP_CONNECTOR_X_F_A_T = httpConnector(NO_ID, false, "a", true);
    private static final String HTTP_CONNECTOR_1_F_A_T = httpConnector("1", false, "a", true);

    private static final String HTTP_CONNECTOR_X_F_A_F = httpConnector(NO_ID, false, "a", false);
    private static final String HTTP_CONNECTOR_1_F_A_F = httpConnector("1", false, "a", false);

    private static final String HTTP_CONNECTOR_X_F_B_T = httpConnector(NO_ID, false, "b", true);
    private static final String HTTP_CONNECTOR_1_F_B_T = httpConnector("1", false, "b", true);

    private static final String HTTP_CONNECTOR_X_F_B_F = httpConnector(NO_ID, false, "b", false);
    private static final String HTTP_CONNECTOR_1_F_B_F = httpConnector("1", false, "b", false);

    public static class ConfigElementSpec {
        public final String location;
        public final MergeBehavior mergeBehavior;
        public final String xmlText;

        public ConfigElementSpec(int seqNo, String xmlText) {
            this("doc" + Integer.toString(seqNo), MergeBehavior.MERGE, xmlText);
        }

        public ConfigElementSpec(String location, MergeBehavior mergeBehavior, String xmlText) {
            this.location = location;
            this.mergeBehavior = mergeBehavior;
            this.xmlText = xmlText;
        }

        public ConfigElement asConfig() throws ConfigParserException {
            return parseConfiguration(location, mergeBehavior, xmlText);
        }
    }

    public static ConfigElement[] asConfig(ConfigElementSpec...specs) throws ConfigParserException {
        ConfigElement[] elements = new ConfigElement[ specs.length ];
        for ( int specNo = 0; specNo < specs.length; specNo++ ) {
            ConfigElement element = specs[specNo].asConfig();
            element.setSequenceId(specNo);
            elements[specNo] = element;
        }
        return elements;
    }

    public static class ConfigElements {
        public final ConfigElement[] elements;

        public ConfigElements(ConfigElementSpec... specs) throws ConfigParserException {
            this.elements = asConfig(specs);
        }

        public ConfigElements(ConfigElement... elements) {
            this.elements = elements;
        }

        public Map<String, ConfigElementList> asConflictMap() {
            return generateConflictMap(elements);
        }
    }

    public void validate(Map<String, ConfigElementList> conflictMap,
                         Set<String> noConflictAttrNames,
                         Set<String> conflictAttrNames) {

        boolean success = true;

        for ( String attrName : noConflictAttrNames ) {
            ConfigElementList elements = conflictMap.get(attrName);
            if ( elements != null ) {
                if ( elements.hasConflict() ) {
                    success = false;
                    System.out.println("Failure: [ " + attrName + " ] has a conflict");
                }
            } else {
                success = false;
                System.out.println("Failure: [ " + attrName + " ] should be recorded");
            }
        }

        for ( String attrName : conflictAttrNames ) {
            ConfigElementList elements = conflictMap.get(attrName);
            if ( elements != null ) {
                if ( !elements.hasConflict() ) {
                    success = false;
                    System.out.println("Failure: [ " + attrName + " ] has no conflict");
                }
            } else {
                success = false;
                System.out.println("Failure: [ " + attrName + " ] should be recorded");
            }
        }

        for ( String attrName : conflictMap.keySet() ) {
            if ( !noConflictAttrNames.contains(attrName) && !conflictAttrNames.contains(attrName) ) {
                success = true;
                System.out.println("Failure: [ " + attrName + " ] should not be recorded");
            }
        }

        assertTrue("Valid conflicts", success);
    }

    private static <T> Set<T> asSet(T... elements) {
        if ( elements.length == 0 ) {
            return Collections.emptySet();
        } else if ( elements.length == 1 ) {
            return Collections.singleton(elements[0]);
        } else {
            Set<T> set = new HashSet<>(elements.length);
            for ( T element : elements ) {
                set.add(element);
            }
            return set;
        }
    }

    private static void hasActiveValue(
        Map<String, ConfigElementList> conflictMap,
        String attrName,
        String expectedValue, String expectedLocation) {

        ConfigElementList conflictElements = conflictMap.get(attrName);
        ConfigElement activeElement = conflictElements.getActiveElement();

        String actualValue = activeElement.getElementValue();
        if ( !expectedValue.equals(actualValue) ) {
            assertEquals("Attribute value [ " + attrName + " ]", expectedValue, expectedValue);
        }
        String actualLocation = activeElement.getMergedLocation();
        if ( !expectedLocation.equals(actualLocation) ) {
            System.out.println("Attribute [ " + attrName + " ]: ");
            System.out.println("  Location [ " + activeElement.getDocumentLocation() + " ]");
            System.out.println("  Merge Location [ " + activeElement.getMergedLocation() + " ]");
            assertEquals("Attribute location [ " + attrName + " ]", expectedLocation, actualLocation);
        }
    }

    //

    private static final String DOC1 = "doc1";
    private static final String DOC2 = "doc2";
    private static final String DOC3 = "doc3";

    @Test
    public void testMerge() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_X_X),
            new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_1_X_A_X),
            new ConfigElementSpec(DOC3, MergeBehavior.MERGE, HTTP_CONNECTOR_1_X_X_F) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, Collections.emptySet(), Collections.emptySet());

        // hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        // hasActiveValue(conflictMap, "logFile", "a", DOC2);
        // hasActiveValue(conflictMap, "sslEnable", "false", DOC3);
    }

    @Test
    public void testNoConflict() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, Collections.emptySet(), Collections.emptySet());

        // hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        // hasActiveValue(conflictMap, "logFile", "a", DOC1);
        // hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testNoConflict_ignore() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.IGNORE, HTTP_CONNECTOR_X_F_A_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, Collections.emptySet(), Collections.emptySet());

        // hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        // hasActiveValue(conflictMap, "logFile", "a", DOC1);
        // hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testNoConflict_id() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, Collections.emptySet(), Collections.emptySet());

        // hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        // hasActiveValue(conflictMap, "logFile", "a", DOC1);
        // hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testNoConflict_id_ignore() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.IGNORE, HTTP_CONNECTOR_1_F_A_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, Collections.emptySet(), Collections.emptySet());

        // hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        // hasActiveValue(conflictMap, "logFile", "a", DOC1);
        // hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testConflict_boolean() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_F) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("clientAuth", "logFile"), asSet("sslEnable"));

        hasActiveValue(conflictMap, "clientAuth", "false", DOC2);
        hasActiveValue(conflictMap, "logFile", "a", DOC2);
        hasActiveValue(conflictMap, "sslEnable", "false", DOC2);
    }

    @Test
    public void testConflict_boolean_id() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_F) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("id", "clientAuth", "logFile"), asSet("sslEnable"));

        hasActiveValue(conflictMap, "id", "1", DOC2);
        hasActiveValue(conflictMap, "clientAuth", "false", DOC2);
        hasActiveValue(conflictMap, "logFile", "a", DOC2);
        hasActiveValue(conflictMap, "sslEnable", "false", DOC2);
    }

    @Test
    public void testConflict_string() throws Exception {
        ConfigElements elements = new ConfigElements(
                new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T),
                new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_B_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("clientAuth", "sslEnable"), asSet("logFile"));

        hasActiveValue(conflictMap, "clientAuth", "false", DOC2);
        hasActiveValue(conflictMap, "logFile", "b", DOC2);
        hasActiveValue(conflictMap, "sslEnable", "true", DOC2);
    }

    @Test
    public void testConflict_string_id() throws Exception {
        ConfigElements elements = new ConfigElements(
                new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T),
                new ConfigElementSpec(DOC2, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_B_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("id", "clientAuth", "sslEnable"), asSet("logFile"));

        hasActiveValue(conflictMap, "id", "1", DOC2);
        hasActiveValue(conflictMap, "clientAuth", "false", DOC2);
        hasActiveValue(conflictMap, "logFile", "b", DOC2);
        hasActiveValue(conflictMap, "sslEnable", "true", DOC2);
    }

    @Test
    public void testConflict_boolean_ignore() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.IGNORE, HTTP_CONNECTOR_X_F_B_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("clientAuth", "sslEnable"), asSet("logFile"));

        hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        hasActiveValue(conflictMap, "logFile", "a", DOC1);
        hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testConflict_boolean_id_ignore() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.IGNORE, HTTP_CONNECTOR_1_F_A_F) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("id", "clientAuth", "logFile"), asSet("sslEnable"));

        hasActiveValue(conflictMap, "id", "1", DOC1);
        hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        hasActiveValue(conflictMap, "logFile", "a", DOC1);
        hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testConflict_string_ignore() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_X_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.IGNORE, HTTP_CONNECTOR_X_F_B_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("clientAuth", "sslEnable"), asSet("logFile"));

        hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        hasActiveValue(conflictMap, "logFile", "a", DOC1);
        hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }

    @Test
    public void testConflict_string_id_ignore() throws Exception {
        ConfigElements elements = new ConfigElements(
            new ConfigElementSpec(DOC1, MergeBehavior.MERGE, HTTP_CONNECTOR_1_F_A_T),
            new ConfigElementSpec(DOC2, MergeBehavior.IGNORE, HTTP_CONNECTOR_1_F_B_T) );

        Map<String, ConfigElementList> conflictMap = elements.asConflictMap();

        validate(conflictMap, asSet("id", "clientAuth", "sslEnable"), asSet("logFile"));

        hasActiveValue(conflictMap, "id", "1", DOC1);
        hasActiveValue(conflictMap, "clientAuth", "false", DOC1);
        hasActiveValue(conflictMap, "logFile", "a", DOC1);
        hasActiveValue(conflictMap, "sslEnable", "true", DOC1);
    }
}
//@formatter:on