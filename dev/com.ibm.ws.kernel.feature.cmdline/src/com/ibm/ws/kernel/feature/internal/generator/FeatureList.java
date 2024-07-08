/*******************************************************************************
 * Copyright (c) 2012, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.feature.internal.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.stream.XMLStreamException;

import org.apache.aries.util.manifest.ManifestHeaderProcessor;
import org.apache.aries.util.manifest.ManifestHeaderProcessor.GenericMetadata;
import org.apache.aries.util.manifest.ManifestHeaderProcessor.NameValuePair;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

import com.ibm.ws.config.xml.internal.XMLConfigConstants;
import com.ibm.ws.config.xml.internal.schema.MetaTypeInformationSpecification;
import com.ibm.ws.config.xml.internal.schema.ObjectClassDefinitionSpecification;
import com.ibm.ws.config.xml.internal.schema.SchemaMetaTypeParser;
import com.ibm.ws.kernel.boot.cmdline.Utils;
import com.ibm.ws.kernel.feature.ProcessType;
import com.ibm.ws.kernel.feature.Visibility;
import com.ibm.ws.kernel.feature.internal.cmdline.APIType;
import com.ibm.ws.kernel.feature.internal.generator.FeatureListOptions.ReturnCode;
import com.ibm.ws.kernel.feature.internal.subsystem.FeatureDefinitionUtils;
import com.ibm.ws.kernel.feature.provisioning.FeatureResource;
import com.ibm.ws.kernel.feature.provisioning.HeaderElementDefinition;
import com.ibm.ws.kernel.feature.provisioning.ProvisioningFeatureDefinition;
import com.ibm.ws.kernel.feature.provisioning.SubsystemContentType;
import com.ibm.ws.kernel.provisioning.BundleRepositoryRegistry;
import com.ibm.ws.kernel.provisioning.ContentBasedLocalBundleRepository;
import com.ibm.wsspi.kernel.service.utils.PathUtils;

//@formatter:off
public class FeatureList {
	/** Control parameter: Tells if java versions are to be included in the feature report. */
    protected final static boolean writingJavaVersion = Boolean.getBoolean("ibm.javaVersion");

    /** List of java version properties.  Populated only if {@link #writingJavaVersion} is enabled. */
    private static final List<Map<String, Object>> javaVersions;
    
    /**
     * Table of Java capabilities.  Keys are JavaSE version names.  Values are parsed OSGi
     * capabilities, obtained from {@link ManifestHeaderProcessor#parseCapabilityString(String)}.
     */
    private static final Map<String, Collection<GenericMetadata>> eeToCapability = new HashMap<>();

	protected static List<GenericMetadata> toCapabilityRequirements(String[] values) {
		List<GenericMetadata> metadata = new ArrayList<>();
		for (String val : values) {
			Collection<GenericMetadata> data = eeToCapability.get(val);
			if (data != null) {
				metadata.addAll(data);
			}
		}
		return metadata;
	}		
	    
    private static final String[] allJavaVersions =
    	new String[] { "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9",
    			       "1.8", "1.7", "1.6", "1.5", "1.4", "1.3", "1.2", "1.1" };
    
    static {
    	List<Map<String, Object>> useJVMs;
        Map<String, Collection<GenericMetadata>> capabilities;
        
        if (writingJavaVersion) {
        	useJVMs = new ArrayList<>(6);

            useJVMs.add( javaVersion(7, allJavaVersions) );
            useJVMs.add( javaVersion(8, allJavaVersions) );
            useJVMs.add( javaVersion(11, allJavaVersions) );
            useJVMs.add( javaVersion(17, allJavaVersions) );
            useJVMs.add( javaVersion(21, allJavaVersions) );
            useJVMs.add( javaVersion(22, allJavaVersions) );

            capabilities = new HashMap<>(11);

            List<GenericMetadata> requireEE7 = parseCapability("osgi.ee; filter:=\"(&(osgi.ee=JavaSE)(version=1.7))\"");
            List<GenericMetadata> requireEE8 = parseCapability("osgi.ee; filter:=\"(&(osgi.ee=JavaSE)(version=1.8))\""); 
            List<GenericMetadata> requireEE11 = parseCapability("osgi.ee; filter:=\"(&(osgi.ee=JavaSE)(version=11))\""); 
            List<GenericMetadata> requireEE17 = parseCapability("osgi.ee; filter:=\"(&(osgi.ee=JavaSE)(version=17))\""); 
            List<GenericMetadata> requireEE21 = parseCapability("osgi.ee; filter:=\"(&(osgi.ee=JavaSE)(version=21))\""); 
            List<GenericMetadata> requireEE22 = parseCapability("osgi.ee; filter:=\"(&(osgi.ee=JavaSE)(version=22))\"");             

            // Java7 is the minimum supported.  Any requirement on pre-Java7 becomes a requirement
            // on Java7.

            capabilities.put("J2SE-1.2", requireEE7);
            capabilities.put("J2SE-1.3", requireEE7);
            capabilities.put("J2SE-1.4", requireEE7);
            capabilities.put("JavaSE-1.5", requireEE7);
            capabilities.put("JavaSE-1.6", requireEE7);
            capabilities.put("JavaSE-1.7", requireEE7);
            capabilities.put("JavaSE-1.8", requireEE8);
            capabilities.put("JavaSE-11", requireEE11);
            capabilities.put("JavaSE-17", requireEE17);
            capabilities.put("JavaSE-21", requireEE21);
            capabilities.put("JavaSE-22", requireEE22);
            
        } else {
        	useJVMs = Collections.emptyList();
            capabilities = Collections.emptyMap();
        }

        javaVersions = jvms;
        eeToCapability = capabilities;        
    }

    private static List<GenericMetadata> parseCapability(String capability) {
    	return ManifestHeaderProcessor.parseCapabilityString(capability);
    }
    
    /**
     * Create a mapping which represents a java version and add that to the list of
     * versions.
     * 
     * Each element has a base version name, a list of compatible versions, and the
     * version name (the base version name plus the major and minor version values).
     * 
     * @param javaVersions Storage for the version data.
     * @param version The actual JVM version.
     * @param versions Other JVM versions which are compatible with the JVM version.
     */
    private static Map<String, Object> javaVersion(int versionNo, String[] allVersions) {
    	// Store all compatible versions, which includes the JVM version, and all
    	// additional versions.

        List<Version> compatibleVersions = new ArrayList<>(versionNo);

        // Versions are stored from highest to lowest, and are one based.
        //
        // Select from the version number to the lowest:
        //
        //  0  1  2  3  4  5  6  7  8  9
        // 10, 9, 8,[7, 6, 5, 4, 3, 2, 1]
        // n = 0; vNo = 7; n - (vNo - 1) == 3

    	int numVersions = allVersions.size();        
        for ( int nextVersion = numVersions - (versionNo - 1); nextVersion < numVersions; nextVersion++ ) {
            compatibleVersions.add( new Version( allVersions[nextVersion] ) );
        }
        Version jvmVersion = compatibleVersions.get(0); 

        Map<String, Object> jvm = new HashMap<>();
        jvm.put("osgi.ee", "JavaSE"); // TODO: Can we use "JavaSE" for the pre-Java5 JVMs?
        jvm.put("version", compatibleVersions);
        jvm.put("bree", "JavaSE-" + jvmVersion.getMajor() + "." + jvmVersion.getMinor());

        return jvm;
    }

    /**
     * Lookup the java version name stored in a JVM table.
     * This is the name stored under key "bree".
     * 
     * @param version A JVM table.
     * 
     * @return The java version name stored in the table.
     */
	protected static String toJavaEE(Map<String, Object> version) {
		return (String) version.get("bree");
	}

    //

    private static final File installDir = computeInstallDir();
    private static final String installAbsPath = ((installDir == null ) ? null : installDir.getAbsolutePath());

    protected static String relativeToInstall(String absPath) {
    	String relPath;
    	if ((installAbsPath != null) && absPath.startsWith(installAbsPath)) {
    		int installDirLen = installAbsPath.length() + 1;
    		relPath = PathUtils.slashify(absPath.substring(installDirLen));
    	} else {
    		relPath = PathUtils.slashify(absPath);
    	}
    	return relPath;
    }

    /**
     * Determine the server installation directory, usually, from the system property
     * "wlp.install.dir".
     * 
     * If the system property is not set, try to set the installation directory
     * using the location of the code source of the protection domain of this
     * class.  This class is expected to be in the server "lib" folder, meaning
     * the installation directory is set as the parent of this folder.
     * 
     * @return The server installation directory.  Null not available.
     */    
    private static File computeInstallDir() {
    	String installDirProp = System.getProperty("wlp.install.dir");
    	if (installDirProp != null) {
    		return new File(installDirProp);
    	}
    	
    	// No property ... try to get a location from the feature list class.

    	URL url = FeatureList.class.getProtectionDomain().getCodeSource().getLocation();
    	if (!url.getProtocol().equals("file")) {
    		return null;
    	}

    	try {
    		if (url.getAuthority() != null) {
    			url = new URL("file://" + url.toString().substring("file:".length()));
    		}
    		
    		// The class is expected to live in "wlp/lib/someJar.jar".
    		//
    		// Feature list is invoked using 'ws-featurelist.jar", in "wlp/bin/tools/someJar.jar".
    		// However, that JAR redirects to library jars.
    		//
    		// TODO: This doesn't work for testing.  Usually, however, the environment variable is set.

    		File f = new File(url.toURI());
    		return f.getParentFile();

    	} catch (MalformedURLException e) {
    		// Bad code source location, or bad file name.  Both
    		// are unexpected.
    		// Not sure we can get here so ignore.
    	} catch (URISyntaxException e) {
    		// Bad code source location, or bad file name.  Both
    		// are unexpected.    		
    		// Not sure we can get here so ignore.
    	}
    	return null;
    }

    //

    /**
     * Control parameter: Is the current build a GA build?
     * 
     * GA builds do not include beta features in the features report.
     */
    private static final boolean gaBuild = computeGABuild();
    
    /**
     * Tell if a GA report is to be generated.  GA reports do not include
     * beta features.
     *
     * Answer false if the product major version is higher than 2012.
     * 
     * Answer true if the product major version is less than or equal to 2012,
     * or is not available.
     *
     * @return True or false, telling if a GA report is to be generated.
     */
    private static boolean computeGABuild() {
    	File serverPropsFile = new File(serverInstallDir, "lib/versions/WebSphereApplicationServer.properties");
        Properties serverProps = readProperties(serverPropsFile);
        if ( serverProps == null ) {
        	return true; // No props, or a read failure.
        }
        
        String v = serverProps.getProperty("com.ibm.websphere.productVersion");
        if (v == null) {
        	return true; // No product version!
        }

        int index = v.indexOf('.');
        if (index == -1) {
        	return true; // Incorrect version format.
        }

        try {
        	int major = Integer.parseInt(v.substring(0, index));
        	return (major <= 2012); // TODO: What is special about this value?
        } catch (NumberFormatException e) {
        	return true; // Bad version value.
        }
    }
    
    private static Properties readProperties(File propsFile) {
    	return AccessController.doPrivileged(new PrivilegedAction<Properties>() {
            @Override
            public Properties run() {
                try {
                    FileInputStream propsInput = new FileInputStream(propsFile);
                    try {
                    	Reader propsReader = new InputStreamReader(propsInput, "UTF-8");
                    	try {
                    		Properties props = new Properties();
                    		props.load(propsReader);
                    		return props;
                    	} finally {
                    		propsReader.close();
                    	}
                    } finally {
                    	propsInput.close();
                    }
                } catch (IOException e) {
                    // Ignore and answer null.
                	// TODO: Should this always be ignored? 
                }
                return null;
            }
        });
    }
    
    //

    /**
     * Create a new feature list widget.
     *
     * The options provide the produce name, locale, and may be set with the writer
     * exit code.
     * 
     * @param options Writer options.
     * @param fds The list of features associated with a particular product.
     * @param coreFDs The list of features associated with the core product.  Null
     *     if the product is the core product.
	 * @param util Utilities widget. Needed by the feature list writer. 
     */
    public FeatureList(FeatureListOptions options,
                       Map<String, ProvisioningFeatureDefinition> fds,
                       Map<String, ProvisioningFeatureDefinition> coreFDs,
                       FeatureListUtils utils) {

        this.options = options;
        this.productName = options.getProductName();
        this.locale = options.getLocale();
		this.includeInternal = options.getIncludeInternals();

        this.features = fds;
        this.coreFeatures = coreFDs;
        this.featureListUtils = utils;

        // Initialize placeholders for the default repositories ("", and "usr"),
        // we do not want to use caches, and we can't use Tr.
        BundleRepositoryRegistry.initializeDefaults(null, false);
        
        int numFeatures = this.features.size() + ((this.coreFeatures != null) ? this.coreFeatures.size() : 0);

        this.javaVersionsByFeature = new HashMap<>(numFeatures);
        // Don't know the size: need the count of all bundles of all features.        
        this.javaVersionsByBundle = new HashMap<>();

        this.allJarFeatureData = new HashMap<>(numFeatures);
		this.allBundleFeatureData = new HashMap<>(numFeatures);
		
        // Don't know the size: This has data for all bundle and jar constituents.
        this.allConstituentData = new HashMap<>();		
    }

    // Options access ...
    
    private final FeatureListOptions options;
    private final String productName;
    private final Locale locale;
    private final boolean includeInternal;

    private void setReturnCode(int code) {
        options.setReturnCode(code);
    }

    // Features access ...

    private final Map<String, ProvisioningFeatureDefinition> features;
    private final Map<String, ProvisioningFeatureDefinition> coreFeatures;

    /**
     * Retrieve a feature, first looking at the features collection, then looking at
     * core features.
     * 
     * @param symbolicName The symbolic name of the feature which is to be retrieved.
     * 
     * @return The feature having the specified symbolic name.
     */
    private ProvisioningFeatureDefinition getFeature(String symbolicName) {
    	ProvisioningFeatureDefinition feature = features.get(symbolicName);

    	// For extensions, also look for core features.
    	if ((feature == null) && (coreFeatures != null)) {
    		feature = coreFeatures.get(symbolicName);
    	}
    	
    	return feature;
	}

	private static ContentBasedLocalBundleRepository getBundleRepo(String enabledFeatureName) {
		if (enabledFeatureName == null) {
			enabledFeatureName = "";
		}
		ContentBasedLocalBundleRepository cbr;
		if (enabledFeatureName.startsWith("usr:")) {
			cbr = BundleRepositoryRegistry.getUsrInstallBundleRepository();
		} else {
			if (enabledFeatureName.contains(":")) {
				cbr = mfp.getBundleRepository(enabledFeatureName.substring(0, enabledFeatureName.indexOf(":")), null);
			} else {
				cbr = BundleRepositoryRegistry.getInstallBundleRepository();
			}
		}
		return cbr;
	}
    
    /**
     * Main API: Using the supplied manifest processor, create a XML report
     * of current features.
     * 
     * Handle write exceptions as runtime exceptions, setting the options return
     * code to {@link ReturnCode#RUNTIME_EXCEPTION}.
     *
     * @param mfp A manifest processor.
     */
    public void writeFeatureList(ManifestFileProcessor mfp) {
    	try {
    		FeatureWriter featureWriter = new FeatureListWriter(mfp);
    		featureWriter.writeFeatures();
    	} catch (Exception e) {
            setReturnCode(ReturnCode.RUNTIME_EXCEPTION);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extended feature list writer.  This encapsulates additional state.
     * 
     * TODO: This is a partial fix for the feature list state / object structure.
     * The top level static values, class FileList, and class FeatureWriter might
     * be better collapsed into a single class.
     */
	private class FeatureWriter {
		private final ManifestFileProcessor mfp;
		
		public FeatureWriter(ManifestFileProcessor mfp) throws IOException {
			this.mfp = mfp;
			this.writer = new FeatureListWriter(featureListUtils);
		}

		public void writeFeatures() {
			for ( ProvisioningFeatureDefinition fd : features.values() ) {
				writeFeature(fd);
			}
		}

		/**
		 * Write a single feature.
		 * 
		 * Features are of five base types: Public, Protected, Kernel, Auto, and Private.
		 * 
		 * Externals are not shown for protected and private features.
		 * 
		 * @param fd The feature which is to be written
		 *
		 * @throws IOException Thrown in case of a write failure.
		 * @throws XMLStreamException Thrown in case of an XML write failure.
		 */
		private void writeFeature(ProvisioningFeatureDefinition fd) throws IOException, XMLStreamException {
			boolean isPublic = false;
			boolean isPrivate = false;
			boolean showExternals = true;

			String symbolicName = fd.getSymbolicName();
			
			Collection<HeaderElementDefinition> autoFeature =
			    fd.getHeaderElements(FeatureDefinitionUtils.IBM_PROVISION_CAPABILITY);

			if ( fd.getVisibility() == Visibility.PUBLIC ) {
				String featureName = fd.getIbmShortName();
				if ( featureName == null ) {
					featureName = symbolicName;
				}
				startFeature("feature", featureName);
				isPublic = true;
			} else if ( fd.getVisibility() == Visibility.PROTECTED ) {
				startFeature("protectedFeature");
				showExternals = false;
			} else if ( fd.isKernel() ) {
				startFeature("kernelFeature");
			} else if ( !autoFeature.isEmpty() ) {
				startFeature("autoFeature");
			} else {
				startFeature("privateFeature");
				showExternals = false;
				isPrivate = true;
			}

			writeElement("symbolicName", symbolicName);

			if ( fd.isSingleton() ) {
				writeElement("singleton", "true");
			}

			// WARNING!! Special case for client only features
			if ( FeatureDefinitionUtils.ALLOWED_ON_CLIENT_ONLY_FEATURES.contains(symbolicName) ) {
				writeElement("processType", ProcessType.CLIENT.name());
			}

			if ( !isPrivate ) {
				String name = fd.getHeader("Subsystem-Name", locale);
				if ( name != null ) {
					writeElement("displayName", name);
				}

				if ( fd.isSuperseded() ) {
					writeElement("superseded", "true");
				}
				String supersededBy = fd.getSupersededBy();
				if ( supersededBy != null ) {
					String[] supersededByList = supersededBy.split(",");
					for ( String supersedByFeature : supersededByList ) {
						supersedByFeature = supersedByFeature.trim();
						if ( !supersedByFeature.isEmpty() ) {
							writeElement("supersededBy", supersedByFeature);
						}
					}
				}

				String desc = fd.getHeader("Subsystem-Description", locale);				
				if ( desc != null ) {
					writeElement("description", desc);
				}

				if ( writingJavaVersion ) {
					List<Map<String, Object>> javaVersions = getJavaVersion(fd);
					for ( Map<String, Object> version : javaVersions ) {
						writeElement("javaVersion", toJavaEE(version));
					}
				}

				String categories = fd.getHeader("Subsystem-Category");
				if ( categories != null ) {
					for ( String category : categories.split(",") ) {
						writeElement("category", category);
					}
				}
			}

			for ( HeaderElementDefinition element : autoFeature ) {
				String filter = element.getDirectives().get("filter");
				writeElement("autoProvision", filter);
			}

			if ( !isPrivate ) {
				writeConstituents(fd, isPublic, showExternals);
			}

			writeIncludes(fd);
			
			endFeature();
		}

		private void writeConstituents(ProvisioningFeatureDefinition fd,
			                           boolean isPublic,
			                           boolean showExternals) {
			
			// Walk the feature constituents, gathering enabled feature names,
			// API jars and their java ranges, and SPI jars and their java ranges.
			//
			// Enabled features are written only for public features.
			// API jars are written only if writing externals is enabled. 

			ContentBasedLocalBundleRepository cbr = getBundleRepo(fd.getFeatureName());				
			
			Set<String> enabledFeatureNames = (isPublic ? new TreeSet<>() : null);
			Set<File> bundles = new TreeSet<>();
			Map<File, Map<String, String>> apiJars = (showExternals ? new TreeMap<>() : null);
			Map<File, Map<String, String>> spiJars = new TreeMap<>();

			collectConstituents(fd, cbr, enabledFeatureNames, bundles, apiJars, spiJars);

			if (enabledFeatureNames != null) {
				for (String enabledFeatureName : enabledFeatureNames) {
					writeElement("enables", enabledFeatureName);
				}
			}

			if ( apiJars != null ) {
				writeApiSpiJars(apiJars, "apiJar");
			}
			writeApiSpiJars(spiJars, "spiJar");

			String apiPkgs = fd.getHeader("IBM-API-Package");
			String spiPkgs = fd.getHeader("IBM-SPI-Package");
			
			if ( apiPkgs != null ) {
				writeApiSpiPkgs(apiPkgs, "apiPackage");
			}
			if ( spiPkgs != null ) {
				writeApiSpiPkgs(spiPkgs, "spiPackage");
			}

			if (showExternals) {
				Set<String> elements = collectExternals(fd);
				for (String configElement : elements) {
					writeElement("configElement", configElement);
				}
			}
		}
		
		/**
		 * Write text elements for API or SPI jars.
		 * 
		 * @param jars Tables of jars and their java ranges.
		 * @param elementName The jar type, API or SPI.
		 * 
		 * @throws IOException Thrown in case of a write failure.
		 * @throws XMLStreamException Thrown in case of an XML write failure.
		 */
		private void writeApiSpiJars(Map<File, Map<String, String>> jars, String elementName)
			throws IOException, XMLStreamException {
			
			for (Entry<File, Map<String, String>> jarEntry : jars.entrySet()) {
				File jarFile = jarEntry.getKey();
				Map<String,String> jarAttr = jarEntry.getValue();

				String jarRelPath = relativeToInstall(jarFile.getAbsolutePath());

				writeElement(elementName, jarRelPath, jarAttr);
			}
		}
		
		/**
		 * Write text elements for the API SPI packages.
		 * 
		 * Do nothing if the package export string is null.
		 * 
		 * Ignore internal exports.
		 *
		 * @param pkgs Packages export string.
		 * @param pkgType The type of packages which are being written.
		 * 
		 * @throws IOException Thrown in case of a write failure.
		 * @throws XMLStreamException Thrown in case of an XML write failure.
		 */
		private void writeApiSpiPkgs(String pkgs, String pkgType)
			throws IOException, XMLStreamException {

			List<NameValuePair> pkgExports = ManifestHeaderProcessor.parseExportString(pkgs);
			for (NameValuePair pkgExport : pkgExports) {
				String pkgName = pkgExport.getName();
				Map<String, String> attrs = pkgExport.getAttributes();
				if (!"internal".equals(attrs.get("type"))) {
					writeElement(pkgType, pkgName, attrs);
				}
			}
		}
		
		private void writeIncludes(ProvisioningFeatureDefinition fd) {
			for (FeatureResource constituentResource : fd.getConstituents(SubsystemContentType.FEATURE_TYPE)) {
				String symbolicName = constituentResource.getSymbolicName();
				ProvisioningFeatureDefinition constituentFeature = getFeature(symbolicName);

				String shortName = null;
				if (constituentFeature != null) {
					if (constituentFeature.getVisibility() == Visibility.PUBLIC) {
						shortName = constituentFeature.getFeatureName();
					}
				}

				writeInclude(symbolicName, constituentResource.getTolerates(), shortName);
			}
		}

		//

		private void collectConstituents(ProvisioningFeatureDefinition feature,
					                     ContentBasedLocalBundleRepository cbr,
					                     Set<String> enabledFeatureNames,
					                     Set<File> bundles,
					                     Map<File, Map<String, String>> apiJars,
					                     Map<File, Map<String, String>> spiJars) {

			if ( enabledFeatureNames != null ) {
				for (FeatureResource fr : feature.getConstituents(SubsystemContentType.FEATURE_TYPE)) {
					ProvisioningFeatureDefinition constituentFeature = getFeature(fr.getSymbolicName());
					if (constituentFeature == null) {
						continue;
					}

					if (constituentFeature.getVisibility() == Visibility.PUBLIC) {
						enabledFeatureNames.add(constituentFeature.getFeatureName());

					} else {
						collectConstituents(constituentFeature, cbr,
								            enabledFeatureNames,
								            bundles,
								            (((apiJars != null) && APIType.API.matches(fr)) ? apiJars : null),
								            (((spiJars != null) && APIType.SPI.matches(fr)) ? spiJars : null));
					}
				}
			}
			
			FeatureData data = getJarFeatureData(feature, cbr);
			data.contributeTo(null, apiJars, spiJars);
			
			FeatureData data = getBundleFeatureData(feature, cbr);
			data.contributeTo(bundles, apiJars, spiJars);
		}

		//

		private Set<String> collectExternals(ProvisioningFeatureDefinition fd) {
			Set<String> configElements = new HashSet<>();

			// if we see the kernel feature we want to add include and variable.
			if ("com.ibm.websphere.appserver.kernelCore-1.0".equals(fd.getSymbolicName())) {
				elements.add("include");
				elements.add("variable");
			}

			// TODO: Should this use the default locale?
			SchemaMetaTypeParser parser =
				new SchemaMetaTypeParser(Locale.getDefault(), new ArrayList<>(bundles), productName);

			
			for (MetaTypeInformationSpecification spec : parser.getMetatypeInformation()) {
				for (ObjectClassDefinitionSpecification ocds : spec.getObjectClassSpecifications()) {
					if (!includeInternal && "internal".equals(ocds.getName())) {
						continue;
					}
					
					if (!ocds.getExtensionUris().contains(XMLConfigConstants.METATYPE_EXTENSION_URI)) {
						continue;
					}
					Map<String, String> attribs = ocds.getExtensionAttributes(XMLConfigConstants.METATYPE_EXTENSION_URI);
					if (attribs == null) {
						continue;
					}
						
					String isBeta = attribs.get("beta");
					if (gaBuild && "true".equals(isBeta)) {
						continue;
					}
						
					String alias = attribs.get("alias");
					if ((alias != null) && !attribs.containsKey("childAlias")) {
						configElements.add(alias);
					}
				}
			}

			return configElements;
		}

		// Java version caching.
		
	    private final Map<String, List<Map<String, Object>>> javaVersionsByFeature;
	    private final Map<File, List<Map<String, Object>>> javaVersionsByBundle;
		
	    /**
	     * Answer the java versions supported by the feature definition.
	     * 
	     * Answer all current java versions if none is set for the feature.
	     *
	     * @param fd A feature definition.
	     * 
	     * @return The java versions supported by the feature.
	     */
		private List<Map<String, Object>> getJavaVersion(ProvisioningFeatureDefinition fd) {
			String featureName = fd.getFeatureName();

			List<Map<String, Object>> versions = javaVersionsByFeature.get(featureName);
			if (versions == null) {
				versions = computeVersions(fd);
				javaVersionsByFeature.put(featureName, versions);
			}

			return verions.isEmpty() ? javaVersions : versions;
		}

		/**
		 * Compute the java versions supported by a feature definition.
		 * 
		 * Start with all supported versions.  Retain those which are 
		 * supported by all bundle constituents and all feature constituents
		 * of the feature.
		 *  
		 * @param fd A feature definition.
		 * 
		 * @return The java versions supported by a feature definition.
		 */
		private List<Map<String, Object>> computeVersions(ProvisioningFeatureDefinition fd) {
			List<Map<String, Object>> versions = new ArrayList<>(javaVersions);

			for (FeatureResource res : fd.getConstituents(SubsystemContentType.BUNDLE_TYPE)) {
				if (res.getJavaRange() != null) {
					// Bundles & Jars with the java range attribute should not be considered for the
					// supported java versions as they are only enabled once the java runtime matches
					// that particular range.
					continue;
				}

				ContentBasedLocalBundleRepository repo = mfp.getBundleRepository(fd.getBundleRepositoryType(), null);
				File bundleFile = repo.selectBundle(res.getLocation(), res.getSymbolicName(), res.getVersionRange());

				List<Map<String, Object>> bundleVersions = javaVersionsByBundle.get(bundleFile);
				if (bundleVersions == null) {
					bundleVersions = computeBundleVersions(fd, bundleFile);
					javaVersionsByBundle.put(bundleFile, bundleVersions);
				}
				
				versions.retainAll(bundleVersions);
			}

			for (FeatureResource res : fd.getConstituents(SubsystemContentType.FEATURE_TYPE)) {
				ProvisioningFeatureDefinition otherFD = mfp.getFeatureDefinitions().get(res.getSymbolicName());
				if (otherFD != null) {
					List<Map<String, Object>> featureSupportedVersions = getJavaVersion(otherFD);
					versions.retainAll(featureSupportedVersions);
				}
			}

			return versions;
		}

		/**
		 * Compute the java versions supported by a bundle.
		 * 
		 * These are specified in the bundle's manifest.
		 * 
		 * Answer all supported java versions if an error occurs, or if no
		 * java requirements are specified.
		 * 
		 * @param fd A feature definition.
		 * @param bundleFile The file of a bundle constituent of the feature.
		 * 
		 * @return The java versions supported by a bundle.
		 */
		private List<Map<String, Object>> computeBundleVersions(ProvisioningFeatureDefinition fd, File bundleFile) { 
			Manifest man = readManifest(bundleFile);
			if ( man == null ) {
				// No manifest!  Default to all supported versions.
				return new ArrayList<>(javaVersions);
			}

			// Try 'BUNDLE_REQUIREDEXECUTIONENVIRONMENT' (obsolete), then
			// 'REQUIRE_CAPABILITY'.  If neither is specified, default
			// to all supported version.

			List<GenericMetadata> capabilityRequirements;

			Attributes a = man.getMainAttributes();
			@SuppressWarnings("deprecation")
			String eeValue = a.getValue(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
			if (eeValue != null) {
				String[] values = eeValue.split(",");
				capabilityRequirements = toCapabilityRequirements(values);
			} else {
				String rq = a.getValue(Constants.REQUIRE_CAPABILITY);
				if (rq != null) {
					capabilityRequirements = ManifestHeaderProcessor.parseCapabilityString(rq);
				} else {
					return new ArrayList<>(javaVersions);
				}
			}

			// Collect the version requirements from the capability filters.

			List<Map<String, Object>> bundleVersions = new ArrayList<>();
						
			for (GenericMetadata capability : capabilityRequirements) {
				if ("osgi.ee".equals(capability.getNamespace())) {
					String filterString = String.valueOf(capability.getDirectives().get("filter"));
					try {
						Filter filter = FrameworkUtil.createFilter(filterString);
						for (Map<String, Object> props : javaVersions) {
							if (filter.matches(props)) {
								bundleVersions.add(props);
							}
						}
					} catch (InvalidSyntaxException e) {
						// TODO: Display an error?
					}
				}
			}
			
			return bundleVersions;
		}

		// Writer primitives.

		/** Utilities used by the feature list writer. */
	    private final FeatureListUtils featureListUtils;
	    /** Low level feature list writer utility widget. */
		private final FeatureListWriter writer;
		
		private void startFeature(String nodeName, String name) {
			writer.startFeature(nodeName, name);
		}		
		
		private void startFeature(String nodeName) {
			writer.startFeature(nodeName);
		}
		
		private void endFeature() {
			writer.endFeature();
		}

		private void writeElement(String nodeName, String text) throws IOException, XMLStreamException {
			writer.writeTextElement(nodeName, text);
		}
		
		private void writeElement(String nodeName, String text, Map<String, String> attr) throws IOException, XMLStreamException {		
			writer.writeTextElementWithAttributes(nodeName, text, attr);
		}
		
		private void writeInclude(String preferred, List<String> tolerates, String shortName) throws IOException, XMLStreamException {		
			writer.writeIncludeFeature(preferred, tolerates, shortName);
		}
		
		// Tables of data for features.
		//
		// These roll up the data of the immediate constituents of the feature.
		//
		// Data is collected for JAR and for BUNDLE constituents.
		
		private final Map<String, FeatureData> allJarFeatureData;
		private final Map<String, FeatureData> allBundleFeatureData;
		
		private FeatureData getJarFeatureData(ProvisioningFeatureDefinition fd,
                                              ContentBasedLocalBundleRepository cbr) {

			String symbolicName = fd.getSymbolicName();
			FeatureData data = allJarFeatureData.get(symbolicName);
			if ( data == null ) {
				data = new FeatureData(fd, SubsystemContentType.JAR_TYPE, cbf);
				allJarFeatureData.put(symbolicName, data);
			}
			return data;
		}

		private FeatureData getBundleFeatureData(ProvisioningFeatureDefinition fd,
				                                 ContentBasedLocalBundleRepository cbr) {

			String symbolicName = fd.getSymbolicName();
			FeatureData data = allBundleFeatureData.get(symbolicName);
			if ( data == null ) {
				data = new FeatureData(fd, SubsystemContentType.BUNDLE_TYPE, cbf);
				allBundleFeatureData.put(symbolicName, data);
			}
			return data;
		}		
		
		// Table of data for feature constituents. 
		
		private final Map<String, ConstituentData> allConstituentData;
		
		private ConstituentData getConstituentData(ProvisioningFeatureDefinition fd,
                                                   FeatureResource fr,
                                                   ContentBasedLocalBundleRepository cbr) {
			String symbolicName = fr.getSymbolicName();
			ConstituentData data = allConstituentData.get(symbolicName);
			if ( data == null ) {
				data = new ConstituentData(fd, fr, cbr);
				allConstituentData.put(symbolicName, data);
			}
			return data;
		}
	}
	
	private static class ConstituentData {
		private final File bundleFile;
		private final String versionRange;
		private final Map<String, String> apiAttrs;
		private final Map<String, String> spiAttrs;

		protected ConstituentData(ProvisioningFeatureDefinition fd,
                                  FeatureResource fr,
                                  ContentBasedLocalBundleRepository cbr) {

			String location = fr.getLocation();
			File f = cbr.selectBundle(location, fr.getSymbolicName(), fr.getVersionRange());
			if (f == null) {
				this.bundleFile = null;
				this.versionRange = null;
				this.apiAttrs = null;
				this.spiAttrs = null;
				
			} else {
				this.bundleFile = f;

				VersionRange range = fr.getJavaRange();
				if ( range == null ) {
					this.versionRange = null;
					this.apiAttrs = null;
					this.spiAttrs = null;

				} else {
					this.versionRange = range.toString();
			
					APIType apiType = APIType.getAPIType(fr);
					if (apiType == APIType.API) {
						Map<String, String> attrs = Collections.singletonMap("require-java", this.versionRange);						
						apiAttrs = attrs;
						spiAttrs = null;
					} else if (apiType == APIType.SPI) {
						Map<String, String> attrs = Collections.singletonMap("require-java", this.versionRange);												
						apiAttrs = null;
						spiAttrs = attrs;
					} else { // Unexpected.
						apiAttrs = null;
						spiAttrs = null;
					}
				}
			}
		}
	}
	
	private static class FeatureData {
		private final List<File> bundleFiles;
		private final Map<File, Map<String, String>> apiJars;
		private final Map<File, Map<String, String>> spiJars;

		protected FeatureData(ProvisioningFeatureDefinition fd,
       	     	              SubsystemContentType contentType,
                	          ContentBasedLocalBundleRepository cbr) {

			this.bundleFiles = new ArrayList<>();
			this.apiJars = new HashMap<>();
			this.spiJars = new HashMap<>();

			for ( FeatureResource fr : fd.getConstituents(contentType) ) {
				add( getConstituentData(fd, fr, cbr) );
			}
		}
		
		protected boolean add(ConstituentData data) {
			if ( data.bundleFile == null ) {
				return false;
			}

			bundleFiles.add(data.bundleFile);

			if ( data.apiAttrs != null ) {
				apiJars.put(data.bundleFile, data.apiAttrs);
			} else if (data.spiAttrs != null) {
				spiJars.put(data.bundleFile, data.spiAttrs);
			} else {
				// Unexpected.
			}

			return true;
		}		

		protected void contributeTo(List<File> bundleFiles,
				                    Map<File, Map<String, String>> apiJars,
				                    Map<File, Map<String, String>> spiJars) {

			if ( bundleFiles != null) {
				bundleFiles.addAll(this.bundleFiles);
			}
			if ( apiJars != null ) {
				apiJars.putAll(this.apiJars);
			}
			if ( spiJars != null ) {
				spiJars.putAll(this.spiJars);
			}
		}
	}	
	
	private static Manifest readManifest(File file) {
		JarFile jarFile = null;

		try {
			jarFile = new JarFile(file);
			return jarFile.getManifest();

		} catch ( IOException e ) {
			return null; // unlikely to occur probably should do something though.

		} finally {
			if ( jarFile != null ) {
				try {
					jarFile.close();
				} catch ( IOException e ) {
					// ignore since we are doing cleanup.
				}
			}
		}
	}
}
//@formatter:on