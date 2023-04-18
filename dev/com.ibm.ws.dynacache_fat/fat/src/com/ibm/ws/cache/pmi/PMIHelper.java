// 1.11,7/1/08
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2008
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.pmi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.ws.cache.stat.internal.WSDynamicCacheStats;

final public class PMIHelper {

    //    Please read this if you want to take a shot at understanding this program
    //    BTW Best of luck trying to figure this out ....you will need it :-)
    //    PMI Tree for the DynaCache module
    //
    //    cacheModule   (18)
    //
    //        Servlet: <Instance>   (2)    
    //              cacheModule.template    (15)
    //                  <template instance>     (15)
    //              cacheModule.disk    (0)    
    //                  cacheModule.diskOffloadEnabled    (15)
    //                      
    //        Object: <Instance>    (2)
    //              cacheModule.objectCache (15)
    //                  cacheModule.counters    (15)            
    //              cacheModule.disk    (0)    
    //                  cacheModule.diskOffloadEnabled    (15)

    public static boolean debug = false;

    public static final String CACHE_SERVLET =
                    "cacheModule;Servlet: baseCache"; // 2 counters

    public static final String CACHE_SERVLET_TEMPLATE =
                    "cacheModule;Servlet: baseCache;cacheModule.template"; //15 counters

    public static final String CACHE_SERVLET_CUSTOM = //note this needs to be appended by the instance no.
    "cacheModule;Servlet: services/cache/htodServletInstance_"; //2 counters

    public static final String CACHE_OBJECT =
                    "cacheModule;Object: "; //2 counters

    public static final String OBJECT_MODULE = "cacheModule.objectCache";

    public static final String CACHE_OBJECT_CUSTOM =
                    "cacheModule;Object: services/cache/htodObjectInstance_"; //18 counters

    // 2 at the top  
    public static final String MaxInMemoryCacheEntryCount = "MaxInMemoryCacheEntryCount";
    public static final String InMemoryCacheEntryCount = "InMemoryCacheEntryCount";

    //15 counters 
    public static final String HitsInMemoryCount = "HitsInMemoryCount";
    public static final String HitsOnDiskCount = "HitsOnDiskCount";
    public static final String ExplicitInvalidationCount = "ExplicitInvalidationCount";
    public static final String LruInvalidationCount = "LruInvalidationCount";
    public static final String TimeoutInvalidationCount = "TimeoutInvalidationCount";
    public static final String InMemoryAndDiskCacheEntryCount = "InMemoryAndDiskCacheEntryCount";
    public static final String RemoteHitCount = "RemoteHitCount";
    public static final String MissCount = "MissCount";
    public static final String ClientRequestCount = "ClientRequestCount";
    public static final String DistributedRequestCount = "DistributedRequestCount";
    public static final String ExplicitMemoryInvalidationCount = "ExplicitMemoryInvalidationCount";
    public static final String ExplicitDiskInvalidationCount = "ExplicitDiskInvalidationCount";
    public static final String LocalExplicitInvalidationCount = "LocalExplicitInvalidationCount";
    public static final String RemoteExplicitInvalidationCount = "RemoteExplicitInvalidationCount";
    public static final String RemoteCreationCount = "RemoteCreationCount";

    //15 disk cache counters 
    public static final String ObjectsOnDisk = "ObjectsOnDisk";
    public static final String HitsOnDisk = "HitsOnDisk";
    public static final String ExplicitInvalidationsFromDisk = "ExplicitInvalidationsFromDisk";
    public static final String TimeoutInvalidationsFromDisk = "TimeoutInvalidationsFromDisk";
    public static final String PendingRemovalFromDisk = "PendingRemovalFromDisk";
    public static final String DependencyIdsOnDisk = "DependencyIdsOnDisk";
    public static final String DependencyIdsBufferedForDisk = "DependencyIdsBufferedForDisk";
    public static final String DependencyIdsOffloadedToDisk = "DependencyIdsOffloadedToDisk";
    public static final String DependencyIdBasedInvalidationsFromDisk = "DependencyIdBasedInvalidationsFromDisk";
    public static final String TemplatesOnDisk = "TemplatesOnDisk";
    public static final String TemplatesBufferedForDisk = "TemplatesBufferedForDisk";
    public static final String TemplatesOffloadedToDisk = "TemplatesOffloadedToDisk";
    public static final String TemplateBasedInvalidationsFromDisk = "TemplateBasedInvalidationsFromDisk";
    public static final String GarbageCollectorInvalidationsFromDisk = "GarbageCollectorInvalidationsFromDisk";
    public static final String OverflowInvalidationsFromDisk = "OverflowInvalidationsFromDisk";

    public static boolean initialized = false;

    public static final String[] cacheCountersName = new String[150];

    public static final int TYPE_ADD = 0;
    public static final int TYPE_SUBTRACT = 1;
    public static final int TYPE_SET = 2;

    public static int parseCounters(String counters, Map map) throws Exception {
        boolean finish = false;
        int count = 0;
        do {
            int index = counters.indexOf(";");
            if (index > 0) {
                String counter = counters.substring(0, index);
                index++;
                if (index >= counters.length() - 2) {
                    finish = true;
                } else {
                    counters = counters.substring(index);
                }
                parseCounter(counter, map);
                count++;
            } else if (index <= 0) {
                throw new Exception("Missing \"=\" in the string (" + counters + ")");
            }
        } while (finish == false);
        return count;
    }

    static public void parseCounter(String counter, Map map) throws Exception {
        //System.out.println("**** counter: " + counter);
        if (!initialized) {
            initializeCacheNames();
        }
        int index = counter.indexOf("=");
        String id = "";
        String value = "";
        if (index > 0) {
            id = counter.substring(0, index);
            String name = cacheCountersName[Integer.parseInt(id)];
            value = counter.substring(index + 1);
            //System.out.println("** counter=" + counter + " id=" + id + " name=" + name + " value=" + value);
            map.put(name, value);
        } else {
            throw new Exception("Missing \"=\" in the string (" + counter + ")");
        }
    }

    public static int parseInstances(String instances, Map map) throws Exception {
        boolean finish = false;
        int count = 0;
        do {
            int index = instances.indexOf(";");
            if (index > 0) {
                String instance = instances.substring(0, index);
                index++;
                if (index >= instances.length() - 2) {
                    finish = true;
                } else {
                    instances = instances.substring(index);
                }
                parseInstance(instance, map);
                count++;
            } else if (index <= 0) {
                throw new Exception("Missing \"=\" in the string (" + instances + ")");
            }
        } while (finish == false);
        return count;
    }

    static public void parseInstance(String instance, Map map) throws Exception {
        //System.out.println("**** instance: " + instance);
        int index = instance.indexOf("=");
        String id = "";
        String value = "";
        if (index > 0) {
            id = instance.substring(0, index);
            value = instance.substring(index + 1);
            //System.out.println("** instance=" + instance + " id=" + id + " value=" + value);
            map.put(id, value);
        } else {
            throw new Exception("Missing \"=\" in the string (" + instance + ")");
        }
    }

    /* Used by PMITest */
    static public void setExpectedCounter(Map map, int id, int count, int type) throws Exception {

        String name = cacheCountersName[id];
        String svalue = (String) map.get(name);
        if (svalue == null) {
            throw new Exception("No value for name (" + name + ")");
        }

        int value = Integer.parseInt(svalue);

        if (type == TYPE_ADD) {
            value = value + count;
        } else if (type == TYPE_SUBTRACT) {
            value = value - count;
        } else {
            value = count;
        }
        svalue = String.valueOf(value);
        map.put(name, svalue);
        return;
    }

    static public void setExpectedCounter(
                                          Map[] maps, // map for that counter level
                                          int id, //counter id
                                          int count, //value
                                          int type // add or subtract   
    ) throws Exception {

        /*
         * Understanding the DynaCache PMI tree is critical in understanding this method
         * A servlet map array is of size 5 and contains....
         * d1_servlet_1_maps[0] = d1_e_cm_hmap; 0 pmi ctrs i = 0
         * d1_servlet_1_maps[2] = d1_e_cm_servlet_bc_hmap; 2 pmi ctrs i = 1
         * d1_servlet_1_maps[3] = d1_e_cm_servlet_bc_template_hmap; 15 pmi ctrs i = 2
         * d1_servlet_1_maps[4] = d1_e_cm_servlet_bc_template_t1_hmap; 15 pmi ctrs i = 3
         * 
         * God forgive me for not simplifying this method !
         */

        if ((maps.length == 4)) { // for servlet PMI counters

            for (int i = 0; i < maps.length; i++) {

                if (debug) {
                    System.out.println("i:" + i + " id: " + id);
                }

                //d1_e_cm_hmap is the topmost level with no counters
                if (i == 0) {
                    if (debug) {
                        System.out.println("skipping for  i = 0 ");
                    }
                    continue; //dont load the counter

                }

                //we do not want to load d1_e_cm_servlet_bc_hmap
                //with ids other than MaxInMemoryCacheEntryCount & InMemoryCacheEntryCount           
                else if ((i == 1) &&
                            (id != WSDynamicCacheStats.MaxInMemoryCacheEntryCount &&
                                id != WSDynamicCacheStats.InMemoryCacheEntryCount)) {

                    if (debug) {
                        System.out.println("skipping for i = 1 ");
                    }
                    continue;
                }

                // we do not want to store any ServletMaxInMemoryCacheEntryCount and  ServletInMemoryCacheEntryCount 
                //in d1_e_cm_servlet_bc_template_hmap and d1_e_cm_servlet_bc_template_t1_hmap    
                else if ((i == 2 || i == 3)
                         && (id == WSDynamicCacheStats.MaxInMemoryCacheEntryCount
                            || id == WSDynamicCacheStats.InMemoryCacheEntryCount)) {

                    if (debug) {
                        System.out.println("skipping for (2 || 3) ");
                    }
                    continue;

                } else {

                    loadCounter(maps, id, count, type, i);

                }
            }

        } else if (maps.length == 3) { //for Dmap PMI counters

            /*
             * A Dmap array is of size 3 and contains....
             * d1_object_1_maps[0] = d1_e_cm_hmap; ctr = 0 i = 0
             * d1_object_1_maps[1] = d1_e_cm_object_default_hmap; ctr = 2 i = 1
             * d1_object_1_maps[2] = d1_e_cm_object_dmap1_hmap; ctr = 15 i = 2
             */
            for (int i = 0; i < maps.length; i++) {

                if (debug) {
                    System.out.println("i:" + i + " id: " + id);
                }

                // d1_e_cm_hmap is the topmost level 
                if ((i == 0)) {
                    if (debug) {
                        System.out.println("skipping for i = 0 ");
                    }
                    continue;

                }

                //we do not want to load d1_e_cm_object_hmap with ids other than 
                //ObjectMaxInMemoryCacheEntryCount and ObjectInMemoryCacheEntryCount           
                else if ((i == 1) &&
                            (id != WSDynamicCacheStats.MaxInMemoryCacheEntryCount &&
                                id != WSDynamicCacheStats.InMemoryCacheEntryCount)) {
                    if (debug) {
                        System.out.println("skipping for i = 1 ");
                    }
                    continue;
                }

                // all counters are needed for d1_e_cm_object_dmap1_hmap    
                else if ((i == 2) &&
                            (id == WSDynamicCacheStats.MaxInMemoryCacheEntryCount
                                || id == WSDynamicCacheStats.InMemoryCacheEntryCount)) {
                    if (debug) {
                        System.out.println("skipping for i = 2 ");
                    }
                    continue;
                } else {

                    loadCounter(maps, id, count, type, i);
                }
            }
        }

        if (debug) {
            printMaps(maps);
        }

        return;
    }

    private static void loadCounter(Map[] maps, int id, int count, int type, int i)
                    throws Exception {

        String name = cacheCountersName[id];
        String svalue = (String) maps[i].get(name);
        if (svalue == null) {
            throw new Exception("No value for name (" + name + ")");
        }
        int value = Integer.parseInt(svalue);
        if (type == TYPE_ADD) {
            value = value + count;
        } else if (type == TYPE_SUBTRACT) {
            value = value - count;
        }
        svalue = String.valueOf(value);
        maps[i].put(name, svalue);

        if (debug) {
            System.out.println("loading " + name + ":" + svalue);
        }
    }

    public static void printMaps(Map[] maps) {
        for (int i = 0; i < maps.length; i++) {
            System.out.println("maps[" + i + "]=" + maps[i]);
        }
        System.out.println();
    }

    static public int getCounter(Map map, int id) throws Exception {

        String name = cacheCountersName[id];
        String svalue = (String) map.get(name);
        if (svalue == null) {
            throw new Exception("No value for name (" + name + ")");
        }
        int value = Integer.parseInt(svalue);
        return value;
    }

    static public void compareCounters(String counter, Map map1, Map map2) throws Exception {

        Set entries = map1.entrySet();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value1 = (String) entry.getValue();
            String value2 = (String) map2.get(key);
            if (!value1.equals(value2)) {
                System.out.println("PMICounter: " + counter);
                System.out.println("Expected: " + map1);
                System.out.println("Received: " + map2);
                throw new Exception("counter value not equals for (" + key + ") expected=" + value1 + " but received=" + value2);
            }
        }
    }

    static public void compareCountersInRange(String counter, Map map1, Map map2, int delta, Set approxCounters) throws Exception {

        Set entries = map1.entrySet();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value1 = (String) entry.getValue();
            String value2 = (String) map2.get(key);

            if (approxCounters.contains(key)) {

                long val1 = new Long(value1).longValue();
                long lowThreshold = val1 - delta;
                long highThreshold = val1 + delta;
                long val2 = new Long(value2).longValue();

                if (lowThreshold > val2 || val2 > highThreshold) {
                    System.out.println("PMICounter: " + counter);
                    System.out.println("Expected: " + map1);
                    System.out.println("Received: " + map2);
                    throw new Exception("counter value not equals for (" + key + ") expected value in range=[" + lowThreshold + " - " + highThreshold + "] but received=" + value2);
                }
            } else {
                if (!value1.equals(value2)) {
                    System.out.println("PMICounter: " + counter);
                    System.out.println("Expected: " + map1);
                    System.out.println("Received: " + map2);
                    throw new Exception("counter value not equals for (" + key + ") expected=" + value1 + " but received=" + value2);
                }
            }

        }
    }

    static public void compareCountersInRangeMap(String counter, Map map1, Map map2, Map<String, Long> approxCounters) throws Exception {

        Set entries = map1.entrySet();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value1 = (String) entry.getValue();
            String value2 = (String) map2.get(key);

            if (approxCounters.keySet().contains(key)) {

                long deltaValueForIndividualCounter = approxCounters.get(key);

                long val1 = new Long(value1).longValue();
                long lowThreshold = val1 - deltaValueForIndividualCounter;
                long highThreshold = val1 + deltaValueForIndividualCounter;
                long val2 = new Long(value2).longValue();

                if (lowThreshold > val2 || val2 > highThreshold) {
                    System.out.println("PMICounter: " + counter);
                    System.out.println("Expected: " + map1);
                    System.out.println("Received: " + map2);
                    throw new Exception("counter value not equals for (" + key + ") expected value in range=[" + lowThreshold + " - " + highThreshold + "] but received=" + value2);
                }
            } else {
                if (!value1.equals(value2)) {
                    System.out.println("PMICounter: " + counter);
                    System.out.println("Expected: " + map1);
                    System.out.println("Received: " + map2);
                    throw new Exception("counter value not equals for (" + key + ") expected=" + value1 + " but received=" + value2);
                }
            }
        }
    }

    static public void initializeCacheNames() {

        //Servlet PMICounters    
        cacheCountersName[WSDynamicCacheStats.MaxInMemoryCacheEntryCount] = MaxInMemoryCacheEntryCount;
        cacheCountersName[WSDynamicCacheStats.InMemoryCacheEntryCount] = InMemoryCacheEntryCount;

        cacheCountersName[WSDynamicCacheStats.HitsInMemoryCount] = HitsInMemoryCount;
        cacheCountersName[WSDynamicCacheStats.HitsOnDiskCount] = HitsOnDiskCount;
        cacheCountersName[WSDynamicCacheStats.ExplicitInvalidationCount] = ExplicitInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.LruInvalidationCount] = LruInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.TimeoutInvalidationCount] = TimeoutInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount] = InMemoryAndDiskCacheEntryCount;
        cacheCountersName[WSDynamicCacheStats.RemoteHitCount] = RemoteHitCount;
        cacheCountersName[WSDynamicCacheStats.MissCount] = MissCount;
        cacheCountersName[WSDynamicCacheStats.ClientRequestCount] = ClientRequestCount;
        cacheCountersName[WSDynamicCacheStats.DistributedRequestCount] = DistributedRequestCount;
        cacheCountersName[WSDynamicCacheStats.ExplicitMemoryInvalidationCount] = ExplicitMemoryInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.ExplicitDiskInvalidationCount] = ExplicitDiskInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.LocalExplicitInvalidationCount] = LocalExplicitInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.RemoteExplicitInvalidationCount] = RemoteExplicitInvalidationCount;
        cacheCountersName[WSDynamicCacheStats.RemoteCreationCount] = RemoteCreationCount;

        //Disk Cache PMI counters
        cacheCountersName[WSDynamicCacheStats.ObjectsOnDisk] = ObjectsOnDisk;
        cacheCountersName[WSDynamicCacheStats.HitsOnDisk] = HitsOnDisk;
        cacheCountersName[WSDynamicCacheStats.ExplicitInvalidationsFromDisk] = ExplicitInvalidationsFromDisk;
        cacheCountersName[WSDynamicCacheStats.TimeoutInvalidationsFromDisk] = TimeoutInvalidationsFromDisk;
        cacheCountersName[WSDynamicCacheStats.PendingRemovalFromDisk] = PendingRemovalFromDisk;
        cacheCountersName[WSDynamicCacheStats.DependencyIdsOnDisk] = DependencyIdsOnDisk;
        cacheCountersName[WSDynamicCacheStats.DependencyIdsBufferedForDisk] = DependencyIdsBufferedForDisk;
        cacheCountersName[WSDynamicCacheStats.DependencyIdsOffloadedToDisk] = DependencyIdsOffloadedToDisk;
        cacheCountersName[WSDynamicCacheStats.DependencyIdBasedInvalidationsFromDisk] = DependencyIdBasedInvalidationsFromDisk;
        cacheCountersName[WSDynamicCacheStats.TemplatesOnDisk] = TemplatesOnDisk;
        cacheCountersName[WSDynamicCacheStats.TemplatesBufferedForDisk] = TemplatesBufferedForDisk;
        cacheCountersName[WSDynamicCacheStats.TemplatesOffloadedToDisk] = TemplatesOffloadedToDisk;
        cacheCountersName[WSDynamicCacheStats.TemplateBasedInvalidationsFromDisk] = TemplateBasedInvalidationsFromDisk;
        cacheCountersName[WSDynamicCacheStats.GarbageCollectorInvalidationsFromDisk] = GarbageCollectorInvalidationsFromDisk;
        cacheCountersName[WSDynamicCacheStats.OverflowInvalidationsFromDisk] = OverflowInvalidationsFromDisk;

        initialized = true;
    }

    static public String getStackTrace(Throwable oThrowable) {
        if (oThrowable == null)
            return null;
        StringWriter oStringWriter = new StringWriter();
        PrintWriter oPrintWriter = new PrintWriter(oStringWriter);
        oThrowable.printStackTrace(oPrintWriter);

        return oStringWriter.toString();
    }

    static public void initializeExpectedCounters(Map map, int count, String cacheModule) throws Exception {

        map.clear();

        if ((count == 15) && (cacheModule.indexOf(WSDynamicCacheStats.DISK_GROUP) != -1)) {

            map.put(ObjectsOnDisk, String.valueOf(0));
            map.put(HitsOnDisk, String.valueOf(0));
            map.put(ExplicitInvalidationsFromDisk, String.valueOf(0));
            map.put(TimeoutInvalidationsFromDisk, String.valueOf(0));
            map.put(PendingRemovalFromDisk, String.valueOf(0));
            map.put(DependencyIdsOnDisk, String.valueOf(0));
            map.put(DependencyIdsBufferedForDisk, String.valueOf(0));
            map.put(DependencyIdsOffloadedToDisk, String.valueOf(0));
            map.put(DependencyIdBasedInvalidationsFromDisk, String.valueOf(0));
            map.put(TemplatesOnDisk, String.valueOf(0));
            map.put(TemplatesBufferedForDisk, String.valueOf(0));
            map.put(TemplatesOffloadedToDisk, String.valueOf(0));
            map.put(TemplateBasedInvalidationsFromDisk, String.valueOf(0));
            map.put(GarbageCollectorInvalidationsFromDisk, String.valueOf(0));
            map.put(OverflowInvalidationsFromDisk, String.valueOf(0));

        } else if ((count == 15) && (cacheModule.indexOf(CACHE_OBJECT) != -1)) {

            map.put(HitsInMemoryCount, String.valueOf(0));
            map.put(HitsOnDiskCount, String.valueOf(0));
            map.put(ExplicitInvalidationCount, String.valueOf(0));
            map.put(LruInvalidationCount, String.valueOf(0));
            map.put(TimeoutInvalidationCount, String.valueOf(0));
            map.put(InMemoryAndDiskCacheEntryCount, String.valueOf(0));
            map.put(RemoteHitCount, String.valueOf(0));
            map.put(MissCount, String.valueOf(0));
            map.put(ClientRequestCount, String.valueOf(0));
            map.put(DistributedRequestCount, String.valueOf(0));
            map.put(ExplicitMemoryInvalidationCount, String.valueOf(0));
            map.put(ExplicitDiskInvalidationCount, String.valueOf(0));
            map.put(LocalExplicitInvalidationCount, String.valueOf(0));
            map.put(RemoteExplicitInvalidationCount, String.valueOf(0));
            map.put(RemoteCreationCount, String.valueOf(0));

        } else if ((count == 15) && (cacheModule.indexOf(CACHE_SERVLET) != -1)) {

            map.put(HitsInMemoryCount, String.valueOf(0));
            map.put(HitsOnDiskCount, String.valueOf(0));
            map.put(ExplicitInvalidationCount, String.valueOf(0));
            map.put(LruInvalidationCount, String.valueOf(0));
            map.put(TimeoutInvalidationCount, String.valueOf(0));
            map.put(InMemoryAndDiskCacheEntryCount, String.valueOf(0));
            map.put(RemoteHitCount, String.valueOf(0));
            map.put(MissCount, String.valueOf(0));
            map.put(ClientRequestCount, String.valueOf(0));
            map.put(DistributedRequestCount, String.valueOf(0));
            map.put(ExplicitMemoryInvalidationCount, String.valueOf(0));
            map.put(ExplicitDiskInvalidationCount, String.valueOf(0));
            map.put(LocalExplicitInvalidationCount, String.valueOf(0));
            map.put(RemoteExplicitInvalidationCount, String.valueOf(0));
            map.put(RemoteCreationCount, String.valueOf(0));

        } else if (count == 2) {

            if (cacheModule.indexOf(CACHE_SERVLET) != -1) {

                map.put(MaxInMemoryCacheEntryCount, String.valueOf(0));
                map.put(InMemoryCacheEntryCount, String.valueOf(0));

            } else if (cacheModule.indexOf(CACHE_OBJECT) != -1) {

                map.put(MaxInMemoryCacheEntryCount, String.valueOf(0));
                map.put(InMemoryCacheEntryCount, String.valueOf(0));

            } else {
                throw new Exception(
                                "Invalid cacheModule:"
                                                + cacheModule
                                                + " specified in initializeExpectedCounters");
            }
        }
    }

    static public void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
