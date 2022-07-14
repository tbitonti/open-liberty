/*******************************************************************************
 * Copyright (c) 2010, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.xml.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osgi.framework.Bundle;

import com.ibm.websphere.config.ConfigParserException;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Sensitive;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.xml.LibertyVariable;
import com.ibm.ws.config.xml.internal.DefaultConfiguration.DefaultConfigFile;
import com.ibm.ws.config.xml.internal.variables.ConfigVariable;
import com.ibm.ws.config.xml.internal.variables.ConfigVariableRegistry;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.kernel.service.util.DesignatedXMLInputFactory;
import com.ibm.wsspi.kernel.service.location.MalformedLocationException;
import com.ibm.wsspi.kernel.service.location.WsLocationAdmin;
import com.ibm.wsspi.kernel.service.location.WsResource;
import com.ibm.wsspi.kernel.service.utils.OnErrorUtil.OnError;
import com.ibm.wsspi.kernel.service.utils.PathUtils;

//@formatter:off
public class XMLConfigParser {
    private static final TraceComponent tc =
        Tr.register(XMLConfigParser.class,
                    XMLConfigConstants.TR_GROUP, XMLConfigConstants.NLS_PROPS);

    private static boolean logWarnings() {
        return ( tc.isWarningEnabled() &&
                        (ErrorHandler.INSTANCE.getOnError() != OnError.IGNORE) );
    }

    private static boolean logErrors() {
        return ( tc.isErrorEnabled() &&
                 (ErrorHandler.INSTANCE.getOnError() != OnError.IGNORE) );
    }

    private static void logError(String msgKey, Location l) {
        Tr.error(tc, msgKey, l.getLineNumber(), l.getSystemId());
    }

    private static final String MESSAGE_HEADER = "Message: ";

    /**
     * Answer the adjusted text of an XML stream exception.
     *
     * An adjustment is needed for sun JDKs, which inject hard-coded English
     * text around the actual message. Remove this text from the message
     *
     * @param xse An XML stream exception.
     *
     * @return The adjusted text of the exception.
     */
    private static String getMessage(XMLStreamException xse) {
        String message = xse.getMessage();
        if ( (message != null) && message.startsWith("ParseError at [row,col]:[") ) {
            int index = message.indexOf(MESSAGE_HEADER);
            if ( index >= 0 ) {
                return message.substring(index + MESSAGE_HEADER.length());
            }
        }
        return message;
    }

    //

    public enum MergeBehavior {
        MERGE, REPLACE,
        IGNORE,
        MERGE_WHEN_EXISTS, MERGE_WHEN_MISSING; // Default configuration only.
    };

    protected static MergeBehavior parseBehavior(String behaviorValue) {
        if (behaviorValue == null) {
            return MergeBehavior.MERGE; // Default to MERGE.

        } else if (behaviorValue.equalsIgnoreCase(MergeBehavior.MERGE.name())) {
            return MergeBehavior.MERGE;
        } else if (behaviorValue.equalsIgnoreCase(MergeBehavior.IGNORE.name())) {
            return MergeBehavior.IGNORE;
        } else if (behaviorValue.equalsIgnoreCase(MergeBehavior.REPLACE.name())) {
            return MergeBehavior.REPLACE;

        } else {
            // We don't need MERGE_WHEN_EXISTS or MERGE_WHEN_MISSING here --
            // they are only specified in default configuration manifests.

            // Be forgiving: Issue a warning and use default behavior.
            if ( logWarnings() ) {
                Tr.warning(tc, "warning.unrecognized.merge.behavior", behaviorValue);
            }
            return MergeBehavior.MERGE;
        }
    }

    //

    private static final String IS_SUPPORTING_LOCATION_COORDINATES_PROPERTY =
        "javax.xml.stream.isSupportingLocationCoordinates";

    private static final XMLInputFactory INSTANCE;

    static {
        XMLInputFactory xif = DesignatedXMLInputFactory.newInstance();

        xif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

        // On an IBM JDK this property needs to be enabled in order to support the
        // reporting of the location of an error when an XMLStreamException is thrown.
        // Note that the XMLStreamReader in the Sun JDK reports error coordinates by default.

        if (xif.isPropertySupported(IS_SUPPORTING_LOCATION_COORDINATES_PROPERTY) ||
            "com.ibm.xml.xlxp.api.stax.XMLInputFactoryImpl".equals(xif.getClass().getName())) {
            // xlxp supports location coordinates in all versions, but reports incorrectly in
            // versions prior to Java 6 SR2 and Java 7 SR1, so we need this impl check until our
            // minimum supported version is increased beyond those levels.
            xif.setProperty(IS_SUPPORTING_LOCATION_COORDINATES_PROPERTY, Boolean.TRUE);
        }

        INSTANCE = xif;
    }

    private static XMLInputFactory getXMLInputFactory() {
        return INSTANCE;
    }

    private interface ParseAction<T> {
        T parse(DepthAwareXMLStreamReader xmlReader)
            throws XMLStreamException, ConfigParserException;
    }

    /**
     * Parsing primitive.  Use a supplied parse action to parse the specified
     * input stream.  Answer the parsed configuration.
     *
     * Any {@link XMLStreamException} is rethrown as a {@link ConfigParserException}.
     *
     * This implementation creates an {@link XMLStreamReader} and a
     * {@link DepthAwareXMLStreamReader} and provides the depth aware reader to
     * the parse action.
     *
     * The main effect of this method is to safely create and close the readers
     * which are provided to the parse action, and to capture and rethrow stream
     * exceptions.
     *
     * @param <T> The type of configuration which is being parsed.  Needed
     *     as a type parameter since the return type must be the same as the
     *     parameter type.
     * @param location The location of the configuration.
     * @param input An input stream on the configuration data.
     * @param parseAction The action that is used to parse the configuration.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    @FFDCIgnore(XMLStreamException.class)
    private <T extends BaseConfiguration>
        T parse(String location, InputStream input, ParseAction<T> parseAction)
        throws ConfigParserException {

        try {
            XMLStreamReader parser = getXMLInputFactory().createXMLStreamReader(location, input);
            try {
                DepthAwareXMLStreamReader xmlReader = new DepthAwareXMLStreamReader(parser);
                return parseAction.parse(xmlReader);
            } finally {
                parser.close();
            }

        } catch ( XMLStreamException e ) {
            throw new ConfigParserException(e);
        }
    }

    /**
     * Parsing primitive.  Use a supplied parse action to parse the specified
     * reader.  Answer the parsed configuration.
     *
     * Any {@link XMLStreamException} is rethrown as a {@link ConfigParserException}.
     *
     * This implementation creates an {@link XMLStreamReader} and a
     * {@link DepthAwareXMLStreamReader} and provides the depth aware reader to
     * the parse action.
     *
     * The main effect of this method is to safely create and close the readers
     * which are provided to the parse action, and to capture and rethrow stream
     * exceptions.
     *
     * @param <T> The type of configuration which is being parsed.  Needed
     *     as a type parameter since the return type must be the same as the
     *     parameter type.
     * @param reader The reader of the configuration data.
     * @param parseAction The action that is used to parse the configuration.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */

    @FFDCIgnore(XMLStreamException.class)
    private <T> T parse(Reader reader, ParseAction<T> parseAction)
        throws ConfigParserException {

        try {
            XMLStreamReader parser = getXMLInputFactory().createXMLStreamReader(reader);
            try {
                DepthAwareXMLStreamReader xmlReader = new DepthAwareXMLStreamReader(parser);
                return parseAction.parse(xmlReader);
            } finally {
                parser.close();
            }

        } catch ( XMLStreamException e ) {
            throw new ConfigParserException(e);
        }
    }

    //

    /**
     * Answer the attribute having the specified name and with a null
     * or empty namespace.
     *
     * @param reader The reader providing attribute values.
     * @param attrName An attribute name.
     *
     * @return The value of the attribute.  Null if there is no
     *    attribute matching the specified name.
     */
    private static String getAttributeValue(XMLStreamReader reader, String attrName) {
        int attrCount = reader.getAttributeCount();
        for ( int attrNo = 0; attrNo < attrCount; attrNo++ ) {
            if ( !attrName.equals( reader.getAttributeLocalName(attrNo) ) ) {
                continue;
            }

            String attrNs = reader.getAttributeNamespace(attrNo);
            if ( (attrNs != null) && !attrNs.isEmpty() ) {
                continue;
            }

            return reader.getAttributeValue(attrNo);
        }

        return null;
    }

    //

    /**
     * Configuration parser initializer.  A parser must have a location service and
     * a variable registry.
     *
     * (A null location service is tolerated so long as no "include" is encountered.
     * A null location causes a parse exception upon encountering the first include
     * element.)
     *
     * @param locationService A location service.  Used to resolve include locations.
     * @param variableRegistry A variable registry.  Used to resolve include locations.
     */
    public XMLConfigParser(WsLocationAdmin locationService, ConfigVariableRegistry variableRegistry) {
        this.locationService = locationService;

        this.variableRegistry = variableRegistry;
        this.tempVariables = new BaseConfiguration();

        this.locationStack = new ArrayList<String>();
        this.behaviorStack = new ArrayList<MergeBehavior>();
    }

    //

    private final WsLocationAdmin locationService;

    private String resolveLocation(String path) {
        return locationService.resolveString(path);
    }

    private WsResource resolveResource(String path) {
        return locationService.resolveResource(path);
    }

    //

    private final ConfigVariableRegistry variableRegistry;

    private void updateVariables(Map<String, LibertyVariable> variables) {
        variableRegistry.updateSystemVariables(variables);
    }

    private String lookupVariable(String varName) {
        String varValue = variableRegistry.lookupVariable(varName);
        if ( varValue == null ) {
            varValue = variableRegistry.lookupVariableFromAdditionalSources(varName);
        }
        if ( varValue == null ) {
            varValue = variableRegistry.lookupVariableDefaultValue(varName);
        }
        return varValue;
    }

    // TODO: This use of a base configuration solely to provide
    //       storage for temporary variables should be removed.
    //       The temporary variables and necessary API should be
    //       accessed directly.

    private final BaseConfiguration tempVariables;

    private void clearTempVariables() {
        tempVariables.variables.clear();
    }

    private void addTempVariable(ConfigVariable variable) {
        tempVariables.addVariable(variable);
    }

    /**
     * Apply a function to a path with variables temporarily set to the
     * current temporary variables.
     *
     * @param path The path which is to be updated.
     * @param varFun The function used to update the path.
     *
     * @return The updated path.
     */
    private String withTempVariables(String path, Function<String, String> varFun) {
        Map<String, LibertyVariable> currentVariables = variableRegistry.getConfigVariables();
        updateVariables( tempVariables.getVariables() );
        try {
            return varFun.apply(path);
        } finally {
            updateVariables(currentVariables);
        }
    }

    //

    /**
     * Resolve an include path as a resource.
     *
     * Answer null if the include path is null or empty.
     *
     * Answer the resolved include path.  If the resolved include path
     * is a relative path, resolve the base path and answer the resolved
     * include path relative to the resolved base path.
     *
     * @param includePath The path which was included.
     * @param basePath The path of the document which contains the include.
     *
     * @return The resolved include path, as a resource, possibly
     *     relative to the base path.
     */
    WsResource resolveInclude(String includePath, String basePath) {
        if ( includePath == null ) {
            return null;
        }

        includePath = includePath.trim();
        if ( includePath.isEmpty() ) {
            return null;
        }

        String resolvedPath = PathUtils.normalize( resolvePath(includePath) );

        if ( basePath != null ) {
            if ( !PathUtils.pathIsAbsolute(resolvedPath) ) {
                basePath = resolveLocation(basePath);
                String parentPath = PathUtils.getParent(basePath);

                if ( parentPath != null ) {
                    resolvedPath = pathAppend(parentPath, resolvedPath);
                } else {
                    // Leave 'resolvedPath' as is.
                }
            }
        }

        return resolveResource(resolvedPath);
    }

    private static String pathAppend(String parentPath, String childPath) {
        int parentLen = parentPath.length();
        if ( parentLen > 0 ) {
            if ( parentPath.charAt(parentLen - 1) == '/' ) {
                return parentPath + childPath;
            } else {
                return parentPath + '/' + childPath;
            }
        } else {
            return '/' + childPath;
        }
    }

    private String resolvePath(String path) {
        if (PathUtils.isSymbol(path)) {
            return resolveSymbolicPath(path);
        } else {
            return resolveLocation(path);
        }
    }

    /**
     * Resolve a path which is known to contain variables.
     *
     * Resolve all variable occurrences, including variables within
     * variables.
     *
     * Resolution locates the first unresolved variable, resolves
     * that variable, then resets.  This results in depth first
     * resolution of variables within variables.
     *
     * @param path The path which is to be resolved.
     *
     * @return The resolved path.
     */
    private String resolveSymbolicPath(String path) {
        return withTempVariables( path, (value) -> {
            Matcher matcher = XMLConfigConstants.VAR_PATTERN.matcher(value);
            while ( matcher.find() ) {
                String varName = matcher.group(1);
                String varValue = lookupVariable(varName);

                // Start over again if a replacement is made.
                //
                // Move to the next variable if no replacement is found.
                //
                // Note: This is inefficient when there are multiple variables
                // and the first fails to resolve.
                //
                // For example:
                //     "${a}${b}${c}${d}${e}${f}"
                // does nine failed substitutions on 'a', 'b', and 'c' while
                // iterating to reach 'd', 'e', and 'f'.
                //
                // This case is not expected to occur.

                if ( varValue != null ) {
                    // This replacement is imprecise: All occurrences of
                    // the variable are replaced, not just the single occurrence
                    // which was found.
                    //
                    // This would be significant if escaping were allowed, and if
                    // escaped variable substitutions were expected in location
                    // values.  Bother are unexpected.

                    value = value.replace( matcher.group(0), varValue );
                    matcher.reset(value);
                }
            }

            return value;
        });
    }

    //

    private final List<String> locationStack;
    private final List<MergeBehavior> behaviorStack;

    private String getLocation() {
        int size = locationStack.size();
        return ( (size == 0) ? null : locationStack.get(size - 1) );
    }

    private MergeBehavior getBehavior() {
        int size = behaviorStack.size();
        return ( (size == 0) ? null : behaviorStack.get(size - 1) );
    }

    /**
     * Do nothing if the document location is null.  Otherwise,
     * push the document location and merge behavior.  Fail with
     * a false return value if there is a location cycle.
     *
     * If there is a location cycle, emit a warning with the elements
     * of the cycle and pop the elements.
     *
     * @param location The document location which is to be pushed.
     * @param behavior The merge behavior which is to be pushed.
     *
     * @return True or false telling if the push was successful.
     */
    private boolean pushLocation(String location, MergeBehavior behavior) {
        // A null location in the middle of an include hierarchy would
        // produce odd results: The document which had a null location
        // would use the first available parent document location.

        if ( location == null ) {
            return true;
        }

        int docIndex = locationStack.indexOf(location);

        // Do this add even when there is a cycle so to make
        // it easy to obtain the cycle as a sublist.

        locationStack.add(location);
        behaviorStack.add(behavior);

        if ( docIndex != -1 ) {
            if ( logWarnings() ) {
                List<String> docCycle = locationStack.subList(docIndex, locationStack.size());
                Tr.warning(tc, "warn.parse.circular.include", docCycle);
            }

            popLocation(location, behavior);
            return false;

        } else {
            return true;
        }
    }

    /**
     * Pop the document location and the merge behavior.
     *
     * Do nothing if the document location is null.
     *
     * @param location The expected last document location.
     * @param behavior The expected last merge behavior.
     */
    private void popLocation(String location, MergeBehavior behavior) {
        if ( location == null ) {
            return;
        }

        int stackSize = locationStack.size();
        locationStack.remove(stackSize - 1);
        behaviorStack.remove(stackSize - 1);
    }

    //

    private int nextElementId;

    @Trivial
    private int getNextSquenceNo() {
        return nextElementId++;
    }

    //

    /**
     * Main parsing entry point: Parse a default configuration.
     *
     * @param defaultConfigFile The resource which contains the default configuration.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    @FFDCIgnore(IOException.class)
    public BaseConfiguration parseDefaultConfiguration(DefaultConfigFile defaultConfigFile)
        throws ConfigParserException {

        String location = defaultConfigFile.fileURL.toExternalForm();
        MergeBehavior behavior = defaultConfigFile.behavior;

        try ( InputStream in = defaultConfigFile.fileURL.openStream() ) {
            return parseServerConfiguration(in, location, new BaseConfiguration(), behavior);

        } catch ( IOException e ) {
            throw new ConfigParserException(e);
        }
    }

    /**
     * Main parsing entry point: Reset temporary variables then parse the resource into
     * the server configuration.
     *
     * @param resource A resource containing a server configuration.
     * @param config Storage for the parsed configuration.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    @FFDCIgnore(IOException.class)
    public ServerConfiguration parseServerConfiguration(WsResource resource, ServerConfiguration config)
        throws ConfigParserException {

        clearTempVariables();

        String location = resource.toExternalURI().toString();
        long lastModified =  resource.getLastModified();

        try ( InputStream in = resource.get() ) {
            config.updateLastModified(lastModified);
            return parseServerConfiguration(in, location, config, MergeBehavior.MERGE);

        } catch ( IOException e ) {
            throw new ConfigParserException("Error loading configuration file " + location, e);
        }
    }

    /**
     * Main parsing entry point: Parse an input stream into a configuration.
     *
     * @param in An input stream containing the configuration which is to be parsed.
     * @param location The location of the input stream.
     * @param config Storage for the parsed configuration.
     * @param behavior The merge behavior to be used.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    public <T extends BaseConfiguration> T parseServerConfiguration(
        InputStream in, String location, T config, MergeBehavior behavior)
        throws ConfigParserException {

        return parse(location, in, (xmlReader) -> {
            return parseServerConfiguration(xmlReader, config, location, behavior);
        });
    }

    /**
     * Parse a a server configuration.  This is a private entry to the main server
     * parse API used only for parsing included configurations.
     *
     * @param resource The resource which contains the included configuration.
     * @param config The configuration which contains the include element which is being
     *     processed.
     * @param behavior The active merge behavior, either from the including configuration
     *     or from the include element.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    @FFDCIgnore(IOException.class)
    private void parseServerConfiguration(WsResource resource, BaseConfiguration config, MergeBehavior behavior)
        throws ConfigParserException {

        String location = resource.toExternalURI().toString();
        long lastModified =  resource.getLastModified();

        try ( InputStream in = resource.get() ) {
            config.updateLastModified(lastModified);
            parseServerConfiguration(in, location, config, behavior);

        } catch ( IOException e ) {
            throw new ConfigParserException("Error loading configuration file " + location, e);
        }
    }

    /**
     * Main server configuration parse operation.
     *
     * Push the location and behavior, then parse the root element.  This is expected
     * to be either "server" or "client".
     *
     * @param <T> The type of configuration which is being processed.  This type parameter
     *     is needed because the return type is the same as the parameter type.
     * @param parser The active parser.
     * @param config The active configuration.
     * @param location The active location.
     * @param behavior The active merge behavior.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    @FFDCIgnore(XMLStreamException.class)
    private <T extends BaseConfiguration> T parseServerConfiguration(
        DepthAwareXMLStreamReader parser, T config,
        String location, MergeBehavior behavior)
        throws ConfigParserException, XMLStreamException {

        if ( !pushLocation(location, behavior) ) {
            return config; // Consider the parse successful if there was an include cycle.
        }

        try {
            String processType = resolveLocation("${wlp.process.type}");

            int depth = parser.getDepth();
            while ( parser.hasNext(depth) ) {
                int event = parser.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) {
                    String name = parser.getLocalName();
                    // TODO: Improve the following line that is hard coded with "server"
                    //       See Task 154493.
                    //
                    // Task 154493 is cancelled.  The work-around specified by the task
                    // is currently in place in the code.
                    //
                    // The work-around is to allow parsing to continue if the root element
                    // is "server".  The effect of this is to allow client configurations
                    // to include server configuration.  That is:
                    //
                    // client: <client><include location="other.xml"></client>
                    // other: <server>...</server>

                    if ( processType.equals(name) || "server".equals(name) ) {
                        // Usually, the root element should match the process type.
                        // But, always allow "server" to be the root element.
                        // That allows freer inclusion of server configurations.
                        return parseServerElement(parser, config, processType);

                    } else if ( "client".equals(name) ) {
                        // Silently ignore "client" root elements.  That is,
                        // "client" configurations are only allowed when the
                        // process type is "client".
                        //
                        // This is asymmetric with "server", which is always allowed.
                        // That is, "client" configurations can include "server"
                        // configurations.  "server" configurations may not include
                        // "client configurations.
                        return null;

                    } else {
                        // Ignore any root element which does not match the process
                        // type, and which is neither "server" nor "client".
                        //
                        // This will usually cause an exception after the parse
                        // event loop:  Usually a single root element is available.
                    }
                }
            }

            // Either, the document was empty, or it had an element which was
            // neither "server" nor "client".
            if ( logErrors() ) {
                Tr.error(tc, "error.root.must.be.server", location, processType);
            }
            throw new ConfigParserTolerableException();

        } finally {
            popLocation(location, behavior);
        }
    }

    /** Obsolete behavior attribute constant definition. */
    @Deprecated
    protected static final String BEHAVIOR_ATTRIBUTE = XMLConfigConstants.BEHAVIOR_ATTRIBUTE;

    /**
     * Main implementation point: A root "server" or "client" element has been
     * reached by the parser.  Process that root element.
     *
     * @param <T> The type of configuration which is being processed.  This type parameter
     *     is needed because the return type is the same as the parameter type.
     * @param parser The active parser.
     * @param config The active configuration.
     * @param processType The type of process which is being parsed.  Used to check
     *     for accidentally nested elements.
     *
     * @return The parsed configuration.  Null if a "client" configuration was
     *     provided and the process type is not "client", or in case of accidental
     *     nested root elements.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     * @throws XMLStreamException Thrown if parsing fails.
     */
    @FFDCIgnore({ ConfigParserTolerableException.class })
    private <T extends BaseConfiguration> T parseServerElement(
        DepthAwareXMLStreamReader parser, T config, String processType)
        throws ConfigParserException, XMLStreamException {

        String descriptionValue = getAttributeValue(parser, "description");
        if ( descriptionValue != null ) {
            config.setDescription(descriptionValue);
        }

        // Remember any saved tolerable exception.  A tolerable exception causes
        // termination of include parsing, and causes termination of variable
        // parsing, but does not terminate overall parsing.  Continue parsing
        // elements of this document, then rethrow the first tolerable exception
        // if one occurred.

        ConfigParserTolerableException savedException = null;

        int depth = parser.getDepth();
        while ( parser.hasNext(depth) ) {
            int event = parser.next();

            if ( event == XMLStreamConstants.START_ELEMENT ) {
                String name = parser.getLocalName();
                if ( XMLConfigConstants.INCLUDE.equals(name) ) {
                    // Create the included configuration here.
                    // That allows access to the configuration even if there
                    // is a non-local return by way of a tolerable exception.
                    BaseConfiguration includedConfig = new BaseConfiguration();
                    try {
                        parseIncludeElement(parser, includedConfig);
                    } catch ( ConfigParserTolerableException e ) {
                        if ( savedException == null ) {
                            savedException = e;
                        } else {
                            // TODO: Log ignored exception
                        }
                    }

                    config.append(includedConfig);
                    config.updateLastModified( includedConfig.getLastModified() );

                } else if ( XMLConfigConstants.VARIABLE.equals(name) ) {
                    try {
                        ConfigVariable variable = parseVariable(parser);
                        config.addVariable(variable);
                        addTempVariable(variable);
                    } catch ( ConfigParserTolerableException e ) {
                        if ( savedException == null ) {
                            savedException = e;
                        } else {
                            // TODO: Log ignored exception
                        }
                    }

                } else if ( processType.equals(name) ) {
                    // Assume this is a copy/paste error where the user has done something like
                    // <server> <server> ...</server> </server>, or
                    // <client> <client> ...</client> </client>.
                    if ( logWarnings() ) {
                        Tr.warning(tc, "warning.unexpected.server.element");
                    }

                } else {
                    SimpleElement configElement = parseConfigElement(parser, name, config, null, !IS_CHILD);
                    configElement.setDocumentLocation( getLocation() );
                    config.addConfigElement(configElement);
                }
            }
        }

        if ( savedException != null ) {
            throw new ConfigParserTolerableException(savedException);
        }

        return config;
    }

    /**
     * An "include" element has been reached.  Parse that element, then
     * recursively parse the included resource.
     *
     * An "include" element is only handled as a top level element of the
     * configuration.  That is, as a direct child of a "server" or "client"
     * root element.
     *
     * Emit a warning and throw a tolerable exception if no location value
     * was specified for the include.
     *
     * If the include location cannot be resolved, either emit a warning
     * and simply return, or emit an error and throw a tolerable exception,
     * depending on whether the include was specified as optional.
     *
     * If the include location does not exist, either simply return with
     * no parsing being done, or emit and error and throw a tolerable exception,
     * depending on whether the include was specified as optional.
     *
     * @param parser The active parser.
     * @param includedConfig Storage for the included configuration.
     *
     * @throws ConfigParserException Thrown if parsing fails.  Thrown if
     *     a null location service is in use.  Thrown if
     */
    @FFDCIgnore({ MalformedLocationException.class })
    private void parseIncludeElement(
        DepthAwareXMLStreamReader parser, BaseConfiguration includedConfig)
        throws ConfigParserException {

        if ( locationService == null ) {
            throw new ConfigParserException("LocationService is not available");
        }

        String includeLocation = getAttributeValue(parser, "location");
        if ( includeLocation == null ) {
            if ( logErrors() ) {
                logError("error.include.location.not.specified", parser.getLocation());
            }
            throw new ConfigParserTolerableException();
        }

        String behaviorValue = getAttributeValue(parser, BEHAVIOR_ATTRIBUTE);
        MergeBehavior behavior = (behaviorValue == null) ? getBehavior() : parseBehavior(behaviorValue);

        String optionalValue = getAttributeValue(parser, "optional");
        boolean optional = "true".equalsIgnoreCase(optionalValue);

        WsResource includeResource;
        try {
            includeResource = resolveInclude( includeLocation, getLocation() );
        } catch ( MalformedLocationException mle ) {
            includeResource = null;
        }

        if ( includeResource == null ) {
            if ( optional ) {
                if ( logWarnings() ) {
                    Tr.warning(tc, "warn.cannot.resolve.optional.include", resolvePath(includeLocation));
                }
                return;
            } else {
                // TODO: This is the wrong message.
                if ( logErrors() ) {
                    Tr.error(tc, "error.cannot.read.location", resolvePath(includeLocation));
                }
                throw new ConfigParserTolerableException();
            }
        }

        // The case of a non-FILE, non-REMOTE resource which
        // exists will be logged the same as a resource which does
        // not exist.  That is expected to never occur, because
        // resource types should only be FILE or REMOTE.

        if ( !includeResource.exists() ||
             ( !includeResource.isType(WsResource.Type.FILE) && !includeResource.isType(WsResource.Type.REMOTE) ) ) {
            if ( !optional ) {
                Tr.error(tc, "error.cannot.read.location", resolvePath(includeLocation));
                throw new ConfigParserTolerableException();
            } else {
                // TODO: Should a warning be emitted?
                return; // Ignore.
            }
        }

        if ( includeResource.isType(WsResource.Type.FILE) ) {
            Tr.audit(tc, "audit.include.being.processed", includeResource.asFile());
        } else {
            Tr.audit(tc, "audit.include.being.processed", includeResource.toExternalURI());
        }
        parseServerConfiguration(includeResource, includedConfig, behavior);
    }

    /**
     * A "variable" element has been reached.  Parse that element.  Answer
     * the parsed variable element.
     *
     * Assign the current location and merge behavior to the new variable
     * element.
     *
     * A "variable" element is only handled as a top level element of the
     * configuration.  That is, as a direct child of a "server" or "client"
     * root element.
     *
     * @param parser The active parser.
     *
     * @return The parsed variable element.
     *
     * @throws ConfigParserTolerableException Thrown if the variable
     *     name is missing, or if both the value and the default value
     *     of the variable are missing.
     */
    @Sensitive
    private ConfigVariable parseVariable(DepthAwareXMLStreamReader parser)
       throws ConfigParserTolerableException {

        String variableName = null;
        String variableValue = null;
        String variableDefault = null;

        int attrCount = parser.getAttributeCount();
        for (int attrNo = 0; attrNo < attrCount; ++attrNo) {
            String name = parser.getAttributeLocalName(attrNo);
            String value = parser.getAttributeValue(attrNo);
            if ( XMLConfigConstants.VARIABLE_NAME.equals(name) ) {
                variableName = value;
            } else if ( XMLConfigConstants.VARIABLE_VALUE.equals(name) ) {
                variableValue = value;
            } else if ( XMLConfigConstants.VARIABLE_DEFAULT_VALUE.equals(name) ) {
                variableDefault = value;
            }
        }

        if (variableName == null) {
            logError("error.variable.name.missing", parser.getLocation());
            throw new ConfigParserTolerableException();
        } else if ((variableValue == null) && (variableDefault == null)) {
            logError("error.variable.value.missing", parser.getLocation());
            throw new ConfigParserTolerableException();
        }

        // TODO: We don't support sensitive variables??

        return new ConfigVariable(variableName, variableValue, variableDefault,
                                  getBehavior(), getLocation(), false);
    }

    private static final boolean IS_CHILD = true;

    /**
     * A nested element has been encountered, and is not an include or a
     * variable element.
     *
     * Parse and return the element as a simple element.
     *
     * Assign a new ID to the new simple element.
     *
     * @param parser The active parser.
     * @param elementName The name of the nested element.
     * @param config The active configuration.
     * @param parentId The ID of the parent of the nested element.
     * @param isChild True or false telling if the new element is multiply
     *     nested.  That is, false if the element is a direct child of the
     *     root server element, true if the element is multiply nested within
     *     the root server element.
     *
     * @return The new simple element.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     * @throws XMLStreamException Thrown if parsing fails.
     */
    private SimpleElement parseConfigElement(
        DepthAwareXMLStreamReader parser,
        String elementName,
        BaseConfiguration config,
        String parentId, boolean isChild)
        throws ConfigParserException, XMLStreamException {

        // Note: The sequence number is NOT the same as the element ID.

        SimpleElement element =
            new SimpleElement(elementName, getNextSquenceNo(), locationStack, behaviorStack);

        int attrCount = parser.getAttributeCount();
        for ( int attrNo = 0; attrNo < attrCount; ++attrNo ) {
            String attributeName = parser.getAttributeLocalName(attrNo);
            String attributeValue = parser.getAttributeValue(attrNo);
            if ( XMLConfigConstants.CFG_INSTANCE_ID.equals(attributeName) ) {
                element.setId(attributeValue);
            }
            element.addAttribute(attributeName, attributeValue);
        }

        int depth = parser.getDepth();
        while ( parser.hasNext(depth) ) {
            int event = parser.next();
            if ( event == XMLStreamConstants.CHARACTERS ) {
                element.setElementValue( element.getElementValue() + parser.getText() );

            } else if (event == XMLStreamConstants.START_ELEMENT) {
                if ( isChild ) { // TODO: Why?
                    element.setTextOnly(false);
                }

                String childElementName = parser.getLocalName();
                String childMergeOperation = getAttributeValue(parser, "merge-op");
                if ( childMergeOperation != null ) {
                    if ( "append".equals(childMergeOperation) ) {
                        element.setMergeOperation(childElementName, ConfigElement.MERGE_OP.APPEND);
                    } else if ("set".equals(childMergeOperation)) {
                        element.setMergeOperation(childElementName, ConfigElement.MERGE_OP.SET);
                    } else {
                        // ??
                    }
                }

                // Recursively parse any children of this element.
                // Call directly to parsing a child element: Doubly nested children
                // of the root server element must be simple elements.  They cannot
                // be server, include, or variable elements.
                //
                // A failure to parse the element causes the rest of the enclosing
                // document to be ignored.

                SimpleElement childElement =
                    parseConfigElement(parser, childElementName, config, element.getFullId(), IS_CHILD);

                if ( childElement.isChildElement() && (childMergeOperation == null) ) {
                    if ( childElement.getRefAttr() == null ) {
                        childElement.setDocumentLocation( getLocation() );
                        element.addChildConfigElement(childElementName, childElement);
                    } else {
                        element.addReference( childElement.getNodeName(), childElement.getRefAttr() );
                    }
                } else {
                    element.addCollectionAttribute( childElementName, childElement.getElementValue() );
                }

            } else {
                // IGNORE
            }
        }

        return element;
    }

    // Test entry points

    public BaseConfiguration parseDefaultConfiguration(Reader reader, String location)
        throws ConfigParserException {

        return parse(reader, (xmlReader) -> {
            BaseConfiguration config = new BaseConfiguration();
            return parseServerConfiguration(xmlReader, config, location, MergeBehavior.MERGE);
        });
    }

    public ServerConfiguration parseServerConfiguration(WsResource resource)
        throws ConfigParserException {

        return parseServerConfiguration( resource, new ServerConfiguration() );
    }

    public ServerConfiguration parseServerConfiguration(Reader reader)
        throws ConfigParserException {

        return parseServerConfiguration( reader, new ServerConfiguration() );
    }

    public ServerConfiguration parseServerConfiguration(Reader reader, ServerConfiguration config)
        throws ConfigParserException {

        return parse(reader, (xmlReader) -> {
            return parseServerConfiguration(xmlReader, config, "test", MergeBehavior.MERGE);
        });
    }

    /**
     * Test entry point which provides a quick way to parse configuration elements.
     *
     * This allows parsing of bare elements: The overhead of an enclosing root
     * "server" element is avoided.
     *
     * @param reader A reader containing the element.
     *
     * @return The parsed element.  Null if the reader does not contain an element.
     *
     * @throws ConfigParserException Thrown if parsing fails.
     */
    public ConfigElement parseConfigElement(Reader reader)
        throws ConfigParserException {

        pushLocation("test", MergeBehavior.MERGE);

        return ( parse(reader, (xmlReader) -> {
            ConfigElement configElement = null;
            int depth = xmlReader.getDepth();
            while ( xmlReader.hasNext(depth) ) {
                int event = xmlReader.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) {
                    configElement = parseConfigElement(xmlReader, xmlReader.getLocalName(), null, null, !IS_CHILD);
                    break;
                }
            }
            return configElement;
        }) );
    }

    //

    /**
     * Retrieve and adjust the message of a parse exception, then log
     * that message as an error.
     *
     * Do nothing if errors are not being logged.
     *
     * @param e The parse exception which is to be logged.
     * @param bundle The bundle which contains the document
     *     which is being parsed.  Null if the document is from
     *     the file system.
     */
    public void handleParseError(ConfigParserException e, Bundle bundle) {
        if ( !logErrors() ) {
            return;
        }

        Throwable t = e.getCause();

        if ( t instanceof XMLStreamException ) {
            XMLStreamException xse = (XMLStreamException) t;

            String msg = getMessage(xse);

            Location xmlLocation = xse.getLocation();

            String loc;
            int lineNo;
            int colNo;

            if ( xmlLocation != null ) {
                loc = xmlLocation.getSystemId();
                if ( bundle != null ) {
                    loc = loc + "(" + bundle.getLocation() + ")";
                }
                lineNo = xmlLocation.getLineNumber();
                colNo = xmlLocation.getColumnNumber();

            } else {
                // XMLStreamException is allowed to return a null Location
                // but in practice this will never occur with the JDK built-in
                // StAX implementations.
                loc = "[null]";
                lineNo = -1;
                colNo = -1;
            }
            Tr.error(tc, "error.syntax.parse.server", msg, loc, lineNo, colNo);

        } else {
            String msg = e.getMessage();
            if ( msg != null) {
                Tr.error(tc, "error.parse.server", msg);
            } else {
                // If the message is null, assume we have already logged it.
            }
        }
    }
}
//@formatter:on