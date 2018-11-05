/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WLP Copyright IBM Corp. 2014, 2018
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.anno.targets.cache.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.anno.targets.cache.TargetCache_ParseError;
import com.ibm.ws.anno.targets.cache.TargetCache_Readable;
import com.ibm.ws.anno.targets.cache.TargetCache_Reader;
import com.ibm.ws.anno.targets.internal.TargetsTableClassesMultiImpl;
import com.ibm.ws.anno.targets.internal.TargetsTableContainersImpl;
import com.ibm.ws.anno.targets.internal.TargetsTableImpl;
import com.ibm.ws.anno.util.internal.UtilImpl_InternMap;
import com.ibm.ws.anno.util.internal.UtilImpl_PoolExecutor;
import com.ibm.wsspi.anno.classsource.ClassSource_Aggregate.ScanPolicy;
import com.ibm.wsspi.anno.targets.cache.TargetCache_ExternalConstants;
import com.ibm.wsspi.anno.targets.cache.TargetCache_Options;

/**
 * Annotation cache data for a single module.
 *
 * Each module has its own directory, relative to the directory of the
 * enclosing application, and named using the standard pattern:
 * <code>
 *     appFolder + MOD_PREFIX + encode(modName)
 * </code>
 *
 * Module information consists of a list of containers (including policy settings for
 * each container), the actual data of the containers (which is held by the enclosing
 * application and which is shared between modules), resolved class references,
 * unresolved class references, and four result containers (for SEED, PARTIAL,
 * EXCLUDED, and EXTERNAL results).
 *
 * The list of containers, resolved class references, and unresolved class references
 * are stored each in their own file.
 *
 * Storage of non-result container data is managed by the enclosing application, with the
 * container data stored in sub-directories of the application directory.
 *
 * Storage of result container is managed at the module level, with the result container
 * data stored in sub-directories of the module.
 *
 * Module data is held weakly by the enclosing application.
 *
 * Container data of a module is held strongly by the module.
 */
public class TargetCacheImpl_DataMod extends TargetCacheImpl_DataBase {
    private static final String CLASS_NAME = TargetCacheImpl_DataMod.class.getSimpleName();

    //

    private static final String CALLBACK_CLASS_NAME = CLASS_NAME + "$ScheduleCallback";

    protected interface ScheduleCallback {
        void execute();
    }

    //

    public TargetCacheImpl_DataMod(
        TargetCacheImpl_DataApp app,
        String modName, String e_modName, File modDir, boolean isLightweight) {

        super( app.getFactory(), modName, e_modName, modDir );

        this.app = app;
        this.isLightweight = isLightweight;

        //

        this.cons = new HashMap<String, TargetCacheImpl_DataCon>();
        this.containersFile = getDataFile(TargetCache_ExternalConstants.CONTAINERS_NAME);

        this.seedCon = null;
        this.partialCon = null;
        this.excludedCon = null;
        this.externalCon = null;

        this.unresolvedRefsFile = getDataFile(TargetCache_ExternalConstants.UNRESOLVED_REFS_NAME);
        this.resolvedRefsFile = getDataFile(TargetCache_ExternalConstants.RESOLVED_REFS_NAME);
        this.classRefsFile = getDataFile(TargetCache_ExternalConstants.CLASS_REFS_NAME);

        //

        // Writes are done by modules, including for per-application container data and
        // including module data.
        //
        // No writes are possible if the application is unnamed.
        //
        // Module writes are not possible while container writes are possible when the application
        // is named but the module is unnamed or is lightweight.

        if ( !app.isNamed() ) {
            this.writePool = null;

        } else {
            int writeThreads = this.cacheOptions.getWriteThreads();

            if ( writeThreads == 1 ) {
                this.writePool = null;

            } else {
                int corePoolSize = 0;

                int maxPoolSize;
                if ( writeThreads == TargetCache_Options.WRITE_THREADS_UNBOUNDED) {
                    maxPoolSize = TargetCache_Options.WRITE_THREADS_MAX;
                } else if ( writeThreads > TargetCache_Options.WRITE_THREADS_MAX ) {
                    maxPoolSize = TargetCache_Options.WRITE_THREADS_MAX;
                } else {
                    maxPoolSize = writeThreads;
                }

                this.writePool = UtilImpl_PoolExecutor.createNonBlockingExecutor(corePoolSize, maxPoolSize);
            }
        }
    }

    //

    private final TargetCacheImpl_DataApp app;

    @Trivial
    public TargetCacheImpl_DataApp getApp() {
        return app;
    }

    //

    private final boolean isLightweight;

    public boolean getIsLightweight() {
        return isLightweight;
    }

    @Override
    public File getDataFile(String relativePath) {
        if ( getIsLightweight() ) {
                return null;
        } else {
                return super.getDataFile(relativePath);
        }
    }

    //

    protected final File containersFile;

    public File getContainersFile() {
        return containersFile;
    }

    public boolean hasContainersTable() {
        return exists( getContainersFile() );
    }

    public boolean readContainerTable(TargetsTableContainersImpl containerTable) {
        boolean didRead;

        long readStart = System.nanoTime();

        didRead = super.read( containerTable, getContainersFile() );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Containers");

        return didRead;
    }

    public void writeContainersTable(TargetsTableContainersImpl containerTable) {
        // String methodName = "writeContainersTable";

        final String writeDescription;
        if ( logger.isLoggable(Level.FINER) ) {
            writeDescription = "Container [ " + getName() + " ] Containers table [ " + getContainersFile().getPath() + " ]";
        } else {
            writeDescription = null;
        }

        ScheduleCallback writer = new ScheduleCallback() {
            @Override
            public void execute() {
                String methodName = "writeContainersTable.execute";
                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "ENTER {0}", writeDescription);
                }

                long writeStart = System.nanoTime();

                try {
                    createWriter( getContainersFile() ).write(containerTable);
                } catch ( IOException e ) {
                    // CWWKC00??W
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "ANNO_TARGETS_CACHE_EXCEPTION [ {0} ]", e);
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "Cache error", e);
                }

                @SuppressWarnings("unused")
                long writeDuration = addWriteTime(writeStart, writeDescription);

                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "RETURN {0}", writeDescription);
                }
            }
        };

        scheduleWrite(writer, writeDescription);
    }

    //

    private final Map<String, TargetCacheImpl_DataCon> cons;

    @Trivial
    protected Map<String, TargetCacheImpl_DataCon> getCons() {
        return cons;
    }

    public TargetCacheImpl_DataCon getConForcing(String conPath) {
        Map<String, TargetCacheImpl_DataCon> useCons = getCons();

        TargetCacheImpl_DataCon con = useCons.get(conPath);
        if ( con == null ) {
            con = getApp().getConForcing(conPath);
            useCons.put(conPath, con);
        }

        return con;
    }

    //

    public long getContainerReadTime() {
        long containerReadTime = 0L;

        for ( TargetCacheImpl_DataCon con : getCons().values() ) {
            containerReadTime += con.getReadTime();
        }

        return containerReadTime;
    }

    public long getContainerWriteTime() {
        long containerWriteTime = 0L;

        for ( TargetCacheImpl_DataCon con : getCons().values() ) {
            containerWriteTime += con.getWriteTime();
        }

        return containerWriteTime;
    }

    //

    /**
     * Create result container data.
     *
     * Result container data differs from simple container data in several ways:
     *
     * Result container data is named according to the category of data (SEED,
     * PARTIAL, EXCLUDED, or EXTERNAL) which is held.  Simple container data is
     * named based on the container from which the data was obtained.
     *
     * Result container data is stored relative to the enclosing module.  Simple
     * container data is stored relative to the enclosing application.
     *
     * Result container data is written if and only if the enclosing module
     * is written.  Simple container data is written if and only if the enclosing
     * application is written.
     *
     * @param resultConName
     *
     * @return New result container data.
     */
    @Trivial
    public TargetCacheImpl_DataCon createResultConData(String resultConName) {
        String e_resultConName = encode(resultConName);
        File e_resultConDir = e_getConDir(e_resultConName);
        return createConData(this, resultConName, e_resultConName, e_resultConDir);
    }

    //

    private TargetCacheImpl_DataCon seedCon;
    private TargetCacheImpl_DataCon partialCon;
    private TargetCacheImpl_DataCon excludedCon;
    private TargetCacheImpl_DataCon externalCon;

    private static final String[] RESULT_NAMES = new String[ScanPolicy.values().length];

    static {
        RESULT_NAMES[ ScanPolicy.SEED.ordinal()     ] = TargetCache_ExternalConstants.SEED_RESULT_NAME;
        RESULT_NAMES[ ScanPolicy.PARTIAL.ordinal()  ] = TargetCache_ExternalConstants.PARTIAL_RESULT_NAME;
        RESULT_NAMES[ ScanPolicy.EXCLUDED.ordinal() ] = TargetCache_ExternalConstants.EXCLUDED_RESULT_NAME;
        RESULT_NAMES[ ScanPolicy.EXTERNAL.ordinal() ] = TargetCache_ExternalConstants.EXTERNAL_RESULT_NAME;
    }

    @Trivial
    public static String getResultName(ScanPolicy scanPolicy) {
        return RESULT_NAMES[ scanPolicy.ordinal() ];
    }

    public TargetCacheImpl_DataCon getResultCon(ScanPolicy scanPolicy) {
        String resultConName = getResultName(scanPolicy);

        if ( scanPolicy == ScanPolicy.SEED ) {
            if ( seedCon == null ) {
                seedCon = createResultConData(resultConName);
            }
            return seedCon;

        } else if ( scanPolicy == ScanPolicy.PARTIAL ) {
            if ( partialCon == null ) {
                partialCon = createResultConData(resultConName);
            }
            return partialCon;

        } else if ( scanPolicy == ScanPolicy.EXCLUDED ) {
            if ( excludedCon == null ) {
                excludedCon = createResultConData(resultConName);
            }
            return excludedCon;

        } else if ( scanPolicy == ScanPolicy.EXTERNAL ) {
            if ( externalCon == null ) {
                externalCon = createResultConData(resultConName);
            }
            return externalCon;

        } else {
            throw new IllegalArgumentException("Unknown policy [ " + scanPolicy + " ]");
        }
    }

    public boolean hasResultCon(ScanPolicy scanPolicy) {
        return getResultCon(scanPolicy).exists();
    }

    public boolean readResultCon(ScanPolicy scanPolicy, TargetsTableImpl resultData) {
        return getResultCon(scanPolicy).read(resultData);
    }

    public void writeResultCon(ScanPolicy scanPolicy, TargetsTableImpl resultData) {
        getResultCon(scanPolicy).write(this, resultData);
    }

    //

    private final File unresolvedRefsFile;

    public File getUnresolvedRefsFile() {
        return unresolvedRefsFile;
    }

    public boolean hasUnresolvedRefs() {
        return ( exists( getUnresolvedRefsFile() ) );
    }

    public List<TargetCache_ParseError> basicReadUnresolvedRefs(UtilImpl_InternMap classNameInternMap, Set<String> i_unresolvedClassNames)
        throws FileNotFoundException, IOException {

        return createReader( getUnresolvedRefsFile() ).readUnresolvedRefs(classNameInternMap, i_unresolvedClassNames);
        // 'createReader' throws IOException
        // 'read' throws IOException
    }

    public boolean readUnresolvedRefs(final UtilImpl_InternMap classNameInternMap,
                                      final Set<String> i_unresolvedClassNames) {

        TargetCache_Readable refsReadable = new TargetCache_Readable() {
            @Override
            public List<TargetCache_ParseError> readUsing(TargetCache_Reader reader) throws IOException {
                return basicReadUnresolvedRefs(classNameInternMap, i_unresolvedClassNames);
            }
        };

        long readStart = System.nanoTime();

        boolean didRead = super.read( refsReadable, getUnresolvedRefsFile() );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Unresolved Refs");

        return didRead;
    }

    public void writeUnresolvedRefs(final Set<String> unresolvedClassNames) {
        if ( !shouldWrite("Unresolved class references") ) {
            return;
        }

        final String writeDescription;
        if ( logger.isLoggable(Level.FINER) ) {
            writeDescription = "Container [ " + getName() + " ] Targets [ " + getUnresolvedRefsFile().getPath() + " ]";
        } else {
            writeDescription = null;
        }

        ScheduleCallback writer = new ScheduleCallback() {
            @Override
            public void execute() {
                String methodName = "writeUnresolvedRefs.execute";
                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "ENTER {0}", writeDescription);
                }

                long writeStart = System.nanoTime();

                try {
                    createWriter( getUnresolvedRefsFile() ).writeUnresolvedRefs(unresolvedClassNames); // throws IOException
                } catch ( IOException e ) {
                    // CWWKC00??W
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "ANNO_TARGETS_CACHE_EXCEPTION [ {0} ]", e);
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "Cache error", e);
                }

                @SuppressWarnings("unused")
                long writeDuration = addWriteTime(writeStart, writeDescription);

                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "RETURN {0}", writeDescription);
                }
            }
        };

        scheduleWrite(writer, writeDescription);
    }

    //

    protected final File resolvedRefsFile;

    public File getResolvedRefsFile() {
        return resolvedRefsFile;
    }

    public boolean hasResolvedRefs() {
        return ( exists( getResolvedRefsFile() ) );
    }

    public List<TargetCache_ParseError> basicReadResolvedRefs(
        UtilImpl_InternMap classNameInternMap,
        Set<String> i_resolvedClassNames) throws FileNotFoundException, IOException {

        return createReader( getResolvedRefsFile() ).readResolvedRefs(classNameInternMap, i_resolvedClassNames);
        // 'createReader' throws IOException
        // 'read' throws IOException
    }

    public boolean readResolvedRefs(
        final UtilImpl_InternMap classNameInternMap,
        final Set<String> i_resolvedClassNames) {

        TargetCache_Readable refsReadable = new TargetCache_Readable() {
            @Override
            public List<TargetCache_ParseError> readUsing(TargetCache_Reader reader) throws IOException {
                return basicReadResolvedRefs(classNameInternMap, i_resolvedClassNames);
            }
        };

        long readStart = System.nanoTime();

        boolean didRead = super.read( refsReadable, getResolvedRefsFile() );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Resolved Refs");

        return didRead;
    }

    public void writeResolvedRefs(final Set<String> resolvedClassNames) {
        if ( !shouldWrite("Resolved class references") ) {
            return;
        }

        final String writeDescription;
        if ( logger.isLoggable(Level.FINER) ) {
            writeDescription = "Container [ " + getName() + " ] Targets [ " + getResolvedRefsFile().getPath() + " ]";
        } else {
            writeDescription = null;
        }

        ScheduleCallback writer = new ScheduleCallback() {
            @Override           
            public void execute() {
                String methodName = "writeResolvedRefs.execute";
                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "ENTER {0}", writeDescription);
                }

                long writeStart = System.nanoTime();

                try {
                    createWriter( getResolvedRefsFile() ).writeResolvedRefs(resolvedClassNames); // throws IOException
                } catch ( IOException e ) {
                    // CWWKC00??W
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "ANNO_TARGETS_CACHE_EXCEPTION [ {0} ]", e);
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "Cache error", e);
                }

                @SuppressWarnings("unused")
                long writeDuration = addWriteTime(writeStart, writeDescription);

                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "RETURN {0}", writeDescription);
                }
            }
        };

        scheduleWrite(writer, writeDescription);
    }

    //

    protected final File classRefsFile;

    public File getClassRefsFile() {
        return classRefsFile;
    }

    public boolean hasClassRefs() {
        return ( exists( getClassRefsFile() ) );
    }

    public boolean readClassRefs(TargetsTableClassesMultiImpl classesTable) {
        long readStart = System.nanoTime();

        boolean didRead = read( classesTable, getClassRefsFile() );

        @SuppressWarnings("unused")
        long readDuration = addReadTime(readStart, "Read Class Refs");

        return didRead;
    }

    public void writeClassRefs(final TargetsTableClassesMultiImpl classesTable) {
        if ( !shouldWrite("Class relationship table") ) {
            return;
        }

        final String writeDescription;
        if ( logger.isLoggable(Level.FINER) ) {
            writeDescription = "Container [ " + getName() + " ] Targets [ " + getClassRefsFile().getPath() + " ]";
        } else {
            writeDescription = null;
        }

        ScheduleCallback writer = new ScheduleCallback() {
            @Override
            public void execute() {
                String methodName = "writeClassRefs.execute";
                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "ENTER {0}", writeDescription);
                }

                long writeStart = System.nanoTime();

                try {
                    // See the comment on 'mergeClasses': This must be synchronized
                    // with updates to the class table which occur in 
                    // TargetsScannerImpl_Overall.validExternal'.
                    synchronized ( classesTable ) {
                        createWriter( getClassRefsFile() ).write(classesTable); // 'write' throws IOException
                    }
                } catch ( IOException e ) {
                    // CWWKC00??W
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "ANNO_TARGETS_CACHE_EXCEPTION [ {0} ]", e);
                    logger.logp(Level.WARNING, CLASS_NAME, methodName, "Cache error", e);
                }

                @SuppressWarnings("unused")
                long writeDuration = addWriteTime(writeStart, writeDescription);

                if ( writeDescription != null ) {
                    logger.logp(Level.FINER, CALLBACK_CLASS_NAME, methodName, "RETURN {0}", writeDescription);
                }
            }
        };

        scheduleWrite(writer, writeDescription);
    }

    //

    public boolean shouldAppRead(String inputDescription) {
        return getApp().shouldWrite(inputDescription);
    }

    @Override
    public boolean shouldRead(String inputDescription) {
        if ( !isNamed() || getIsLightweight() ) {
            return false;
        } else if ( !shouldAppRead(inputDescription) ) {
            return false;
        } else {
            return super.shouldRead(inputDescription);
        }
    }

    public boolean shouldAppWrite(String outputDescription) {
        return getApp().shouldWrite(outputDescription);
    }

    @Override
    public boolean shouldWrite(String outputDescription) {
        if ( !isNamed() || getIsLightweight() ) {
            return false;
        } else if ( !shouldAppWrite(outputDescription) ) {
            return false;
        } else {
            return super.shouldWrite(outputDescription);
        }
    }

    // Handle writes at the module level and below at the module level.
    //
    // Writes are simple: Most can be simply spawned and allowed to complete
    // in their own time.
    //
    // Reads require additional coordination (joins for the results) and are
    // handled by the calling code.

    protected final UtilImpl_PoolExecutor writePool;

    @Trivial
    protected UtilImpl_PoolExecutor getWritePool() {
        return writePool;
    }

    @Trivial
    protected void scheduleWrite(final ScheduleCallback writer, String description) {
        String methodName = "scheduleWrite";

        final Throwable scheduler =
            new Throwable("ModData [ " + getName() + " ] [ " + e_getName() + " ] [ " + description + " ]");

        UtilImpl_PoolExecutor useWritePool = getWritePool();
        if ( useWritePool == null ) {
            if ( logger.isLoggable(Level.FINER) ) {
                logger.logp(Level.FINER, CLASS_NAME, methodName,
                    "ENTER (immediate) [ {0} ] [ {1} ] [ {2} ]",
                    new Object[] { getName(), writer, description });
            }

            writer.execute();

            if ( logger.isLoggable(Level.FINER) ) {
                logger.logp(Level.FINER, CLASS_NAME, methodName,
                    "RETURN (immediate) [ {0} ] [ {1} ] [ {2} ]",
                    new Object[] { getName(), writer, description });
            }

        } else {
            Runnable writeRunner = new Runnable() {
                @Override
                public void run() {
                    String methodName = "scheduleWrite.run";
                    try {
                        // System.out.println("BEGIN write [ " + writer + " ]");
                        writer.execute();
                        // System.out.println("END write [ " + writer + " ]");

                    } catch ( RuntimeException e ) {
                        // Capture and display any exception from the spawned writer thread.
                        // Without this added step information about the spawning thread is
                        // lost, making debugging writer problems very difficult.

                        logger.logp(Level.WARNING, CLASS_NAME, methodName, "Caught Asynchronous exception", e);
                        logger.logp(Level.WARNING, CLASS_NAME, methodName, "Scheduler", scheduler);
                        logger.logp(Level.WARNING, CLASS_NAME, methodName, "Synchronization error", e);

                        throw e;
                    }
                }
            };

            if ( logger.isLoggable(Level.FINER) ) {
                logger.logp(Level.FINER, CLASS_NAME, methodName,
                    "ENTER (scheduled) [ {0} ] [ {1} ] [ {2} ]",
                    new Object[] { getName(), writer, description });
            }

            useWritePool.execute(writeRunner);

            if ( logger.isLoggable(Level.FINER) ) {
                logger.logp(Level.FINER, CLASS_NAME, methodName,
                    "RETURN (scheduled) [ {0} ] [ {1} ] [ {2} ]",
                    new Object[] { getName(), writer, description });
            }
        }
    }
}
