/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.ras.instrument.internal.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.SerialVersionUIDAdder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.ibm.ws.ras.instrument.internal.bci.CheckInstrumentableClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.FFDCClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.JSR47TracingClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.JSR47TracingMethodAdapter;
import com.ibm.ws.ras.instrument.internal.bci.LibertyTracePreprocessClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.LibertyTracingClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.LibertyTracingMethodAdapter;
import com.ibm.ws.ras.instrument.internal.bci.WebSphereTrTracingClassAdapter;
import com.ibm.ws.ras.instrument.internal.bci.WebSphereTrTracingMethodAdapter;
import com.ibm.ws.ras.instrument.internal.introspect.InjectedTraceAnnotationVisitor;
import com.ibm.ws.ras.instrument.internal.introspect.TraceObjectFieldAnnotationVisitor;
import com.ibm.ws.ras.instrument.internal.introspect.TraceOptionsAnnotationVisitor;
import com.ibm.ws.ras.instrument.internal.model.PackageInfo;
import com.ibm.ws.ras.instrument.internal.model.TraceOptionsData;
import com.ibm.ws.ras.instrument.internal.model.TraceType;

/**
 * Liberty trace injection class transformer.
 */
public class LibertyTracePreprocessInstrumentation extends AbstractInstrumentation {
	
    public final static void main(String[] args) throws Exception {
        if ((args == null) || (args.length <= 0) || contains(args, "--usage") ) {
        	printUsage(LibertyTracePreprocessInstrumentation::printTypesUsage,
     			       LibertyTracePreprocessInstrumentation::printOptionsUsage);

        } else if ( contains(args, "--help") ) {
        	printHelp(LibertyTracePreprocessInstrumentation::printTypesHelp,
        			  LibertyTracePreprocessInstrumentation::printOptionsHelp);

        } else {
        	(new LibertyTracePreprocessInstrumentation()).performInstrumentation(args);
        }
    }

    protected static void printTypesUsage() {
    	System.out.println("  [ --liberty | --tr | --java-logging ]");
    }

    protected static void printOptionsUsage() {    
    	System.out.println("  [ --ffdc ] [ --static ]");
    }

    protected static void printTypesHelp() {
    	System.out.println("  [ --liberty | --tr | --java-logging ]");
    	System.out.println("    Specify what trace format is to be used.  Default to use the Liberty");
    	System.out.println("    format.  Secify '--liberty' or '--tr' to use the Liberty trace format.");
    	System.out.println("    Specify '--java-logging' to use the JSR-47 trace format.");            	
    	System.out.println("");
    }
    
    protected static void printOptionsHelp() {
    	System.out.println("  [ --ffdc ]");
    	System.out.println("    Enable FFDC injection.");            	    	
    	System.out.println("");            	    	
    	System.out.println("  [ --static ]");
    	System.out.println("    Perform static trace injection.");
    	System.out.println("");    	
    }
    
    public LibertyTracePreprocessInstrumentation() {
    	super();
    }

    @Override
    protected void initTraceType() {
    	traceType = TraceType.LIBERTY;
    }    
    
    @Override
    protected void initInjectionOptions() {
    	injectFfdc = false;
    	injectStatic = false;
    	traceComponent = "$$$tc$$$";
    }

    @Override
    public boolean handleTraceType(String arg) {
    	TraceType traceType;
    	if ( arg.equals("--liberty")) {
    		traceType = TraceType.LIBERTY;
    	} else if (arg.equals("--tr")) {
    		traceType = TraceType.TR;
    	} else if (arg.equals("--java-logging")) {
    		traceType = TraceType.JAVA_LOGGING;
    	} else {
    		return false;
    	}
    	setTraceType(traceType);
    	return true;
    }
    
    @Override
    public boolean handleInjectionOption(String arg) {
    	if (arg.equals("--ffdc")) {
    		injectFfdc = true;
    		return true;
    	} else if (arg.equals("--static")) {
    		injectStatic = true;
    		return true;
    	} else {
    		return false;
    	}
    }

    private boolean injectFfdc;

    public boolean getInjectFFDC() {
    	return injectFfdc;
    }

    private boolean injectStatic;

    public boolean getInjectStatic() {
    	return injectStatic;
    }
    
    private String traceComponent;

    public String getDefaultTraceComponentName() {
		return traceComponent;
	}

    //

    /**
     * Perform trace instrumentation on a class.
     * 
     * The process is complex, beginning with a read of the class bytes,
     * followed by an examination of the class information to prepare for
     * trace injection, followed by the actual trace injection.
     * 
     * @param inputStream A stream containing the class bytes.
     *
     * @return The class bytes with injected trace.  Null if no
     *     trace injection was performed.
     *     
     * @throws IOException Thrown if the read or write of the class data failed.
     */
    @Override
    protected byte[] transform(InputStream inputStream) throws IOException {
    	// Step A: Read the class information into the ClassNode tree.
    	ClassReader classReader = new ClassReader(inputStream);
    	ClassNode classNode = new ClassNode();
    	CheckInstrumentableClassAdapter checkInstrumentableAdapter = new CheckInstrumentableClassAdapter(classNode);
    	SerialVersionUIDAdder uidAdder = new SerialVersionUIDAdder(checkInstrumentableAdapter);
    	classReader.accept(uidAdder, 0);

    	// Step B: Prepare for trace injection.
    	
    	// Start assembling the class information with important
    	// information necessary for trace injection.
    	//
    	// Creation of the class info causes package information for
    	// the class to be read.
    	ClassTraceInfo classInfo = new ClassTraceInfo(classNode);

    	// TODO: Inner class support to look for Options on outer class before package?
    
    	// #1: Unless the class is an inner class, there may be a trace
    	//     options annotation.  This is merged in with the trace options
    	//     provided by the package information.
    	if ( !classInfo.isInner() ) {
    		processTraceOptions(classInfo);
    	}
    	
    	// #2: Find declared trace components.
    	// #3: Find declared java loggers.

    	discoverLibertyTraceComponents(classInfo);
    	discoverWebsphereTraceComponents(classInfo);
    	discoverJavaLoggers(classInfo);

    	// #4: Start counting the discovered trace fields...
    	int traceFields = 0;
    	if (classInfo.libertyTraceFieldNode != null) {
    		traceFields++;
    	}
    
    	// TODO: Note that we check the field count after counting the
    	//       liberty trace field, but before counting websphere and
    	//       logger trace fields.
    	
    	// #5a: Don't instrument non-runnable types.
    	if ( !checkInstrumentableAdapter.isInstrumentableClass() ) {
    		return null;
    	}
    	// #5b: Don't instrument trivial classes which don't have a trace field.
    	if ( classInfo.isTrivial() && (traceFields == 0) ) {
    		return null;
    	}

    	// #4 Continue counting the discovered trace fields.
    	if (classInfo.websphereTraceFieldNode != null) {
    		traceFields++;
    	}
    	if (classInfo.loggerFieldNode != null) {
    		traceFields++;
    	}
    	if (traceFields > 1) {
    		classInfo.addWarning( multipleTraceFields(classInfo) );
    	}

    	// #6 Check if Inner class, skip any static field initialization if doesn't
    	// already exist
    	//   #7: Determine if Logger/TraceComponent is initialized
    	//   #8: Define the TraceComponent if needed		
    	if ( !classInfo.isInner() || (traceFields == 0) ) {
    		processExistingStaticInitializer(classInfo);
    		setupTraceStateObjectField(classInfo);
    	}

    	// #9: Examine the 'toString' implementation and issue warnings for
    	//     any calls to non-trivial local or superclass methods. 
    	validateToString(classInfo);

    	// #10: Look for methods that have hard-coded entry/exit trace points
    	processManuallyTracedMethods(classInfo);

    	// #11: Dump the list of warnings
    	for ( String warning : classInfo.warnings ) {
    		System.out.println(warning);
    	}
    	if ( classInfo.failInstrumentation ) {
    		System.out.println( instrumentationFailure(classInfo) );
    		return null;
    	}

    	// Step C: Perform trace injection.

    	// Use the class writer which is optimized for "mostly adding" to
    	// an existing class.  See the ASM documentation for more information.
    	//
    	// If debugging is enabled, add a class validator, to make sure the
    	// trace injection was done correctly, and write the class information
    	// to Standard Output.
    	// 
    	// Setup the tracing adapters according to the state of the trace field
    	// and according to the desired trace type.

    	ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
    	ClassVisitor cv = classWriter;

    	if ( isDebug() ) {
    		cv = new CheckClassAdapter(cv);
    		cv = new TraceClassVisitor(cv, new PrintWriter(System.out));
    	}

    	if ( classInfo.traceFieldNode != null ) {
    		if ( getInjectStatic() &&
    			 LIBERTY_TRACE_COMPONENT_TYPE.getDescriptor().equals(classInfo.traceFieldNode.desc)) {
    			cv = new LibertyTracingClassAdapter(cv, classInfo, true);
    		}
    		cv = new LibertyTracePreprocessClassAdapter(cv, !classInfo.traceFieldInitialized, classInfo);

    	} else {
    		TraceType useTraceType = getTraceType();
    		if (useTraceType == TraceType.TR) {
    			cv = new WebSphereTrTracingClassAdapter(cv, null, classInfo);
    		} else if (useTraceType == TraceType.JAVA_LOGGING) {
    			cv = new JSR47TracingClassAdapter(cv, null, classInfo);
    		} else {
    			// Don't perform trace injection.
    		}
    	}

    	if ( getInjectFFDC() && !classInfo.isTrivial() ) {
    		cv = new FFDCClassAdapter(cv, null,classInfo);
    	}

    	classNode.accept(cv);

    	return classWriter.toByteArray();
    }

    private String multipleTraceFields(ClassTraceInfo classInfo) {
    	return
    		"WARNING: More than one type of tracing is present" +
    		" on class " + classInfo.className + ".";
    }
    
    private String instrumentationFailure(ClassTraceInfo classInfo) {
    	return
    		"ERROR: Trace instrumentation failed on class " + classInfo.className + "." +
    		"  Please see previous messages.";
    }
    
    public final static Type TRIVIAL_TYPE = Type.getType(com.ibm.websphere.ras.annotation.Trivial.class);
    public final static Type TRACE_OPTIONS_TYPE = Type.getType(com.ibm.websphere.ras.annotation.TraceOptions.class);

    public final static Type LIBERTY_TR_TYPE = LibertyTracingClassAdapter.TR_TYPE;
    public final static Type LIBERTY_TRACE_COMPONENT_TYPE = LibertyTracingClassAdapter.TRACE_COMPONENT_TYPE;

    public final static Type WEBSPHERE_TR_TYPE = WebSphereTrTracingClassAdapter.TR_TYPE;
    public final static Type WEBSPHERE_TRACE_COMPONENT_TYPE = WebSphereTrTracingClassAdapter.TRACE_COMPONENT_TYPE;

    public final static Type LOGGER_TYPE = Type.getType(java.util.logging.Logger.class);

    public final static Type INJECTED_TRACE_TYPE = Type.getType(com.ibm.websphere.ras.annotation.InjectedTrace.class);
    public final static Type MANUAL_TRACE_TYPE = Type.getType(com.ibm.websphere.ras.annotation.ManualTrace.class);
    public final static Type TRACE_OBJECT_FIELD_TYPE = Type.getType(com.ibm.websphere.ras.annotation.TraceObjectField.class);

    /**
     * Transient class that collects class information needed during
     * pre-processing.
     */
    public class ClassTraceInfo {
    	public ClassTraceInfo(ClassNode classNode) {
    		this.className = classNode.name.replace('/', '.');
    		this.classNode = classNode;

    		this.packageInfo = getPackageInfo( classNode.name.replaceAll("/[^/]+$", "") );
    	}
    	
    	public final String className;
        public final ClassNode classNode;

        public AnnotationNode getTrivialAnnotation() {
        	return getAnnotation(TRIVIAL_TYPE.getDescriptor(), classNode.visibleAnnotations);    	
        }

        public AnnotationNode getTraceOptionsAnnotation() {
        	return getAnnotation(TRACE_OPTIONS_TYPE.getDescriptor(), classNode.visibleAnnotations);    
        }

        public AnnotationNode getTraceObjectAnnotation() {
        	return getAnnotation(TRACE_OBJECT_FIELD_TYPE.getDescriptor(), classNode.visibleAnnotations);
        }    

        public boolean isTrivial() {
            return (getTrivialAnnotation() != null);
        }
        
        public boolean isInner() {
        	if ( classNode.innerClasses.isEmpty() ) {
        		return false;
        	} else {
        		int innerIdentifierIndex = classNode.name.lastIndexOf("$");
        		return (innerIdentifierIndex != -1);
    		}
        }
        
        public List<FieldNode> getFields(String desc) {
            List<FieldNode> fields = null;
            for (FieldNode fn : classNode.fields) {
                if (desc.equals(fn.desc)) {
                    if (fields == null) {
                    	fields = new ArrayList<FieldNode>(1);            
                    }
                    fields.add(fn);
                }
            }
            return ( (fields == null) ? Collections.emptyList() : fields );
        }        
        
        public FieldNode getField(String fieldName, String desc) {
            for (FieldNode fn : classNode.fields) {
                if (fieldName.contentEquals(fn.name) && desc.equals(fn.desc)) {
                	return fn;
                }
            }
            return null;
        }                
                
        public List<MethodNode> getMethods(String methodName) {
            List<MethodNode> methods = null;
            for (MethodNode mn : classNode.methods) {
                if (methodName.equals(mn.name)) {
                	if ( methods == null ) {
                		methods = new ArrayList<MethodNode>(1);
                	}
                    methods.add(mn);
                }
            }
            return ( (methods == null) ? Collections.emptyList() : methods );
        }
        
        public MethodNode getMethod(String methodName, String desc) {
        	return (LibertyTracePreprocessInstrumentation.this).getMethod(methodName, desc, classNode.methods);
        }

        //

        final PackageInfo packageInfo;

		public TraceOptionsData getTraceOptionsData() {
			return ( (packageInfo != null) ? packageInfo.getTraceOptionsData() : null );
		}        

		//

        FieldNode libertyTraceFieldNode; // Explicit Liberty TraceComponent. 
        boolean libertyTraceFieldInitialized;

        FieldNode websphereTraceFieldNode; // Explicit WebSphere TraceComponent.
        boolean websphereTraceFieldInitialized;

        FieldNode loggerFieldNode; // Explicit java logger.
        boolean loggerFieldInitialized;

        FieldNode traceFieldNode; // The active trace field. 
        boolean traceFieldInitialized;

        //

        List<String> warnings = new ArrayList<String>(0);

        public void addWarning(String message) {
        	warnings.add(message);
        }
        
        boolean failInstrumentation;        
    }
    
    //
    
    private AnnotationNode getTrivialAnnotation(MethodNode methodNode) {
    	return getAnnotation(TRIVIAL_TYPE.getDescriptor(), methodNode.visibleAnnotations);    	
    }
    
    private boolean isTrivial(MethodNode methodNode) {
    	return ( getTrivialAnnotation(methodNode) != null );
    }

    private AnnotationNode getInjectedTraceAnnotation(MethodNode methodNode) {
    	return getAnnotation(INJECTED_TRACE_TYPE.getDescriptor(), methodNode.visibleAnnotations);
    }    
    
    private AnnotationNode getManualTraceAnnotation(MethodNode methodNode) {
    	return getAnnotation(MANUAL_TRACE_TYPE.getDescriptor(), methodNode.visibleAnnotations);
    }    
    
    public  AnnotationNode getAnnotation(String desc, List<AnnotationNode> annotations) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode an : annotations) {
            if (desc.equals(an.desc)) {
                return an;
            }
        }
        return null;
    }

    public MethodNode getMethod(String methodName, String desc, List<MethodNode> methods) {
        for (MethodNode mn : methods) {
            if (methodName.equals(mn.name) && desc.equals(mn.desc)) {
                return mn;
            }
        }
        return null;
    }
    
    /**
     * Locate and merge the metadata from the {@code TraceOptions} annotations
     * specified on the class and the package. This is used to determine the
     * resource bundle name, trace group names, and other miscellaneous info.
     * <p>
     * The class annotation is intended to override package information when
     * appropriate.
     * 
     * @param info the collected class information
     */
    private void processTraceOptions(ClassTraceInfo info) {
    	// Start with the trace options annotation which was on the class.
        AnnotationNode optionsNode = info.getTraceOptionsAnnotation();
        if ( optionsNode == null ) {
        	return;
        }

        // The goal is to merge those trace options into the package's trace
        // options.  Package information is normally available for the class.
        if ( info.packageInfo == null ) {
        	info.addWarning( unableToMergeOptions(info) );
        	return;
        }
        TraceOptionsData packageOptions = info.packageInfo.getTraceOptionsData();

        // Build trace options from the annotation. 
        TraceOptionsAnnotationVisitor optionsVisitor = new TraceOptionsAnnotationVisitor();
        optionsNode.accept(optionsVisitor);
        TraceOptionsData annoOptions = optionsVisitor.getTraceOptionsData();

        // Nothing to do if the options are the same. 
        if ( annoOptions.equals(packageOptions) ) {
        	return;
        }

        // Merge the message bundle and trace groups into the annotation options.
        if ( (annoOptions.getMessageBundle() == null) && (packageOptions.getMessageBundle() != null) ) {
        	annoOptions.setMessageBundle( packageOptions.getMessageBundle() );
        }
        if ( annoOptions.getTraceGroups().isEmpty() && !packageOptions.getTraceGroups().isEmpty() ) {
        	for ( String group : packageOptions.getTraceGroups() ) {
        		annoOptions.addTraceGroup(group);
        	}
        }

        // Replace the options node with a new clean node.
        info.classNode.visibleAnnotations.remove(optionsNode);        
        optionsNode = (AnnotationNode) info.classNode.visitAnnotation(TRACE_OPTIONS_TYPE.getDescriptor(), true);

        // Populate the new clean node by a simulated visit that is driven
        // by the merged annotations data.
        AnnotationVisitor groupsVisitor = optionsNode.visitArray("traceGroups");
        for (String group : annoOptions.getTraceGroups()) {
        	groupsVisitor.visit(null, group);
        }
        groupsVisitor.visitEnd();
        optionsNode.visit("traceGroup", "");
        optionsNode.visit("messageBundle", annoOptions.getMessageBundle() == null ? "" : annoOptions.getMessageBundle());
        optionsNode.visit("traceExceptionThrow", Boolean.valueOf(annoOptions.isTraceExceptionThrow()));
        optionsNode.visit("traceExceptionHandling", Boolean.valueOf(annoOptions.isTraceExceptionHandling()));
        optionsNode.visitEnd();
    }

    private String unableToMergeOptions(ClassTraceInfo info) {
    	return
    		"WARNING: No package information is present for class " + info.className + "." +
    		"  Unable to merge the trace options annotation.";
    }
    
    /**
     * Examine the class to obtain the Liberty trace component fields.
     * 
     * These are static fields of type <code>com.ibm.websphere.ras.TraceComponent</code>.
     * 
     * Ignore non-static fields.
     * 
     * @param info The class which is to be examined.
     */
    private void discoverLibertyTraceComponents(ClassTraceInfo info) {
        List<FieldNode> traceComponentFields = info.getFields(LIBERTY_TRACE_COMPONENT_TYPE.getDescriptor());
        if (traceComponentFields.isEmpty()) {
        	return;
        }

        for (int i = traceComponentFields.size() - 1; i >= 0; i--) {
        	FieldNode fn = traceComponentFields.get(i);
        	if ((fn.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
        		traceComponentFields.remove(i); // We don't want non-static fields.
        		
            	// TODO: Only Liberty discovery emits a warning when
        		//       a non-static trace component is present, and only
        		//       Liberty marks instrumentation as failed.

        		info.addWarning( nonStaticTraceComponent(info, "com.ibm.websphere.ras.TraceComponent") );
        		info.failInstrumentation = true;
        	}
        }
        
        if (traceComponentFields.size() > 0) {
        	if (traceComponentFields.size() > 1) {
        		info.addWarning( multipleTraceComponents(info, traceComponentFields, "com.ibm.websphere.ras.TraceComponent") );
        	}
        	info.libertyTraceFieldNode = traceComponentFields.get(0);
        }
    }
    
    /**
     * Examine the class to obtain the Websphere trace component fields.
     * 
     * These are static fields of type <code>com.ibm.ejs.ras.TraceComponent</code>.
     * 
     * Ignore non-static fields.
     * 
     * @param info The class which is to be examined.
     */
    private void discoverWebsphereTraceComponents(ClassTraceInfo info) {
        List<FieldNode> traceComponentFields = info.getFields(WEBSPHERE_TRACE_COMPONENT_TYPE.getDescriptor());
        if (traceComponentFields.isEmpty()) {
        	return;
        }
        
        for (int i = traceComponentFields.size() - 1; i >= 0; i--) {
        	FieldNode fn = traceComponentFields.get(i);
        	if ((fn.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
        		traceComponentFields.remove(i); // We don't want non-static fields.
        	}
        }

        if (traceComponentFields.size() > 0) {
        	if (traceComponentFields.size() > 1) {
        		info.addWarning( multipleTraceComponents(info, traceComponentFields, "com.ibm.ejs.ras.TraceComponent") );
        	}            	
        	info.websphereTraceFieldNode = traceComponentFields.get(0);
        }
    }

    /**
     * Examine the class to obtain the Websphere trace component fields.
     * 
     * These are static fields of type <code>com.ibm.ejs.ras.TraceComponent</code>.
     * 
     * Ignore non-static fields.
     * 
     * @param info The class which is to be examined.
     */
    private void discoverJavaLoggers(ClassTraceInfo info) {
        List<FieldNode> loggerFields = info.getFields(LOGGER_TYPE.getDescriptor());        		
        if (loggerFields.isEmpty()) {
        	return;
        }
        
        for (int i = loggerFields.size() - 1; i >= 0; i--) {
        	FieldNode fn = loggerFields.get(i);
        	if ((fn.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
        		loggerFields.remove(i); // We don't want static fields.
        		info.addWarning( nonStaticTraceComponent(info, "java.util.logging.Logger") );
        	}
        }

        if (loggerFields.size() > 0) {
        	if (loggerFields.size() > 1) {
        		info.addWarning( multipleTraceComponents(info, loggerFields, "java.util.logging.Logger") );
        	}
        	info.loggerFieldNode = loggerFields.get(0);
        }
    }

    private String nonStaticTraceComponent(ClassTraceInfo info, String componentType) {
    	return
    		"WARNING: Non-static " + componentType + " field declared on class " + info.className;
    }
    
    private String multipleTraceComponents(ClassTraceInfo info, List<FieldNode> fields, String componentType) {
        StringBuilder sb = new StringBuilder(
        	"WARNING: Multiple " + componentType + " fields declared on class " + info.className + ": ");

        boolean onFirst = true;
        for ( FieldNode fn : fields ) {
        	if ( !onFirst ) {
        		sb.append(", ");
        	} else {
        		onFirst = false;
        	}
            sb.append(fn.name);
        }

    	return sb.toString();
    }
    
    /**
     * Find or create the field that will hold the {@code TraceComponent} or {@code Logger} and create a class level annotation holding the field
     * name and descriptor.
     * 
     * @param info the collected class information
     */
    private void setupTraceStateObjectField(ClassTraceInfo info) {
        // Skip adding trace object field if it already exists
        AnnotationNode traceObjectAnnotation = info.getTraceObjectAnnotation();
        if (traceObjectAnnotation != null) {        	
            TraceObjectFieldAnnotationVisitor visitor = new TraceObjectFieldAnnotationVisitor();
            traceObjectAnnotation.accept(visitor);
            info.traceFieldNode = info.getField(visitor.getFieldName(), visitor.getFieldDescriptor());
            if (info.traceFieldNode != null) // Only return if matching field found
            	return;
        }

        // If a logger or trace component has been declared, use it.
        // Preference is given to the Liberty TraceComponent over the
        // WebSphere TraceComponent and either TraceComponent over a
        // Logger reference.  If none are declared, generate as a
        // synthetic.
        if (info.libertyTraceFieldNode != null) {
            info.traceFieldNode = info.libertyTraceFieldNode;
            info.traceFieldInitialized = info.libertyTraceFieldInitialized;
        } else if (info.websphereTraceFieldNode != null) {
            info.addWarning( noRuntimeInjectionForEJS(info) );
            info.traceFieldNode = info.websphereTraceFieldNode;
            info.traceFieldInitialized = info.websphereTraceFieldInitialized;
        } else if (info.loggerFieldNode != null) {
            info.addWarning( noRuntimeInjectionForJSR47(info) );
            info.traceFieldNode = info.loggerFieldNode;
            info.traceFieldInitialized = info.loggerFieldInitialized;
        } else if (getTraceType() == TraceType.LIBERTY) {
            // TODO: Check for an outer class and a declared field
            int access = (Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC);
            info.traceFieldNode = (FieldNode) info.classNode.visitField(access, getDefaultTraceComponentName(), LIBERTY_TRACE_COMPONENT_TYPE.getDescriptor(), null, null);
        }

        // Add the class annotation with the field name and descriptor
        if (info.traceFieldNode != null) {
            AnnotationVisitor av = info.classNode.visitAnnotation(TRACE_OBJECT_FIELD_TYPE.getDescriptor(), true);
            av.visit("fieldName", info.traceFieldNode.name);
            av.visit("fieldDesc", info.traceFieldNode.desc);
            av.visitEnd();
        }
    }

    private String noRuntimeInjectionForEJS(ClassTraceInfo info) {
    	return
    		"INFO: Runtime BCI is not supported for com.ibm.ejs.ras." +
    		"  Build-time BCI will be used for class " + info.className + "." + 
    		"  Consider using com.ibm.websphere.ras.";
    }
    
    private String noRuntimeInjectionForJSR47(ClassTraceInfo info) {
    	return
    		"INFO: Runtime BCI is not supported for JSR45 logging." +
    		"  Build-time BCI will be used for class " + info.className + "." + 
    		"  Consider using com.ibm.websphere.ras.";
    }

    /**
     * Check if the specified method has already been annotated as processed by the
     * trace injection framework.
     * 
     * @param methodNode the method to examine
     * 
     * @return true if a non-FFDC RAS method adapter processed the specified method
     */
    private boolean isMethodAlreadyInjectedAnnotationPresent(MethodNode methodNode) {
        AnnotationNode injectedTraceAnnotation = getInjectedTraceAnnotation(methodNode);
        AnnotationNode manualTraceAnnotation = getManualTraceAnnotation(methodNode);
        if (manualTraceAnnotation != null)
            return true;

        if (injectedTraceAnnotation != null) {
            InjectedTraceAnnotationVisitor itav = new InjectedTraceAnnotationVisitor();
            injectedTraceAnnotation.accept(itav);
            List<String> methodAdapters = itav.getMethodAdapters();
            if (methodAdapters.contains(LibertyTracingMethodAdapter.class.getName())) {
                return true;
            }
            if (methodAdapters.contains(WebSphereTrTracingMethodAdapter.class.getName())) {
                return true;
            }
            if (methodAdapters.contains(JSR47TracingMethodAdapter.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Analyze the class static initializer looking for initializations of the various Logger/TraceComponent fields.
     * <p>
     * When the next phase of instrumentation occurs, the code to get a {@code Logger} or a {@code TraceComponent} will be injected at the
     * beginning of the class static initializer if one is not already present.
     * 
     * @param info the collected class information
     */
    private void processExistingStaticInitializer(ClassTraceInfo info) {
        List<MethodNode> clinitMethods = info.getMethods("<clinit>");
        MethodNode staticInitializer = clinitMethods.isEmpty() ? null : clinitMethods.get(0);
        if (staticInitializer == null) {
            return;
        }
        if (isMethodAlreadyInjectedAnnotationPresent(staticInitializer)) {
            return;
        }

        Iterator<? extends AbstractInsnNode> instructionIterator = staticInitializer.instructions.iterator();
        
        while (instructionIterator.hasNext()) {
            AbstractInsnNode insnNode = instructionIterator.next();
            // Determine if a Logger/TraceComponent field is being initialized.
            if (insnNode.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fieldInsn = (FieldInsnNode) insnNode;
                if (fieldInsn.getOpcode() == Opcodes.PUTSTATIC) {
                    if (info.libertyTraceFieldNode != null && fieldInsn.name.equals(info.libertyTraceFieldNode.name)) {
                    	if (fieldInsn.getPrevious().getOpcode() == Opcodes.INVOKESTATIC) {
                    		info.libertyTraceFieldInitialized = true;
                    	}
                    }
                    if (info.websphereTraceFieldNode != null && fieldInsn.name.equals(info.websphereTraceFieldNode.name)) {
                        info.websphereTraceFieldInitialized = true;
                    }
                    if (info.loggerFieldNode != null && fieldInsn.name.equals(info.loggerFieldNode.name)) {
                        info.loggerFieldInitialized = true;
                    }
                }
            }
        }
    }

    /**
     * Emit a warning if any non-trivial methods are invoked by the
     * class's {@code toString()} implementation.
     * 
     * @param info Class information to examine.
     */
    private void validateToString(ClassTraceInfo info) {
    	MethodNode toStringMethod = info.getMethod("toString", "()Ljava/lang/String;");
    	if ( toStringMethod == null ) {
    		return;
    	}

    	Map<String, ClassNode> superClasses = null;

    	for ( AbstractInsnNode insnNode : toStringMethod.instructions ) {
    		if ( insnNode.getType() != AbstractInsnNode.METHOD_INSN) {
    			continue;
    		}
    		MethodInsnNode methodInsn = (MethodInsnNode) insnNode;

    		if (methodInsn.getOpcode() == Opcodes.INVOKESTATIC) {
    			continue; // Ignore static.
    		}
    		if (!methodInsn.owner.equals(info.classNode.name)) {
    			continue; // Ignore calls to other types.
    		}

    		MethodNode m = info.getMethod(methodInsn.name, methodInsn.desc);
    		if ( m != null ) {
    			if ( !isTrivial(m) ) {
    				info.addWarning( nonTrivial(info, methodInsn) );
    			} else {
        			// Invocation of trivial method ... not a problem.
    			}
    			
    			// Don't check any superclass implementations.  The reasoning
    			// is that if the local implementation is non-trivial, we have
    			// issued a warning, and if the local implementation is trivial,
    			// we can rely on that to override any non-trivial superclass
    			// implementations.
    			continue;
    		}

    		String superName = info.classNode.superName;
    		while ( superName != null ) {
    			if ( superName.startsWith("java/") || superName.startsWith("javax/") || superName.startsWith("jakarta/") ) {
    				break;
    			}

    			if ( superClasses == null ) {
    				superClasses = new HashMap<>(3);
    			}
    			ClassNode superNode = superClasses.computeIfAbsent(superName, className -> getClassNode(className) );
    			if ( superNode == null ) {
    				info.addWarning( uncheckedNotLoaded(info, methodInsn, superName) );
    				break;
    			}

    			m = getMethod(methodInsn.name, methodInsn.desc, superNode.methods);
    			if ( m != null ) {
    				break; // Important: The super name is left assigned.
    			}

    			superName = superNode.superName;
    		}

    		// We stop on the first superclass implementation that we found.
			// The reasoning is the same as with finding a local implementation:
			// If the first superclass implementation is non-trivial, we have
			// issued a warning, and if the implementation is trivial, we can
			// rely on that to override any non-trivial implementation in an
    		// even higher superclass.

    		if ( m == null ) {
				info.addWarning( uncheckedNotFound(info, methodInsn) );
    		} else if ( !isTrivial(m) ) {
    			info.addWarning( nonTrivial(info, methodInsn, superName) );
    		} else {
    			// Invocation of trivial method ... not a problem.
    		}
        }
    }

    private String uncheckedNotLoaded(ClassTraceInfo info, MethodInsnNode insnNode, String superName) {
    	return 
			"INFO: Unchecked call from " + info.className + ".toString" +
			" to " + " superclass method " + superName.replace('/', '.') + "." + insnNode.name + insnNode.desc + 
			": " + "The superclass could not be loaded.";
    }
    
    private String uncheckedNotFound(ClassTraceInfo info, MethodInsnNode insnNode) {
    	return 
    		"INFO: Unchecked call from " + info.className + ".toString" +
    		" to " + insnNode.name + insnNode.desc + ": " + "The method was not found.";
    }

    private String nonTrivial(ClassTraceInfo info, MethodInsnNode insnNode) {
    	return
    		"WARNING: Call from " + info.className + ".toString" + " to non-trivial method " + insnNode.name + insnNode.desc + "." +
    		"  This may result in an infinite loop.  Consider modifying the implementation to directly reference fields or to mark the called methods as trivial.";
    }

    private String nonTrivial(ClassTraceInfo info, MethodInsnNode insnNode, String superName) {
    	return
    		"WARNING: Call from " + info.className + ".toString" + " to non-trivial superclass method " + superName + "." + insnNode.name + insnNode.desc + "." +
    		"  This may result in an infinite loop.  Consider modifying the implementation to directly reference fields or to mark the called methods as trivial.";
    }
    
    /**
     * Read an input stream to populate a {@code ClassNode}.
     * 
     * @param inputStream the input stream containing the byte code
     * @param flags flags to pass to the class reader
     * 
     * @return the {@code ClassNode} or {@code null} if an error ocurred
     */
    private ClassNode getClassNode(InputStream inputStream, int flags) {
        ClassNode cn = new ClassNode();
        try {
            ClassReader reader = new ClassReader(inputStream);
            reader.accept(cn, flags);
            inputStream.close();
        } catch (IOException e) {
            cn = null;
        }
        return cn;
    }

    private ClassNode getClassNode(String className) {
    	InputStream inputStream = getClassInputStream(className);
    	if (inputStream == null) {
    		return null;
    	}
    	return getClassNode(inputStream, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
    }
    
    /**
     * Process the class and look for hard-coded entry/exit trace points.
     * Methods with hard-coded trace points will not be instrumented and
     * a warning will be issued.
     * 
     * @param info the collected class information
     */
    private void processManuallyTracedMethods(ClassTraceInfo info) {
        for (MethodNode mn : (List<MethodNode>) info.classNode.methods) {
            // Don't re-process methods that have already had trace injected
            if (isMethodAlreadyInjectedAnnotationPresent(mn)) {
                continue;
            }

            // Look through the method's instruction stream for well known entry/exit methods
            Iterator<? extends AbstractInsnNode> instructionIterator = mn.instructions.iterator();
            while (instructionIterator.hasNext()) {
                AbstractInsnNode insnNode = instructionIterator.next();

                // Look for calls to Tr.entry, Tr.exit, Logger.entering, Logger.exiting
                boolean manuallyTraced = false;
                if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insnNode;
                    String methodName = methodInsn.name;
                    if (methodInsn.owner.equals(LOGGER_TYPE.getInternalName())) {
                        manuallyTraced = (methodName.equals("entering") || methodName.equals("exiting"));
                    } else if (methodInsn.owner.equals(LIBERTY_TR_TYPE.getInternalName())) {
                        manuallyTraced = (methodName.equals("entry") || methodName.equals("exit"));
                    } else if (methodInsn.owner.equals(WEBSPHERE_TR_TYPE.getInternalName())) {
                        manuallyTraced = (methodName.equals("entry") || methodName.equals("exit"));
                    }
                }

                // Mark the manually traced method, and create a warning
                if (manuallyTraced) {
                    mn.visitAnnotation(MANUAL_TRACE_TYPE.getDescriptor(), true).visitEnd();

                    StringBuilder sb = new StringBuilder();
                    sb.append("WARNING: Hard coded entry/exit trace point found in ");
                    sb.append(info.className).append(".").append(mn.name).append(mn.desc);
                    sb.append(".  Skipping method.");
                    info.addWarning(sb.toString());
                    break;
                }
            }
        }
    }
}