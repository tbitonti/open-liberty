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

package com.ibm.ws.ras.instrument.internal.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;

import com.ibm.ws.ras.instrument.internal.introspect.TraceConfigPackageVisitor;
import com.ibm.ws.ras.instrument.internal.model.ClassInfo;
import com.ibm.ws.ras.instrument.internal.model.InstrumentationOptions;
import com.ibm.ws.ras.instrument.internal.model.PackageInfo;
import com.ibm.ws.ras.instrument.internal.model.TraceType;
import com.ibm.ws.ras.instrument.internal.xml.TraceConfigFileParser;

/**
 * Abstract base class that encapsulates some of the common file and argument
 * processing primitives needed by command line tools and ant tasks.
 */
public abstract class AbstractInstrumentation {

    protected static void transfer(InputStream input, OutputStream output, byte[] buffer) throws IOException {
    	int read = 0;
        while ( (read = input.read(buffer)) != -1 ) {
        	output.write(buffer, 0, read);
        }
    }

    protected static byte[] collect(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096 * 8];
        int read = 0;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    protected static String getClassName(byte[] classBytes) {
    	return (new ClassReader(classBytes)).getClassName();
    }

    //
    
    public AbstractInstrumentation() {
    	this.initParser();
    	this.initTraceType();
    	this.initInjectionOptions();
    }
    
    protected void initParser() {
    	this.configFileParser = null;        
    	this.instrumentationOptions = new InstrumentationOptions();
    }
    
    protected void initTraceType() {
    	// Default to do nothing.
    }
    
    protected void initInjectionOptions() {
    	// Default to do nothing
    }

	//

    protected boolean debug;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
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
    
    protected InstrumentationOptions instrumentationOptions;
    
    public InstrumentationOptions getInstrumentationOptions() {
        return instrumentationOptions;
    }

    public void setInstrumentationOptions(InstrumentationOptions options) {
        this.instrumentationOptions = options;

        handleOptions(options);
    }

    public boolean isPackageIncluded(String packageName) {
    	return getInstrumentationOptions().isPackageIncluded(packageName);
    }

    protected void handleOptions(InstrumentationOptions useOptions) {
    	setTraceType(useOptions.getTraceType());        
    }
    
    //

    protected TraceType traceType;

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }

    public TraceType getTraceType() {
        return traceType;
    }
    
    //

    protected Map<String, File> classFileMap;

    public Map<String, File> getClassFileMap() {
    	if ( classFileMap == null ) {
    		List<File> files = getClassFiles();

    		Map<String, File> map = new HashMap<String, File>(files.size());
    		for ( File classFile : files ) {
    			String className = classFile.getName();
    			if ( map.containsKey(className) ) {
    				System.err.println("Duplicate class name [ " + classFile.getAbsolutePath() + " ]");
    			} else {
    				map.put(className,  classFile);
    			}
    		}
    		
    		classFileMap = map;
    	}
    	return classFileMap;
    }

    public File lookupClassFile(String className) {
    	return getClassFileMap().get(className); 
    }

    //
    
    protected Map<String, Set<String>> jarClassesMap;
    protected Map<String, File> jarFileMap;
    
    public Map<String, Set<String>> getJarClassesMap() {
    	if ( jarClassesMap == null ) {
    		List<File> jarFiles = getJarFiles();
    		Map<String, Set<String>> map = new HashMap<String, Set<String>>(jarFiles.size());
    		for ( File rawJarFile : jarFiles ) {
    			Set<String> entryNames = new HashSet<>();
    			try ( ZipFile jarFile = new ZipFile(rawJarFile) ) {
    				Enumeration<? extends ZipEntry> entries = jarFile.entries();
    				while ( entries.hasMoreElements() ) {
    					ZipEntry jarEntry = entries.nextElement();
    					String entryName = jarEntry.getName();
    					if ( entryName.endsWith(".class") ) {
    						entryNames.add(entryName);
    					}
    				}
        		} catch ( IOException e ) {
        			System.err.println("Failure during read of archive [ " + rawJarFile.getAbsolutePath() + " ]");
        			e.printStackTrace(System.err);
        		}
    			map.put(rawJarFile.getPath(), entryNames);
    		}
    		jarClassesMap = map;
    	}
    	return jarClassesMap;
    }
    
    protected Set<String> lookupJarEntries(String jarName) {
    	return getJarClassesMap().get(jarName);
    }

    protected Map<String, File> getJarFileMap() {
    	if ( jarFileMap == null ) {
    		Map<String, File> map = new HashMap<>();

    		getJarFiles().forEach(jarFile -> {
    			lookupJarEntries(jarFile.getPath()).forEach( (className) -> {
    				if ( !map.containsKey(className) ) {
    					map.put(className, jarFile);
    				} else {
    					// Ignore: Priority is given to the first occurrence.
    				}
    			});
    		});
    		
    		jarFileMap = map;
    	}

    	return jarFileMap;
    }
    
    protected File lookupJarFile(String className) {
    	return getJarFileMap().get(className);
    }
    
    //

    protected List<File> classFiles;
    protected List<File> jarFiles;

    public List<File> getClassFiles() {
        return ( (classFiles == null) ? Collections.emptyList() : classFiles ); 
    }

    public void setClassFiles(List<File> classFiles) {
        this.classFiles = classFiles;
        this.classFileMap = null;
    }

    public List<File> getJarFiles() {
        return ( (jarFiles == null) ? Collections.emptyList() : jarFiles );
    }

    public void setJarFiles(List<File> jarFiles) {
        this.jarFiles = jarFiles;
        this.jarFileMap = null;
        this.jarClassesMap = null; 
    }

    protected List<File> getClassFiles(File root) {
        if ((root == null) || !root.isDirectory()) {
            return null;
        } else {
        	return select(root, (file) -> file.getName().endsWith(".class") );
        }
    }

    protected List<File> getJarFiles(File root) {
        if ( (root == null) || !root.isDirectory() ) {
            return null;
        } else {
        	return select(root, (file) -> file.getName().endsWith(".jar") ||
        			                      file.getName().endsWith(".zip") );
        }
    }

    protected List<File> select(File parent, Predicate<File> selector) {
    	if ( (parent == null) || !parent.isDirectory() ) {
    		return null;
    	}
    	List<File> selected = new ArrayList<>();
    	select(parent, selector, selected);
    	return selected;
    }
    
    protected void select(File parent, Predicate<File> selector, List<File> selected) {
        File[] children = parent.listFiles();
        if ( children == null ) {
        	return;
        }
        for ( File child : children ) {
            if (child.isDirectory()) {
                select(child, selector, selected);
            } else if (selector.test(child) ) {
                selected.add(child);
            } else {
            	// Ignore: Not selected
            }
        }
    }

    //
    
    /**
     * Answer an input stream across the bytes of a specified class.
     * 
     * Search the class files and jar files which have be previously
     * provided for a specified class.  If the class resource is located,
     * and has the correct internal name, answer an input stream across
     * the bytes of the class.
     * 
     * @param classInternalName The name of the class which is to be found.
     *     The name must have '/' instead of '.' and must not have the suffix
     *     ".class".
     * 
     * @return An input stream across the bytes of the specified class.  Null
     *     if the class was not found, or could not be read, or has a different
     *     name.
     */
    protected InputStream getClassInputStream(String classInternalName) {
        if ( (classInternalName == null) || "".equals(classInternalName)) {
            return null;
        }

        String classResourceName = classInternalName + ".class";

        File rawJarFile = lookupJarFile(classResourceName);
        if ( rawJarFile != null ) {
        	try ( ZipFile jarFile = new ZipFile(rawJarFile) ) {
        		ZipEntry zipEntry = jarFile.getEntry(classResourceName);
        		try ( InputStream entryInputStream = jarFile.getInputStream(zipEntry) ) {
        			byte[] classBytes = collect(entryInputStream);
        			String readerClassName = getClassName(classBytes);
        			if ( readerClassName.equals(classInternalName) ) {
        				return new ByteArrayInputStream(classBytes);
        			} else {
        				System.err.println("Strange class name [ " + readerClassName + " ] expecting [ " + classInternalName + " ]");
        			}
        		}
        	} catch (IOException ioe) {
        		System.err.println("Failed to read class [ " + classResourceName + " ]" +
                                   " from [ " + rawJarFile.getAbsolutePath() + " ]");
        		ioe.printStackTrace(System.err);
        		return null;
            }
        }
        
        String classFileName = classResourceName.substring(classResourceName.lastIndexOf('/') + 1);
        File classFile = lookupClassFile(classFileName);
        if ( classFile != null ) {
        	try ( FileInputStream fileInputStream = new FileInputStream(classFile) ) {
        		byte[] classBytes = collect(fileInputStream);
    			String readerClassName = getClassName(classBytes);
    			if ( readerClassName.equals(classInternalName) ) {
        			return new ByteArrayInputStream(classBytes);
        		} else {
    				System.err.println("Strange class name [ " + readerClassName + " ] expecting [ " + classInternalName + " ]");
        		}
        	} catch (IOException ioe) {
        		System.err.println("Failed to read class [ " + classFile.getAbsolutePath() + " ]");
        		ioe.printStackTrace(System.err);
        		return null;
        	}
        }

        return null;
    }

    //

    protected final Set<String> missingPackageInfo = new HashSet<>();
    protected final Map<String, PackageInfo> packageInfoMap = new HashMap<>();

    protected Set<String> getMissingPackageInfo() {
    	return missingPackageInfo;
    }

    protected Map<String, PackageInfo> getPackageInfoMap() {
        return packageInfoMap;
    }

    protected boolean isMissingPackageInfo(String packageName) {
    	return missingPackageInfo.contains(packageName);
    }

    protected void addPackageInfo(PackageInfo packageInfo) {
    	packageInfoMap.put(packageInfo.getInternalPackageName(), packageInfo);
    }

    protected void addMissingPackageInfo(String packageResourceName) {
    	missingPackageInfo.add(packageResourceName);    	
    }
    
    protected PackageInfo getPackageInfo(String packageName) {
        return packageInfoMap.get(packageName);
    }

    protected PackageInfo ensurePackageInfo(ClassLoader loader, String classResourceName) {
    	String internalPackageName = classResourceName.replaceAll("/[^/]+$", "");
    	PackageInfo packageInfo = getPackageInfo(internalPackageName);
    	if (packageInfo != null) {
    		return packageInfo;
    	}

    	String packageResourceName = internalPackageName + "/package-info.class";
    	if (isMissingPackageInfo(packageResourceName)) {
    		return null;
    	}
    	
    	try ( InputStream is = loader.getResourceAsStream(packageResourceName) ) {
    		if ( is == null ) {
    			packageInfo = null;
    		} else {
    			packageInfo = processPackageInfo(packageResourceName, is);
    		}
    	} catch ( IOException e ) {
    		packageInfo = null;
    		System.err.println("Failed to read package information [ " + packageResourceName + " ]");
    		e.printStackTrace(System.err);
    	}

    	if ( packageInfo == null ) {
    		addMissingPackageInfo(packageResourceName);
    	} else {
    		addPackageInfo(packageInfo);
    	}
    	
    	return packageInfo;
    }
    
    /**
     * Read trace annotations from package information class bytes.
     * 
     * @param packageResourceName The name of the package information resource.
     * @param inputStream Stream across the package information bytes.  Null if
     *     no class bytes were located for the package.
     * 
     * @return Package information which was obtained from the class bytes.  Null
     *     if the input stream is null, or if an error occurred while processing
     *     the package information bytes.
     */
    protected PackageInfo processPackageInfo(String packageResourceName, InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        try {
            ClassReader classReader = new ClassReader(inputStream);
            TraceConfigPackageVisitor packageVisitor = new TraceConfigPackageVisitor();
            classReader.accept(packageVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return packageVisitor.getPackageInfo();

        } catch ( Throwable th ) {
        	System.err.println("Unexpected error processing package information [ " + packageResourceName + " ]");
            th.printStackTrace(System.err);
            return null;
        }
    }

    //
    	
    public static boolean contains(String[] args, String value) {
    	for ( String arg : args ) {
    		if ( arg.equalsIgnoreCase(value) ) {
    			return true;
    		}
    	}
    	return false;
    }

    public static void printUsage(Runnable typesUsage, Runnable optionsUsage) {
    	System.out.println("Usage: " + StaticTraceInstrumentation.class.getName());
    	System.out.println("  [ --usage ] [ --help ]");
    	System.out.println("  [ --config <configFileName> ]");
    	typesUsage.run();
    	optionsUsage.run();
    	System.out.println("  [ --debug | -d ]");
    	System.out.println("  <target>+");
    	System.out.println("");
    }
    
    public static void printHelp(Runnable typeHelp, Runnable optionHelp) {
        System.out.println("Perform trace injection on one or more target classes.");
        System.out.println("");
    	System.out.println("  [ --usage ] [ --help ]");        
    	System.out.println("    Display usage information, or, display help information.  Do not");
    	System.out.println("    perform any instrumentation.");        
    	System.out.println("");        
    	System.out.println("  [ --config <configFileName> ]");
    	System.out.println("    Specify configuration options through a configuration file.");
    	System.out.println("");
    	typeHelp.run();
    	optionHelp.run();
    	System.out.println("  [ --debug | -d ]");
    	System.out.println("    Enable debugging output.");            	    	
    	System.out.println("");
    	System.out.println("  <target>+");
    	System.out.println("    One or more target directories, JAR or ZIP files, or classes.");
    	System.out.println("    (Any target which is not a directory, JAR or ZIP file, or class");
    	System.out.println("    is ignored.");
    	System.out.println("");
    }

    //  

    public void performInstrumentation(String[] args) throws IOException {
        processArguments(args);
        processPackageInfo();
        executeInstrumentation();
    }  

    public void processArguments(String[] args) throws IOException {

        List<File> useClassFiles = new ArrayList<File>();
        List<File> useJarFiles = new ArrayList<File>();

        String[] fileArgs = null;

        for ( int argNo = 0; argNo < args.length; argNo++ ) {
        	String arg = args[argNo].toLowerCase();

            if (arg.equals("--config")) {
            	if ( argNo == args.length - 1 ) {
            		throw new IllegalArgumentException("Trace injection: '--config' specified with no file argument");
            	}
            	String configFileName = args[++argNo];
            	File configFile = new File(configFileName);
            	if ( !configFile.exists() ) {
            		throw new IllegalArgumentException("Trace injection: Configuration file does not exist [ " + configFile.getAbsolutePath() + " ]");
            	}
            	setConfigFile(configFile); 

            } else if ( handleTraceType(arg) ) {
            	// Handled as a trace type
            } else if ( handleInjectionOption(arg) ) {
            	// Handled as a trace injection specific option

            } else if ( arg.equals("--debug") || arg.equals("-d") ) {
                setDebug(true);
                System.err.println("Trace injection: Debugging is enabled.");

            } else {
                fileArgs = new String[args.length - argNo];
                System.arraycopy(args, argNo, fileArgs, 0, fileArgs.length);
                break;
            }
        }

        if ( (fileArgs == null) || (fileArgs.length == 0) ) {
            throw new IllegalArgumentException("Trace injection: No targets were specified.");
        }

        for ( String fileArg : fileArgs ) {
            File f = new File(fileArg);
            if (!f.exists()) {
                throw new IllegalArgumentException("Trace injection: Target does not exist [ " + f.getAbsolutePath() + " ]");
            }

            String fName = f.getName();
            if (f.isDirectory()) {
                useClassFiles.addAll(getClassFiles(f));
            } else if (fName.endsWith(".class")) {
                useClassFiles.add(f);
            } else if ( fName.endsWith(".jar") || fName.endsWith(".zip") ) {
        		useJarFiles.add(f);
            } else {
                System.err.println("Trace injection: Unexpected target [ " + f.getAbsolutePath() + " ]: Ignoring");
            }
        }

        setClassFiles(useClassFiles);
        setJarFiles(useJarFiles);
    }    

    protected boolean handleTraceType(String arg) {
    	return false;
    }
    
    protected boolean handleInjectionOption(String arg) {
    	return false;
    }

    //
    
    public void processPackageInfo() throws IOException {
    	for ( File classFile : getClassFiles() ) {
    		if (classFile.getName().equals("package-info.class")) {
                try ( InputStream inputStream = new FileInputStream(classFile) ) {
                	processPackageInfo(classFile.getPath(), inputStream);
                }
            }
    	}

        for ( File rawJarFile : getJarFiles() ) {
            try ( ZipFile zipFile = new ZipFile(rawJarFile) ) {
            	Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            	while (zipEntries.hasMoreElements()) {
            		ZipEntry zipEntry = zipEntries.nextElement();
            		if (zipEntry.getName().endsWith("/package-info.class")) {
            			try ( InputStream inputStream = zipFile.getInputStream(zipEntry) ) {
            				processPackageInfo(zipEntry.getName(), inputStream);
            			}
            		}
            	}
            }
        }
    }    
    
    //

    protected List<Throwable> errors = new ArrayList<Throwable>();
    
    public List<Throwable> getErrors() {
        return errors;
    }

    /**
     * Instrument the classes and jar files.
     */
    public void executeInstrumentation() throws IOException {
        errors.clear();

        getClassFiles().forEach( (classFile) -> {
            try {
                instrumentClassFile(classFile);
            } catch (Exception e) {
            	System.err.println("Unexpected instrumentation failure of [ " + classFile.getAbsolutePath() + " ]");
                e.printStackTrace(System.err);
                errors.add(e);
            }
        });

        getJarFiles().forEach((jarFile) -> {
            try {
                instrumentZipFile(jarFile);
            } catch (Exception e) {
            	System.err.println("Unexpected instrumentation failure of [ " + jarFile.getAbsolutePath() + " ]");            	
                e.printStackTrace(System.err);
                errors.add(e);
            }
        });

        if ( !errors.isEmpty() ) {
            throw new IOException("Unexpected instrumentation failure.  [ " + errors.size() + " ] errors occurred", errors.get(0));
        }
    }

    protected void verifyReadWrite(File targetFile) throws IOException {
        if (!targetFile.canRead()) {
            throw new IOException(targetFile.getAbsolutePath() + " cannot be read");
        } else if (!targetFile.canWrite()) {
            throw new IOException(targetFile.getAbsolutePath() + " cannot be written");
        }
    }

    public void instrumentClassFile(File classFile) throws IOException {
    	verifyReadWrite(classFile);
    	
        byte[] classBytes;
    	try ( InputStream inputStream = new FileInputStream(classFile) ) {
    		classBytes = transform(inputStream);
    	}

    	if (classBytes != null) {
        	try ( OutputStream outputStream = new FileOutputStream(classFile) ) {
        		outputStream.write(classBytes);
        	}
        }
    }
    
    public void instrumentZipFile(File rawInputFile) throws IOException {
    	verifyReadWrite(rawInputFile);

        File rawOutputFile = File.createTempFile(rawInputFile.getName(), null, rawInputFile.getParentFile());

        IOException boundException = null;
        try {
        	instrumentZipFile(rawInputFile, rawOutputFile);
        } catch ( IOException e ) {
        	boundException = e;
        }

        if ( boundException != null ) {
        	try {
        		Files.move(rawOutputFile.toPath(), rawInputFile.toPath(),
        				StandardCopyOption.REPLACE_EXISTING,
        				StandardCopyOption.ATOMIC_MOVE);
        	} catch ( IOException e ) {
        		boundException = e;
        	}
        }
        
        if ( boundException != null ) {
        	rawOutputFile.delete();
        	throw boundException;
        }
    }
    
    public void instrumentZipFile(File inputZipFile, File outputZipFile) throws IOException {
        try ( ZipFile zipFile = new ZipFile(inputZipFile); 
        	  OutputStream inputOutputStream = new FileOutputStream(inputZipFile);
              ZipOutputStream zipOutputStream = new ZipOutputStream(inputOutputStream) ) {

        	byte[] buffer = new byte[1024 * 32];

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                InputStream entryInputStream = zipFile.getInputStream(entry);

                ZipEntry newEntry;
                if (!entry.getName().endsWith(".class")) {
                	newEntry = entry;
                } else {
                	byte[] classBytes = transform(entryInputStream);
                    if (classBytes == null) {
                    	newEntry = entry;
                        entryInputStream = zipFile.getInputStream(entry);
                    } else {
                    	newEntry = new ZipEntry(entry.getName());
                    	newEntry.setTime(System.currentTimeMillis());
                    	newEntry.setComment(entry.getComment());
                    	newEntry.setExtra(entry.getExtra());
                    	newEntry.setSize(classBytes.length);
                    	entryInputStream = new ByteArrayInputStream(classBytes);
                    }
                }

                zipOutputStream.putNextEntry(newEntry);
                transfer(entryInputStream, zipOutputStream, buffer);
            }

            zipOutputStream.finish();

        } catch (IOException ioe) {
        	throw ioe;
        } catch (Throwable t) {
            throw new IOException("Unexpected instrumentation failure", t);
        }
    }

    /**
     * Perform trace injection on a target class.
     * 
     * @param inputStream Stream containing the bytes of the target class.
     * 
     * @return The transformed bytes.  Null if the class was not transformed.
     *     (One case of this is that the class was already transformed.)
     * 
     * @throws IOException If the class bytes could not be read.
     */
    protected abstract byte[] transform(InputStream inputStream) throws IOException;
}
