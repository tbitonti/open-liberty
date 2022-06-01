/*******************************************************************************
 * Copyright (c) 2006, 2022 IBM Corporation and others.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.SerialVersionUIDAdder;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.ibm.ws.ras.instrument.internal.bci.FFDCClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.JSR47TracingClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.WebSphereTrTracingClassAdapter;
import com.ibm.ws.ras.instrument.internal.introspect.TraceConfigClassVisitor;
import com.ibm.ws.ras.instrument.internal.model.ClassInfo;
import com.ibm.ws.ras.instrument.internal.model.InstrumentationOptions;
import com.ibm.ws.ras.instrument.internal.model.PackageInfo;
import com.ibm.ws.ras.instrument.internal.model.TraceType;
import com.ibm.ws.ras.instrument.internal.xml.TraceConfigFileParser;

/**
 * Trace injection class transformer.
 */
public class StaticTraceInstrumentation extends AbstractInstrumentation {
    
    public final static void main(String[] args) throws Exception {
        if ((args == null) || (args.length <= 0) || contains(args, "--usage") ) {
        	printUsage(StaticTraceInstrumentation::printTypesUsage,
     			       StaticTraceInstrumentation::printOptionsUsage);

        } else if ( contains(args, "--help") ) {
        	printHelp(StaticTraceInstrumentation::printTypesHelp,
        			  StaticTraceInstrumentation::printOptionsHelp);

        } else {
        	(new StaticTraceInstrumentation()).performInstrumentation(args);
        }
    }

    protected static void printTypesUsage() {
    	System.out.println("  [ --tr | --websphere | --java-logging | --jsr45 | --none | --no-trace ]");
    }
    
    protected static void printOptionsUsage() {
    	System.out.println("  [ --ffdc ] [ --compute-frames ]");    	
    }
    
    protected static void printTypesHelp() {
    	System.out.println("  [ --tr | --websphere | --java-logging | --jsr45 | --none | --no-trace ]");
    	System.out.println("    Specify what trace format is to be used.  Default to use the Liberty");
    	System.out.println("    format.  Secify '--tr' or '--websphere' to use the Liberty trace format.");
    	System.out.println("    Specify '--java-logging' or '--jsr45' to use the JSR-45 trace format.");            	
    	System.out.println("    Specify '--none' or '--no-trace' to disable trace injection.");            	
    	System.out.println("");
    }

    protected static void printOptionsHelp() {
    	System.out.println("  [ --ffdc ]");
    	System.out.println("    Enable FFDC injection.");            	    	
    	System.out.println("");            	    	
    	System.out.println("  [ --compute-frames ]");
    	System.out.println("    Specify that ASM should compute frames.");
    	System.out.println("");    	
    }

    //
    
    protected boolean handleTraceType(String arg) {
    	TraceType traceType;
    	
    	if (arg.equals("--tr") || arg.equals("--websphere")) { 
    		traceType = TraceType.TR;
    	} if (arg.equals("--java-logging") || arg.equals("--jsr47")) { 
    		traceType = TraceType.JAVA_LOGGING;
    	} else if (arg.equals("--none") || arg.equals("--no-trace")) { 
    		traceType = TraceType.NONE;
    	} else {
    		return false;
    	}

    	setTraceType(traceType);
    	return true;
    }

    protected boolean handleTraceOption(String arg) {
    	if (arg.equals("--ffdc")) {
    		setInstrumentWithFFDC(true);
    		return true;
    	} else if (arg.equals("--compute-frames")) {
    		setComputeFrames(true);
    		return true;
    	} else {
    		return false;
    	}
    }

    //

    public StaticTraceInstrumentation() {
    	super();
    }

    protected void initTraceType() {
        this.traceType = TraceType.TR;            	
    }

    protected void initInjectionOptions() {
        this.instrumentWithFFDC = false;    	
        this.computeFrames = false;
        this.introspectAnnotations = true;    	
    }
    
    //
    
    protected TraceConfigFileParser configFileParser;

    protected void setConfigFile(File configFile) throws IOException {     
    	configFileParser = new TraceConfigFileParser(configFile);
    	configFileParser.parse();
    	setInstrumentationOptions( configFileParser.getInstrumentationOptions() );
    }

    protected PackageInfo getParsedPackageInfo(String internalPackageName) {
    	return ( (configFileParser == null) ? null : configFileParser.getPackageInfo(internalPackageName));
    }
    
    protected ClassInfo getParsedClassInfo(String internalClassName) {
    	return ( (configFileParser == null) ? null : configFileParser.getClassInfo(internalClassName));
    }
    
    //

    protected void handleOptions(InstrumentationOptions useOptions) {
    	super.handleOptions(useOptions);

    	if (useOptions.getAddFFDC()) {
    		setInstrumentWithFFDC(true);
    	}
    }   

    //

    protected boolean instrumentWithFFDC;
    
    public void setInstrumentWithFFDC(boolean instrumentWithFFDC) {
        this.instrumentWithFFDC = instrumentWithFFDC;
    }

    public boolean getInstrumentWithFFDC() {
        return instrumentWithFFDC;
    }
    
    //

    protected boolean introspectAnnotations;

    public void setIntrospectAnnotations(boolean introspectAnnotations) {
        this.introspectAnnotations = introspectAnnotations;
    }

    public boolean getIntrospectAnnotations() {
        return introspectAnnotations;
    }

    //

    protected boolean computeFrames;
    
    public void setComputeFrames(boolean computeFrames) {
        this.computeFrames = computeFrames;
    }

    public boolean isComputeFrames() {
        return computeFrames;
    }
    
    //

    @Override
    public void processPackageInfo() throws IOException {
        if (!getIntrospectAnnotations()) {
            return;
        }

        super.processPackageInfo();
    }

    //
    
    /**
     * Read and instrument class bytes using the specified trace options.
     * 
     * Do nothing and answer null if the package is not specified to be
     * instrumented.
     *
     * @param inputStream The stream containing the class bytes.
     *
     * @return The transformed class bytes.  Null if the class was not
     *     transformed.
     * 
     * @throws IOException if an error is encountered while reading from
     *             the <code>InputStream</code>
     */
    final protected byte[] transform(InputStream inputStream) throws IOException {
        if (!getIntrospectAnnotations()) {
        	return null;
        }

        byte[] classBytes = collect(inputStream);
        ClassInfo classInfo = readClassInfo(classBytes);
        if ( !isPackageIncluded(classInfo.getPackageName()) ) {
            return null;
        }

        merge(classInfo);

        ClassReader classReader = new ClassReader(classBytes);
        
        int classWriterOptions = ( isComputeFrames() ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS );
        ClassWriter classWriter = new ClassWriter(classReader, classWriterOptions);

        ClassVisitor classVisitor = createClassVisitor(classWriter, classInfo);

        try {
            int classVisitorOptions = ( isComputeFrames() ? ClassReader.EXPAND_FRAMES : 0 );
            classReader.accept(classVisitor, classVisitorOptions);
        } catch ( Throwable t ) {
            throw new IOException("Failed to instrument [ " + classInfo.getClassName() + " ]", t);
        }

        return classWriter.toByteArray();
    }
    
    private ClassVisitor createClassVisitor(ClassWriter classWriter, ClassInfo classInfo) {
        ClassVisitor classVisitor = classWriter;

        if ( isDebug() ) {
            classVisitor = new CheckClassAdapter(classVisitor);
            classVisitor = new TraceClassVisitor(classVisitor, new PrintWriter(System.out));
        }

        // Trace types 'JAVA_LOGGING', 'TR', or 'NONE' are expected.

        TraceType useTraceType = getTraceType();
        if ( useTraceType == TraceType.JAVA_LOGGING ) {
            classVisitor = new JSR47TracingClassAdapter(classVisitor, classInfo);
        } else if ( useTraceType == TraceType.TR ) {
            classVisitor = new WebSphereTrTracingClassAdapter(classVisitor, classInfo);
        } else {
        	// Do not add tracing
        }

        if (getInstrumentWithFFDC()) {
            classVisitor = new FFDCClassAdapter(classVisitor, classInfo);
        }

        // The SerialVersionUIDAdder adder must be the first visitor in
        // the chain in order to calculate the serialVersionUID before
        // the tracing class adapter mucks around and (possibly) adds a
        // class static initializer.
        classVisitor = new SerialVersionUIDAdder(classVisitor);
        
        return classVisitor;
    }

    /**
     * Read class information.
     * 
     * @param classBytes Class bytes which are to be read.
     *
     * @return Class information read from the class bytes.
     * 
     * @throws IOException Thrown if an error occurred reading the class bytes.
     */
    private ClassInfo readClassInfo(byte[] classBytes) throws IOException {
        ClassReader classReader = new ClassReader(classBytes);
        TraceConfigClassVisitor classVisitor = new TraceConfigClassVisitor();
        classReader.accept(classVisitor, 0);
        return classVisitor.getClassInfo();
    }
    
    /**
     * Merged parsed package and class information.  Merged using scanned package
     * information if parsed package information is not available.
     *
     * @param classInfo The class information into which to merged parsed and
     *     scanned package and class information.
     */
    private void merge(ClassInfo classInfo) {
    	String internalPackageName = classInfo.getInternalPackageName();
        PackageInfo packageInfo = getParsedPackageInfo(internalPackageName);
        if ( packageInfo == null ) {
            packageInfo = getPackageInfo(internalPackageName);
        }
        classInfo.updateDefaultValuesFromPackageInfo(packageInfo);

        String internalClassName = classInfo.getInternalClassName();
        ClassInfo parsedClassInfo = getParsedClassInfo(internalClassName);
        if ( parsedClassInfo != null ) {
            classInfo.overrideValuesFromExplicitClassInfo(parsedClassInfo);
        }
    }    
}
