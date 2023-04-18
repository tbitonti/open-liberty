// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2008
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.ChangeEvent;
import com.ibm.websphere.cache.ChangeListener;
import com.ibm.websphere.cache.DistributedObjectCache;
import com.ibm.websphere.cache.DynamicCacheAccessor;
import com.ibm.websphere.cache.EntryInfo;
import com.ibm.websphere.cache.InvalidationEvent;
import com.ibm.websphere.cache.InvalidationListener;
import com.ibm.ws.cache.DistributedMapImpl;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.ws.cache.spi.DistributedMapFactory;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

//------------------------------------------------------------
// com.ibm.ws.cache.servlet.DRSServlet
//------------------------------------------------------------
public class DRSServlet extends HttpServlet implements InvalidationListener, ChangeListener {

    public static DRSServlet instance = null;
    public PrintWriter out = null;
    public Random rand = new Random();
    public Object objectIn = null;
    public Object objectIn1 = null;
    public int loopCount = 50;
    public int ttl = 30000;
    public int globalTtl = 30000;
    public boolean interactiveMode = false;

    public int shareType = EntryInfo.SHARED_PUSH;
    public int globalShareType = EntryInfo.SHARED_PUSH;
    public int shareTypeLast = -99;
    public boolean getShareType = false;
    public boolean getMapType = false;
    public boolean getCacheId = false;
    public boolean getCacheName = false;
    public boolean getServletName = false;
    public boolean getGlobalShareType = false;
    public boolean getChangeType = false;
    public boolean getInvalidationType = false;
    public boolean startStress = false;
    public int entryCount = 3000;
    public int changeCause = -1;
    public int changeSource = -1;
    public int invalidationCause = -1;
    public int invalidationSource = -1;
    public Exception exception = null;

    public Hashtable changeEvents = new Hashtable();
    public Hashtable invalidationEvents = new Hashtable();

    public int SHARE_TYPE_NONE = EntryInfo.NOT_SHARED;
    public int SHARE_TYPE_PUSH = EntryInfo.SHARED_PUSH;
    public int SHARE_TYPE_PULL = EntryInfo.SHARED_PULL;
    public int SHARE_TYPE_PUSH_PULL = EntryInfo.SHARED_PUSH_PULL;

    public String cacheName = null;
    public String servletName = null;
    public String cacheId = null;
    public String html = null;
    public HttpServletRequest request;
    public HttpServletResponse response;

    public int maxInstances = 5;
    public DistributedObjectCache[] distributedMaps = new DistributedObjectCache[maxInstances];
    public String jndiNames[] = new String[maxInstances];
    {
        for (int i = 0; i != maxInstances; i++) {
            jndiNames[i] = "cache/dynacachepmetests/instance" + (i + 1);
        }
    }

    public DistributedObjectCache distributedMap = null;
    public DistributedObjectCache distributedMap_B = null;
    public DistributedObjectCache distributedMap_1 = null;
    public DistributedObjectCache distributedMap_1r = null;
    public DistributedObjectCache distributedMap_2 = null;
    public DistributedObjectCache distributedMap_2r = null;
    public DistributedObjectCache distributedMap_L1 = null;
    public DistributedObjectCache distributedMap_L2 = null;
    public DistributedObjectCache distributedMap_N1 = null;
    public DistributedObjectCache distributedMap_N2 = null;
    public DistributedObjectCache distributedMap_OG1 = null;
    public DistributedObjectCache distributedMap_OG2 = null;
    public DistributedObjectCache distributedMap_OGS1 = null;
    public DistributedObjectCache distributedMap_OGS2 = null;

    public static final int TYPE_BASE_CACHE = 0x01;
    public static final int TYPE_DMap_1 = 0x02;
    public static final int TYPE_DMap_2 = 0x03;
    public static final int TYPE_DMap_L1 = 0x04;
    public static final int TYPE_DMap_N1 = 0x05;
    public static final int TYPE_DMap_OG1 = 0x06;
    public static final int TYPE_DMap_OG2 = 0x07;
    public static final int TYPE_DMap_OGS1 = 0x08;
    public static final int TYPE_DMap_OGS2 = 0x09;
    public int mapTypeCurrent = TYPE_BASE_CACHE;

    public boolean getCacheContentIn = false;
    public String cacheContentIn = null;

    //Tamera's fields
    public HttpServletRequest req = null;
    public HttpServletResponse resp = null;
    public String ids = " ";
    //end of Tamera's fields

    public boolean doTestDLockingMap = false;
    public boolean doTestDNioMap = true;

    //------------------------------------------------------------
    //
    //------------------------------------------------------------
    public DRSServlet() {
        if (instance == null) {
            instance = this;
        }
    }

    //------------------------------------------------------------

    public static DRSServlet getInstance() {
        return instance;
    }

    //------------------------------------------------------------
    //
    //------------------------------------------------------------
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean error = false;

        final String methodName = "DRSServlet.service()";
        System.out.println(methodName + " " + request.getQueryString());

        this.request = request;
        this.response = response;

        // DistributedMap - default
        if (distributedMap == null) {
            try {
                Context context = new InitialContext();
                distributedMap = (DistributedObjectCache) context.lookup("services/cache/distributedmap");
                distributedMap_B = distributedMap;
                System.out.println("DMap created " + distributedMap_B);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }

        // DistributedMap - instance 1
        if (distributedMap_1 == null) {
            try {
                Context context = new InitialContext();
                distributedMap_1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_1");
                //distributedMap_1r = (DistributedObjectCache) context.lookup("java:comp/env/cache/one");
                System.out.println("DMap created " + distributedMap_1);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }

        // DistributedMap - instance 2
        if (distributedMap_2 == null) {
            try {
                Context context = new InitialContext();
                distributedMap_2 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_2");
                //distributedMap_2r = (DistributedObjectCache) context.lookup("java:comp/env/cache/two");
                System.out.println("DMap created " + distributedMap_2);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }

        // DistributedLockingMap - instance 1
        if (doTestDLockingMap && distributedMap_L1 == null) {
            try {
                Context context = new InitialContext();
                distributedMap_L1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_L1");
                System.out.println("DMap created " + distributedMap_L1);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }

        // DistributedLockingMap - instance 2
        if (doTestDLockingMap && distributedMap_L2 == null) {
            try {
                Context context = new InitialContext();
                distributedMap_L2 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_L2");
                System.out.println("DMap created " + distributedMap_L2);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }

        // DistributedNioMap - instance 1
        if (distributedMap_N1 == null) {
            try {
                Context context = new InitialContext();
                distributedMap_N1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_N1");
                System.out.println("DMap created " + distributedMap_N1);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }

        // DistributedNioMap - instance 2
        if (distributedMap_N2 == null) {
            try {
                Context context = new InitialContext();
                distributedMap_N2 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_N2");
                System.out.println("DMap created " + distributedMap_N2);
            } catch (Throwable e) {
                error = true;
                sendException(request, response, e);
            }
        }
/*
 * if (distributedMap_OG1 == null) {
 * try {
 * Context context = new InitialContext();
 * distributedMap_OG1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_OG1");
 * System.out.println("DMap created " + distributedMap_OG1);
 * } catch (Throwable e) {
 * error = true;
 * sendException(request, response, e);
 * }
 * }
 * 
 * if (distributedMap_OG2 == null) {
 * try {
 * Context context = new InitialContext();
 * distributedMap_OG2 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_OG2");
 * System.out.println("DMap created " + distributedMap_OG2);
 * } catch (Throwable e) {
 * error = true;
 * sendException(request, response, e);
 * }
 * }
 * 
 * if (distributedMap_OGS1 == null) {
 * try {
 * Context context = new InitialContext();
 * distributedMap_OGS1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_OGS1");
 * System.out.println("DMap created " + distributedMap_OGS1);
 * } catch (Throwable e) {
 * error = true;
 * sendException(request, response, e);
 * }
 * }
 * 
 * if (distributedMap_OGS2 == null) {
 * try {
 * Context context = new InitialContext();
 * distributedMap_OGS2 = (DistributedObjectCache) context.lookup("services/cache/distributedmap_OGS2");
 * System.out.println("DMap created " + distributedMap_OGS2);
 * } catch (Throwable e) {
 * error = true;
 * sendException(request, response, e);
 * }
 * }
 */
        // Verify unique maps
        if (!error) {
            if (distributedMap_2 == distributedMap_1 ||
                distributedMap_1 == distributedMap_B ||
                distributedMap_2 == distributedMap_B) {
                try {
                    throw new Exception("DMaps are not unique! ");
                } catch (Throwable e) {
                    error = true;
                    sendException(request, response, e);
                }
            }
        }

        // Verify unique maps
        if (doTestDNioMap && !error) {
            if (distributedMap_N1 == distributedMap_B ||
                distributedMap_N1 == distributedMap_1 ||
                distributedMap_N1 == distributedMap_2 ||
                distributedMap_N1 == distributedMap_N2) {
                try {
                    throw new Exception("DMaps are not unique! ");
                } catch (Throwable e) {
                    error = true;
                    sendException(request, response, e);
                }
            }
        }

        // Verify unique maps
        if (doTestDLockingMap && !error) {
            if (distributedMap_L1 == distributedMap_B ||
                distributedMap_L1 == distributedMap_1 ||
                distributedMap_L1 == distributedMap_2 ||
                distributedMap_L1 == distributedMap_L2 ||
                distributedMap_L2 == distributedMap_B ||
                distributedMap_L2 == distributedMap_1 ||
                distributedMap_L2 == distributedMap_2) {
                try {
                    throw new Exception("DMaps are not unique! ");
                } catch (Throwable e) {
                    error = true;
                    sendException(request, response, e);
                }
            }
        }

        if (!error) {
            cacheContentIn = null;
            super.service(request, response);
        }
    }

    //------------------------------------------------------------
    private void sendException(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException
    //------------------------------------------------------------
    {
        out = response.getWriter();
        if (interactiveMode) {
            out.println("<br>");
            out.println(getStackTrace(e));
            out.println("<br>");
        } else {
            out.print("\n\n\nDRSServlet exception\n\n\n" + getStackTrace(e) + "\n\n\n");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    //------------------------------------------------------------
    {
        //----Tamera's code----

        req = request;
        resp = response;
        ids = request.getParameter("ids");

        /*
         * out = response.getWriter();
         * Object o = null;
         * Hashtable ht = null;
         * String myString = null;
         * 
         * o = req.getAttribute("nonSerObject");
         * ht = (Hashtable) req.getAttribute("myHashtable");
         * myString = (String) req.getAttribute("myString");
         * //out.println("<br>mystring = "+myString);
         * 
         * if(o!=null && ht !=null){
         * 
         * out.println("<br> Object = "+o);
         * out.println("<br> Hashtable = "+ht);
         * out.println("<br>");
         * }
         */
        //---- end of Tamera's code---

        //---------------------------------------------
        // Check for required parms
        //---------------------------------------------
        String method = request.getParameter("method");
        if (method == null) {
            method = "defaultAction";
        }
        //---------------------------------------------

        //interactiveMode = request.getParameter("quietMode") == null ? true : false;
        getShareType = request.getParameter("shareType") != null ? true : false;
        getMapType = request.getParameter("mapType") != null ? true : false;
        getGlobalShareType = request.getParameter("globalShareType") != null ? true : false;
        getCacheContentIn = request.getParameter("cacheContent") != null ? true : false;
        getChangeType = request.getParameter("changeType") != null ? true : false;
        getInvalidationType = request.getParameter("invalidationType") != null ? true : false;
        getCacheName = request.getParameter("cacheName") != null ? true : false;
        getCacheId = request.getParameter("cacheId") != null ? true : false;

        response.setContentType("text/html");

        if (interactiveMode)
            out = response.getWriter();

        //---------------------------------------------
        // Map type?
        //---------------------------------------------
        if (getMapType) {
            String tmp = request.getParameter("mapType");
            if (tmp.equalsIgnoreCase("BaseCache")) {
                mapTypeCurrent = TYPE_BASE_CACHE;
                distributedMap = distributedMap_B;
            } else if (tmp.equalsIgnoreCase("DMap_1")) {
                mapTypeCurrent = TYPE_DMap_1;
                distributedMap = distributedMap_1;
            } else if (tmp.equalsIgnoreCase("DMap_2")) {
                mapTypeCurrent = TYPE_DMap_2;
                distributedMap = distributedMap_2;
            } else if (tmp.equalsIgnoreCase("DMap_L1")) {
                mapTypeCurrent = TYPE_DMap_L1;
                distributedMap = distributedMap_L1;
            } else if (tmp.equalsIgnoreCase("DMap_N1")) {
                mapTypeCurrent = TYPE_DMap_N1;
                distributedMap = distributedMap_N1;
            } else if (tmp.equalsIgnoreCase("DMap_OG1")) {
                mapTypeCurrent = TYPE_DMap_OG1;
                distributedMap = distributedMap_OG1;
            } else if (tmp.equalsIgnoreCase("DMap_OG2")) {
                mapTypeCurrent = TYPE_DMap_OG2;
                distributedMap = distributedMap_OG2;
            } else if (tmp.equalsIgnoreCase("DMap_OGS1")) {
                mapTypeCurrent = TYPE_DMap_OGS1;
                distributedMap = distributedMap_OGS1;
            } else if (tmp.equalsIgnoreCase("DMap_OGS2")) {
                mapTypeCurrent = TYPE_DMap_OGS2;
                distributedMap = distributedMap_OGS2;
            }
        }
        //---------------------------------------------

        //---------------------------------------------
        // cacheName ?
        //---------------------------------------------
        if (getCacheName) {
            cacheName = request.getParameter("cacheName");
        }
        //---------------------------------------------

        //---------------------------------------------
        // cacheId ?
        //---------------------------------------------
        if (getCacheId) {
            cacheId = request.getParameter("cacheId");
        }
        //---------------------------------------------

        //---------------------------------------------
        // Change type?
        //---------------------------------------------
        if (getChangeType) {
            String tmp = request.getParameter("changeType");
            if (tmp.equalsIgnoreCase("LOCAL_EXISTING_VALUE_CHANGED")) {
                changeCause = ChangeEvent.EXISTING_VALUE_CHANGED;
                changeSource = ChangeEvent.LOCAL;
            } else if (tmp.equalsIgnoreCase("REMOTE_EXISTING_VALUE_CHANGED")) {
                changeCause = ChangeEvent.EXISTING_VALUE_CHANGED;
                changeSource = ChangeEvent.REMOTE;
            } else if (tmp.equalsIgnoreCase("LOCAL_NEW_ENTRY_ADDED")) {
                changeCause = ChangeEvent.NEW_ENTRY_ADDED;
                changeSource = ChangeEvent.LOCAL;
            } else if (tmp.equalsIgnoreCase("REMOTE_NEW_ENTRY_ADDED")) {
                changeCause = ChangeEvent.NEW_ENTRY_ADDED;
                changeSource = ChangeEvent.REMOTE;
            }
        }
        //---------------------------------------------

        //---------------------------------------------
        // Change type?
        //---------------------------------------------
        if (getInvalidationType) {
            String tmp = request.getParameter("invalidationType");
            if (tmp.equalsIgnoreCase("LOCAL_EXISTING_VALUE_CHANGED")) {
                changeCause = ChangeEvent.EXISTING_VALUE_CHANGED;
                changeSource = ChangeEvent.LOCAL;
            } else if (tmp.equalsIgnoreCase("REMOTE_EXISTING_VALUE_CHANGED")) {
                changeCause = ChangeEvent.EXISTING_VALUE_CHANGED;
                changeSource = ChangeEvent.REMOTE;
            } else if (tmp.equalsIgnoreCase("LOCAL_NEW_ENTRY_ADDED")) {
                changeCause = ChangeEvent.NEW_ENTRY_ADDED;
                changeSource = ChangeEvent.LOCAL;
            } else if (tmp.equalsIgnoreCase("REMOTE_NEW_ENTRY_ADDED")) {
                changeCause = ChangeEvent.NEW_ENTRY_ADDED;
                changeSource = ChangeEvent.REMOTE;
            }
        }
        //---------------------------------------------

        //---------------------------------------------
        // Invalidatoin type?
        //---------------------------------------------
        if (getInvalidationType) {
            String tmp = request.getParameter("invalidationType");
            if (tmp.equalsIgnoreCase("LOCAL_TIMEOUT")) {
                invalidationCause = InvalidationEvent.TIMEOUT;
                invalidationSource = InvalidationEvent.LOCAL;
                // com.ibm.websphere.cache., 
                // com.ibm.websphere.cache.InvalidationEvent.LOCAL
            } else if (tmp.equalsIgnoreCase("REMOTE_TIMEOUT")) {
                invalidationCause = InvalidationEvent.TIMEOUT;
                invalidationSource = InvalidationEvent.REMOTE;
            } else if (tmp.equalsIgnoreCase("LOCAL_CLEAR_ALL")) {
                invalidationCause = InvalidationEvent.CLEAR_ALL;
                invalidationSource = InvalidationEvent.LOCAL;
            } else if (tmp.equalsIgnoreCase("REMOTE_CLEAR_ALL")) {
                invalidationCause = InvalidationEvent.CLEAR_ALL;
                invalidationSource = InvalidationEvent.REMOTE;
            } else if (tmp.equalsIgnoreCase("LOCAL_EXPLICIT")) {
                invalidationCause = InvalidationEvent.EXPLICIT;
                invalidationSource = InvalidationEvent.LOCAL;
            } else if (tmp.equalsIgnoreCase("REMOTE_EXPLICIT")) {
                invalidationCause = InvalidationEvent.EXPLICIT;
                invalidationSource = InvalidationEvent.REMOTE;
            }
        }
        //---------------------------------------------

        //---------------------------------------------
        // Sharing type?
        //---------------------------------------------
        if (getShareType) { // DMAP put share type
            String tmp = request.getParameter("shareType");
            if (tmp.equalsIgnoreCase("NONE")) {
                shareType = EntryInfo.NOT_SHARED;
            } else if (tmp.equalsIgnoreCase("PUSH")) {
                shareType = EntryInfo.SHARED_PUSH;
            } else if (tmp.equalsIgnoreCase("PULL")) {
                shareType = EntryInfo.SHARED_PULL;
            } else if (tmp.equalsIgnoreCase("PUSH-PULL")) {
                shareType = EntryInfo.SHARED_PUSH_PULL;
            }
            if (shareTypeLast != shareType) {
                System.out.println("SHARE TYPE CHANGED FROM " + shareTypeLast + " to " + shareType);
                shareTypeLast = shareType;
            }
        }

        if (getGlobalShareType) { // DMAP global share type
            String tmp = request.getParameter("globalShareType");
            if (tmp.equalsIgnoreCase("NONE")) {
                distributedMap.setSharingPolicy(EntryInfo.NOT_SHARED);
                globalShareType = EntryInfo.NOT_SHARED;
            } else if (tmp.equalsIgnoreCase("PUSH")) {
                distributedMap.setSharingPolicy(EntryInfo.SHARED_PUSH);
                globalShareType = EntryInfo.SHARED_PUSH;
            } else if (tmp.equalsIgnoreCase("PULL")) {
                distributedMap.setSharingPolicy(EntryInfo.SHARED_PULL);
                globalShareType = EntryInfo.SHARED_PULL;
            } else if (tmp.equalsIgnoreCase("PUSH-PULL")) {
                distributedMap.setSharingPolicy(EntryInfo.SHARED_PUSH_PULL);
                globalShareType = EntryInfo.SHARED_PUSH_PULL;
            }

            /*
             * for (int i=0;i!=maxInstances;i++) {
             * if ( tmp.equalsIgnoreCase("NONE") ) {
             * distributedMaps[i].setSharingPolicy( EntryInfo.NOT_SHARED );
             * } else
             * if ( tmp.equalsIgnoreCase("PUSH") ) {
             * distributedMaps[i].setSharingPolicy( EntryInfo.SHARED_PUSH );
             * } else
             * if ( tmp.equalsIgnoreCase("PULL") ) {
             * distributedMaps[i].setSharingPolicy( EntryInfo.SHARED_PULL );
             * } else
             * if ( tmp.equalsIgnoreCase("PUSH-PULL") ) {
             * distributedMaps[i].setSharingPolicy( EntryInfo.SHARED_PUSH_PULL );
             * }
             * }
             */

        }
        //---------------------------------------------

        //---------------------------------------------
        // Cache Content
        //---------------------------------------------
        if (getCacheContentIn) {
            cacheContentIn = request.getParameter("cacheContent");
        }
        //---------------------------------------------

        //---------------------------------------------
        // Ping request?
        //---------------------------------------------
        if (method.equalsIgnoreCase("ping")) {
            if (!interactiveMode)
                out = response.getWriter();
            out.print("pong");
            return;
        }
        //---------------------------------------------

        //---------------------------------------------
        // Get default cache sharing policy?
        //---------------------------------------------
        if (method.equalsIgnoreCase("getDefaultCacheSharePolicy")) {
            if (!interactiveMode)
                out = response.getWriter();
            out.print("" + com.ibm.ws.cache.ServerCache.getSharingPolicy());
            return;
        }
        //---------------------------------------------

        //---------------------------------------------
        // Check for parm sets
        //---------------------------------------------
        String tmp = request.getParameter("loopCount");
        if (null != tmp) {
            loopCount = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("ttl");
        if (null != tmp) {
            ttl = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("globalTtl");
        if (null != tmp) {
            globalTtl = new Integer(tmp).intValue();
            distributedMap.setTimeToLive(globalTtl);
        }
        //---------------------------------------------

        try {
            //-------------------------------------------
            // Helper URLs
            //-------------------------------------------
            if (interactiveMode) {

                getServletContext().getRequestDispatcher("/DRSServletWorkArea.jsp").include(request, response);

                /*
                 * out.println( "<table border='0' cellpadding='0' cellspacing='0' width='100%'  height='100%' background='login-background.jpg' >" );
                 * out.println( "<tr><td vALIGN='TOP'>" );
                 * 
                 * out.println( "<table border='0' cellpadding='0' cellspacing='0' width='100%' background='background.jpg' >" );
                 * out.println( "<tr ><td align='left' width='60%'><img src='WS_logo_Admin.jpg'width='395' height='52' alt='WebSphere Admin Console'></td>" );
                 * out.println( "<td align='right' width='40%'><img src='IBM-logo.jpg' width='112' height='52' alt='IBM Logo'></td></tr></table>" );
                 * 
                 * out.println( "<p></p><TABLE><TR><TD NOWRAP>" );
                 * out.println( "" );
                 * 
                 * out.println( "<FONT FACE=verdana SIZE=\"-2\">" );
                 * out.println( "DRSServlet - REMEMBER TO CHECK WEBSPHERE LOGS FOR ERRORS!!!" );
                 * out.println( "<br>" );
                 * out.println( rand.nextInt()+" "+System.currentTimeMillis()+" " );
                 * out.println( "<br>" );
                 * 
                 * out.println("<a href=\"?method=testDistributedMapLock&loopCount=" + loopCount + "&ttl=" + ttl + "\">testDistributedMapLock</a>");
                 * out.println("<br>");
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapSerializable&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializable</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapSerializablePut&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializablePut</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapSerializableObjectKeyPut&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableObjectKeyPut</a>"
                 * ); //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapSerializableObjectKeyGet&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableObjectKeyGet</a>"
                 * ); //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapSerializableMyObjectKeyPut&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableMyObjectKeyPut</a>" );
                 * //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapSerializableMyObjectDepKeyPut&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableMyObjectDepKeyPut</a>" );
                 * //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapSerializableMyObjectKeyGet&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableMyObjectKeyGet</a>" );
                 * //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapSerializableMyObjectKeyGetNone&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableMyObjectKeyGetNone</a>"
                 * ); //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapNonSerializableObjectKeyPut&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapNonSerializableObjectKeyPut</a>" );
                 * //SKS-O
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapNonSerializableObjectKeyGet&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapNonSerializableObjectKeyGet</a>" );
                 * //SKS-O
                 * out.println( "<br>" );
                 * 
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapInvalidate&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapInvalidate</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapInvalidateMyObjectDepKey&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapInvalidateMyObjectDepKey</a>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapInvalidateNonBlocking&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapInvalidateNonBlocking</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapSerializableGet&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableGet</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapSerializableGetNone&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapSerializableGetNone</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapNonSerializable&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapNonSerializable</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapNonSerializablePut&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapNonSerializablePut</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapNonSerializableGet&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapNonSerializableGet</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapNonSerializableGetNone&loopCount="+loopCount+"&ttl="+ttl+"\">testDistributedMapNonSerializableGetNone</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapSerializableContent&loopCount"+loopCount+"&ttl="+ttl+"&cacheContent=content_1\">testDistributedMapSerializableContent_1</a>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println(
                 * "<a href=\"?method=testDistributedMapSerializableContent&loopCount"+loopCount+"&ttl="+ttl+"&cacheContent=content_2\">testDistributedMapSerializableContent_2</a>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println("<a href=\"?method=testDistributedMapSerializableGetContent&loopCount="+loopCount+"&ttl="+ttl+
                 * "&cacheContent=content_1\">testDistributedMapSerializableGetContent_1</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println("<a href=\"?method=testDistributedMapSerializableGetContent&loopCount="+loopCount+"&ttl="+ttl+
                 * "&cacheContent=content_2\">testDistributedMapSerializableGetContent_2</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapStress\">testDistributedMapStress</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapChangeListenerRegister&loopCount="+loopCount+"\">testDistributedMapChangeListenerRegister</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testCacheInstanceMapLookup&loopCount="+loopCount+"&ttl=30\">testCacheInstanceMapLookup</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testCacheInstanceSecondMapLookup&loopCount="+loopCount+"&ttl=30\">testCacheInstanceSecondMapLookup</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapChangeListenerUnRegister&loopCount="+loopCount+"\">testDistributedMapChangeListenerUnRegister</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapChangeListenerChange&loopCount="+loopCount+"\">testDistributedMapChangeListenerChange</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapChangeListenerVerify&loopCount="+loopCount+"\">testDistributedMapChangeListenerVerify</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapChangeListenerVerifyNone&loopCount="+loopCount+"\">testDistributedMapChangeListenerVerifyNone</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<hr>" );
                 * 
                 * out.println( "DistributedMap Instance Type (current=<b>" + getMapType( mapTypeCurrent ) +
                 * ") </b><nobr><a href=\"?mapType=BaseCache\">BaseCache</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapType=DMap_1\">DMap_1</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapType=DMap_2\">DMap_2</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapType=DMap_L1\">DMap_L1</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapType=DMap_N1\">DMap_N1</a></nobr>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<hr>" );
                 * 
                 * out.println("Set ChangeEvent Verification Type (current=<b>"+getChangeSource(changeSource)+"_"+getChangeCause(changeCause)+
                 * "</b>)<br><a href=\"?changeType=LOCAL_NEW_ENTRY_ADDED\">&nbsp;LOCAL_NEW_ENTRY_ADDED&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?changeType=LOCAL_EXISTING_VALUE_CHANGED\">&nbsp;LOCAL_EXISTING_VALUE_CHANGED&nbsp;</a>&nbsp;&nbsp;&nbsp;<br><a href=\"?changeType=REMOTE_NEW_ENTRY_ADDED\">&nbsp;REMOTE_NEW_ENTRY_ADDED&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?changeType=REMOTE_EXISTING_VALUE_CHANGED\">&nbsp;REMOTE_EXISTING_VALUE_CHANGED&nbsp;</a>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println( "Set DMap Global share type (current=<b>" + getShareType( distributedMap.getSharingPolicy() ) +
                 * "</b>) <nobr><a href=\"?globalShareType=NONE\">NONE</a>&nbsp;&nbsp;&nbsp;<a href=\"?globalShareType=PUSH\">PUSH</a>&nbsp;&nbsp;&nbsp;<a href=\"?globalShareType=PUSH-PULL\">PUSH-PULL</a>&nbsp;&nbsp;&nbsp;<a href=\"?globalShareType=PULL\">PULL</a></nobr>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println( "Set DMap Put share type (current=<b>"+getShareType( shareType)+
                 * ")</b> <nobr><a href=\"?shareType=NONE\">NONE</a>&nbsp;&nbsp;&nbsp;<a href=\"?shareType=PUSH\">PUSH</a>&nbsp;&nbsp;&nbsp;<a href=\"?shareType=PUSH-PULL\">PUSH-PULL</a>&nbsp;&nbsp;&nbsp;<a href=\"?shareType=PULL\">PULL</a></nobr>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println("Set Loop Count (current=<b>"+loopCount+
                 * "</b>) <nobr><a href=\"?loopCount=1\">&nbsp;1&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?loopCount=11\">&nbsp;11&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?loopCount=101\">&nbsp;101&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?loopCount=151\">&nbsp;151&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?loopCount=2101\">&nbsp;2101&nbsp;</a></nobr>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println("Set TTL (current=<b>"+ttl+
                 * "</b>) <nobr><a href=\"?ttl=1\">&nbsp;1&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?ttl=30\">&nbsp;30&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?ttl=300\">&nbsp;300&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?ttl=3000\">&nbsp;3000&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?loopCount=-1\">&nbsp;-1&nbsp;</a></nobr>"
                 * );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=resetCache\">Reset Cache ( default & "+getMapType( mapTypeCurrent )+" )</a>" );
                 * out.println( "<hr>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapListenerRegister&loopCount="+loopCount+"&ttl=30\">testDistributedMapListenerRegister</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapListenerUnRegister&loopCount="+loopCount+"&ttl=30\">testDistributedMapListenerUnRegister</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapListenerInvalidate&loopCount="+loopCount+"&ttl=30\">testDistributedMapListenerInvalidate</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapListenerVerify&loopCount="+loopCount+"&ttl=30\">testDistributedMapListenerVerify</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println( "<a href=\"?method=testDistributedMapListenerVerifyNone&loopCount="+loopCount+"&ttl=30\">testDistributedMapListenerVerifyNone</a>" );
                 * out.println( "<br>" );
                 * 
                 * out.println("Set Verification Type (current=<b>"+getInvalidationSource(invalidationSource)+"_"+getInvalidationCause(invalidationCause)+
                 * "</b>)<br><a href=\"?invalidationType=LOCAL_EXPLICIT\">&nbsp;LOCAL_EXPLICIT&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?invalidationType=REMOTE_EXPLICIT\">&nbsp;REMOTE_EXPLICIT&nbsp;</a>&nbsp;&nbsp;&nbsp;<br><a href=\"?invalidationType=LOCAL_TIMEOUT\">&nbsp;LOCAL_TIMEOUT&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?invalidationType=REMOTE_TIMEOUT\">&nbsp;REMOTE_TIMEOUT&nbsp;</a>&nbsp;&nbsp;&nbsp;<br><a href=\"?invalidationType=LOCAL_CLEAR_ALL\">&nbsp;LOCAL_CLEAR_ALL&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?invalidationType=REMOTE_CLEAR_ALL\">&nbsp;REMOTE_CLEAR_ALL&nbsp;</a><br>"
                 * );
                 * 
                 * out.println( "<br></font>" );
                 */

            }
            //-------------------------------------------

            //---------------------------------------------
            // Execute the request
            //---------------------------------------------
            this.getClass().getMethod(method, null).invoke(this, null);
            //---------------------------------------------

            if (interactiveMode) {
                out.println("</body></html>");
            }

            if (!interactiveMode)
                out = response.getWriter();

            if (html == null) {
                out.print("done");
            } else {
                //out.print( html );
                //out.flush();
                html = null;
            }

        } catch (Exception e) {
            if (interactiveMode) {
                out.println("<br><PRE><b>");
                out.println(getStackTrace(e));
                out.println("</b></PRE><br>");
                out.println("</body></html>");
            } else {
                out = response.getWriter();
                out.print("\n\n\nDRSServlet exception\n\n\n" + getStackTrace(e) + "\n\n\n");
            }
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    //
    //------------------------------------------------------------
    public void ping() throws Exception {
        out.println("pong");
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    //
    //------------------------------------------------------------
    public void testDistributedMapLock() throws Exception {
        /*
         * 
         * if (interactiveMode) {
         * out.println("testDistributedMapLock() - ENTRY");
         * out.println("<br>");
         * }
         * 
         * if ( !doTestDLockingMap ) {
         * return;
         * }
         * 
         * objectIn = "mySerializableObject";
         * DMapRequest dmaprequest = new DMapRequest();
         * DMapRequest dmaprequest1 = new DMapRequest();
         * DMapResponse dmapresponse = new DMapResponse();
         * distributedMap.clearAllMapEntryLocks(dmaprequest, dmapresponse);
         * if (interactiveMode) {
         * out.println("putting  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * for (int i = 0; i != loopCount; i++) {
         * String s = "test:" + i;
         * distributedMap.put(s, objectIn);
         * }
         * 
         * if (interactiveMode) {
         * out.println("getting and locking   " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * for (int j = 0; j != loopCount; j++) {
         * String s1 = "test:" + j;
         * Object obj = distributedMap.getAndLock(s1, dmaprequest, dmapresponse);
         * if (null == obj)
         * throw new Exception("testDistributedMapLock() - error - missing cache entry" + dmapresponse);
         * if (!(obj instanceof Serializable))
         * throw new Exception("testDistributedMapLock() - error - serializable object was not returned");
         * if (!objectIn.equals(obj))
         * throw new Exception("testDistributedMapLock() - error - compare error " + objectIn + " " + obj);
         * }
         * 
         * if (interactiveMode) {
         * out.println("putting and unlocking   " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * for (int k = 0; k != loopCount; k++) {
         * String s2 = "test:" + k;
         * distributedMap.putAndUnlock(s2, objectIn, dmaprequest, dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - error - " + dmapresponse);
         * }
         * 
         * String s3 = "Key_OOl";
         * if (interactiveMode) {
         * out.println("begin lock test 1  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (!distributedMap.lockMapEntry(s3, dmaprequest, dmapresponse))
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - error - " + dmapresponse);
         * else
         * throw new Exception("testDistributedMapLock() - missing error - " + dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * distributedMap.put(s3, "xxx", dmaprequest, dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 2  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (!distributedMap.lockMapEntry(s3, dmaprequest, dmapresponse)) {
         * if (dmapresponse.isErrorCurrentlyLocked())
         * throw new Exception("testDistributedMapLock() - unexpected already locked error - " + dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * else
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * }
         * if (interactiveMode) {
         * out.println("begin lock test 3  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (distributedMap.lockMapEntry(s3, dmaprequest1, dmapresponse))
         * throw new Exception("testDistributedMapLock() - unexpected success - " + dmapresponse);
         * if (!dmapresponse.isErrorCurrentlyLocked())
         * throw new Exception("testDistributedMapLock() - missing already locked error - " + dmapresponse);
         * if (!dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - missing error - " + dmapresponse);
         * Object obj1 = null;
         * if (interactiveMode) {
         * out.println("begin lock test 4  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * obj1 = distributedMap.getAndLock(s3, dmaprequest1, dmapresponse);
         * if (!dmapresponse.isErrorCurrentlyLocked())
         * throw new Exception("testDistributedMapLock() - missing already locked error - " + dmapresponse);
         * if (!dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - missing error - " + dmapresponse);
         * if (obj1 != null)
         * throw new Exception("testDistributedMapLock() - unexpected sucess - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 5  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * obj1 = distributedMap.getAndLock(s3, dmaprequest, dmapresponse);
         * if (obj1 == null)
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (dmapresponse.isErrorCurrentlyLocked())
         * throw new Exception("testDistributedMapLock() - unexpected already locked error - " + dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 6  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (distributedMap.unlockMapEntry(s3, dmaprequest1, dmapresponse))
         * throw new Exception("testDistributedMapLock() - unexpected success - " + dmapresponse);
         * if (!dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - missing error - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 7a  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (!distributedMap.unlockMapEntry(s3, dmaprequest, dmapresponse))
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 7b  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (!distributedMap.unlockMapEntry(s3, dmaprequest, dmapresponse))
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 7c  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (!distributedMap.unlockMapEntry(s3, dmaprequest, dmapresponse))
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - unexpected error - " + dmapresponse);
         * if (interactiveMode) {
         * out.println("begin lock test 7d  " + loopCount + " serializable objects");
         * out.println("<br>");
         * }
         * if (distributedMap.unlockMapEntry(s3, dmaprequest, dmapresponse))
         * throw new Exception("testDistributedMapLock() - unexpected success - " + dmapresponse);
         * if (!dmapresponse.isErrorCurrentlyNotLocked())
         * throw new Exception("testDistributedMapLock() - missing error - " + dmapresponse);
         * if (!dmapresponse.isError())
         * throw new Exception("testDistributedMapLock() - missing error - " + dmapresponse);
         * distributedMap.clearAllMapEntryLocks(dmaprequest, dmapresponse);
         * int l = 1;
         * int i1 = 5000;
         * runLockUnlock(l, i1);
         * if (interactiveMode) {
         * out.println("Threads = " + l);
         * out.println("<br>");
         * out.println("Lock  failures:" + lockFailure);
         * out.println("<br>");
         * out.println("Lock successes:" + lockSuccess + "  (" + lockSuccess / (long)(i1 / 1000) + " locks per second)");
         * out.println("<br>");
         * }
         * l = 2;
         * i1 = 5000;
         * runLockUnlock(l, i1);
         * if (interactiveMode) {
         * out.println("Threads = " + l);
         * out.println("<br>");
         * out.println("Lock  failures:" + lockFailure);
         * out.println("<br>");
         * out.println("Lock successes:" + lockSuccess + "  (" + lockSuccess / (long)(i1 / 1000) + " locks per second)");
         * out.println("<br>");
         * }
         * l = 10;
         * i1 = 5000;
         * runLockUnlock(l, i1);
         * if (interactiveMode) {
         * out.println("Threads = " + l);
         * out.println("<br>");
         * out.println("Lock  failures:" + lockFailure);
         * out.println("<br>");
         * out.println("Lock successes:" + lockSuccess + "  (" + lockSuccess / (long)(i1 / 1000) + " locks per second)");
         * out.println("<br>");
         * }
         * l = 40;
         * i1 = 5000;
         * runLockUnlock(l, i1);
         * if (interactiveMode) {
         * out.println("Threads = " + l);
         * out.println("<br>");
         * out.println("Lock  failures:" + lockFailure);
         * out.println("<br>");
         * out.println("Lock successes:" + lockSuccess + "  (" + lockSuccess / (long)(i1 / 1000) + " locks per second)");
         * out.println("<br>");
         * }
         * if (interactiveMode) {
         * out.println("testDistributedMapLock() - EXIT");
         * out.println("<br>");
         * }
         */
    }

    //------------------------------------------------------------
    //
    //------------------------------------------------------------
    /*
     * 
     * void runLockUnlock(int i, long l)
     * {
     * runNow = true;
     * lockFailure = 0L;
     * lockSuccess = 0L;
     * for (int j = 0; j != i; j++)
     * (new LockUnlockTest_1()).start();
     * 
     * try {
     * Thread.sleep(l);
     * } catch (Exception exception) {
     * }
     * runNow = false;
     * try {
     * Thread.sleep(2000L);
     * } catch (Exception exception1) {
     * }
     * }
     */
    //------------------------------------------------------------

    //------------------------------------------------------------
    //
    //------------------------------------------------------------
    public void testDistributedMapChangeListenerRegister() throws Exception {
        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerRegister() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("Registering " + loopCount + " callbacks");
            out.println("<br>");
        }
        distributedMap.enableListener(true);
        distributedMap.addChangeListener(this);
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerRegister() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapChangeListenerUnRegister() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerUnRegister() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("UnRegistering " + loopCount + " callbacks");
            out.println("<br>");
        }
        distributedMap.removeChangeListener(this);
        distributedMap.enableListener(false);
        //-----------------------------------------

        changeEvents.clear();

        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerUnRegister() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapChangeListenerChange() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerChange() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("Changing " + loopCount + " ids");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i + "map:";
            objectIn = "mySerializableObject-map: forId:" + i;
            //distributedMap.put( id, objectIn );
            distributedMap.put(id, objectIn, 1, ttl, shareType, null);
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerChange() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapChangeListenerVerify() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerVerify() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("Checking " + loopCount + " change events");
            out.println("<br>");
        }

        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i + "map:";
            objectIn = "mySerializableObject-map: forId:" + i;

            ChangeEvent ce = null;
            if ((ce = (ChangeEvent) changeEvents.get(id)) == null) {
                throw new Exception("Change event missing for id: " + id);
            }
            if (ce.getCauseOfChange() != changeCause) {
                throw new Exception("Change cause mismatch for id:" + id + " Expected:" + getChangeCause(changeCause) + " Received:" + getChangeCause(ce.getCauseOfChange()));
            }
            if (ce.getSourceOfChange() != changeSource) {
                throw new Exception("Change source mismatch for id:" + id + " Expected:" + getChangeSource(changeSource) + " Received:" + getChangeSource(ce.getSourceOfChange()));
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerVerify() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapChangeListenerVerifyNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerVerifyNone() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("Checking " + loopCount + " change events");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i + "map:";
            //objectIn = "mySerializableObject-map: forId:"+i;

            if (changeEvents.get(id) != null) {
                throw new Exception("Change event received for id: " + id);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapChangeListenerVerifyNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapListenerRegister() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableRegister() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------

        if (interactiveMode) {
            out.println("Registering " + loopCount + " callbacks");
            out.println("<br>");
        }
        distributedMap.enableListener(true);
        distributedMap.addInvalidationListener(this);

        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableRegister() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapListenerUnRegister() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapListenerUnRegister() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("UnRegistering " + loopCount + " callbacks");
            out.println("<br>");
        }
        distributedMap.removeInvalidationListener(this);
        distributedMap.enableListener(false);
        //-----------------------------------------

        invalidationEvents.clear();

        if (interactiveMode) {
            out.println("testDistributedMapListenerUnRegister() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapListenerVerify() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapListenerVerify() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("Checking " + loopCount + " invalidations");
            out.println("<br>");
        }

        int mapIndex = 0;

        if (invalidationCause == InvalidationEvent.CLEAR_ALL) {

            InvalidationEvent ie = null;
            if ((ie = (InvalidationEvent) invalidationEvents.get("*")) == null) {
                throw new Exception("Invalidation event missing for id:*");
            }
            if (ie.getSourceOfInvalidation() != invalidationSource) {
                throw new Exception("Invalidation source mismatch for id:*  Expected:" + getInvalidationSource(invalidationSource) + " Received:"
                                    + getInvalidationSource(ie.getSourceOfInvalidation()));
            }
            testDistributedMapListenerVerifyNone();
        } else {
            for (int i = 0; i != loopCount; i++) {
                String id = "test:" + i + "map:";//+mapIndex;
                objectIn = "mySerializableObject-map:" + mapIndex + " forId:" + i;

                InvalidationEvent ie = null;
                if ((ie = (InvalidationEvent) invalidationEvents.get(id)) == null) {
                    throw new Exception("Invalidation event missing for id: " + id);
                }
                if (ie.getCauseOfInvalidation() != invalidationCause) {
                    throw new Exception("Invalidation cause mismatch for id:" + id + " Expected:" + getInvalidationCause(invalidationCause) + " Received:"
                                        + getInvalidationCause(ie.getCauseOfInvalidation()));
                }
                if (invalidationCause == InvalidationEvent.TIMEOUT) {
                    if (ie.getSourceOfInvalidation() != InvalidationEvent.LOCAL && ie.getSourceOfInvalidation() != InvalidationEvent.REMOTE) {
                        throw new Exception("Invalidation source mismatch for id:" + id + " Expected: LOCAL or REMOTE" + "  Received:"
                                            + getInvalidationSource(ie.getSourceOfInvalidation()));
                    }
                } else {
                    if (ie.getSourceOfInvalidation() != invalidationSource) {
                        throw new Exception("Invalidation source mismatch for id:" + id + " Expected:" + getInvalidationSource(invalidationSource) + " Received:"
                                            + getInvalidationSource(ie.getSourceOfInvalidation()));
                    }
                }

            }
        }

        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapListenerVerify() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapListenerInvalidate() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapListenerInvalidate() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        int mapIndex = 0;

        if (interactiveMode) {
            out.println("Invalidating " + loopCount + " ids");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i + "map:";//+mapIndex;
            objectIn = "mySerializableObject-map:" + mapIndex + " forId:" + i;
            distributedMap.invalidate(id);
        }

        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapListenerInvalidate() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapListenerVerifyNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapListenerVerifyNone() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        int mapIndex = 0;
        if (interactiveMode) {
            out.println("Checking " + loopCount + " invalidations");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i + "map:";//+mapIndex;
            objectIn = "mySerializableObject-map:" + mapIndex + " forId:" + i;

            if (invalidationEvents.get(id) != null) {
                throw new Exception("Invalidation event received for id: " + id);
            }
        }

        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapListenerVerifyNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testCacheInstanceMapLookup() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testCacheInstanceMapLookup() - ENTRY");
        }

        // Get initial context
        Context context = new InitialContext();
        if (context == null) {
            throw new Exception("testCacheInstanceMapLookup - error - context is null");
        }

        // Lookup the maps
        for (int i = 0; i != maxInstances; i++) {
            distributedMaps[i] = (DistributedObjectCache) context.lookup(jndiNames[i]);
            if (distributedMaps[i] == null) {
                throw new Exception("testCacheInstanceMapLookup - error - distributedMap is null ");
            }
            if (interactiveMode)
                out.println("<br>Created: " + distributedMaps[i]);
        }

        // Verify maps are unique
        for (int x = 0; x != maxInstances - 1; x++) {
            for (int y = x + 1; y != maxInstances; y++) {
                if (interactiveMode)
                    out.println("<br>Compare map " + x + " with map " + y);
                if (distributedMaps[x] == distributedMaps[y]) {
                    throw new Exception("testCacheInstanceMapLookup - error - distributedMaps are not unique");
                }
            }
        }

        // Vefrify exception on invalid jndi name
        boolean pass = false;
        try {
            context.lookup("services/cache/distributedmap_FORCE_EXPECTED_ERROR");
        } catch (javax.naming.NameNotFoundException e) {
            pass = true;
            if (interactiveMode)
                out.println("<br>Expected exception NameNotFoundException Okay<br>");
        }
        if (!pass) {
            throw new Exception("testCacheInstanceMapLookup - error - Failed to see NameNotFoundException on invalid jndi name");
        }

        if (interactiveMode) {
            out.println("testCacheInstanceMapLookup() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testCacheInstanceSecondMapLookup() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testCacheInstanceSecondMapLookup() - ENTRY");
        }

        Context context = new InitialContext();
        if (context == null) {
            throw new Exception("testCacheInstanceSecondMapLookup - error - context is null");
        }

        for (int i = 0; i != maxInstances; i++) {
            if (distributedMaps[i] != (DistributedObjectCache) context.lookup(jndiNames[i])) {
                throw new Exception("testCacheInstanceSecondMapLookup - error - distributedMap is different");
            }
            if (interactiveMode)
                out.println("<br>Compare Okay: " + distributedMaps[i]);
        }

        if (interactiveMode) {
            out.println("<br>testCacheInstanceSecondMapLookup() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializable() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializable() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            //distributedMap.put( id, objectIn );
            distributedMap.put(id, objectIn, 1, ttl, shareType, null);
        }
        //-----------------------------------------

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry");
            }
            if (!(objectOut instanceof Serializable)) {
                throw new Exception("testSerialNonSerial() - error - serializable object was not returned");
            }
            if (!objectIn.equals(objectOut)) {
                throw new Exception("testSerialNonSerial() - error - compare error " + objectIn + " " + objectOut);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializable() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableContent() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableContent() - ENTRY");
            out.println("<br>");
        }

        objectIn = cacheContentIn;

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            //distributedMap.put( id, objectIn );
            distributedMap.put(id, objectIn, 1, ttl, shareType, null);
        }
        //-----------------------------------------

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }

        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry " + id);
            }
            if (!(objectOut instanceof Serializable)) {
                throw new Exception("testSerialNonSerial() - error - serializable object was not returned");
            }
            if (!objectIn.equals(objectOut)) {
                throw new Exception("testSerialNonSerial() - error - compare error " + objectIn + " " + objectOut);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableContent() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializablePut() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializablePut() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            if (distributedMap.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP) {
                distributedMap.put(id, objectIn, null, 1, ttl, shareType, null, null);
            } else {

                //distributedMap.put( id, objectIn );
                distributedMap.put(id, objectIn, 1, ttl, shareType, null);
            }

        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializablePut() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableGet() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableGet() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            if (distributedMap.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP) {
                com.ibm.websphere.cache.CacheEntry ce = distributedMap.getCacheEntry(id);
                if (ce == null) {
                    throw new Exception("testSerialNonSerial() - error - missing cache entry");
                }
                if (!objectIn.equals(ce.getValue())) {
                    throw new Exception("testSerialNonSerial() - error - compare error " + objectIn + " " + ce.getValue());
                }
                ce.finish();
            } else {
                Object objectOut = distributedMap.get(id);
                if (objectOut == null) {
                    throw new Exception("testSerialNonSerial() - error - missing cache entry");
                }
                if (!(objectOut instanceof Serializable)) {
                    throw new Exception("testSerialNonSerial() - error - serializable object was not returned");
                }
                if (!objectIn.equals(objectOut)) {
                    throw new Exception("testSerialNonSerial() - error - compare error " + objectIn + " " + objectOut);
                }
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableGet() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableGetContent() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableGetContent() - ENTRY");
            out.println("<br>");
        }

        objectIn = cacheContentIn;

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }

        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry" + id);
            }
            if (!(objectOut instanceof Serializable)) {
                throw new Exception("testSerialNonSerial() - error - serializable object was not returned");
            }
            if (!objectIn.equals(objectOut)) {
                throw new Exception("testSerialNonSerial() - error - compare error " + objectIn + " " + objectOut);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableGetContent() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableGetNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableGetNone() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null != objectOut) {
                throw new Exception("testSerialNonSerial() - error - cache entry still alive");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //SKS-O
    //------------------------------------------------------------
    public void testDistributedMapSerializableMyObjectKeyGetNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyGetNone() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(new MySerializableObjectKey(id, true));
            if (null != objectOut) {
                throw new Exception("testSerializableMyObjectKeyGetNone() - error - cache entry still alive");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyGetNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------
    //SKS-O

    //------------------------------------------------------------
    public void testDistributedMapNonSerializable() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializable() - ENTRY");
            out.println("<br>");
        }

        objectIn = new Object();
        objectIn1 = new Hashtable();

        ((Hashtable) objectIn1).put("key", new Object());

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " non-serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            //distributedMap.put( id, objectIn );
            try {
                distributedMap.put(id, objectIn, 1, ttl, shareType, null);
            } catch (Exception e) {
                System.out.println("WARNING: - 1st - DRSServlet caught the following exception:");
                System.out.println(getStackTrace(e));
            }

            String id1 = "test1:" + i;
            //distributedMap.put( id1, objectIn1 );
            try {
                distributedMap.put(id1, objectIn1, 1, ttl, shareType, null);
            } catch (Exception e) {
                System.out.println("WARNING: - 2st - DRSServlet caught the following exception:");
                System.out.println(getStackTrace(e));
            }
        }
        //-----------------------------------------

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " non-serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry");
            }

            if ((objectOut instanceof Serializable)) {
                throw new Exception("testSerialNonSerial() - error - serializable object was returned");
            }

            String id1 = "test1:" + i;

            objectOut = distributedMap.get(id1);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializable() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapClear() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapClear() - ENTRY");
            out.println("<br>");
        }

        distributedMap.clear();

        if (interactiveMode) {
            out.println("testDistributedMapClear() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    private void waitForStartCommand()
    //------------------------------------------------------------
    {
        while (!startStress) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapStress() throws Exception
    //------------------------------------------------------------
    {
        final String methodName = "testDistributedMapStress()";
        if (interactiveMode) {
            out.println("testDistributedMapStress() - ENTRY");
            out.println("<br>");
        }

        System.out.println(methodName + " begin");

        distributedMap.clear();

        int threadCount = 5;

        // Fill the cache
        for (int x = 0; x != entryCount; x++) {
            distributedMap.put("key" + x, "xxxx");
        }

        // 1 thread - keep LRU evict running
        for (int i = 0; i != (5 * threadCount); i++) {
            System.out.println(methodName + " LRU evict running " + i);
            Thread t = new Thread() {
                @Override
                public void run() {
                    waitForStartCommand();
                    try {
                        while (startStress) {
                            for (int x = 0; x != 100000; x++) {
                                try {
                                    //distributedMap.put("key"+x,"yyy");
                                    distributedMap.put("key" + x, "yyy", 1, ttl, shareType, null);
                                } catch (Exception e) {
                                    exception = e;
                                    startStress = false;
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    } finally {
                    }
                }
            };
            t.setDaemon(true);
            t.start();
        }

        // 5 Threads  - Sets / Removes  1 - 5
        for (int i = 0; i != threadCount; i++) {
            System.out.println(methodName + " set/remove thread waiting " + i);
            Thread t1 = new Thread() {
                @Override
                public void run() {
                    waitForStartCommand();
                    System.out.println(methodName + " set/remove thread running");
                    try {
                        while (startStress) {
                            for (int x = 0; x != 5; x++) {
                                try {
                                    //distributedMap.put("key"+x,"xxxx");
                                    distributedMap.put("key" + x, "xxx", 1, ttl, shareType, null);
                                    distributedMap.remove("key" + x);
                                } catch (Exception e) {
                                    exception = e;
                                    startStress = false;
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    } finally {
                    }
                }
            };
            t1.setDaemon(true);
            t1.start();
        }

        // 5 Threads   - Gets 1 - 5
        for (int i = 0; i != threadCount; i++) {
            System.out.println(methodName + " get thread waiting " + i);
            Thread t1 = new Thread() {
                @Override
                public void run() {
                    waitForStartCommand();
                    System.out.println(methodName + " get thread running");
                    try {
                        while (startStress) {
                            for (int x = 0; x != 5; x++) {
                                try {
                                    distributedMap.get("key" + x);
                                } catch (Exception e) {
                                    exception = e;
                                    startStress = false;
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    } finally {
                    }
                }
            };
            t1.setDaemon(true);
            t1.start();
        }

        startStress = true;

        // Run for 2 min

        //Thread.sleep( 1000 * 60 * 2 );
        Thread.sleep(1000 * 30 * 1);

        startStress = false;

        Thread.sleep(1000 * 10);

        if (interactiveMode) {
            out.println("testDistributedMapStress() - EXIT");
            out.println("<br>");
        }

        if (exception != null) {
            throw exception;
        }

        System.out.println(methodName + " end");
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializablePut() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializablePut() - ENTRY");
            out.println("<br>");
        }

        objectIn = new Object();
        objectIn1 = new Hashtable();

        ((Hashtable) objectIn1).put("key", new Object());

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " non-serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            //distributedMap.put( id, objectIn );
            distributedMap.put(id, objectIn, 1, ttl, shareType, null);

            String id1 = "test1:" + i;
            //distributedMap.put( id1, objectIn1 );
            distributedMap.put(id1, objectIn1, 1, ttl, shareType, null);

        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializablePut() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //SKS-O
    //------------------------------------------------------------
    public void testDistributedMapSerializableObjectKeyPut() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyPut() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable object keys");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            //distributedMap.put( new MySerializableObjectKey(id), objectIn, 1, ttl, shareType, null ); //sajan
            Properties prop = new java.util.Properties();
            prop.put("key", "value");
            distributedMap.put(new javax.naming.CompoundName(id, prop), objectIn, 1, ttl, shareType, null); //sajan

        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyPut() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableMyObjectKeyPut() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyPut() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable object keys");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.put(new MySerializableObjectKey(id, true), objectIn, 1, ttl, shareType, null); //sajan
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyPut() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableMyObjectDepKeyPut() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectDepKeyPut() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable object keys");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.put(new MySerializableObjectKey(id, true), objectIn, 1, ttl, shareType, new Object[] { new MySerializableObjectKey("dep_" + id, true) });
            //Properties prop = new java.util.Properties();
            //prop.put("key", "value");
            //distributedMap.put( new javax.naming.CompoundName(id, prop), objectIn, 1, ttl, shareType, null ); //sajan

        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectDepKeyPut() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializableObjectKeyPut() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyPut() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        // distributedMap.clear();

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable object keys");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.put(new MyNonSerializableObjectKey(id), objectIn, 1, ttl, shareType, null);

        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyPut() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializableObjectKeyGet() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyGet() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable object keys");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            Object objectOut = distributedMap.get(new MyNonSerializableObjectKey(id));
            if (objectOut == null) {
                throw new Exception("testDistributedMapNonSerializableObjectKeyGet() - error - missing cache entry");
            }
            if (!objectIn.equals(objectOut)) {
                throw new Exception("testDistributedMapNonSerializableObjectKeyGet() - error - compare error " + objectIn + " " + objectOut);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyGet() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializableObjectKeyGetNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyGetNone() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("putting " + loopCount + " serializable object keys");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            Object objectOut = distributedMap.get(new MyNonSerializableObjectKey(id));
            if (objectOut != null) {
                throw new Exception("testDistributedMapNonSerializableObjectKeyGetNone() - error - cache entry still alive");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyGetNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializableObjectKeyInvalidate() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyInvalidate() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("invalidating " + loopCount + " objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.invalidate(new MyNonSerializableObjectKey(id));
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableObjectKeyInvalidate() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableObjectKeyGet() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyGet() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            //Object objectOut = distributedMap.get(new MySerializableObjectKey(id)); //sajan
            Properties prop = new java.util.Properties();
            prop.put("key", "value");
            Object objectOut = distributedMap.get(new javax.naming.CompoundName(id, prop)); //sajan
            if (null == objectOut) {
                throw new Exception("testDistributedMapSerializableObjectKeyGet() - error - missing cache entry");
            }
            if (!objectIn.equals(objectOut)) {
                throw new Exception("testDistributedMapSerializableObjectKeyGet() - error - compare error " + objectIn + " " + objectOut);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyGet() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableObjectKeyGetNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyGetNone() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Properties prop = new java.util.Properties();
            prop.put("key", "value");
            Object objectOut = distributedMap.get(new javax.naming.CompoundName(id, prop)); //sajan
            if (null != objectOut) {
                throw new Exception("testDistributedMapSerializableObjectKeyGetNone() - error - cache entry still alive");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyGetNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------
    //------------------------------------------------------------
    public void testDistributedMapSerializableObjectKeyInvalidate() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyInvalidate() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("invalidating " + loopCount + " objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Properties prop = new java.util.Properties();
            prop.put("key", "value");
            distributedMap.invalidate(new javax.naming.CompoundName(id, prop));
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableObjectKeyInvalidate() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableMyObjectKeyGet() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyGet() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(new MySerializableObjectKey(id, true)); //sajan
            //Properties prop = new java.util.Properties();
            //prop.put("key", "value");
            //Object objectOut = distributedMap.get(new javax.naming.CompoundName(id, prop)); //sajan
            if (null == objectOut) {
                throw new Exception("testDistributedMapSerializableMyObjectKeyGet() - error - missing cache entry");
            }
            if (!objectIn.equals(objectOut)) {
                throw new Exception("testDistributedMapSerializableMyObjectKeyGet() - error - compare error " + objectIn + " " + objectOut);
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyGet() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapSerializableMyObjectKeyInvalidate() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyInvalidate() - ENTRY");
            out.println("<br>");
        }

        objectIn = "mySerializableObject";

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("invalidating " + loopCount + " objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.invalidate(new MySerializableObjectKey(id, true));
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapSerializableMyObjectKeyInvalidate() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapInvalidateMyObjectDepKey() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapInvalidateMyObjectDepKey() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("invalidating " + loopCount + " objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.invalidate(new MySerializableObjectKey("dep_" + id, true));
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapInvalidateMyObjectDepKey() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapInvalidate() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapInvalidate() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("invalidating " + loopCount + " objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.invalidate(id);

            id = "test1:" + i;
            distributedMap.invalidate(id);
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapInvalidate() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapInvalidateNonBlocking() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapInvalidateNonBlocking() - ENTRY");
            out.println("<br>");
        }

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("invalidating " + loopCount + " objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap.invalidate(id, false);

            id = "test1:" + i;
            distributedMap.invalidate(id, false);

        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapInvalidateNonBlocking() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializableGet() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableGet() - ENTRY");
            out.println("<br>");
        }

        objectIn = new Object();
        objectIn1 = new Hashtable();

        ((Hashtable) objectIn1).put("key", new Object());

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " non-serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry");
            }

            if ((objectOut instanceof Serializable)) {
                throw new Exception("testSerialNonSerial() - error - serializable object was returned");
            }

            String id1 = "test1:" + i;

            objectOut = distributedMap.get(id1);
            if (null == objectOut) {
                throw new Exception("testSerialNonSerial() - error - missing cache entry");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableGet() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapNonSerializableGetNone() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableGetNone() - ENTRY");
            out.println("<br>");
        }

        objectIn = new Object();
        objectIn1 = new Hashtable();

        ((Hashtable) objectIn1).put("key", new Object());

        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("getting " + loopCount + " non-serializable objects");
            out.println("<br>");
        }
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;

            Object objectOut = distributedMap.get(id);
            if (null != objectOut) {
                throw new Exception("testDistributedMapNonSerializableGetNone() - error #1 - cache entry still alive");
            }

            String id1 = "test1:" + i;

            objectOut = distributedMap.get(id1);
            if (null != objectOut) {
                throw new Exception("testDistributedMapNonSerializableGetNone() - error #2 - cache entry still alive");
            }
        }
        //-----------------------------------------

        if (interactiveMode) {
            out.println("testDistributedMapNonSerializableGetNone() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapFactory() throws Exception
    //------------------------------------------------------------
    {
        final String methodName = "testDistributedMapFactory()";

        boolean pass = false;

        if (interactiveMode) {
            out.println(methodName + " - ENTRY");
            out.println("<br>");
        }

        // Create named map 1
        String mapName = "myMap_1";
        DistributedObjectCache distributedMap_1 = (DistributedObjectCache) DistributedMapFactory.getMap(mapName);
        if (distributedMap_1 == null) {
            throw new Exception(methodName + " - distributedMap_1 is null");
        }
        if (interactiveMode) {
            out.println(methodName + " - created " + mapName);
            out.println("<br>");
        }

        // Create named map 2
        mapName = "myMap_2";
        DistributedObjectCache distributedMap_2 = (DistributedObjectCache) DistributedMapFactory.getMap(mapName);
        if (distributedMap_2 == null) {
            throw new Exception(methodName + " - distributedMap_2 is null");
        }
        if (interactiveMode) {
            out.println(methodName + " - created " + mapName);
            out.println("<br>");
        }

        // Verify maps are unique
        if (distributedMap_1 == distributedMap_2) {
            throw new Exception(methodName + " - distributedMap 1 & 2 are not unique");
        }
        if (interactiveMode) {
            out.println(methodName + " - map 1 and map 2 are unique");
            out.println("<br>");
        }

        // Create unnamed map 3 - should fail
        pass = false;
        try {
            DistributedObjectCache distributedMap_3 = (DistributedObjectCache) DistributedMapFactory.getMap(null);
        } catch (IllegalStateException e) {
            pass = true;
        }
        if (!pass) {
            throw new Exception(methodName + " - distributedMapFacroy failed to throw an IllegalStateException");
        }
        if (interactiveMode) {
            out.println(methodName + " - expected exception received for null map name");
            out.println("<br>");
        }

        // Create unnamed map 4 - should fail
        pass = false;
        try {
            DistributedObjectCache distributedMap_4 = (DistributedObjectCache) DistributedMapFactory.getMap("");
        } catch (IllegalStateException e) {
            pass = true;
        }
        if (!pass) {
            throw new Exception(methodName + " - distributedMapFacroy failed to throw an IllegalStateException");
        }
        if (interactiveMode) {
            out.println(methodName + " - expected exception received for unnamed map name");
            out.println("<br>");
        }

        // Create unnamed map 5 - should fail
        pass = false;
        try {
            DistributedObjectCache distributedMap_5 = (DistributedObjectCache) DistributedMapFactory.getMap(" ");
        } catch (IllegalStateException e) {
            pass = true;
        }
        if (!pass) {
            throw new Exception(methodName + " - distributedMapFacroy failed to throw an IllegalStateException");
        }
        if (interactiveMode) {
            out.println(methodName + " - expected exception received for space map name");
            out.println("<br>");
        }

        // Verify maps 1 & 2 do not intersect
        Object key_1 = "key_1";
        Object key_2 = "key_2";
        distributedMap_1.put(key_1, key_1);
        distributedMap_2.put(key_2, key_2);
        Object value_1 = distributedMap_1.get(key_1);
        Object value_2 = distributedMap_2.get(key_2);
        Object value_1a = distributedMap_1.get(key_2);
        Object value_2a = distributedMap_2.get(key_1);
        if (!((String) value_1).equals(key_1)) {
            throw new Exception(methodName + " - distributedMap failed a put / get");
        }
        if (!((String) value_2).equals(key_2)) {
            throw new Exception(methodName + " - distributedMap failed a put / get");
        }
        if (value_1a != null) {
            throw new Exception(methodName + " - distributedMapFactory failed to create unique map");
        }
        if (value_2a != null) {
            throw new Exception(methodName + " - distributedMapFactory failed to create unique map");
        }
        if (interactiveMode) {
            out.println(methodName + " - no map collisions");
            out.println("<br>");
        }

        // Verify default cache accessed via JNDI and Factory are the same.
        DistributedObjectCache defaultMap_1 = null;
        DistributedObjectCache defaultMap_2 = null;
        try {
            Context context = new InitialContext();
            defaultMap_1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap");
        } catch (Throwable e) {
            throw new Exception(methodName + " - unable to access default cache (1)" + e);
        }
        defaultMap_2 = (DistributedObjectCache) DistributedMapFactory.getMap("default");
        defaultMap_1.clear();
        defaultMap_2.clear();
        defaultMap_1.put(key_1, key_1);
        String result = (String) defaultMap_2.put(key_1, key_1);
        if (result != null && result.equals(key_1)) {

        } else {
            throw new Exception(methodName + " - default maps not the same");
        }
        if (interactiveMode) {
            out.println(methodName + " - default maps are the same");
            out.println("<br>");
        }

        // Verify base cache can be wrapped
        defaultMap_1 = null;
        defaultMap_2 = null;
        try {
            Context context = new InitialContext();
            defaultMap_1 = (DistributedObjectCache) context.lookup("services/cache/basecache");
        } catch (Throwable e) {
            throw new Exception(methodName + " - unable to access base cache (1)" + e);
        }
        defaultMap_2 = (DistributedObjectCache) DistributedMapFactory.getMap("baseCache");
        defaultMap_1.clear();
        defaultMap_2.clear();
        defaultMap_1.put(key_1, key_1);
        result = (String) defaultMap_2.put(key_1, key_1);
        if (result != null && result.equals(key_1)) {

        } else {
            throw new Exception(methodName + " - baseCache maps not the same");
        }
        if (interactiveMode) {
            out.println(methodName + " - baseCache maps are the same");
            out.println("<br>");
        }

        // Verify base cache via JNDI and DynamicCacheAccessor are the same
        // defaultMap_1 is base cache via JNDI
        com.ibm.websphere.cache.DistributedMap accessorDMap = DynamicCacheAccessor.getDistributedMap();
        accessorDMap.clear();
        defaultMap_1.clear();
        defaultMap_1.put(key_1, key_1);
        result = (String) accessorDMap.put(key_1, key_1);
        if (result != null && result.equals(key_1)) {

        } else {
            throw new Exception(methodName + " - Problem with DynamicCacheAccessor");
        }
        if (interactiveMode) {
            out.println(methodName + " - DynamicCacheAccessor can wrap baseCache");
            out.println("<br>");
        }

        // Verify base cache and default cache are different
        defaultMap_1 = null;
        defaultMap_2 = null;
        try {
            Context context = new InitialContext();
            defaultMap_1 = (DistributedObjectCache) context.lookup("services/cache/basecache");
        } catch (Throwable e) {
            throw new Exception(methodName + " - unable to access base cache (1)" + e);
        }
        defaultMap_2 = (DistributedObjectCache) DistributedMapFactory.getMap("default");
        defaultMap_1.clear();
        defaultMap_2.clear();
        defaultMap_1.put(key_1, key_1);
        result = (String) defaultMap_2.put(key_1, key_1);
        if (result == null) {

        } else {
            throw new Exception(methodName + " - baseCache and default map are the same");
        }
        if (interactiveMode) {
            out.println(methodName + " - baseCache and defeult map are not the same");
            out.println("<br>");
        }

        // Done
        if (interactiveMode) {
            out.println(methodName + " - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedObjectCacheFactory() throws Exception
    //------------------------------------------------------------
    {
        final String methodName = "testDistributedObjectCacheFactory()";
        boolean pass = false;

        if (interactiveMode) {
            out.println(methodName + " ENTRY");
            out.println("<br>");
        }

        // Create named map 1
        String mapName = "myMap_1";
        DistributedObjectCache distributedMap_1 = DistributedObjectCacheFactory.getMap(mapName);
        if (distributedMap_1 == null) {
            throw new Exception(methodName + " - distributedMap_1 is null");
        }
        if (interactiveMode) {
            out.println(methodName + " - created " + mapName);
            out.println("<br>");
        }

        // Create named map 2
        mapName = "myMap_2";
        DistributedObjectCache distributedMap_2 = DistributedObjectCacheFactory.getMap(mapName);
        if (distributedMap_2 == null) {
            throw new Exception(methodName + " - distributedMap_2 is null");
        }
        if (interactiveMode) {
            out.println(methodName + " - created " + mapName);
            out.println("<br>");
        }

        // Verify maps are unique
        if (distributedMap_1 == distributedMap_2) {
            throw new Exception(methodName + " - distributedMap 1 & 2 are not unique");
        }
        if (interactiveMode) {
            out.println(methodName + " - map 1 and map 2 are unique");
            out.println("<br>");
        }

        // Create unnamed map 3 - should fail
        pass = false;
        try {
            DistributedObjectCache distributedMap_3 = DistributedObjectCacheFactory.getMap(null);
        } catch (IllegalStateException e) {
            pass = true;
        }
        if (!pass) {
            throw new Exception(methodName + " - distributedMapFacroy failed to throw an IllegalStateException");
        }
        if (interactiveMode) {
            out.println(methodName + " - expected exception received for null map name");
            out.println("<br>");
        }

        // Create unnamed map 4 - should fail
        pass = false;
        try {
            DistributedObjectCache distributedMap_4 = DistributedObjectCacheFactory.getMap("");
        } catch (IllegalStateException e) {
            pass = true;
        }
        if (!pass) {
            throw new Exception(methodName + " - distributedMapFacroy failed to throw an IllegalStateException");
        }
        if (interactiveMode) {
            out.println(methodName + " - expected exception received for unnamed map name");
            out.println("<br>");
        }

        // Create unnamed map 5 - should fail
        pass = false;
        try {
            DistributedObjectCache distributedMap_5 = DistributedObjectCacheFactory.getMap(" ");
        } catch (IllegalStateException e) {
            pass = true;
        }
        if (!pass) {
            throw new Exception(methodName + " - distributedMapFacroy failed to throw an IllegalStateException");
        }
        if (interactiveMode) {
            out.println(methodName + " - expected exception received for space map name");
            out.println("<br>");
        }

        // Verify maps 1 & 2 do not intersect
        Object key_1 = "key_1";
        Object key_2 = "key_2";
        distributedMap_1.put(key_1, key_1);
        distributedMap_2.put(key_2, key_2);
        Object value_1 = distributedMap_1.get(key_1);
        Object value_2 = distributedMap_2.get(key_2);
        Object value_1a = distributedMap_1.get(key_2);
        Object value_2a = distributedMap_2.get(key_1);
        if (!((String) value_1).equals(key_1)) {
            throw new Exception(methodName + " - distributedMap failed a put / get");
        }
        if (!((String) value_2).equals(key_2)) {
            throw new Exception(methodName + " - distributedMap failed a put / get");
        }
        if (value_1a != null) {
            throw new Exception(methodName + " - distributedMapFactory failed to create unique map");
        }
        if (value_2a != null) {
            throw new Exception(methodName + " - distributedMapFactory failed to create unique map");
        }
        if (interactiveMode) {
            out.println(methodName + " - no map collisions");
            out.println("<br>");
        }

        // Verify default cache accessed via JNDI and Factory are the same.
        DistributedObjectCache defaultMap_1 = null;
        DistributedObjectCache defaultMap_2 = null;
        try {
            Context context = new InitialContext();
            defaultMap_1 = (DistributedObjectCache) context.lookup("services/cache/distributedmap");
        } catch (Throwable e) {
            throw new Exception(methodName + " - unable to access default cache (1)" + e);
        }
        defaultMap_2 = DistributedObjectCacheFactory.getMap("default");
        defaultMap_1.clear();
        defaultMap_2.clear();
        defaultMap_1.put(key_1, key_1);
        String result = (String) defaultMap_2.put(key_1, key_1);
        if (result != null && result.equals(key_1)) {

        } else {
            throw new Exception(methodName + " - default maps not the same");
        }
        if (interactiveMode) {
            out.println(methodName + " - default maps are the same");
            out.println("<br>");
        }

        // Verify base cache can be wrapped
        defaultMap_1 = null;
        defaultMap_2 = null;
        try {
            Context context = new InitialContext();
            defaultMap_1 = (DistributedObjectCache) context.lookup("services/cache/basecache");
        } catch (Throwable e) {
            throw new Exception(methodName + " - unable to access base cache (1)" + e);
        }
        defaultMap_2 = DistributedObjectCacheFactory.getMap("baseCache");
        defaultMap_1.clear();
        defaultMap_2.clear();
        defaultMap_1.put(key_1, key_1);
        result = (String) defaultMap_2.put(key_1, key_1);
        if (result != null && result.equals(key_1)) {

        } else {
            throw new Exception(methodName + " - baseCache maps not the same");
        }
        if (interactiveMode) {
            out.println(methodName + " - baseCache maps are the same");
            out.println("<br>");
        }

        //----------------------------------------
        // Verify map type DistributedMap 
        //----------------------------------------

        mapName = "myDistributedMap";
        Properties p = new Properties();
        p.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "10");
        p.put(DistributedObjectCacheFactory.KEY_DISABLE_DEPENDENCY_ID,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_DISABLE_TEMPLATES_SUPPORT,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD,
              DistributedObjectCacheFactory.VALUE_FALSE);
        //p.put(DistributedObjectCacheFactory.KEY_ENABLE_LOCKING_SUPPORT, 
        //      DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_FLUSH_TO_DISK_ON_STOP,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_USE_LISTENER_CONTEXT,
              DistributedObjectCacheFactory.VALUE_FALSE);

        DistributedObjectCache map = DistributedObjectCacheFactory.getMap(mapName, p);
        if (map.getMapType() != DistributedObjectCache.TYPE_DISTRIBUTED_MAP) {
            throw new Exception(methodName + " - received map type " + map.getMapType() + " expected map type " + DistributedObjectCache.TYPE_DISTRIBUTED_MAP);
        }
        if (interactiveMode) {
            out.println(methodName + " - Map Type correct for DistributedMap");
            out.println("<br>");
        }

        //----------------------------------------
        // Verify map type DistributedNioMap 
        //----------------------------------------

        mapName = "myDistributedNioMap";
        p = new Properties();
        p.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "10");
        p.put(DistributedObjectCacheFactory.KEY_DISABLE_DEPENDENCY_ID,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_DISABLE_TEMPLATES_SUPPORT,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD,
              DistributedObjectCacheFactory.VALUE_FALSE);
        //p.put(DistributedObjectCacheFactory.KEY_ENABLE_LOCKING_SUPPORT, 
        //      DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT,
              DistributedObjectCacheFactory.VALUE_TRUE);
        p.put(DistributedObjectCacheFactory.KEY_FLUSH_TO_DISK_ON_STOP,
              DistributedObjectCacheFactory.VALUE_FALSE);
        p.put(DistributedObjectCacheFactory.KEY_USE_LISTENER_CONTEXT,
              DistributedObjectCacheFactory.VALUE_FALSE);

        map = DistributedObjectCacheFactory.getMap(mapName, p);
        if (map.getMapType() != DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP) {
            throw new Exception(methodName + " - received map type " + map.getMapType() + " expected map type " + DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP);
        }

        if (interactiveMode) {
            out.println(methodName + " - Map Type correct for DistributedNioMap");
            out.println("<br>");
        }

        // Done
        if (interactiveMode) {
            out.println(methodName + " - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    String ddTest_id1 = "id1";
    String ddTest_id2 = "id2";
    String ddTest_dd1[] = { "dd1" };
    String ddTest_dd2[] = { "dd2" };
    String ddTest_v1 = "This is a cached object for id1";
    String ddTest_v2 = "This is a cached object for id2";

    //------------------------------------------------------------
    public void testDistributedMapDataIds_Put() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapDataIds_Put() - ENTRY");
            out.println("<br>");
        }
        distributedMap.put(ddTest_id1, ddTest_v1, 1, ttl, shareType, ddTest_dd1);
        String value = (String) distributedMap.get(ddTest_id1);
        if (!value.equals(ddTest_v1)) {
            throw new Exception("testDistributedMapDataIds_Put() - basic put error ");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapDataIds_Get_BeforeReplace() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapDataIds_Get_BeforeReplace() - ENTRY");
            out.println("<br>");
        }
        String value = (String) distributedMap.get(ddTest_id1);
        if (value == null) {
            throw new Exception("testDistributedMapDataIds_Get_BeforeReplace() - error - value is null");
        }
        if (!value.equals(ddTest_v1)) {
            throw new Exception("testDistributedMapDataIds_Get_BeforeReplace() - error - data mismatch");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapDataIds_Replace() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapDataIds_Get() - ENTRY");
            out.println("<br>");
        }
        distributedMap.put(ddTest_id1, ddTest_v2, 1, ttl, shareType, ddTest_dd2);
        String value = (String) distributedMap.get(ddTest_id1);
        if (!value.equals(ddTest_v2)) {
            throw new Exception("testDistributedMapDataIds_Get() - basic put error ");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapDataIds_Invalidate() throws Exception
    //------------------------------------------------------------
    {
        distributedMap.invalidate(ddTest_dd1[0]);
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testDistributedMapDataIds_Get_AfterReplace() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testDistributedMapDataIds_Get_AfterReplace() - ENTRY");
            out.println("<br>");
        }
        String value = (String) distributedMap.get(ddTest_id1);
        if (value == null) {
            throw new Exception("testDistributedMapDataIds_Get_AfterReplace() - error - value is null ( old dependencyID still existed )");
        }
        if (!value.equals(ddTest_v2)) {
            throw new Exception("testDistributedMapDataIds_Get_AfterReplace() - error - data mismatch");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testInvalidationAuditDaemon() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testInvalidationAuditDaemon() - ENTRY");
            out.println("<br>");
        }
        objectIn = "mySerializableObject";
        //-----------------------------------------
        //
        //-----------------------------------------
        if (interactiveMode) {
            out.println("loopCount=" + loopCount + " Put in distributedMap_1 Push-mode; ");
            out.println("invalidate with id");
            out.println("put in distributedMap_2 Push-mode");
            out.println("<br>");
        }

        String depid[] = { "did-1" };
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap_1.put(id, objectIn, 1, 0, EntryInfo.SHARED_PUSH, depid);
            try {
                Thread.sleep(10);
            } catch (Exception exception1) {
            }
            distributedMap_1.invalidate(id, false);
            try {
                Thread.sleep(10);
            } catch (Exception exception1) {
            }
            distributedMap_2.put(id, objectIn, 1, 0, EntryInfo.SHARED_PUSH, depid);
            try {
                Thread.sleep(10);
            } catch (Exception exception1) {
            }
        }

        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            distributedMap_B.put(id, objectIn, 1, 0, EntryInfo.SHARED_PUSH_PULL, depid);
            try {
                Thread.sleep(10);
            } catch (Exception exception1) {
            }
            distributedMap_B.invalidate(id, false);
        }

        /*
         * for (int i=0; i != loopCount; i++) {
         * String id = "test:"+i;
         * distributedMap_1.put( id, objectIn, 1, 0, EntryInfo.SHARED_PUSH, depid );
         * }
         * distributedMap_1.invalidate("did-1");
         * for (int i=0; i != loopCount; i++) {
         * String id = "test:"+i;
         * distributedMap_2.put( id, objectIn, 1, 0, EntryInfo.SHARED_PUSH, depid );
         * }
         */

        //-----------------------------------------

        if (interactiveMode) {
            out.println("testInvalidationAuditDaemon() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void testRenounceForMixedMode() throws Exception
    //------------------------------------------------------------
    {
        if (interactiveMode) {
            out.println("testRenounceForMixedMode() - ENTRY");
            out.println("<br>");
        }
        objectIn = "mySerializableObject";
        //-----------------------------------------
        //
        //-----------------------------------------
        String depid[] = { "did-1" };
        int j = 0;
        for (int i = 0; i != loopCount; i++) {
            String id = "test:" + i;
            if (j == 0) {
                distributedMap_1.put(id, objectIn, 1, 0, EntryInfo.NOT_SHARED, depid);
            } else if (j == 1) {
                distributedMap_1.put(id, objectIn, 1, 0, EntryInfo.SHARED_PUSH, depid);
            } else if (j == 2) {
                distributedMap_1.put(id, objectIn, 1, 0, EntryInfo.SHARED_PUSH_PULL, depid);
            }
            j++;
            if (j == 3) {
                j = 0;
            }
            try {
                Thread.sleep(2);
            } catch (Exception exception1) {
            }
        }

        distributedMap_1.invalidate("did-1");

        //-----------------------------------------

        if (interactiveMode) {
            out.println("testRenounceForMixedMode() - EXIT");
            out.println("<br>");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void defaultAction()
    //------------------------------------------------------------
    {}

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void resetCache()
    //------------------------------------------------------------
    {
        distributedMap.clear();

        /*
         * for (int mapIndex=0; mapIndex!=maxInstances; mapIndex++ ) {
         * distributedMaps[mapIndex].clear();
         * }
         */
    }

    //------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    static String getStackTrace(Throwable e)
    //-------------------------------------------------------------------------------------
    {
        if (null == e) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    //-------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    public static String getShareType(int shareType)
    //-------------------------------------------------------------------------------------
    {
        String s = "unknown";
        switch (shareType) {
            case EntryInfo.NOT_SHARED: {
                s = "NONE";
                break;
            }
            case EntryInfo.SHARED_PUSH: {
                s = "PUSH";
                break;
            }
            case EntryInfo.SHARED_PUSH_PULL: {
                s = "PUSH-PULL";
                break;
            }
            case EntryInfo.SHARED_PULL: {
                s = "PULL";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    public static String getMapType(int mapType)
    //-------------------------------------------------------------------------------------
    {
        String s = "unknown";
        switch (mapType) {
            case TYPE_BASE_CACHE: {
                s = "BaseCache";
                break;
            }
            case TYPE_DMap_1: {
                s = "DMap_1";
                break;
            }
            case TYPE_DMap_2: {
                s = "DMap_2";
                break;
            }
            case TYPE_DMap_L1: {
                s = "DMap_L1";
                break;
            }
            case TYPE_DMap_N1: {
                s = "DMap_N1";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    //--------------Tamera's Code---------------------------------

    public void clearCache() {

        com.ibm.websphere.cache.Cache c = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();

        //out.println("<br>In clearCache() before clear:  Number of cache entries = "+c.getNumberCacheEntries());

        c.clear();

        try {
            out = resp.getWriter();
        } catch (Exception e) {
            out.println("<br> Caught exception! e= " + e);
        }
        out.println("<br>Numentries =>" + c.getNumberCacheEntries() + "<=");

    }

    public void numCacheEntries() {

        com.ibm.websphere.cache.Cache c = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();

        try {
            out = resp.getWriter();
        } catch (Exception e) {
            out.println("<br> Caught exception! e= " + e);
        }

        out.println("Used Entries =>" + c.getNumberCacheEntries() + "<=");

        //return c.getNumberCacheEntries();
    }

    public void getCacheHits() {
        try {
            out = resp.getWriter();
        } catch (Exception e) {
            out.println("<br> Caught exception! e= " + e);
        }
        DCache c = (DCache) com.ibm.websphere.cache.DynamicCacheAccessor.getCache();
        out.println("Entry hits =>" + c.getCacheStatistics().getCacheHitsCount() + "<=");
        out.println("Value hits =>" + c.getCacheStatistics().getCacheHitsCount() + "<=");
    }

    public void getAllCacheIDs() {
        com.ibm.websphere.cache.Cache c = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();
        StringBuffer sb = new StringBuffer();
        for (Enumeration e = c.getAllIds(); e.hasMoreElements();) {
            sb.append((String) e.nextElement() + ",");
        }
        try {
            out = resp.getWriter();
        } catch (Exception e) {
            out.println("<br> Caught exception! e= " + e);
        }
        out.println("All Cache IDs =>" + sb.toString() + "<=");
    }

    public void getCacheEntry() {
        com.ibm.websphere.cache.Cache c = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();
        //StringBuffer sb = new StringBuffer();
        int success = 0;
        StringTokenizer st = new StringTokenizer(ids, ",");
        String[] cacheids = new String[c.getNumberCacheEntries()];
        int n = 0;
        while (st.hasMoreTokens() && n < c.getNumberCacheEntries()) {
            String temp = st.nextToken();
            temp = temp.trim();
            //System.out.println("temp="+temp);
            cacheids[n] = new String(temp);
            n++;
        }//end of while
        for (int i = 0; i < cacheids.length; i++) {
            com.ibm.websphere.cache.CacheEntry ce = c.getEntry(cacheids[0]);
            if (ce != null)
                success++;
        }
        try {
            out = resp.getWriter();
        } catch (Exception e) {
            out.println("<br> Caught exception! e= " + e);
        }
        //out.println("All Cache IDs =>"+sb.toString()+"<=");
        out.println("success =>" + success + "<=");
    }

    public void setShareType() {

    }

    public void setNonSerAttribute() {
        out.println("<br> in setNonSerAttribute");
        /*
         * try{
         * req.setAttribute("nonSerObject", new Object());
         * 
         * Hashtable ht = new Hashtable();
         * ht.put("one", new String("one"));
         * ht.put("two", new Object());
         * ht.put("three", new String("three"));
         * 
         * req.setAttribute("myHashtable", ht);
         * req.setAttribute("myString", "Tamera");
         * 
         * getServletContext().getRequestDispatcher("/STMTestServlet").forward(req,resp);
         * 
         * //com.ibm.websphere.cache.Cache c = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();
         * 
         * //System.out.println("<br>testServletPUSHDRSSerializable() after forward:  Number of cache entries = "+c.getNumberCacheEntries());
         * }
         * catch(Exception e){
         * System.out.println("<br> Caught exception! e= "+e);
         * }
         */
        Object o = null;
        Hashtable ht = null;
        String myString = null;

        o = req.getAttribute("nonSerObject");
        ht = (Hashtable) req.getAttribute("myHashtable");
        myString = (String) req.getAttribute("myString");
        out.println("<br>mystring = " + myString);

        //if(o!=null && ht !=null){
        out.println("<br> Object = " + o);
        out.println("<br> Hashtable = " + ht);

        //}

    }

    //--------------End of Tamera's Code---------------------------

    //-------------------------------------------------------------------------------------
    static public String getChangeCause(int changeCause)
    //------------------------------------------------------------------------------------- 
    {
        String s = "unknown:" + changeCause;
        switch (changeCause) {
            case ChangeEvent.EXISTING_VALUE_CHANGED: {
                s = "EXISTING_VALUE_CHANGED";
                break;
            }
            case ChangeEvent.NEW_ENTRY_ADDED: {
                s = "NEW_ENTRY_ADDED";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    // CPF_NEW
    //-------------------------------------------------------------------------------------
    public static String getChangeSource(int changeSource)
    //------------------------------------------------------------------------------------- 
    {
        String s = "unknown:" + changeSource;
        switch (changeSource) {
            case ChangeEvent.LOCAL: {
                s = "LOCAL";
                break;
            }
            case ChangeEvent.REMOTE: {
                s = "REMOTE";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    // CPF_NEW Change Listener
    //------------------------------------------------------------
    @Override
    public void cacheEntryChanged(com.ibm.websphere.cache.ChangeEvent ce)
    //------------------------------------------------------------
    {
        changeEvents.put(ce.getId(), ce);

    }

    //------------------------------------------------------------

    boolean runNow;
    long lockFailure;
    long lockSuccess;

    /*
     * class LockUnlockTest_1 {
     * 
     * void start()
     * {
     * Thread thread = new Thread() {
     * 
     * public void run()
     * {
     * try {
     * DMapRequest dmaprequest = new DMapRequest();
     * DMapResponse dmapresponse = new DMapResponse();
     * while (runNow) {
     * for (int i = 0; runNow && i != loopCount; i++) {
     * String s = "test:" + i;
     * Object obj = null;
     * int j = 0;
     * do {
     * obj = distributedMap.getAndLock(s, dmaprequest, dmapresponse);
     * if (dmapresponse.isErrorCurrentlyLocked()) {
     * lockFailure++;
     * try {
     * Thread.sleep(5L);
     * } catch (Exception exception1) {
     * }
     * }
     * j++;
     * lockSuccess++;
     * if (j > 400) {
     * j = 0;
     * System.out.println("** Unable to lock: " + s);
     * }
     * } while (runNow && dmapresponse.isErrorCurrentlyLocked());
     * if (runNow && obj == null) {
     * runNow = false;
     * throw new Exception("testDistributedMapLock() - error - missing cache entry" + dmapresponse);
     * }
     * distributedMap.putAndUnlock(s, obj, dmaprequest, dmapresponse);
     * if (runNow && dmapresponse.isError()) {
     * runNow = false;
     * throw new Exception("testDistributedMapLock() - error - " + dmapresponse);
     * }
     * }
     * 
     * }
     * } catch (Exception exception) {
     * exception.printStackTrace();
     * }
     * }
     * 
     * };
     * thread.setDaemon(true);
     * thread.start();
     * }
     * 
     * LockUnlockTest_1()
     * {
     * }
     * }
     */

    //-------------------------------------------------------------------------------------
    public static String getInvalidationCause(int invalidationCause)
    //------------------------------------------------------------------------------------- 
    {
        String s = "unknown:" + invalidationCause;
        switch (invalidationCause) {
            case InvalidationEvent.CLEAR_ALL: {
                s = "CLEAR_ALL";
                break;
            }
            case InvalidationEvent.TIMEOUT: {
                s = "TIMEOUT";
                break;
            }
            case InvalidationEvent.EXPLICIT: {
                s = "EXPLICIT";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    public static String getInvalidationSource(int invalidationSource)
    //------------------------------------------------------------------------------------- 
    {
        String s = "unknown:" + invalidationSource;
        switch (invalidationSource) {
            case InvalidationEvent.LOCAL: {
                s = "LOCAL";
                break;
            }
            case InvalidationEvent.REMOTE: {
                s = "REMOTE";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    // InvalidationListener
    //------------------------------------------------------------
    @Override
    public void fireEvent(com.ibm.websphere.cache.InvalidationEvent ie)
    //------------------------------------------------------------
    {
        //InvalidationListenerInfo info = new InvalidationListenerInfo(ie.getId(), ie.getCauseOfInvalidation(), ie.getSourceOfInvalidation());
        //_ie.add(info);

        invalidationEvents.put(ie.getId(), ie);

    }

    //------------------------------------------------------------

    Hashtable servletMaps = new Hashtable();

    private DistributedObjectCache getServletWrapper(String cacheName) throws javax.naming.NamingException {

        // v6 does not allow JNDI lookup of servlet cache instances
        // Context context = new InitialContext();
        // return (DistributedObjectCache)context.lookup(cacheName);

        DistributedObjectCache map = (DistributedObjectCache) servletMaps.get(cacheName);
        if (map == null) {
            DCache cache = ServerCache.getConfiguredCache(cacheName);
            if (cache == null) {
                throw new IllegalStateException("Cache instance not configured. cacheName=" + cacheName);
            }
            map = new DistributedMapImpl(cache);
            servletMaps.put(cacheName, map);
        }
        return map;
    }

    public void testServletCacheInstance_Clear() throws javax.naming.NamingException {

        final String methodName = "testServletCacheInstance_Clear()";
        if (interactiveMode) {
            out.println(methodName + " ENTRY");
            out.println("<br>");
        }

        DistributedObjectCache map = getServletWrapper(cacheName);

        map.clear();

        if (interactiveMode) {
            out.println(methodName + " EXIT");
            out.println("<br>");
        }
    }

    public void testServletCacheInstance_Verify() throws javax.naming.NamingException {

        final String methodName = "testServletCacheInstance_Verify()";
        if (interactiveMode) {
            out.println(methodName + " ENTRY");
            out.println("<br>");
        }

        DistributedObjectCache map = getServletWrapper(cacheName);

        Object o = map.get(cacheId);
        html = "";
        if (o instanceof FragmentComposerMemento) {
            try {
                ((FragmentComposerMemento) o).viewContents(request, response);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (interactiveMode) {
            out.println(methodName + " EXIT");
            out.println("<br>");
        }
    }

    public void testJspUpdateLastModified() throws javax.naming.NamingException {

        final String methodName = "testJspUpdateLastModified()";
        if (interactiveMode) {
            out.println(methodName + " ENTRY");
            out.println("<br>");
        }

        String fileNameJsp = "JspRecompileTest.jsp";
        ServletContext context = this.getServletContext();
        File rootServlet = new File(context.getRealPath("/"));
        File rootWebInf = new File(rootServlet, "WEB-INF");
        File fileJsp = new File(rootServlet, fileNameJsp);

        if (!fileJsp.exists()) {
            throw new IllegalStateException("File not found. JspFile=" + fileJsp.getAbsolutePath());
        }

        fileJsp.setLastModified(System.currentTimeMillis());

        if (interactiveMode) {
            out.println(methodName + " EXIT");
            out.println("<br>");
        }
    }

}
//-------------------------------------------------------------------------------------

