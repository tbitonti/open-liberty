// 1.2, 3/6/06
// IBM Confidential OCO Source Material
// 5724-I63, 5724-H88, 5655-N01, 5733-W60 (C) COPYRIGHT International Business Machines Corp. 1997, 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.htod;
import java.util.Properties;

/**
 * This helper class is a simple collection of numbers.
 * The numbers correspond to those numbers stored in cacheinstance.properties
 *
 * One instance of this class is associated with one cache instance.
 * For example...
 *       # SERVLET CUSTOM - for EvictionTest
 *       cache.instance.14=services/cache/htodServletInstance_14
 *       cache.instance.14.cacheSize = 100
 *       cache.instance.14.diskCacheSize = 500
 *       cache.instance.14.enableDiskOffload = true
 *       cache.instance.14.disableTemplatesSupport = false
 *       cache.instance.14.diskCachePerformanceLevel = 2
 *       cache.instance.14.htodDelayOffloadDepIdBuckets = 100
 *       cache.instance.14.htodDelayOffloadTemplateBuckets = 100
 *       cache.instance.14.htodDelayOffloadEntriesLimit = 100
 *       cache.instance.14.enableServletSupport = true
 *       cache.instance.14.diskCacheEvictionPolicy = 1
 *       cache.instance.14.diskCacheHighThreshold = 75
 *       cache.instance.14.diskCacheLowThreshold = 50
 *       cache.instance.14.diskCacheSizeInGB = 5
 */
public class CacheInstanceProps {

    //-------------------------------------------------------------------------
    // Member variables.
    //-------------------------------------------------------------------------

    /**
     * List of interesting values from cacheinstances.properties with
     * default unspecified or uninitialized values.
     */
    public String  name = "UNSPECIFIED";
    public int     cacheSize = -999;
    public int     diskCacheSize = -999;
    public boolean enableDiskOffload = false;
    public boolean disableTemplatesSupport = false;
    public int     diskCachePerformanceLevel = -999;
    public int     htodDelayOffloadDepIdBuckets = -999;
    public int     htodDelayOffloadTemplateBuckets = -999;
    public int     htodDelayOffloadEntriesLimit = -999;
    public boolean enableServletSupport = false;
    public int     diskCacheEvictionPolicy = -999;
    public int     diskCacheHighThreshold = -999;
    public int     diskCacheLowThreshold = -999;
    public int     diskCacheSizeInGB = -999;
    // Note: Whenever you add a new member variable, be sure to add
    // them to all the supporting methods below.

    //-------------------------------------------------------------------------
    // Public methods.
    //-------------------------------------------------------------------------

    /**
     * Class constructor.
     */
    public CacheInstanceProps() {
    }

    /**
     * Returns an instance of this class loaded with values extracted from
     * the supplied Properties object and corresponding to the specified
     * cache instance number.
     */
    public static CacheInstanceProps getInstance(int cacheNumber,
                                                 Properties properties) {
        String m = "getInstance: ";
        sop(m,"Entry. cacheNumber=" + cacheNumber);

        // Search for the specified cache instance in the properties object.
        //
        String key = "cache.instance." + cacheNumber;
        String name = properties.getProperty(key);
        if (null == name) {
            sop(m,"Exit. Error. Could not find cache instance name in properties. Returning null.");
            return null;
        }
        CacheInstanceProps cip = new CacheInstanceProps();
        cip.name = name;

        // Assimilate known properties from the properties object into this object.
        //
        key = key + ".";
        cip.cacheSize = getIntProperty(properties,key + "cacheSize",cip.cacheSize);
        cip.diskCacheSize = getIntProperty(properties,key + "diskCacheSize",cip.diskCacheSize);
        cip.enableDiskOffload = getBoolProperty(properties,key + "enableDiskOffload",cip.enableDiskOffload);
        cip.disableTemplatesSupport = getBoolProperty(properties,key + "disableTemplatesSupport",cip.disableTemplatesSupport);
        cip.diskCachePerformanceLevel = getIntProperty(properties,key + "diskCachePerformanceLevel",cip.diskCachePerformanceLevel);
        cip.htodDelayOffloadDepIdBuckets = getIntProperty(properties,key + "htodDelayOffloadDepIdBuckets",cip.htodDelayOffloadDepIdBuckets);
        cip.htodDelayOffloadTemplateBuckets = getIntProperty(properties,key + "htodDelayOffloadTemplateBuckets",cip.htodDelayOffloadTemplateBuckets);
        cip.htodDelayOffloadEntriesLimit = getIntProperty(properties,key + "htodDelayOffloadEntriesLimit",cip.htodDelayOffloadEntriesLimit);
        cip.enableServletSupport = getBoolProperty(properties,key + "enableServletSupport",cip.enableServletSupport);
        cip.diskCacheEvictionPolicy = getIntProperty(properties,key + "diskCacheEvictionPolicy",cip.diskCacheEvictionPolicy);
        cip.diskCacheHighThreshold = getIntProperty(properties,key + "diskCacheHighThreshold",cip.diskCacheHighThreshold);
        cip.diskCacheLowThreshold = getIntProperty(properties,key + "diskCacheLowThreshold",cip.diskCacheLowThreshold);
        cip.diskCacheSizeInGB = getIntProperty(properties,key + "diskCacheSizeInGB",cip.diskCacheSizeInGB);

        sop(m,"Exit. Returning cip=" + cip);
        return cip;
    }

    /**
     * Gets a human-readable representation of this object.
     */
    public String toString() {
        StringBuffer sb= new StringBuffer();

        try {
            sb.append("CacheInstanceProperteies: ");
            sb.append("\nname=").append(name);
            sb.append("\ncacheSize=").append(cacheSize);
            sb.append("\ndiskCacheSize=").append(diskCacheSize);
            sb.append("\nenableDiskOffload=").append(enableDiskOffload);
            sb.append("\ndisableTemplatesSupport=").append(disableTemplatesSupport);
            sb.append("\ndiskCachePerformanceLevel=").append(diskCachePerformanceLevel);
            sb.append("\nhtodDelayOffloadDepIdBuckets=").append(htodDelayOffloadDepIdBuckets);
            sb.append("\nhtodDelayOffloadTemplateBuckets=").append(htodDelayOffloadTemplateBuckets);
            sb.append("\nhtodDelayOffloadEntriesLimit=").append(htodDelayOffloadEntriesLimit);
            sb.append("\nenableServletSupport=").append(enableServletSupport);
            sb.append("\ndiskCacheEvictionPolicy=").append(diskCacheEvictionPolicy);
            sb.append("\ndiskCacheHighThreshold=").append(diskCacheHighThreshold);
            sb.append("\ndiskCacheLowThreshold=").append(diskCacheLowThreshold);
            sb.append("\ndiskCacheSizeInGB=").append(diskCacheSizeInGB);
        }
        catch(Exception e) {
            return "CacheInstanceProperteies: Error formatting toString. e=" + e;
        }
        return sb.toString();
    }

    //-------------------------------------------------------------------------
    // Private methods.
    //-------------------------------------------------------------------------

    /**
     * Helper method to write to the console.
     */
    private static void sop(String m, String msg) {
        System.out.println("CacheInstanceProps/" + m + msg);
    }

    /**
     * Helper method to extract a bool value from a properties object.
     * Returns the original value if the property is not found.
     */
    private static boolean getBoolProperty(Properties properties, String key, boolean originalValue) {
        String m = "getBoolProperty: ";

        String newValueString = properties.getProperty(key);
        if (null == newValueString) {
            sop(m,"Warning: Did not find property. Returning original value. key=" + key);
            return originalValue;
        }
        else {
            try {
                boolean b = new Boolean(newValueString).booleanValue();
                // sop(m,"Exit. Extracted property. key=" + key + " value=" + b);
                return b;
            }
            catch (Exception e) {
                sop(m,"Warning: Caught exception extracting property. Returning original value. key=" + key + " e=" + e);
                return originalValue;
            }
        }
    }

    /**
     * Helper method to extract an int value from a properties object.
     * Returns the original value if the property is not found.
     */
    private static int getIntProperty(Properties properties, String key, int originalValue) {
        String m = "getIntProperty: ";

        String newValueString = properties.getProperty(key);
        if (null == newValueString) {
            sop(m,"Warning: Did not find property. Returning original value. key=" + key);
            return originalValue;
        }
        else {
            try {
                int i = new Integer(newValueString).intValue();
                // sop(m,"Exit. Extracted property. key=" + key + " value=" + i);
                return i;
            }
            catch (Exception e) {
                sop(m,"Warning: Caught exception extracting property. Returning original value. key=" + key + " e=" + e);
                return originalValue;
            }
        }
    }

    /**
     * Helper method to extract a String value from a properties object.
     * Returns the original value if the property is not found.
     *
     * Note: This seems to be a dumb method, since Properties objects contain strings,
     * but its value comes from returning the originalValue if the key is not found!
     */
    private static String getStringProperty(Properties properties, String key, String originalValue) {
        String m = "getStringProperty: ";

        String newValueString = properties.getProperty(key);
        if (null == newValueString) {
            sop(m,"Warning: Did not find property. Returning original value. key=" + key);
            return originalValue;
        }
        else {
            try {
                sop(m,"Exit. Extracted property. key=" + key + " value=" + newValueString);
                return newValueString;
            }
            catch (Exception e) {
                sop(m,"Warning: Caught exception extracting property. Returning original value. key=" + key + " e=" + e);
                return originalValue;
            }
        }
    }
}

