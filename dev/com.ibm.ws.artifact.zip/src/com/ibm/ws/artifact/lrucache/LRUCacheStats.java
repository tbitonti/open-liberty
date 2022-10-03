package com.ibm.ws.artifact.lrucache;

import static com.ibm.ws.artifact.lrucache.util.ArrayUtil.*;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LRUCacheStats {
    public final int valueCountLimit;
    public final long valueSizeLimit;
    public final long valueTotalLimit;

    //

    public class Stats {
        protected Stats(String name, int sizeCount) {
            this.name = name;

            this.count = 0;
            this.sizes = alloc(sizeCount);
            this.total = 0L;
            this.max = 0L;
        }

        protected Stats(Stats other) {
            this.name = other.name;

            this.count = other.count;
            this.sizes = other.sizes.clone();
            this.total = other.total;
            this.max = other.max;
        }

        public final String name;

        public int count;
        public int[] sizes;
        public long total;
        public long max;

        public void add(long size) {
            count++;
            total += size;
            int[] newSizes = update(sizes, size, IS_ADD);
            if ( newSizes != null ) {
                sizes = newSizes;
            }
            if ( size > max ) {
                max = size;
            }
        }

        public void remove(long size) {
            count--;
            total -= size;
            int[] newSizes = update(sizes, size, !IS_ADD);
            if ( newSizes != null ) {
                sizes = newSizes;
            }
        }
    }
    
    public final Stats current;
    public final Stats get;
    public final Stats unselected;
    public final Stats tooBig;
    public final Stats hit;
    public final Stats miss;
    public final Stats remove;

    public long currentAvailable;

    //

    private static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    protected static String now() {
        return DATE_FORMAT.format( new Date() );
    }
    
    protected final int valueDigits;
    protected final String numFormat;

    protected String formatCount(int count) {
        return String.format(numFormat, count);
    }
                
    protected String formatSize(long size) {
        return String.format(numFormat, size);
    }

    protected final String lineFormat;

    protected String formatLine(String description, int count, long total, String extraDescription, long extra) {
        return String.format(lineFormat, description, count, total, extraDescription, extra);
    }
    
    protected String formatLine(Stats stats) {
        return formatLine(stats.name, stats.count, stats.total, "Max", stats.max);
    }
    
    //

    public LRUCacheStats(LRUCacheStats other) {
        this.valueCountLimit = other.valueCountLimit;
        this.valueSizeLimit = other.valueSizeLimit;
        this.valueTotalLimit = other.valueTotalLimit;

        this.current = new Stats(other.current);
        this.get = new Stats(other.get);
        this.unselected = new Stats(other.unselected);
        this.tooBig = new Stats(other.tooBig);
        this.hit = new Stats(other.hit);
        this.miss = new Stats(other.miss);
        this.remove = new Stats(other.remove);

        this.valueDigits = other.valueDigits;
        this.numFormat = other.numFormat;
        this.lineFormat = other.lineFormat;
    }
    
    public LRUCacheStats(LRUCachePolicy<?, ?> policy) {
        this(policy.maxValueCount(), policy.maxValueSize(), policy.maxTotalSize() );
    }
    
    public LRUCacheStats(int valueCountLimit, long valueSizeLimit, long valueTotalLimit) {
        super();

        this.valueCountLimit = valueCountLimit;
        this.valueSizeLimit = valueSizeLimit;
        this.valueTotalLimit = valueTotalLimit;

        int allocTo = log2(valueSizeLimit);

        this.current = new Stats("Current", allocTo);
        this.get = new Stats("Get", allocTo);
        this.unselected = new Stats("Rejected", allocTo);
        this.tooBig = new Stats("Too Big", allocTo);
        this.hit = new Stats("Hit", allocTo);
        this.miss = new Stats("Miss", allocTo);
        this.remove = new Stats("Remove", allocTo);
        
        long useSizeLimit = ((this.valueSizeLimit == -1L) ? 10000000L : this.valueSizeLimit);
        this.valueDigits = Long.toString(useSizeLimit).length();
        this.numFormat = "%" + valueDigits + "d";
        this.lineFormat = "%12s: Count [ " + numFormat + " ] Total [ " + numFormat + " ] %s [ " + numFormat + " ]";
    }

    public void describe(PrintStream output, String title) {
        output.println("LRUCache [ " + title + " ]");
        output.println("  At [ " + now() + " ]");
        output.println();
        output.println(formatLine("Limit", valueCountLimit, valueTotalLimit, "Single", valueSizeLimit));
        output.println();                
        output.println(formatLine(current.name, current.count, current.total, "Available", currentAvailable));
        output.println();        
        output.println(formatLine(get));
        output.println(formatLine(unselected));
        output.println(formatLine(tooBig));
        output.println(formatLine(hit));
        output.println(formatLine(miss));
        output.println(formatLine(remove));

        formatSizes(output, current, get, unselected, tooBig, hit, miss, remove);
    }

    protected void formatSizes(PrintStream output, Stats... stats) {
        int max = max(stats);
        if ( max < 3 ) {
            max = 3;
        }

        String nameFormat = "| %11s ||";
        String maxText = Integer.toString(max);
        String cellFormat = " %" + maxText + "d |";
        
        int maxBucketSize = 0;
        for ( int statNo = 0; statNo < stats.length; statNo++ ) {
            int statCount = stats[statNo].sizes.length;
            if ( statCount > maxBucketSize ) {
                maxBucketSize++;
            }
        }

        StringBuilder lineBuilder = new StringBuilder();

        lineBuilder.append("            ||");
        lineBuilder.append( String.format(cellFormat, 0) );

        for ( int sizeNo = 0; sizeNo < maxBucketSize; sizeNo++ ) {
            lineBuilder.append( String.format(cellFormat, sizeNo + 1) );
        }
        output.println(lineBuilder.toString());
        lineBuilder.setLength(0);        
        
        for ( int statNo = 0; statNo < stats.length; statNo++ ) {
            Stats next = stats[statNo];
            String name = next.name;
            int[] sizes = next.sizes;

            lineBuilder.append( String.format(nameFormat, name) );
            for ( int size : sizes ) {
                lineBuilder.append( String.format(cellFormat, size) ) ;
            }
            output.println(lineBuilder.toString());
            lineBuilder.setLength(0);
        }
    }

    protected static int max(int... values) {
        int max = -1;
        for ( int value : values ) {
            if ( value > max ) {
                max = value;
            }
        }
        return max;
    }
    
    protected static int max(int[] ... buckets) {
        int max = -1;
        for ( int[] bucket : buckets ) {
            int bucketMax = max(bucket);
            if ( bucketMax > max ) {
                max = bucketMax;
            }
        }
        return max;
    }
    
    protected static int max(Stats ... stats) {
        int max = -1;
        for ( Stats stat : stats ) {
            int statMax = max(stat.sizes);
            if ( statMax > max ) {
                max = statMax;
            }
        }
        return max;
    }    
    
    // These are intended to be externally protected.

    /**
     * Tell if a cell must be removed to make space for a value
     * of a specified size.
     * 
     * A cell must be removed if either the maximum count of values
     * has been reached, or, if the available space is less than the
     * size which is needed.
     *
     * Each invocation can be used to remove one cell.  This may not
     * be enough to make room: Multiple invocations and removals may
     * be necessary.
     * 
     * The size must be less than the maximum allowed size.  This
     * guarantees that eventually, there will be enough available
     * space for the value.
     * 
     * @param valueSize The size of the value which is to be added.
     *
     * @return True or false telling if a cell must be removed to
     *     make room for the value.
     */
    protected boolean mustRemove(long valueSize) {
        if ((valueCountLimit != -1) && (current.count == valueCountLimit) ) {
            return true;
        } else if ((valueTotalLimit != -1L) && (valueSize > currentAvailable)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Cache events:
     * <ul>
     * <li>{@link EVENT#UNSELECTED}: A key was not selected by
     *     {@link LRUCachePolicy#select}.  The value was not cached.</li>
     * <li>{@link EVENT#TOO_BIG}: A value was larger than
     *     {@link LRUCachePolicy#maxValueSize()}.  The value was not cached.</li>
     * <li>{@link EVENT#HIT}: A key was found in the cache.  The previously
     *     cached value was used.</li>
     * <li>{@link EVENT#GROW}: A key was not found in the cache.  The value
     *     for that key was stored in the cache.</li>
     * <li>{@link EVENT#REMOVE}: The cache does not have enough space to store
     *     a value.  A value was removed from the cache to make space for the value.</li>
     * </ul>
     */
    protected enum EVENT {
        UNSELECTED, TOO_BIG,
        HIT,
        GROW,
        REMOVE
    }

    /**
     * Record cache activity.
     * 
     * @param valueSize A value size associated with the event.
     * 
     * @return The space available in the cache after the event.
     */
    protected long recordRemove(long valueSize) {
        remove.add(valueSize);
        current.remove(valueSize);
        currentAvailable += valueSize;
        
        return currentAvailable;
    }
    
    protected void recordUnselected(long valueSize) {
        get.add(valueSize);
        unselected.add(valueSize);
    }
    
    protected void recordTooBig(long valueSize) {
        get.add(valueSize);
        tooBig.add(valueSize);
    }
    
    protected void recordHit(long valueSize) {
        get.add(valueSize);
        hit.add(valueSize);
    }
    
    protected long recordMiss(long valueSize) {
        get.add(valueSize);

        miss.add(valueSize);
        current.add(valueSize);
        currentAvailable -= valueSize;
        
        return currentAvailable;
    }
}
