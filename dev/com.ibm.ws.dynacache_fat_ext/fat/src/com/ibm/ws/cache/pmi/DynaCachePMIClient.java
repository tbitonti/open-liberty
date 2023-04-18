// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.pmi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.ibm.websphere.cache.CacheAdminMBean;
import com.ibm.websphere.jmx.connector.rest.ConnectorSettings;
import com.ibm.websphere.pmi.PmiConstants;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.StatLevelSpec;
import com.ibm.websphere.pmi.stat.WSStatistic;
import com.ibm.websphere.pmi.stat.WSStats;
import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.DynaCacheConstants;
import com.ibm.ws.cache.stat.internal.WSDynamicCacheStats;
import com.ibm.ws.jmx.connector.client.rest.ClientProvider;
import com.ibm.ws.pmi.stat.CountStatisticImpl;
import com.ibm.ws.pmi.stat.RangeStatisticImpl;
import com.ibm.ws.pmi.stat.TimeStatisticImpl;

import componenttest.topology.utils.HttpUtils;

public class DynaCachePMIClient implements PmiConstants {

    private final PmiModuleConfig[] configs = null;
    private MBeanServerConnection mbsc = null;
    private ObjectName perfOName = null;

    private String DYNACACHE_MBEAN_NAME = DynaCacheConstants.WEBSPHERE_TYPE; // "WebSphere:type=DynaCache,*"

    public void forceFullMBeanName() {
        DYNACACHE_MBEAN_NAME = CacheAdminMBean.OBJECT_NAME;
    }

    public DynaCachePMIClient(String host, int port, String serverRoot) throws MalformedObjectNameException, IOException, Exception {
        HttpUtils.trustAllCertificates();
        Log.info(DynaCachePMIClient.class, "ctor", " host:" + host + " port:" + port + " serverRoot=" + serverRoot);
        Map<String, Object> environment = new HashMap<String, Object>();
//        environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, ClientProvider.class.getPackage().getName());
        environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "com.ibm.ws.jmx.connector.client");
        environment.put(JMXConnector.CREDENTIALS, new String[] { "bob", "bobpassword" });
        environment.put(ClientProvider.READ_TIMEOUT, 2 * 60 * 1000);
        environment.put(ConnectorSettings.DISABLE_HOSTNAME_VERIFICATION, Boolean.TRUE);

        String restAddress = String.format("service:jmx:rest://%s:%d/IBMJMXConnectorREST", host, port);
//        JMXServiceURL url = new JMXServiceURL(restAddress);
        JMXServiceURL url = new JMXServiceURL("REST", host, port, "/IBMJMXConnectorREST");
        JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, environment);
        Log.info(DynaCachePMIClient.class, "ctor", " sleeping for 15 seconds to allow JMX service to start.");
        Thread.sleep(15000);

        try {
            connector.connect();
        } catch (Exception ex) {
            //Give server a little more time to init.
            Log.info(PMITest.class, "ctor", "Trying connect again because caught exception: " + ex.getMessage());
            Thread.sleep(15000);
            try {
                connector.connect();
            } catch (Exception ex2) {
                Log.info(PMITest.class, "ctor", "2nd connect failed with exception: " + ex.getMessage());
            }
        }

        mbsc = connector.getMBeanServerConnection();

        setObjectNames();
    }

    private void getDynaCacheStats(ObjectName pon) throws InstanceNotFoundException, ReflectionException, MBeanException, IOException {

        Log.info(PMITest.class, "getDynaCacheStats", "\nGet statistics from all cacheModules", pon);

        StatDescriptor dynaCacheSD = new StatDescriptor(new String[] { WSDynamicCacheStats.NAME });
        String[] signature = new String[] {
                                            "[Lcom.ibm.websphere.pmi.stat.StatDescriptor;",
                                            "java.lang.Boolean" };

        Object[] params = new Object[] {
                                         new StatDescriptor[] { dynaCacheSD },
                                         new Boolean(true)
        };

        WSStats[] wsStats = null;
        wsStats = (WSStats[]) mbsc.invoke(pon, "getStatsArray", params, signature);
        if (null != wsStats) {
            processStats(wsStats[0], "");

        }
    }

    public void enableDynaCacheStats() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        //set the instrumentation level for the counters
        StatDescriptor dynaCacheSD = new StatDescriptor(new String[] { WSDynamicCacheStats.NAME });

        StatLevelSpec[] spec = new StatLevelSpec[1];
        spec[0] = new StatLevelSpec(dynaCacheSD.getPath(), new int[] { StatLevelSpec.ALL_STATISTICS });

        String[] signature = new String[] {
                                            "[Lcom.ibm.websphere.pmi.stat.StatLevelSpec;",
                                            "java.lang.Boolean" };
        Object[] params = new Object[] { spec, new Boolean(true) };
        Object o = mbsc.invoke(perfOName,
                               "setInstrumentationLevel",
                               params,
                               signature);

        Log.info(PMITest.class, "enableDynaCacheStats", String.valueOf(o));

    }

    public ObjectName getPerfObjectName(String node, String server) {

        ObjectName pon = null;

        try {

            javax.management.ObjectName onQuery = new javax.management.ObjectName("WebSphere:type=Perf,*");
            Set objectNameSet = mbsc.queryNames(onQuery, null);

            if (null != objectNameSet) {
                System.out.println("Found " + objectNameSet.size());

                if (objectNameSet != null) {

                    ObjectName on = null;
                    Iterator i = objectNameSet.iterator();

                    while (i.hasNext()) {

                        on = (ObjectName) i.next();
                        String onNode = on.getKeyProperty("node");
                        System.out.println("...node:" + onNode + " match: " + onNode.equals(node));
                        String onProcess = on.getKeyProperty("process");
                        System.out.println("...process:" + onProcess + " match: " + onProcess.equals(server));

                        if (onNode.equals(node) && onProcess.equals(server)) {
                            System.out.println("my perf MBean: " + on.toString());
                            pon = on;
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Found *NO* MBeans matching criteria node:" + node + " server:" + server);
            }
        } catch (Exception e) {
            System.out.println("Exception in getPerfObjectName:" + e.getMessage());
            e.printStackTrace();
        }

        if (null == pon) {
            System.out.println("getPerfObjectName returned null for node:" + node + " server:" + server);
        }

        return pon;
    }

    /**
     * get all the ObjectNames.
     *
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws IOException
     */
    public void setObjectNames() throws MalformedObjectNameException, IOException {

        //--------------------------------------------------------------------
        // Get a list of object names
        //--------------------------------------------------------------------
        javax.management.ObjectName on = new javax.management.ObjectName("WebSphere:*");

        //---------------------------------------------------------------------
        // get all objectnames for this server
        //--------------------------------------------------------------------
        Set objectNameSet = mbsc.queryNames(on, null);

        //--------------------------------------------------------------------
        // get the object names that we care about: Perf, Server, JVM, WLM
        //(only applicable in ND)
        //--------------------------------------------------------------------
        if (objectNameSet != null) {
            Iterator i = objectNameSet.iterator();
            while (i.hasNext()) {
                on = (ObjectName) i.next();
                String type = on.getKeyProperty("type");

                // uncomment it if you want to print the ObjectName for each MBean
                System.out.println("\n\n" + on.toString());

                // find the MBeans we are interested
                if (type != null && type.equals("Perf")) {
                    System.out.println("\nMBean: perf =" + on.toString());
                    perfOName = on;
                }
            }
        } else {
            System.err.println("main: ERROR: no object names found");
            throw new IOException("main: ERROR: no object names found");
        }

    }

    public void setStatisticSet(String statisticSet) {

        Object[] params;
        String[] signature;

        try {

            System.out.println("\nSet monitoring to statistic set '" + statisticSet + "'");

            signature = new String[] { "java.lang.String" };
            params = new Object[] { statisticSet };
            mbsc.invoke(perfOName, "setStatisticSet", params, signature);

            System.out.println("\nCurrent statistic set: "
                               + mbsc.invoke(perfOName, "getStatisticSet", null, null));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Customizing PMI data that is enabled using fine-grained control.
     * This method allows to enable or disable statistics selectively.
     * The format of the custom set specification string is STATS_NAME=ID1,ID2,ID3
     * seperated by ':', where STATS_NAME and IDs are defined in WS*Stat interfaces
     * in com.ibm.websphere.pmi.stat package. Use * to enable all the statistics in
     * the given PMI module. For example, to enable all the statistics for JVM and
     * active count, pool size for thread pool use: jvmRuntimeModule=*:threadPoolModule=3,4.
     * The string jvmRuntimeModule is the value of the constant WSJVMStats.NAME
     * and threadPoolModule is the value of WSThreadPoolStats.NAME.
     */

    public void setCustomStatisticsSet(String customSetString) {

        try {

            System.out.println("\n Set monitoring to statistic set '" + customSetString + "'");

            String[] signature = new String[] { "java.lang.String", "java.lang.Boolean" };
            Object[] params = new Object[] { customSetString, Boolean.TRUE };
            mbsc.invoke(perfOName, "setCustomSetString ", params, signature);

            System.out.println("\nCurrent statistic set: "
                               + mbsc.invoke(perfOName, "getStatisticSet", null, null));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sample code to get level
     *
     * @throws MBeanException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     * @throws IOException
     */
    protected MBeanLevelSpec[] getInstrumentationLevel(ObjectName on,
                                                       StatDescriptor sd, boolean recursive) throws InstanceNotFoundException, ReflectionException, MBeanException, IOException {
        if (sd == null)
            return getInstrumentationLevel(on, recursive);
        System.out.println("\ntest getInstrumentationLevel\n");
        Object[] params = new Object[2];
        params[0] = new MBeanStatDescriptor(on, sd);
        params[1] = new Boolean(recursive);
        String[] signature = new String[] { "com.ibm.websphere.pmi.stat.MBeanStatDescriptor", "java.lang.Boolean" };
        MBeanLevelSpec[] mlss = (MBeanLevelSpec[]) mbsc.invoke(perfOName,
                                                               "getInstrumentationLevel", params, signature);
        return mlss;
    }

    /**
     * Sample code to get level
     *
     * @throws MBeanException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     * @throws IOException
     */
    protected MBeanLevelSpec[] getInstrumentationLevel(ObjectName on,
                                                       boolean recursive) throws InstanceNotFoundException, ReflectionException, MBeanException, IOException {
        if (on == null)
            return null;
        System.out.println("\ntest getInstrumentationLevel\n");

        Object[] params = new Object[] { on, new Boolean(recursive) };
        String[] signature = new String[] { "javax.management.ObjectName",
                                            "java.lang.Boolean" };
        MBeanLevelSpec[] mlss = (MBeanLevelSpec[]) mbsc.invoke(perfOName,
                                                               "getInstrumentationLevel", params, signature);
        return mlss;
    }

    /**
     * Sample code to set level
     *
     * @throws MBeanException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     * @throws IOException
     */
    protected void setInstrumentationLevel(ObjectName on, StatDescriptor sd,
                                           int level) throws InstanceNotFoundException, ReflectionException, MBeanException, IOException {
        System.out.println("\ntest setInstrumentationLevel\n");

        Object[] params = new Object[2];
        String[] signature = null;
        MBeanLevelSpec[] mlss = null;
        params[0] = new MBeanLevelSpec(on, sd, level);
        params[1] = new Boolean(true);

        signature = new String[] { "com.ibm.websphere.pmi.stat.MBeanLevelSpec",
                                   "java.lang.Boolean" };
        mbsc.invoke(perfOName, "setInstrumentationLevel", params, signature);

    }

    /**
     * Sample code to navigate and get the data value from the Stats and
     * Statistic object.
     */
    private void processStats(WSStats stat, String indent) {
        if (stat == null)
            return;

        System.out.println("\n\n");

        // get name of the Stats
        String name = stat.getName();
        System.out.println(indent + "stats name=" + name);

        // list data names
        String[] dataNames = stat.getStatisticNames();

        WSStatistic[] dataMembers = stat.getStatistics();
        if (dataMembers != null) {
            for (int i = 0; i < dataMembers.length; i++) {
                System.out.print(indent + "    " + dataMembers[i].getName() + "[" + dataMembers[i].getId() + "]");

                if (null != dataMembers[i].getDataInfo()) {

                    if (dataMembers[i].getDataInfo().getType() == TYPE_LONG) {
                        System.out.println(
                                           "="
                                           + ((CountStatisticImpl) dataMembers[i]).getCount());
                    } else if (dataMembers[i].getDataInfo().getType() == TYPE_STAT) {
                        TimeStatisticImpl data = (TimeStatisticImpl) dataMembers[i];
                        System.out.println(
                                           ", count="
                                           + data.getCount()
                                           + ", total="
                                           + data.getTotal()
                                           + ", mean="
                                           + data.getMean()
                                           + ", min="
                                           + data.getMin()
                                           + ", max="
                                           + data.getMax());
                    } else if (dataMembers[i].getDataInfo().getType() == TYPE_LOAD) {
                        RangeStatisticImpl data = (RangeStatisticImpl) dataMembers[i];
                        System.out.println(
                                           ", current="
                                           + data.getCurrent()
                                           + ", integral="
                                           + data.getIntegral()
                                           + ", avg="
                                           + data.getMean()
                                           + ", lowWaterMark="
                                           + data.getLowWaterMark()
                                           + ", highWaterMark="
                                           + data.getHighWaterMark());
                    }
                }

            }
        }

        // recursively for sub-stats
        WSStats[] substats = stat.getSubStats();
        if (substats == null || substats.length == 0)
            return;
        for (int i = 0; i < substats.length; i++) {
            processStats(substats[i], indent + "    ");
        }
    }

    public MBeanStatDescriptor[] listStatMembers(MBeanStatDescriptor mName) throws InstanceNotFoundException, ReflectionException, MBeanException, IOException {
        if (mName == null)
            return null;

        Object[] params = new Object[] { mName };
        String[] signature = new String[] { "com.ibm.websphere.pmi.stat.MBeanStatDescriptor" };
        MBeanStatDescriptor[] msds = (MBeanStatDescriptor[]) mbsc.invoke(perfOName, "listStatMembers", params, signature);
        if (msds == null)
            return null;
        for (int i = 0; i < msds.length; i++) {
            MBeanStatDescriptor[] msds2 = listStatMembers(msds[i]);
            // you may recursively call listStatMembers until find the one you want
        }
        return msds;

    }

    /**
     * Get PmiModuleConfig from server
     */
    public PmiModuleConfig getStatsConfig(String statsType) {
        try {
            return (PmiModuleConfig) mbsc.invoke(
                                                 perfOName,
                                                 "getConfig",
                                                 new String[] { statsType },
                                                 new String[] { "java.lang.String" });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get PmiModuleConfig based on MBean ObjectName
     *
     * @deprecated Use com.ibm.websphere.pmi.client.PmiClient.findConfig()
     */
    @Deprecated
    public PmiModuleConfig findConfig(ObjectName on) {
        if (on == null)
            return null;

        String type = on.getKeyProperty("type");
        System.out.println("findConfig: mbean type =" + type);

        for (int i = 0; i < configs.length; i++) {

            if (configs[i].getMbeanType().equals(type))
                return configs[i];
        }
        System.out.println("Error: cannot find the config");
        return null;
    }

    public void stopServer(String serverName) throws MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        ObjectName serverON = null;
        ObjectName queryON = new ObjectName(DYNACACHE_MBEAN_NAME); // "WebSphere:type=DynaCache,*"
        Set objectNameSet = mbsc.queryNames(queryON, null);

        if (false == objectNameSet.isEmpty()) {
            serverON = (ObjectName) objectNameSet.iterator().next();
        } else {
            System.out.println("Server MBEAN for " + serverName + " NOT found");
            throw new MBeanException(null, "DynaCache Mbean for " + serverName + " was not found");
        }

        mbsc.invoke(serverON, "stop", null, null);
    }

    public int getPK13460MbeanStat(String cacheInstance,
                                   String statisticName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        int statistic = -1;
        ObjectName dynacacheON = null;
        ObjectName queryON = new ObjectName(DYNACACHE_MBEAN_NAME); // "WebSphere:type=DynaCache,*"
        Set<ObjectName> objectNameSet = mbsc.queryNames(queryON, null);

        if (false == objectNameSet.isEmpty()) {
            dynacacheON = objectNameSet.iterator().next();
        }

        //$AdminControl invoke $mbean getCacheStatistics
        //{services/cache/servletInstance_4 "CacheHits ObjectsOnDisk"}
        String[] signature = new String[] { "java.lang.String", "[Ljava.lang.String;" };
        Object[] params = new Object[] { cacheInstance, new String[] { statisticName } };
        String[] ppt = (String[]) mbsc.invoke(dynacacheON, "getCacheStatistics", params, signature);

        if (null != ppt && ppt.length > 0) {
            statistic = Integer.parseInt(ppt[0].substring(statisticName.length() + 1));
        }

        return statistic;
    }

    public String[] getAllMbeanStat(String cacheInstance,
                                    String serverName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        ObjectName dynacacheON = null;
        ObjectName queryON = new ObjectName(DYNACACHE_MBEAN_NAME); // "WebSphere:type=DynaCache,*"
        Set objectNameSet = mbsc.queryNames(queryON, null);

        if (false == objectNameSet.isEmpty()) {
            dynacacheON = (ObjectName) objectNameSet.iterator().next();
        } else {
            System.out.println("Dynacache MBEAN for " + serverName + " NOT found");
            throw new MBeanException(null, "DynaCache Mbean for " + serverName + " was not found");
        }

        //$AdminControl invoke $mbean getAllCacheStatistics "services/cache/servletInstance_4"
        String[] signature = new String[] { "java.lang.String" };
        Object[] params = new Object[] { cacheInstance };
        String[] statistics = (String[]) mbsc.invoke(dynacacheON, "getAllCacheStatistics", params, signature);

        return statistics;
    }

    static public int getMbeanStat(String[] statistics, String statisticName) {

        int statisticCount = -1;
        if (statistics != null) {
            for (int i = 0; i < statistics.length; i++) {
                if (statistics[i].startsWith(statisticName)) {
                    statisticCount = Integer.parseInt(statistics[i].substring(statisticName.length() + 1));
                    break;
                }
            }
        }
        return statisticCount;
    }

    static public boolean compareMbeanStat(String[] statistics_before, String[] statistics_after, String[] statistics_delta) {
        HashMap<String, Float> expected = new HashMap<String, Float>();
        HashMap<String, Float> received = new HashMap<String, Float>();
        if (statistics_before != null) {
            for (int i = 0; i < statistics_before.length; i++) {
                int index = statistics_before[i].indexOf("=");
                if (index > 0) {
                    String key = statistics_before[i].substring(0, index);
                    String value = statistics_before[i].substring(index + 1);
                    expected.put(key, new Float(value));
                }
            }
        }
        if (statistics_after != null) {
            for (int i = 0; i < statistics_after.length; i++) {
                int index = statistics_after[i].indexOf("=");
                if (index > 0) {
                    String key = statistics_after[i].substring(0, index);
                    String value = statistics_after[i].substring(index + 1);
                    received.put(key, new Float(value));
                }
            }
        }
        //System.out.println("expected=" + expected);
        //System.out.println("received=" + expected);
        if (statistics_delta != null) {
            for (int i = 0; i < statistics_delta.length; i++) {
                int index = statistics_delta[i].indexOf("=");
                if (index > 0) {
                    String key = statistics_delta[i].substring(0, index);
                    String value = statistics_delta[i].substring(index + 1);
                    if (key.equals("MemoryCacheEntries") || key.equals("MemoryCacheSizeInMB")) {
                        expected.put(key, new Float(value));
                    } else {
                        float expValue = expected.get(key).floatValue();
                        float deltaValue = Float.parseFloat(value);
                        expValue += deltaValue;
                        expected.put(key, new Float(expValue));
                    }
                }
            }
        }
        //System.out.println("expected=" + expected);
        Iterator it = expected.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            float exp = expected.get(key).floatValue();
            float rec = received.get(key).floatValue();
            if (exp != rec) {
                System.out.println("Error: key=" + key + " expected=" + exp + " received=" + rec);
                return false;
            }
        }

        return true;
    }

    static public void displayMbeanStat(String[] statistics, String instance) {

        System.out.println("***** \"" + instance + "\" statistics *****");
        if (statistics != null) {
            for (int i = 0; i < statistics.length; i++) {
                System.out.println(statistics[i]);
            }
        }
        System.out.println("***********************************************");
        return;
    }

    public String[] getCacheInstanceNames(String serverName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        ObjectName dynacacheON = null;
        ObjectName queryON = new ObjectName(DYNACACHE_MBEAN_NAME); // "WebSphere:type=DynaCache,*"
        Set objectNameSet = mbsc.queryNames(queryON, null);

        if (false == objectNameSet.isEmpty()) {
            dynacacheON = (ObjectName) objectNameSet.iterator().next();
        } else {
            System.out.println("Dynacache MBEAN for " + serverName + " NOT found");
            throw new MBeanException(null, "DynaCache Mbean for " + serverName + " was not found");
        }

        //$AdminControl invoke $mbean getCacheInstanceNames
        String[] signature = new String[] {};
        String[] params = new String[] {};
        String[] instanceNames = (String[]) mbsc.invoke(dynacacheON, "getCacheInstanceNames", params, signature);

        return instanceNames;
    }

    static public boolean isCacheInstanceExist(String[] instanceNames, String instance) {
        boolean found = false;
        if (instanceNames != null) {
            for (int i = 0; i < instanceNames.length; i++) {
                if (instanceNames[i].equals(instance)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public void clearCache(String cacheInstance,
                           String serverName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        ObjectName dynacacheON = null;
        ObjectName queryON = new ObjectName(DYNACACHE_MBEAN_NAME); // "WebSphere:type=DynaCache,*"
        Set objectNameSet = mbsc.queryNames(queryON, null);

        if (false == objectNameSet.isEmpty()) {
            dynacacheON = (ObjectName) objectNameSet.iterator().next();
        } else {
            System.out.println("Dynacache MBEAN for " + serverName + " NOT found");
            throw new MBeanException(null, "DynaCache Mbean for " + serverName + " was not found");
        }

        //$AdminControl invoke $mbean clearCache "services/cache/servletInstance_4"
        String[] signature = new String[] { "java.lang.String" };
        Object[] params = new Object[] { cacheInstance };
        mbsc.invoke(dynacacheON, "clearCache", params, signature);

        return;
    }

    /**
     * Get PmiModuleConfig based on PMI module name
     *
     * @deprecated Use com.ibm.websphere.pmi.client.PmiClient.findConfig()
     */
    @Deprecated
    public PmiModuleConfig findConfig(String moduleName) {
        if (moduleName == null)
            return null;

        for (int i = 0; i < configs.length; i++) {

            if (configs[i].getShortName().equals(moduleName))
                return configs[i];
        }
        System.out.println("Error: cannot find the config");
        return null;

    }

}
