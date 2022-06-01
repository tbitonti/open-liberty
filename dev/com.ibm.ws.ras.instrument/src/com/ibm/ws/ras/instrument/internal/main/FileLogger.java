/*******************************************************************************
 * Copyright (c) 2009, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.ras.instrument.internal.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

public class FileLogger {
    public static final String CLASS_NAME = FileLogger.class.getSimpleName();

    public static final String PATTERN_PROPERTY_NAME = "lti.trace.pattern";

    public static final String rawPattern = System.getProperty(PATTERN_PROPERTY_NAME);
    public static final Pattern pattern = (rawPattern == null) ? null : Pattern.compile(rawPattern);

    public static boolean matchesPattern(String text) {
    	return ((pattern != null) && pattern.matcher(text).find());
    }
    
    //
    
    public static String getTmpPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String getThreadId() {
        return Thread.currentThread().toString(); 
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    //
    
    private static final SimpleDateFormat formatter =
    	new SimpleDateFormat("MM/dd/yy HH:mm:ss:SSS z");  // 03/22/16 12:01:13:654 ESD

    private static final Date current = new Date( getTime() );
    private static String currentFormatted = formatter.format(current);

    public static String getFormattedTime() {
    	long currentMs = System.currentTimeMillis();
    	if ((currentMs - current.getTime()) > 10)  {
    		current.setTime(currentMs);
    		currentFormatted = formatter.format(current);
    	}
    	return currentFormatted;
    }

    //

    public static final String ENABLED_PROPERTY_NAME     = "lti.trace.enabled";
    public static final String DIR_PROPERTY_NAME         = "lti.trace.dir";
    public static final String FILE_PROPERTY_NAME        = "lti.trace.file";
    public static final String FILE_EXT_PROPERTY_NAME    = "lti.trace.ext"; 
    public static final String PREFIX_PROPERTY_NAME      = "lti.trace.prefix";
    public static final String AUTOFLUSH_PROPERTY_NAME   = "lti.trace.autoflush";
    
    public static void transfer(Properties properties, String ... propertyNames) {
    	for ( String propertyName : propertyNames ) {
    		String propertyValue = System.getProperty(propertyName);
    		if ( propertyValue != null ) {
    			properties.setProperty(propertyName, propertyValue);
    		}
    	}
    }

    public static FileLogger create() {
    	Properties properties = new Properties();

    	transfer(properties,
    			 ENABLED_PROPERTY_NAME,
    			 DIR_PROPERTY_NAME,
    			 FILE_PROPERTY_NAME,
    			 FILE_EXT_PROPERTY_NAME,
    			 PREFIX_PROPERTY_NAME,
    			 AUTOFLUSH_PROPERTY_NAME);

    	return create(properties);
    }

    public static FileLogger create(File logFile, String prefix, boolean autoflush) {
    	Properties properties = new Properties();
    	
    	File parent = logFile.getParentFile();
    	String parentPath = ((parent == null) ? "." : parent.getPath());

    	String logName = logFile.getName();
    	int extOffset = logName.lastIndexOf('.');

    	String baseLogName;
    	String logExt;
    	if ( extOffset == -1 ) {
    		baseLogName = logName;
    		logExt = null;
    	} else {
    		baseLogName = logName.substring(0, extOffset);
    		logExt = logName.substring(extOffset); // Include the "."
    	}

    	properties.setProperty(ENABLED_PROPERTY_NAME, Boolean.TRUE.toString());
    	properties.setProperty(DIR_PROPERTY_NAME, parentPath);
    	properties.setProperty(FILE_PROPERTY_NAME, baseLogName);
    	if ( logExt != null ) {
    		properties.setProperty(FILE_EXT_PROPERTY_NAME, logExt);
    	}

		properties.setProperty(PREFIX_PROPERTY_NAME, prefix);
		properties.setProperty(AUTOFLUSH_PROPERTY_NAME, Boolean.valueOf(autoflush).toString());

    	return create(properties);
    }

    public static FileLogger create(Properties properties) {    
        String enabledValue = properties.getProperty(ENABLED_PROPERTY_NAME);
        boolean enabled = ( (enabledValue != null) && enabledValue.equalsIgnoreCase("true") );
        if ( !enabled ) {
        	return null;
        }

        String dirName = properties.getProperty(DIR_PROPERTY_NAME);
        if ( dirName == null ) {
        	dirName = FileLogger.getTmpPath();
        }

        String fileName = properties.getProperty(FILE_PROPERTY_NAME);
        if ( fileName == null ) {
        	fileName = "LTInject";
        }
            
        String fileExt = properties.getProperty(FILE_EXT_PROPERTY_NAME);
        if ( fileExt == null ) {
        	fileExt = ".log";
        }

        String prefix;
        String prefixPropertyValue = properties.getProperty(PREFIX_PROPERTY_NAME);
        if ( prefixPropertyValue != null ) {
        	prefix = prefixPropertyValue;
        } else {
        	prefix = "LTI: ";
        }
        
        boolean autoflush;
        String autoflushPropertyValue = properties.getProperty(AUTOFLUSH_PROPERTY_NAME);
        if ( autoflushPropertyValue != null ) {
        	autoflush = Boolean.valueOf(autoflushPropertyValue);
        } else {
        	autoflush = false;
        }

        return new FileLogger(dirName, fileName, fileExt, prefix, autoflush);
    }

    //
    
    public static final boolean AUTOFLUSH = true;
    public static final boolean DO_APPEND = true;        

    public FileLogger(
    	String outputDirPath, String outputPrefix, String outputSuffix,
    	String debugPrefix,
    	boolean autoflush) {

        String methodName = "init";

        this.textPrefix = debugPrefix;
        this.autoflush = autoflush;

        File useOutputFile = null;
        
        if ( (outputDirPath != null) || (outputPrefix != null) ) {
        	if ( outputPrefix == null ) {
        		outputPrefix = "LTI: ";
        	}
        	if ( outputSuffix == null ) {
        		outputSuffix = ".log";
        	}

            File outputDir;
            String actualOutputDirPath = null;

        	if ( outputDirPath == null ) {
        		outputDir = new File(".");
        		actualOutputDirPath = outputDir.getAbsolutePath();
        		System.out.println("LTI: Logging [ " + outputPrefix + " ] [ " + outputSuffix + " ] to current directory [ " + actualOutputDirPath + " ]");

        	} else {
                outputDir = new File(outputDirPath);
        		actualOutputDirPath = outputDir.getAbsolutePath();

                if ( !outputDir.exists() ) {
                	outputDir.mkdirs();
                	if ( !outputDir.exists() ) {
                		System.out.println("LTI: ERROR: Logging [ " + outputPrefix + " ] [ " + outputSuffix + " ] failed to create directory [ " + actualOutputDirPath + " ]");
                		outputDir = null;
                	} else {
                    	System.out.println("LTI: Logging [ " + outputPrefix + " ] [ " + outputSuffix + " ] to new directory [ " + actualOutputDirPath + " ]");
                	}
                } else {
                	System.out.println("LTI: Logging [ " + outputPrefix + " ] [ " + outputSuffix + " ] to existing directory [ " + actualOutputDirPath + " ]");
                }
        	}

        	if ( outputDir != null ) {
        		try {
        			useOutputFile = File.createTempFile(outputPrefix, outputSuffix, outputDir);
        		} catch ( IOException e ) {
        			System.out.println("LTI: ERROR: Failed to create [ " + outputPrefix + " ] [ " + outputSuffix + " ] [ " + actualOutputDirPath + " ]");
        			e.printStackTrace(System.out);
        		}
        	}
        }

    	this.outputFile = useOutputFile;
    	
    	String useOutputPath;
        OutputStream useOutputStream;
        PrintStream useOutputPrinter;

        if ( this.outputFile == null ) {
        	useOutputPath = null;
            useOutputStream = null;
            useOutputPrinter = System.out;
			System.out.println("LTI: Logging to Standard Output");

        } else {
        	useOutputPath = this.outputFile.getAbsolutePath();
        	
            try {
                useOutputStream = new FileOutputStream(this.outputFile, DO_APPEND);
                useOutputPrinter = new PrintStream(useOutputStream, this.autoflush);
    			System.out.println("LTI: Logging to [ " + useOutputPath + " ]");        			

            } catch ( IOException e ) {
    			System.out.println("LTI: ERROR: Unable to write to output file [ " + useOutputPath + " ]");
            	e.printStackTrace(System.out);

            	useOutputPath = null;
                useOutputStream = null;
                useOutputPrinter = System.out;
    			System.out.println("LTI: Logging to Standard Output");                
            }
        }

        this.outputPath = useOutputPath;
        this.outputStream = useOutputStream;
        this.outputPrinter = useOutputPrinter;

        //

        if ( this.outputFile != null ) {
        	this.log(CLASS_NAME, methodName, "Output to ", this.outputFile.getAbsoluteFile());
        } else {
        	this.log(CLASS_NAME, methodName, "Output to [ System.out ]");
        }
    }

    public final boolean autoflush;

    public final File outputFile;
    public final String outputPath;

    public final OutputStream outputStream;
    public final PrintStream outputPrinter;

    public final String textPrefix;

    public synchronized void log(String text) {
        outputPrinter.println(head() + text);
    }        
    
    public synchronized void log(String className, String text) {
        outputPrinter.println(head() + className + ": " + text);
    }        
        
    public synchronized void log(String className, String methodName, String text) {
        outputPrinter.println(head() + className + ": " + methodName + ": " + text);
    }        

    public synchronized void log(String className, String methodName, String text, Object value) {
        outputPrinter.println(head() + className + ": " + methodName + ": " + text + " [ " + value + " ]");
    }        

    //
    
    public synchronized void logStack(String text) {
        (new Throwable(head() + text)).printStackTrace(outputPrinter);
    }

    public synchronized void logStack(String className, String text) {
        (new Throwable(head() + className + ": " + text)).printStackTrace(outputPrinter);
    }

    public synchronized void logStack(String className, String methodName, String text) {
        (new Throwable(head() + className + ": " + methodName + ": " + text)).printStackTrace(outputPrinter);
    }

    public synchronized void logStack(String text, Throwable th) {
    	log(text);
        th.printStackTrace(outputPrinter);
    }

    public synchronized void logStack(String className, String text, Throwable th) {
    	log(className, text);
        th.printStackTrace(outputPrinter);
    }

    public synchronized void logStack(String className, String methodName, String text, Throwable th) {
    	log(className, methodName, text);
        th.printStackTrace(outputPrinter);
    }

    //

    public synchronized void log(String text, byte[] bytes) {
    	String header = head() + text;
    	dump(header, bytes);
    }

    public synchronized void log(String className, String text, byte[] bytes) {
        String header = head() + className + ": " + text;
        dump(header, bytes);
    }            
    
    public synchronized void log(String className, String methodName, String text, byte[] bytes) {
        String header = head() + className + ": " + methodName + ": " + text;
        dump(header, bytes);
    }                

    //

    private String head() {
    	return "[ " + getFormattedTime() + " ] [ " + getThreadId() + " ] " + textPrefix;
    }

    private void dump(String header, byte[] bytes) { 
    	String tail = " [ " + bytes.length + " ]";
        outputPrinter.println(header + tail + ": BEGIN");
        dump(bytes);
        outputPrinter.println(header + tail + ": END");
    }   

    public static final int BYTES_PER_ROW = 16;
    public static final int BYTE_INDENT = 4;
    public static final String INDENT = "    ";

    private void dump(byte[] bytes) {
    	int len = bytes.length;
    	if ( len == 0 ) {
    		return;
    	}

    	int rows = len / BYTES_PER_ROW;
    	int rem = len % BYTES_PER_ROW;
    	int partialRow = ((rem == 0) ? 0 : 1);
    	
    	StringBuilder builder = new StringBuilder(BYTE_INDENT + ((BYTES_PER_ROW - 1) * 3) + 2);
    	
    	for ( int rowNo = 0; rowNo < rows + partialRow; rowNo++ ) {
    		int start = rowNo * BYTES_PER_ROW;
    		int end = start + ((rowNo == rows) ? rem : BYTES_PER_ROW);
    		
    		builder.append(INDENT);

    		for ( int byteNo = start; byteNo < end; byteNo++ ) {
    			if ( byteNo > start ) {
    				builder.append(' ');
    			}
    			String nextHex = Integer.toHexString(((int) bytes[byteNo]) & 0xFF);
    			if ( nextHex.length() < 2 ) {
    				builder.append(' ');
    			}
    			builder.append(nextHex);
    		}

    		String output = builder.toString();
    		builder.setLength(0);

    		outputPrinter.println(output);
    	}
    }
}
