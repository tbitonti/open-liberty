/*******************************************************************************
 * Copyright (c) 2014, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.annocache.targets.cache.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

import org.jboss.jandex.Index;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.annocache.jandex.internal.SparseIndex;
import com.ibm.ws.annocache.targets.internal.TargetsTableAnnotationsImpl;
import com.ibm.ws.annocache.targets.internal.TargetsTableClassesImpl;
import com.ibm.ws.annocache.targets.internal.TargetsTableImpl;
import com.ibm.ws.annocache.targets.internal.TargetsTableTimeStampImpl;
import com.ibm.wsspi.annocache.targets.cache.TargetCache_ExternalConstants;
import com.ibm.wsspi.annocache.util.Util_Consumer;

/**
 * Annotation cache data for a single container.
 *
 * Container data is managed in two different ways:
 * 
 * Non-result container data (which has a container) has its own directory relative to
 * the directory of the enclosing application, and named using the
 * standard pattern:
 * <code>
 *     appFolder + CON_PREFIX + encode(containerPath)
 * </code>
 *
 * Result container data (which has no actual container) has its own
 * directory relative to the directory of the enclosing module, and
 * named using tags specific to the result type.  One of:
 * {@link com.ibm.wsspi.annocache.targets.cache.TargetCache_ExternalConstants#SEED_RESULT_NAME},
 * {@link com.ibm.wsspi.annocache.targets.cache.TargetCache_ExternalConstants#PARTIAL_RESULT_NAME},
 * {@link com.ibm.wsspi.annocache.targets.cache.TargetCache_ExternalConstants#EXCLUDED_RESULT_NAME}, or
 * {@link com.ibm.wsspi.annocache.targets.cache.TargetCache_ExternalConstants#EXTERNAL_RESULT_NAME}.
 *
 * Container data has four parts:
 * <ul><li>Time stamp information<li>
 *     <li>Classes information, which consists of class references and detailed class information</li>
 *     <li>Annotation targets information</li>
 * </ul>
 *
 * Each part of the container has its own table.
 */
public class TargetCacheImpl_DataCon extends TargetCacheImpl_DataBase {
    private static final String CLASS_NAME = TargetCacheImpl_DataCon.class.getSimpleName();

    //

    // The container data is relative to the enclosing application,
    // not to the enclosing module.
    //
    // Whether a container file is available depends on whether the
    // application directory is available, not on whether the module
    // directory is available.
    //
    // Container data is stored either as a single file or as a directory,
    // according to the cache settings.

    /** Control parameter: Is this data for a component? */
    public static final boolean IS_COMPONENT_CONTAINER = true;
    /** Control parameter: Is this data for a result bucket? */
    public static final boolean IS_RESULT_CONTAINER = false;

    /**
     * Add a prefix to a file name embedded in a path.  Answer the
     * prefixed file name with path unchanged.
     *
     * For example, for "a/b/aName" and prefix "p-" answer "a/b/p-aName".
     * For just "aName" answer "p-aName".
     *
     * The path must be native file system path, using {@link File#separatorChar}
     * as the path separator.
     *
     * @param path The path which is to be adjusted.
     * @param prefix The prefix to add to the name within the path.
     *
     * @return The path adjusted to insert the prefix to the file name.
     */
    private static String prefixAsPeer(String path, String prefix) {
        int lastSlash = path.lastIndexOf(File.separatorChar);
        if ( lastSlash == -1 ) {
            return prefix + path;
        } else {
            return path.substring(0, lastSlash + 1) + prefix + path.substring(lastSlash + 1, path.length());
        }
    }

    public TargetCacheImpl_DataCon(
        TargetCacheImpl_DataBase parentCache,
        String conName, String e_conName, File conFile,
        boolean isComponent) {

        super( parentCache.getFactory(), conName, e_conName, conFile );

        String methodName = "<init>";

        this.parentCache = parentCache;

        // Jandex formatting only applies for component data.
        // Do not use jandex formatting for result bucket component data.

        this.isComponent = isComponent;

        // Parameter use to adjust container storage:
        // When true, container data is written into a directory
        // using separate files.  When false, container data is
        // written to a single file.

        this.separateContainers = getCacheOptions().getSeparateContainers();
        this.useJandexFormat = getCacheOptions().getUseJandexFormat();

        // When the parent application is unnamed, the container
        // directory is null, which means the three container cache files
        // are null: No writes and no reads will be performed.

        if ( getDataFile() == null ) {
            this.timeStampFile = null;
            this.jandexFile = null;
            this.annoTargetsFile = null;
            this.classRefsFile = null;

        } else if ( this.separateContainers ) {
            this.timeStampFile =
                getDataFile(TargetCache_ExternalConstants.TIMESTAMP_NAME);

            if ( this.useJandexFormat ) {
                this.jandexFile =
                    getDataFile(TargetCache_ExternalConstants.JANDEX_NAME);
                this.annoTargetsFile = null;
                this.classRefsFile = null;

            } else {
                this.jandexFile = null;
                this.annoTargetsFile =
                    getDataFile(TargetCache_ExternalConstants.ANNO_TARGETS_NAME);
                this.classRefsFile =
                    getDataFile(TargetCache_ExternalConstants.CLASS_REFS_NAME);
            }

        } else {
            this.timeStampFile = getDataFile();

            if ( this.isComponent && this.useJandexFormat ) {
                // Put on a jandex prefix: The first character cannot be "C_".
                String containerJandexPath = prefixAsPeer(
                    this.timeStampFile.getPath(), 
                    TargetCache_ExternalConstants.JANDEX_PREFIX);
                this.jandexFile = new File(containerJandexPath);

            } else {
                this.jandexFile = null;
            }

            this.annoTargetsFile = null;
            this.classRefsFile = null;
        }


        if ( logger.isLoggable(Level.FINER) ) {
            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "Container [ {0} ] of [ {1} ]",
                new Object[] { getName(), parentCache.getName() });

            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "IsComponent [ {0} ] Separate [ {1} ] Use Jandex Format [ {2} ]",
                new Object[] { this.isComponent, this.separateContainers, this.useJandexFormat });

            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "Time stamp file [ {0} ]", getPath(this.timeStampFile));
            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "Jandex file [ {0} ]", getPath(this.jandexFile));
            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "Targets file [ {0} ]", getPath(this.annoTargetsFile));
            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "Class refs file [ {0} ]", getPath(this.classRefsFile));
        }

        // System.out.println( "Container [ " + getName() + " ] of [ " + parentCache.getName() + " ]" );

        // System.out.println("IsComponent [ " + this.isComponent + " ]");
        // System.out.println("Separate [ " + this.separateContainers + " ]");
        // System.out.println("Use Jandex Format [ " + this.useJandexFormat + " ]");

        // System.out.println( "Time stamp file [ " + getPath(this.timeStampFile) + " ]" );
        // System.out.println( "Jandex file [ " + getPath(this.jandexFile) + " ]" );
        // System.out.println( "Targets file [ " + getPath(this.annoTargetsFile) + " ]" );
        // System.out.println( "Class refs file [ " + getPath(this.classRefsFile) + " ]" );
    }

    //

    // DataApp for simple container data.
    // DataMod for policy container data.

    private final TargetCacheImpl_DataBase parentCache;

    @Trivial
    public TargetCacheImpl_DataBase getParentCache() {
        return parentCache;
    }

    private final boolean isComponent;

    @Trivial
    public boolean getIsComponent() {
        return isComponent;
    }

    private final boolean separateContainers;

    @Trivial
    public boolean getSeparateContainers() {
        return separateContainers;
    }

    private final boolean useJandexFormat;

    @Trivial
    public boolean getUseJandexFormat() {
        return useJandexFormat;
    }

    //

    private final File timeStampFile;

    @Trivial
    public File getTimeStampFile() {
        return timeStampFile;
    }

    // Answers false if the stamp file is null.

    public boolean hasTimeStampFile() {
        return ( exists( getTimeStampFile() ) );
    }

    //

    private final File annoTargetsFile;

    public File getAnnoTargetsFile() {
        return annoTargetsFile;
    }

    // Answers false if the targets file is null.

    public boolean hasAnnoTargetsFile() {
        return ( exists( getAnnoTargetsFile() ) );
    }

    //

    private final File classRefsFile;

    @Trivial
    public File getClassRefsFile() {
        return classRefsFile;
    }

    // Answers false if the class refs file is null.

    public boolean hasClassRefsFile() {
        return ( exists( getClassRefsFile() ) );
    }

    //

    private final File jandexFile;

    @Trivial
    public File getJandexFile() {
        return jandexFile;
    }

    public boolean hasJandexFile() {
        return ( exists( getJandexFile() ) );
    }

    // 'write' cannot be entered if the stamp file is null.

    private void write(
        TargetCacheImpl_DataMod modData,
        TargetsTableTimeStampImpl stampTable) {

        String description;
        if ( logger.isLoggable(Level.FINER) ) {
            description = "Container [ " + getName() + " ] TimeStamp [ " + getPath( getTimeStampFile() ) + " ]";
        } else {
            description = null;
        }

        Util_Consumer<TargetCacheImpl_Writer, IOException> writeAction =
            (TargetCacheImpl_Writer useWriter) -> {
                useWriter.write(stampTable);
            };

        modData.scheduleWrite(description, getTimeStampFile(), DO_TRUNCATE, writeAction);
    }

    // 'write' cannot be entered if the class refs file is null.

    private void write(
        TargetCacheImpl_DataMod modData,
        TargetsTableClassesImpl classesTable) {

        String description;
        if ( logger.isLoggable(Level.FINER) ) {
            description = "Container [ " + getName() + " ] Class references [ " + getPath( getClassRefsFile() ) + " ]";
        } else {
            description = null;
        }

        Util_Consumer<TargetCacheImpl_Writer, IOException> writeAction =
            (TargetCacheImpl_Writer useWriter) -> {
                useWriter.write(classesTable);
            };

        modData.scheduleWrite(description, getClassRefsFile(), DO_NOT_TRUNCATE, writeAction);
    }

    // 'write' cannot be entered if the targets file is null.

    private void write(
        TargetCacheImpl_DataMod modData,
        TargetsTableAnnotationsImpl targetTable) {

        String description;
        if ( logger.isLoggable(Level.FINER) ) {
            description = "Container [ " + getName() + " ] Targets [ " + getPath( getAnnoTargetsFile() ) + " ]";
        } else {
            description = null;
        }

        Util_Consumer<TargetCacheImpl_Writer, IOException> writeAction =
            (TargetCacheImpl_Writer useWriter) -> {
                useWriter.write(targetTable);
            };

        modData.scheduleWrite(description, getAnnoTargetsFile(), DO_TRUNCATE, writeAction);
    }

    //

    private boolean read(TargetsTableTimeStampImpl stampTable) {
        boolean didRead;

        long readStart = System.nanoTime();

        TargetsTableTimeStampImpl readStampTable = readStampTable();
        if ( readStampTable == null ) {
            didRead = false;
        } else {
            didRead = true;
            stampTable.setName( readStampTable.getName() );
            stampTable.setStamp( readStampTable.getStamp() );
        }

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Stamp");

        return didRead;
    }

    // 'read(TargetsTableClassesImpl)' cannot be entered if the class refs file is null.

    private boolean read(TargetsTableClassesImpl classesTable) {
        long readStart = System.nanoTime();

        boolean didRead = read( getClassRefsFile(), classesTable );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Classes");

        return didRead;
    }

    // 'read(TargetsTableAnnotationsImpl)' cannot be entered if the targets file is null.

    private boolean read(TargetsTableAnnotationsImpl targetTable) {
        long readStart = System.nanoTime();

        boolean didRead = read( getAnnoTargetsFile(), targetTable );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Targets");

        return didRead;
    }

    // 'readStampTable' cannot be entered if the stamp file is null.

    public TargetsTableTimeStampImpl readStampTable() {
        long readStart = System.nanoTime();

        TargetsTableTimeStampImpl stampTable = new TargetsTableTimeStampImpl();

        boolean didRead = read( getTimeStampFile(), stampTable );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Stamp");

        return ( didRead ? stampTable : null );
    }

    //

    /**
     * Tell if data of the container should be read.
     *
     * Container data is read only if the parent is enabled
     * for reads, and only if reading is enabled in general.
     *
     * @param inputDescription The input which is querying
     *     if reads are enabled.
     *
     * @return True or false telling if reads are enabled for
     *     the container.
     */
    @Override
    public boolean shouldRead(String inputDescription) {
        if ( !getParentCache().shouldRead(inputDescription) ) {
            return false;
        } else {
            return super.shouldRead(inputDescription);
        }
    }

    /**
     * Tell if data of the container should be written.
     *
     * Container data is written only if the parent is enabled
     * for writes, and only if writing is enabled in general.
     *
     * @param outputDescription The output which is querying
     *     if writes are enabled.
     *
     * @return True or false telling if writes are enabled for
     *     the container.
     */
    @Override
    public boolean shouldWrite(String outputDescription) {
        if ( !getParentCache().shouldWrite(outputDescription) ) {
            return false;
        } else {
            return super.shouldWrite(outputDescription);
        }
    }

    //

    public boolean hasFiles() {
        if ( getIsComponent() && getUseJandexFormat() ) {
            return ( hasTimeStampFile() && hasJandexFile() );
        } else if ( getSeparateContainers() ) {
            return ( hasTimeStampFile() && hasAnnoTargetsFile() && hasClassRefsFile() );
        } else {
            return ( hasTimeStampFile() );
        }
    }

    public boolean read(TargetsTableImpl targetData) {
        if ( getIsComponent() && getUseJandexFormat() ) {
            return ( read( targetData.getStampTable() ) &&
                     readJandex(targetData) );
        } else if ( getSeparateContainers() ) {
            return ( read( targetData.getStampTable() ) &&
                     read( targetData.getClassTable() ) && 
                     read( targetData.getAnnotationTable() ) );
        } else {
            return readTogether(targetData);
        }
    }

    protected boolean readTogether(TargetsTableImpl targetData) {
        long readStart = System.nanoTime();

        TargetsTableTimeStampImpl readStampTable = new TargetsTableTimeStampImpl();

        boolean didRead = read(
            getTimeStampFile(),
            readStampTable,
            targetData.getClassTable(),
            targetData.getAnnotationTable() );

        if ( didRead ) {
            TargetsTableTimeStampImpl stampTable = targetData.getStampTable();
            stampTable.setName( readStampTable.getName() );
            stampTable.setStamp( readStampTable.getStamp() );
        }

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Together");

        return didRead;
    }

    //

    /**
     * Write stamp information.
     * 
     * Module data is needed, since writes are performed at the module level.  The
     * module data cannot be stored in the container data because the container data
     * is shared between modules.
     * 
     * @param modData The module which will perform the write.
     * @param targetData The data containing the stamp table which is to be written.
     */
    public void writeStamp(TargetCacheImpl_DataMod modData, TargetsTableImpl targetData) {
        if ( getSeparateContainers() || (getIsComponent() && getUseJandexFormat()) ) {
            write( modData, targetData.getStampTable() );
        } else {
            writeTogether( modData, targetData.getStampTable() );
        }
    }

    /**
     * Write all data: The class table, the annotations table, and the stamp table.
     *
     * Module data is needed, since writes are performed at the module level.  The
     * module data cannot be stored in the container data because the container data
     * is shared between modules.
     *
     * @param modData The module which will perform the write.
     * @param targetData The data which is to be written.
     */
    public void write(TargetCacheImpl_DataMod modData, TargetsTableImpl targetData) {
        if ( getIsComponent() && getUseJandexFormat() ) {
            write( modData, targetData.getStampTable() );
            writeJandex( modData, targetData.consumeJandexIndex() );

        } else if ( getSeparateContainers() ) {
            write( modData, targetData.getStampTable() );
            write( modData, targetData.getClassTable() );
            write( modData, targetData.getAnnotationTable() );

        } else {
            writeTogether(modData, targetData);
        }
    }

    /**
     * Write the stamp table to an existing targets table.
     *
     * @param modData The module which will perform the write.
     * @param stampTable The data which is to be written.
     */
    private void writeTogether(TargetCacheImpl_DataMod modData, TargetsTableTimeStampImpl stampTable) {
        String description;
        if ( logger.isLoggable(Level.FINER) ) {
            description = "Container [ " + getName() + " ] Container file [ " + getPath( getTimeStampFile() ) + " ]";
        } else {
            description = null;
        }

        Util_Consumer<TargetCacheImpl_Writer, IOException> writeAction =
            (TargetCacheImpl_Writer useWriter) -> {
                useWriter.write(stampTable);
            };

        modData.scheduleWrite(description, getTimeStampFile(), DO_NOT_TRUNCATE, writeAction);
    }

    /**
     * Write all data to a single file: Write the stamp, then the class table, then
     * the annotations table.
     *
     * Module data is needed, since writes are performed at the module level.  The
     * module data cannot be stored in the container data because the container data
     * is shared between modules.
     *
     * @param modData The module which will perform the write.
     * @param targetData The data which is to be written.
     */
    private void writeTogether(TargetCacheImpl_DataMod modData, TargetsTableImpl targetData) {
        String description;
        if ( logger.isLoggable(Level.FINER) ) {
            description = "Container [ " + getName() + " ] Container file [ " + getPath( getTimeStampFile() ) + " ]";
        } else {
            description = null;
        }

        Util_Consumer<TargetCacheImpl_Writer, IOException> writeAction =
            (TargetCacheImpl_Writer useWriter) -> {
                useWriter.write( targetData.getStampTable() );
                useWriter.write( targetData.getClassTable() );
                useWriter.write( targetData.getAnnotationTable() );
            };

        modData.scheduleWrite(description, getTimeStampFile(), DO_TRUNCATE, writeAction);
    }

    //

    private boolean readJandex(TargetsTableImpl targetData) {
        String methodName = "readJandex";

        long readStart = System.nanoTime();

        File useJandexFile = getJandexFile();

        if ( logger.isLoggable(Level.FINER) ) {
            logger.logp(Level.FINER, CLASS_NAME, methodName,
                "Container [ " + getName() + " ] Jandex File [ " + getPath(useJandexFile) + " ]");
        }

        boolean didRead;

        try {
            TargetCacheImpl_Reader reader = createReader(useJandexFile); // throws IOException
            try {
                SparseIndex sparseIndex = reader.readSparseIndex(); // throws IOException
                targetData.transfer(sparseIndex);
            } finally {
                reader.close(); // throws IOException
            }
            didRead = true;

        } catch ( IOException e ) {
            readError( useJandexFile, e, Collections.emptyList() );
            didRead = false;
        }

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Jandex");

        return didRead;
    }

    private void writeJandex(
        TargetCacheImpl_DataMod modData,
        Index jandexIndex) {

        String description;
        if ( logger.isLoggable(Level.FINER) ) {
            description = "Container [ " + getName() + " ] Jandex File [ " + getPath( getJandexFile() ) + " ]";
        } else {
            description = null;
        }

        Util_Consumer<TargetCacheImpl_Writer, IOException> writeAction =
            (TargetCacheImpl_Writer useWriter) -> {
                useWriter.write(jandexIndex);
            };

        modData.scheduleWrite(description, getJandexFile(), DO_TRUNCATE, writeAction);
    }
}

