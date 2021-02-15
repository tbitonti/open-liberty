/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.internal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.ibm.ws.kernel.boot.BootstrapConfig;
import com.ibm.ws.kernel.boot.Debug;
import com.ibm.ws.kernel.boot.archive.Archive;
import com.ibm.ws.kernel.boot.archive.ArchiveEntryConfig;
import com.ibm.ws.kernel.boot.archive.ArchiveFactory;
import com.ibm.ws.kernel.boot.archive.DirEntryConfig;
import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.archive.FileEntryConfig;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;
import com.ibm.ws.kernel.boot.internal.XMLUtils;

/**
 * Provide the utilities for PackageProcessor use
 */
public class ProcessorUtils {
    /**
     * Get the loose config files from ${server.config.dir}/apps,
     * ${server.config.dir}/dropins or ${shared.app.dir}.
     */
    public static Set<File> getLooseConfigFiles(BootstrapConfig bootProps) {
        Set<File> files = new HashSet<File>();

        List<Pattern> patterns = new ArrayList<Pattern>();
        patterns.add(Pattern.compile(".*(\\.xml)$"));
        // Add loose files under ${server.config.dir}/dropins
        File dropins = getFileFromDirectory(new File(bootProps.get(BootstrapConstants.LOC_PROPERTY_SRVCFG_DIR)), BootstrapConstants.LOC_AREA_NAME_DROP);
        if (dropins.exists()) {
            files.addAll(Arrays.asList(FileUtils.listFiles(dropins, patterns, true)));
        }
        // Add loose files under ${server.config.dir}/apps
        File apps = getFileFromDirectory(new File(bootProps.get(BootstrapConstants.LOC_PROPERTY_SRVCFG_DIR)), BootstrapConstants.LOC_AREA_NAME_APP);
        if (apps.exists()) {
            files.addAll(Arrays.asList(FileUtils.listFiles(apps, patterns, true)));
        }
        // Add loose files under ${shared.app.dir}
        File shared = new File(bootProps.get(BootstrapConstants.LOC_PROPERTY_SHARED_APP_DIR));
        if (shared.exists()) {
            files.addAll(Arrays.asList(FileUtils.listFiles(shared, patterns, true)));
        }
        return files;
    }

    /**
     * Parse the loose config file and create the looseConfig object
     * which contains the file's content.
     *
     * See com.ibm.ws.artifact.api.loose.internal.LooseContainerFactoryHelper.createContainer.
     */
    public static LooseConfig convertToLooseConfig(File looseFile) throws Exception {
        //make sure the file exists, can be read and is an xml
        if (looseFile.exists() && looseFile.isFile() && looseFile.canRead() && looseFile.getName().toLowerCase().endsWith(".xml")) {
            LooseConfig root = new LooseConfig(LooseType.ARCHIVE);

            XMLStreamReader reader = null;
            try {
                LooseConfig cEntry = root;

                Stack<LooseConfig> stack = new Stack<LooseConfig>();
                stack.push(cEntry);

                // Normally we specifically request the system default XMLInputFactory to avoid issues when
                // another XMLInputFactory is available on the thread context class loader. In this case it
                // should be safe to use XMLInputFactory.newInstance() because the commands run separately
                // from the rest of the code base.

                reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(looseFile));
                while (reader.hasNext() && cEntry != null) {
                    int result = reader.next();
                    if (result == XMLStreamConstants.START_ELEMENT) {
                        if ("archive".equals(reader.getLocalName())) {
                            String targetLocation = XMLUtils.getAttribute(reader, "targetInArchive");
                            if (targetLocation != null) {
                                LooseConfig pEntry = cEntry;

                                cEntry = new LooseConfig(LooseType.ARCHIVE);
                                cEntry.targetInArchive = targetLocation;

                                pEntry.children.add(cEntry);
                                //put current Loose Entry into stack
                                stack.push(cEntry);
                            }
                        } else if ("dir".equals(reader.getLocalName())) {
                            cEntry.children.add(createElement(reader, LooseType.DIR));
                        } else if ("file".equals(reader.getLocalName())) {
                            cEntry.children.add(createElement(reader, LooseType.FILE));
                        }
                    } else if (result == XMLStreamConstants.END_ELEMENT) {
                        if ("archive".equals(reader.getLocalName())) {
                            stack.pop();
                            //make sure stack isn't empty before we try and look at it
                            if (!stack.empty()) {
                                //set current entry to be the top of stack
                                cEntry = stack.peek();
                            } else {
                                cEntry = null;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw e;
            } catch (XMLStreamException e) {
                throw e;
            } catch (FactoryConfigurationError e) {
                throw e;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                        reader = null;
                    } catch (XMLStreamException e) {
                        Debug.printStackTrace(e);
                    }
                }
            }
            return root;
        }
        return null;
    }

    private static LooseConfig createElement(XMLStreamReader reader, LooseType type) {
        LooseConfig lEntry = null;

        Set<String> attrNames = new HashSet<String>();
        attrNames.add("targetInArchive");
        attrNames.add("sourceOnDisk");

        if (LooseType.DIR == type) {
            attrNames.add("excludes");
        }

        lEntry = XMLUtils.createInstanceByElement(reader, LooseConfig.class, attrNames);
        lEntry.type = type;
        return lEntry;
    }

    /**
     * Add looseConfigs in both archive and expanded format.
     */
    public static List<ArchiveEntryConfig> createLooseExpandedArchiveEntryConfigs(
        LooseConfig looseConfig, File looseFile,
        BootstrapConfig bootProps,
        String archiveEntryPrefix,
        boolean isUsr) throws IOException {

        List<ArchiveEntryConfig> entryConfigs = new ArrayList<ArchiveEntryConfig>();
        String entryPath = generateBaseEntryPath(looseFile, bootProps, archiveEntryPrefix, isUsr);

        //Add loose config as archive
        File archiveFile = processArchive(looseConfig, looseFile.getName(), bootProps);
        entryConfigs.add(new FileEntryConfig(entryPath, archiveFile));

        //Add expanded folder to entry path
        int lastSlash = entryPath.lastIndexOf("/") + 1;
        if (lastSlash != 0) {
            entryPath = entryPath.substring(0, lastSlash) + "expanded/" + entryPath.substring(lastSlash);
            // Make sure if using dropins, the expanded folder goes to apps folder
            if (entryPath.indexOf("dropins") != 0) {
                entryPath = entryPath.replace("dropins", "apps");
            }
        }

        for ( LooseConfig config : looseConfig ) {
            try {
                entryConfigs.add( processLooseConfig(entryPath, config, bootProps) );
            } catch ( IllegalArgumentException | IOException ex ) {
                // TODO: Is this sufficient?  A warning that loose content
                //       could not be added would be good to see.  Otherwise,
                //       content could invisibly go missing.                
                // FFDC?
                continue;
            }
        }

        return entryConfigs;
    }

    /**
     * Create an entry configuration for a loose configuration.
     * 
     * If necessary, package the loose configuration as an archive.
     *
     * @param prefixEntryPath The prefix to the entry paths which are
     *     to be assigned to the loose configuration entries.
     * @param config The loose configuration.
     * @param bootProps Bootstrap properties.  This contains variable
     *     assignments, which are needed to resolve the loose configuration
     *     locations.
     *
     * @return The new archive configuration.
     *
     * @throws IOException Thrown if there was a failure to create the entry
     *     configuration.  This can only occur if there was a failure to package
     *     the loose configuration as an archive. 
     */
    public static ArchiveEntryConfig processLooseConfig(
        String prefixEntryPath,
        LooseConfig config,
        BootstrapConfig bootProps) throws IOException {

        String sourceOnDisk = config.sourceOnDisk;

        switch ( config.type ) {

        case ARCHIVE: {
            File archiveFile = processArchive(config, null, bootProps); // throws IOException
            String archiveEntryPath = createLooseConfigEntryPath(config, prefixEntryPath);
            return new FileEntryConfig(archiveEntryPath, archiveFile);
        }

        case DIR: {
            File archiveDir = FileUtils.convertPathToFile(sourceOnDisk, bootProps);
            String archiveEntryPath = createLooseConfigEntryPath(config, prefixEntryPath);
            DirEntryConfig dirConfig = new DirEntryConfig(
                archiveEntryPath, archiveDir,
                true, PatternStrategy.ExcludePreference);
            if ( config.excludes != null ) {
                dirConfig.exclude( convertToRegex(config.excludes) );
            }
            return dirConfig;
        }
        
        case FILE:
        default: { // Strange, but consistent with the prior if/else blocks.
            File archiveFile = FileUtils.convertPathToFile(sourceOnDisk, bootProps);
            String archiveEntryPath = createLooseConfigEntryPath(config, prefixEntryPath);
            return new FileEntryConfig(archiveEntryPath, archiveFile);
        }
        }
    }

    /**
     * Create an archive from loose configuration data.
     * 
     * @param looseConfig Loose configuration data used to populate the archive.
     * @param archiveName Override name for the archive file.  If null,
     *     a value from the loose configuration data will be used.
     * @param bootProps Bootstrap properties.  In particular, providing the
     *     temporary folder in which to place the archive.
     *     
     * @throws IOException Thrown if the archive could not be created.
     */
    private static File processArchive(
        LooseConfig looseConfig,
        String archiveName,
        BootstrapConfig bootProps) throws IOException {

        String tmpDir = bootProps.get(BootstrapConstants.LOC_PROPERTY_SRVTMP_DIR);

        if ( archiveName == null ) {
            archiveName = looseConfig.targetInArchive.replace('\\', '/');

            // These first two cases might never occur.
            // "" stays as ""              "/" changes to ""
            //
            // "a" stays as "a"            "/a" changes to "/"
            // "a/" changes to "a"         "/a/" changes to "/"
            //
            // "a/b" changes to "a/"       "/a/b" changes to "/a/"
            // "a/b/" changes to "a/"      "/a/b/" changes to "/a/"
            //
            // "a/b/c" changes to "a/b/"   "/a/b/c" changes to "/a/b/"
            // "a/b/c/" changes to "a/b/"  "/a/b/c/" changes "/a/b/"

            if ( !archiveName.isEmpty() ) {
                int nameLength = archiveName.length();
                if ( archiveName.charAt(nameLength - 1) == '/' ) {
                    archiveName = archiveName.substring(0, nameLength - 1);
                }
                int index = archiveName.lastIndexOf('/');
                if ( index != -1 ) {
                    archiveName = archiveName.substring(index + 1);
                }
            }
        }

        File archiveFile = FileUtils.createTempFile(archiveName, null, tmpDir);
        // throws IOException

        try ( Archive archive = ArchiveFactory.create(archiveFile) ) { // throws IOException
            for ( LooseConfig config : looseConfig ) {
                try {
                    archive.addEntryConfig( processLooseConfig("", config, bootProps) );
                    // 'processLooseConfig' throws IOException
                } catch ( IOException ex ) {
                    Debug.printStackTrace(ex);
                    continue;
                }
            }
            archive.create(); // throws IOException
        }

        return archiveFile;
    }

    /**
     * Common code for generating the entryPath
     */
    private static String generateBaseEntryPath(File looseFile, BootstrapConfig bootProps, String archiveEntryPrefix, boolean isUsr) {
        File usrRoot = bootProps.getUserRoot();
        int len = usrRoot.getAbsolutePath().length();

        String entryPath = null;
        if (archiveEntryPrefix.equalsIgnoreCase(PackageProcessor.PACKAGE_ARCHIVE_PREFIX) || !isUsr) {
            entryPath = archiveEntryPrefix + BootstrapConstants.LOC_AREA_NAME_USR + looseFile.getAbsolutePath().substring(len);
        } else {
            entryPath = archiveEntryPrefix + looseFile.getAbsolutePath().substring(len);
        }

        // Fix path separators if necessary and trim '.xml' from the end
        return entryPath.replace("\\", "/").replaceAll("/(?=/)|.xml$", "");
    }

    /**
     * Adjust the entry path which will be used for a loose configuration.
     *
     * Append the loose configuration 'targetInArchive' value.  Append
     * a '/' between the values if necessary.
     * 
     * The entry path must not be null.
     * 
     * If the entry path is empty, simply answer 'targetInArchive', with no
     * '/' added.  If 'targetInArchive' is empty, answer the entry path
     * with a '/' added.  (This case might not be possible.
     * 
     * @param looseConfig The loose configuration used to adjust the entry path.
     * @param entryPath The entry path which is to be adjusted.
     * 
     * @return The adjusted entry path.
     */
    public static String createLooseConfigEntryPath(LooseConfig looseConfig, String entryPath) {
        String targetInArchive = looseConfig.targetInArchive;
        if ( entryPath.isEmpty() ) {
            return targetInArchive;
        } else if ( targetInArchive.isEmpty() ) {
            return entryPath + '/'; // TFB: This case is strange.  Will it occur?
        } else if ( targetInArchive.charAt(0) == '/') {
            return entryPath + targetInArchive;
        } else {
            return entryPath + '/' + targetInArchive;
        }
    }

    /**
     * Copied from com.ibm.ws.artifact.api.loose.internal.LooseArchive.
     */
    public static Pattern convertToRegex(String excludeStr) {
        System.out.println("convertToRegex: " + excludeStr + " (pre-escape)");

        // make all "." safe decimles then convert ** to .* and /* to /.* to make it regex
        if (excludeStr.contains(".")) {
            // regex for "." is \. - but we are converting the string to a regex string so need to escape the escape slash...
            excludeStr = excludeStr.replace(".", "\\.");
        }
        //if we exclude a dir (eg /**/) we need to remove the closing slash so our regex is /.*
        if (excludeStr.endsWith("/")) {
            excludeStr = excludeStr.substring(0, excludeStr.length() - 1);
        }
        if (excludeStr.contains("**")) {
            excludeStr = excludeStr.replace("**", ".*");
        }
        if (excludeStr.contains("/*")) {
            excludeStr = excludeStr.replace("/*", "/.*");
        }
        //need to escape the file seperators correctly, as / is a regex keychar
        if (excludeStr.contains("/")) {
            excludeStr = excludeStr.replace("/", "\\/");
        }
        //at this point we should not have any **, if we do replace with * as all * should be prefixed with a .
        if (excludeStr.contains("**")) {
            excludeStr = excludeStr.replace("**", "*");
        }
        if (excludeStr.startsWith("*")) {
            excludeStr = "." + excludeStr;
        }
        if (excludeStr.contains("[")) {
            excludeStr = excludeStr.replace("[", "\\[");
        }
        if (excludeStr.contains("]")) {
            excludeStr = excludeStr.replace("]", "\\]");
        }
        if (excludeStr.contains("-")) {
            excludeStr = excludeStr.replace("-", "\\-");
        }
        
        System.out.println("convertToRegex: " + excludeStr + " (escaped)v");

        return Pattern.compile(excludeStr);
    }

    public static File getFileFromDirectory(File dir, String path) {
        StringBuilder sBuilder = new StringBuilder(dir.getAbsolutePath());
        for (String p : path.split("/")) {
            sBuilder.append(File.separator).append(p);
        }

        return new File(sBuilder.toString());
    }

    protected static class LooseConfig implements Iterable<LooseConfig> {
        protected String targetInArchive;
        protected String sourceOnDisk;
        protected String excludes;
        protected LooseType type;

        protected List<LooseConfig> children = new ArrayList<LooseConfig>();

        @Override
        public Iterator<LooseConfig> iterator() {
            return children.iterator();
        }
        
        @Deprecated
        public Iterator<LooseConfig> iteration() {
            return children.iterator();
        }

        public LooseConfig() {
            // EMPTY
        };

        public LooseConfig(LooseType type) {
            this.type = type;
        }
    }

    private enum LooseType {
        DIR, FILE, ARCHIVE
    }
}
