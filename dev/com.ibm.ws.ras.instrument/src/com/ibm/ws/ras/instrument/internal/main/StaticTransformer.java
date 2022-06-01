/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.ras.instrument.internal.main;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ibm.ws.ras.instrument.internal.model.TraceType;

/**
 * Trace injection class transformer.
 * 
 * <ul>
 *
 * <li><code>debug</code> Control property: Enables the display of detailed
 * debugging information to trace.
 * </li>
 *  
 * <li><code>packages.include</code> Comma separated list of packages
 * which are to be instrumented. The inclusion of a package includes the
 * package and all of its sub-packages.
 * </li>
 * 
 * <li><code>packages.exclude</code> Comma separated list of packages
 * which are not to be instrumented.  The exclusion of a package excludes
 * the package and all of its sub-packages.  A package is instrumented
 * if an only if the package is included and the package is not excluded.
 * </li>
 *
 * <li><code>style</code> indicates what style of trace to add to the
 * instrumented classes. Valid options are:
 * <ul><li><code>tr</code> WebSphere logging and tracing</li>
 *     <li><code>jsr47</code> java.util.logging based tracing</li>
 *     <li><code>none</code> None</li></ul>
 * </li>
 *  
 * <li><code>ffdc</code> when set to false, will omit the addition of calls
 * to FFDC in exception blocks.
 * </li>
 *
 * <li><code>computeFrames</code> when set to true, ASM will discard and
 * recalculate stack map frames.
 * </li>
 *
 * </ul>
 * 
 * In addition to the specified package includes and package excludes,
 * this transformer always excludes the transformer package <code>
 * com.ibm.ws.ras.instrument.internal.main</code>, and always excludes
 * the java logging package <code>java.util.logging</code>.
 *
 * When no package includes and no package excludes are specified, these
 * default to include all packages and to exclude <code>java</code>
 * packages.
 */
public class StaticTransformer extends StaticTraceInstrumentation implements ClassFileTransformer {
	
	public static final boolean IS_QUIET = true;

    public static String pack(Properties properties) {
    	StringBuilder builder = new StringBuilder();
    	properties.forEach((key, value) -> {
    		if ( builder.length() != 0 ) {
    			builder.append(';');
    		}
    		builder.append(key);
    		if ( (value != null) && !((String) value).isEmpty() ) {
    			builder.append('=');
    			builder.append(value);
    		}
    	});
    	return builder.toString();
    }

    public static Properties unpack(String packed) {
        Properties props = new Properties();
        if ( (packed == null) || packed.isEmpty() ) {
        	return props;
        }
        
        for ( String arg : packed.split(";") ) {
            String[] keyValue = arg.split("=");

            if ( keyValue.length == 1 ) {
                props.setProperty(keyValue[0], "");
            } else if (keyValue.length == 2) {
                props.setProperty(keyValue[0], keyValue[1]);
            } else {
            	System.err.println("Trace instrumentation property [ " + arg + " ] is not valid: Ignoring");
            }
        }

        return props;
    }	

    /**
     * Create a trace injection class transformer using the
     * supplied parameters.
     *
     * @param packed Configuration parameters, as a packed string.
     *     See {@link #unpack(String)}.
     * @param quiet Control parameter: Tells if the transformer
     *     initializer should display debugging information to
     *     standard error.
     */
    public StaticTransformer(String packed, boolean quiet) {
    	this( unpack(packed), quiet );
    }

    /**
     * Create a trace injection class transformer using the
     * supplied parameters.
     * 
     * @param props Configuration properties for the transformer.
     *     See the class comment for details.
     * @param quiet Control parameter: Tells if the transformer
     *     initializer should display debugging information to
     *     standard error.
     */
    public StaticTransformer(Properties props, boolean quiet) {
        super();
        
        String debug = props.getProperty("debug", "false");
        setDebug(Boolean.valueOf(debug));
        
        if (!props.containsKey("packages.include") && !props.containsKey("packages.exclude")) {
            if (!quiet) {
                System.err.println("No selection arguments: Using default package selection.");
            }
            include(SELECT_ALL);
            exclude("java/");

        } else {
            String includesValue = props.getProperty("packages.include");
            if (includesValue != null) {
                for ( String includePrefix : includesValue.split(",") ) {
                	include( asResourceName(includePrefix) );
                }
            }
            String excludesValue = props.getProperty("packages.exclude");
            if (excludesValue != null) {
                for ( String excludePrefix : excludesValue.split(",") ) {
                	exclude( asResourceName(excludePrefix) );
                }
            }
        }
        
        String traceStyle = props.getProperty("style", "jsr47");
        if (traceStyle.equalsIgnoreCase("jsr47")) {
        	setTraceType(TraceType.JAVA_LOGGING);
        } else if (traceStyle.equalsIgnoreCase("tr")) {
        	setTraceType(TraceType.TR);
        } else if (traceStyle.equalsIgnoreCase("none")) {
        	setTraceType(TraceType.NONE);
        } else {
        	// Leave the default, which is TraceType.TR. 
        }

        String instrumentWithFFDC = props.getProperty("ffdc", "true");
        setInstrumentWithFFDC(Boolean.valueOf(instrumentWithFFDC));

        // Indicate whether or not ASM should recompute the frames
        String computeFrames = props.getProperty("computeFrames", "false");
        setComputeFrames(Boolean.valueOf(computeFrames));

        if (!quiet) {
        	System.err.println("Dynamic trace is enabled");
        	System.err.println("  includes = " + describe(includeAll, includePrefixes));
        	System.err.println("  excludes = " + describe(excludeAll, excludePrefixes));
        	System.err.println("  style = " + traceStyle);
        	System.err.println("  ffdc = " + instrumentWithFFDC);
        	System.err.println("  computeFrames = " + computeFrames);
        	System.err.println("  debug = " + debug);
        }
    }

    private static String asResourceName(String packageName) {
    	return packageName.replace('.', '/') + '/';
    }

    private static String describe(boolean isAll, List<String> values) {
    	if ( isAll ) {
    		return "ALL";
    	} else if ( values.isEmpty() ) {
    		return "NONE";
    	} else {
    		return values.toString();
    	}
    }
    
    private static final String SELECT_ALL = "/";

    private boolean includeAll;
    private final List<String> includePrefixes = new ArrayList<String>();

    private boolean excludeAll;
    private final List<String> excludePrefixes = new ArrayList<String>();

    protected void include(String prefix) {
    	if ( prefix.equals(SELECT_ALL) ) {
    		includeAll = true;
    	} else {
    		includePrefixes.add(prefix);
    	}
    }

    protected boolean isIncluded(String className) {
    	if ( includeAll ) {
    		return true;
    	} else {
    		for (String prefix : includePrefixes) {
    			if ( className.startsWith(prefix) ) {
    				return true;
    			}
    		}
    		return false;
    	}
    }
    
    protected void exclude(String prefix) {
    	if ( prefix.equals(SELECT_ALL) ) {
    		excludeAll = true;
    	} else {
    		excludePrefixes.add(prefix);
    	}
    }
    
    protected boolean isExcluded(String className) {
    	if ( excludeAll ) {
    		return true;
    	} else {
    		for (String prefix : excludePrefixes) {
    			if (className.startsWith(prefix) ) {
    				return true;
    			}
            }
    		return false;
        }
    }

    //

    private static final String TRANSFORMER_PACKAGE_RESOURCE_NAME =
    	asResourceName(StaticTransformer.class.getPackage().getName());
    
    private static final String JAVA_LOGGING_PACKAGE_RESOURCE_NAME =
    	"java/util/logging/";
    
    /**
     * Subclass API: Conditionally transform a target class.
     * 
     * Forward to {@link #transform(InputStream)}, but only if the
     * class is not de-selected for transformation.
     * 
     * Do not transform classes in this package.  Do not transform
     * any classes in "java/util/logging/". Do not transform a class
     * which is either not included or excluded.
     *
     * For any class which is selected to be transformed, load and store
     * package information for the class.
     *
     * @param loader The class loader which is requesting that the
     *     class be transformed.
     * @param classResourceName The resource name of the class which
     *     is being transformed.  This is the class name with '.'
     *     replaced with '/'.
     * @param classBeingRedfined The class which is being transformed.
     * @param protectionDomain The protection domain which is active.
     * @param classBytes The bytes of the class resource.
     * 
     * @return The transformed class bytes.  Null if the transform was
     *     not performed, or if the transform failed.
     */
    @Override
    public byte[] transform(ClassLoader loader,
                            String classResourceName,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classBytes) throws IllegalClassFormatException {

        if ( classResourceName.startsWith(TRANSFORMER_PACKAGE_RESOURCE_NAME) ) {
            return null;
        } else if ( classResourceName.startsWith(JAVA_LOGGING_PACKAGE_RESOURCE_NAME) ) {
            return null;
        } else if ( !isIncluded(classResourceName) || isExcluded(classResourceName) ) {
        	return null;
        }
    
        if ( loader != null ) {
        	ensurePackageInfo(loader, classResourceName);
        }

        try {
            return transform( new ByteArrayInputStream(classBytes) );
        } catch (Throwable t) {
        	System.err.println("Unexpected trace injection failure [ " + classResourceName + " ]");
            t.printStackTrace(System.err);
            return null;
        }
    }
}
