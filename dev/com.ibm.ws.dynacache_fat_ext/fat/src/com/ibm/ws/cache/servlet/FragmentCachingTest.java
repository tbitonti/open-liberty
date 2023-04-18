package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.TestConfig;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class FragmentCachingTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("FragmentCachingTest");

    WebConversation wc = startNewConversation();
    WebResponse resp = null;

    String cacheMiss = null;
    String cacheHit = null;
    String child1TimeStamp = null;
    String parent1TimeStamp = null;
    String grandParentTimeStamp = null;

    int clearCacheDelay = 5000; //If testing on a slow machine change to 5000
    int invalidationDelay = 1000; //If testing on a slow machine change to 1000
//
//    @Test
//    public void testDNC1() throws Exception {
//        System.out.println("DNC Test1: DNCFS=CSF and DCNChild1=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test1";
//        clearCache();
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getIHSWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test1");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test1");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//    }
//
//    @Test
//    public void testDNC2() throws Exception {
//        System.out.println("DNC Test2: DNCFS=CSF, DNCGP=CSF, DNCP1=CSF and DCNChild1=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test2";
//        clearCache();
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getIHSWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test2");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test2");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed by servlet", ""),
//                   !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test2");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test2");
//
//        resp = getIHSWebResponse(wc, "/dynacachetests/DNCGrandParent.jsp?test=test2");
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed grandparent", ""),
//                   !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test2");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test2");
//
//        resp = getIHSWebResponse(wc, "/dynacachetests/DNCParent1.jsp?test=test2");
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed by parent", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//
//    }
//
//    @Test
//    public void testDNC3() throws Exception {
//        System.out.println("DNC Test3: DNCFS=CSF, DNCGP=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test3";
//        clearCache();
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getIHSWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCGrandParent.jsp?inv3=test3");
//        fireInvalidation(wc, "/dynacachetests/DNCGrandParent.jsp?inv3=test3");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("GrandParent Timestamps match. GrandParent fragment was consumed by servlet", ""),
//                   !grandParentTimeStamp.equals(resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not a Cache Hit", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("Child Timestamps do not match. Child fragment was not a Cache Hit", ""), resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0), child1TimeStamp);
//
//    }
//
//    @Test
//    public void testDNC4() throws Exception {
//        System.out.println("DNC Test4: DNCFS=CSF, DNCGP=NC, DNCP1=NC DNCC1=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test4";
//        clearCache();
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getIHSWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test4");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test4");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent was not consumed by servlet", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//        assertEquals(msg("Parent Timestamps do not match. Parent was not consumed by servlet", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//    }
//
//    @Test
//    public void testDNC5() throws Exception {
//        System.out.println("DNC Test5: DNCFS=CSF/DNC, DNCGP=CSF/DNC, DNCP1=CSF/DNC DNCC1=CSF/DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test5";
//        clearCache();
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getIHSWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test5");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test5");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent was not consumed by servlet", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//        assertEquals(msg("Parent Timestamps do not match. Parent was not consumed by servlet", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCParent1.jsp?inv2=test5");
//        fireInvalidation(wc, "/dynacachetests/DNCParent1.jsp?inv2=test5");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertTrue(msg("Parent Timestamps match. Parent fragment was consumed", ""), !parent1TimeStamp.equals(resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0)));
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent was not consumed by servlet", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCGrandParent.jsp?inv3=test5");
//        fireInvalidation(wc, "/dynacachetests/DNCGrandParent.jsp?inv3=test5");
//
//        resp = getIHSWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertTrue(msg("Parent Timestamps match. Parent fragment was consumed", ""), !parent1TimeStamp.equals(resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0)));
//        assertTrue(msg("GrandParent Timestamps match. GrandParent fragment was consumed", ""),
//                   !grandParentTimeStamp.equals(resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0)));
//    }
//
//    @Test
//    public void testDNC6() throws Exception {
//        if (TestConfig.getIhsEnabled()) {
//            System.out.println("DNC Test6: DNCFS=CSF, DNCC1=EC/DNC");
//            String URI = "/dynacachetests/DNCForwardServlet?test=test6";
//            clearCache();
//
//            resp = getIHSWebResponse(wc, URI);
//            assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//            cacheMiss = resp.getText();
//
//            resp = getIHSWebResponse(wc, URI);
//            cacheHit = resp.getText();
//            assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//            assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//            grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//            parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//            child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//            //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test6");
//            fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test6");
//
//            resp = getIHSWebResponse(wc, URI);
//            assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//            assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//            assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                         grandParentTimeStamp);
//        }
//    }
//
//    @Test
//    public void testDNC6_AppServer() throws Exception {
//        System.out.println("DNC Test6_AppServer: DNCFS=CSF, DNCC1=EC/DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test6";
//        clearCache();
//
//        resp = getWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test6");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test6");
//
//        resp = getWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//
//    }
//
//    @Test
//    public void testDNC7() throws Exception {
//        if (TestConfig.getIhsEnabled()) {
//            System.out.println("DNC Test7: DNCFS=EC/CSF, DNCC1=DNC");
//            String URI = "/dynacachetests/DNCForwardServlet?test=test7";
//            clearCache();
//
//            resp = getIHSWebResponse(wc, URI);
//            assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//            cacheMiss = resp.getText();
//
//            resp = getIHSWebResponse(wc, URI);
//            cacheHit = resp.getText();
//            assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//            assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//            grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//            parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//            child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//            //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test7");    
//            fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test7");
//
//            resp = getWebResponse(wc, URI);
//            assertTrue(msg("Child Timestamps match. Child timeout did not invalidate servlet", ""),
//                       !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//            assertTrue(msg("GrandParent Timestamps match. Child timeout did not invalidate servlet", ""),
//                       !grandParentTimeStamp.equals(resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0)));
//            assertTrue(msg("Parent Timestamps match. Child timeout did not invalidate servlet", ""),
//                       !parent1TimeStamp.equals(resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0)));
//        }
//    }
//
//    @Test
//    public void testDNC7_AppServer() throws Exception {
//        System.out.println("DNC Test7_AppServer: DNCFS=EC, DNCC1=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test7";
//        clearCache();
//
//        resp = getWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getIHSWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test7");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test7");
//
//        resp = getWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//
//    }
//
//    @Test
//    public void testDNC_API1() throws Exception {
//        System.out.println("DNC_API Test1: DNCFS=CSF and DCNChild1=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test8";
//        clearCache();
//
//        Thread.sleep(1000);
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        Thread.sleep(1000);
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//    }
//
//    @Test
//    public void testDNC_API2() throws Exception {
//        if (TestConfig.getIhsEnabled()) {
//            System.out.println("DNC_API Test2: DNCFS=EC/CSF and DCNChild1=DNC");
//            String URI = "/dynacachetests/DNCForwardServlet?test=test9";
//            clearCache();
//
//            resp = getIHSWebResponse(wc, URI);
//            assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//            cacheMiss = resp.getText();
//
//            resp = getIHSWebResponse(wc, URI);
//            cacheHit = resp.getText();
//            assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//            assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        }
//
//    }
//
//    @Test
//    public void testDNC_API2_AppServer() throws Exception {
//        System.out.println("DNC_API Test2_AppServer: DNCFS=EC/CSF and DCNChild1=DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test9";
//        clearCache();
//
//        resp = getWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        // Pause to ensure timestamps do not match
//        try {
//            Thread.sleep(50);
//        } catch (Exception e) {
//        }
//
//        resp = getIHSWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//    }
//
//    @Test
//    public void testDNC_API3_AppServer() throws Exception {
//        System.out.println("DNC_API Test3_AppServer: DNCFS=CSF and DCNChild1=EC/DNC");
//        String URI = "/dynacachetests/DNCForwardServlet?test=test10";
//        clearCache();
//
//        resp = getWebResponse(wc, URI);
//        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
//        cacheMiss = resp.getText();
//
//        resp = getWebResponse(wc, URI);
//        cacheHit = resp.getText();
//        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
//        assertEquals(msg("CacheHit does't match CacheMiss", ""), cacheMiss, cacheHit);
//        grandParentTimeStamp = resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0);
//        parent1TimeStamp = resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0);
//        child1TimeStamp = resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0);
//
//        //resp = getWebResponse(wc,"/dynacachetests/DNCChild1.jsp?inv1=test10");
//        fireInvalidation(wc, "/dynacachetests/DNCChild1.jsp?inv1=test10");
//
//        resp = getWebResponse(wc, URI);
//        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("DNCChild1TimeStamp").getCellAsText(0, 0)));
//        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("DNCParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
//        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("DNCGrandParentBegin").getCellAsText(0, 0),
//                     grandParentTimeStamp);
//
//    }

    @Test
    public void testJSTL1() throws Exception {
        System.out.println("JSTL Test1: JSTLFS=CSF and JSTLChild1=DNC");
        String URI = "/dynacachetests/JSTLForwardServlet?test=test1";
        clearCache();

        resp = getWebResponse(wc, URI);
        assertEquals(msg("Incorrect number of fragments on CacheMiss", ""), 7, resp.getTables().length);
        cacheMiss = resp.getText();

        resp = getWebResponse(wc, URI);
        cacheHit = resp.getText();
        assertEquals(msg("Incorrect number of fragments on a CacheHit", ""), 7, resp.getTables().length);
        //assertEquals(msg("CacheHit does't match CacheMiss",""), cacheMiss, cacheHit);
        grandParentTimeStamp = resp.getTableWithID("JSTLGrandParentBegin").getCellAsText(0, 0);
        parent1TimeStamp = resp.getTableWithID("JSTLParent1Begin").getCellAsText(0, 0);
        child1TimeStamp = resp.getTableWithID("JSTLChild1TimeStamp").getCellAsText(0, 0);

        //resp = getIHSWebResponse(wc,"/dynacachetests/JSTLChild1.jsp?inv1=test1");
        fireInvalidation(wc, "/dynacachetests/JSTLChild1.jsp?inv1=test1");

        resp = getIHSWebResponse(wc, URI);
        assertTrue(msg("Child Timestamps match. Child fragment was consumed", ""), !child1TimeStamp.equals(resp.getTableWithID("JSTLChild1TimeStamp").getCellAsText(0, 0)));
        assertEquals(msg("Parent Timestamps do not match. Parent fragment was not consumed", ""), resp.getTableWithID("JSTLParent1Begin").getCellAsText(0, 0), parent1TimeStamp);
        assertEquals(msg("GrandParent Timestamps do not match. GrandParent fragment was not consumed", ""), resp.getTableWithID("JSTLGrandParentBegin").getCellAsText(0, 0),
                     grandParentTimeStamp);
    }

    @Override
    public WebResponse getWebResponse(WebConversation wc, String uri) throws Exception {

        WebRequest req = new GetMethodWebRequest(TestConfig.getBaseURL() + uri);
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebResponse getIHSWebResponse(WebConversation wc, String uri) throws Exception {
        if (TestConfig.getIhsEnabled()) {
            WebRequest req = new GetMethodWebRequest(TestConfig.getIhsURL() + uri);
            WebResponse resp = wc.getResponse(req);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            return resp;
        }
        else {
            return getWebResponse(wc, uri);
        }
    }

    @Override
    public void clearCache() throws Exception {
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, "/dynacachetests/clearcacheservlet");
        assertEquals("cache clear failed", resp.getResponseCode(), 200);

        try {
            Thread.sleep(clearCacheDelay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fireInvalidation(WebConversation wc, String uri) throws Exception {
        WebResponse resp = getWebResponse(wc, uri);
        assertEquals("invalidation failed", resp.getResponseCode(), 200);

        try {
            Thread.sleep(invalidationDelay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
