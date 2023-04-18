package com.ibm.ws.cache;

public class TestConfig {

    static public String host = "localhost";

    static public int port = 9080;
    static public int pmiPort = 8879;
    static public int soapPort = 8879;
    static public String connector = "SOAP";
    static private String node = "Node01";
    static public String server = "dynacachetests";
    static public String serverRoot = "";

    static public boolean ihsEnabled = false;
    static public String ihsHost = "localhost";
    static public int ihsPort = 80;

    static public boolean cacheMonitorEnabled = true;
    static public int cacheMonitorPort = 9060;
    static public String cacheMonitorUser = null;
    static public String cacheMonitorPassword = null;

    static public boolean globalSecurityEnabled = true;
    static public String adminUser = "user1";
    static public String adminPassword = "security";

    static public int baseCacheSize = 2000;

    /* Only needed for XS cache provider testing */
    static public String cell = "Cell01";
    static public String distribHost = "localhost";
    static public String distribPort = "9081";
    static public String distribServer = "server1";
    static public String distribNode = "Node01";
    static public String distribHost2 = "none";
    static public String distribPort2 = "none";
    static public boolean distrib = false;
    static public int ndPort = 9060;
    static public String ndHost = "localhost";
    static public int ndPmiPort = 8879;

    static private boolean zOSMode = false;
    static private int zOSInvDelay = 10;
    static private int zOSRemoteDelay = 5;

    static public String getBaseURL() {
        return "http://" + host + ":" + port;
    }

    static public String getCloneURL() {
        return "http://" + distribHost + ":" + distribPort;
    }

    static public boolean getIhsEnabled() {
        return ihsEnabled;
    }

    static public boolean getDistributed() {
        return distrib;
    }

    static public String getIhsURL() {
        return "http://" + getIhsHost() + ":" + getIhsPort();
    }

    static public String getIhsHost() {
        return ihsHost;
    }

    static public int getIhsPort() {
        return ihsPort;
    }

    static public String getConnector() {
        return connector;
    }

    static public int getBaseCacheSize() {
        return baseCacheSize;
    }

    static public String getNDHost() { //NK begin
        return ndHost;
    }

    static public int getNDPmiPort() {
        return ndPmiPort;
    }

    static public String getNode() {
        return node;
    }

    static public String getServer() {
        return server;
    }

    static public String getDistribServer() {
        return distribServer;
    }

    static public String getDistribNode() {
        return distribNode;
    }

    static public String getHost() {
        return host;
    }

    static public int getPmiPort() {
        return pmiPort;
    }

    static public boolean getZOSMode() {
        return zOSMode;
    }

    static public int getZOSInvDelay() {
        return zOSInvDelay;
    }

    static public int getZOSRemoteDelay() {
        return zOSRemoteDelay;
    }
}
