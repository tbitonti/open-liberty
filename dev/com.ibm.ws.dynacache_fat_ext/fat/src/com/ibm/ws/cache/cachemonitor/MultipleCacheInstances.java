// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.cachemonitor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class MultipleCacheInstances extends CacheMonitorTest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("MultipleCacheInstances", "cachemonitor");

    @Test
    public void testMultipleCacheInstances() throws Exception {
        WebRequest req = null;
        WebResponse resp = null;
        WebForm webForm = null;
        WebResponse frame = null;
        String instances[] = null;
        ArrayList instancesList = null;
        WebImage warning = null;
        WebImage error = null;
        WebLink link = null;
        WebTable statistics = null;
        WebTable contents = null;

        String usedEntries = null;
        String cacheHits = null;
        String cacheMisses = null;
        String explicitRemovals = null;

        WebConversation wc = startNewConversation();
        System.out.println("MultipleCacheInstances Test");

        System.out.println("********* Verify that all web instances (active, inactive and unconfigured) can be seen through the cache monitor *********");
        //Make instance2 an active instance
        resp = getWebResponse(wc, "/dynacachetests/TimeStampCM2?arg1=1000");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        req = new GetMethodWebRequest(url + "/selectInstance.jsp");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        webForm = resp.getFormWithName("selectInstance");
        instances = webForm.getOptionValues("instance");
        instancesList = new ArrayList();

        for (int i = 0; i < instances.length; i++) {
            instancesList.add(instances[i]);
        }

        assertTrue(msg("Active instance (services/cache/servletInstance_2) not displayed in CacheInstance Selection", ""),
                   instancesList.contains("services/cache/servletInstance_2"));
        assertTrue(msg("Inactive instance (services/cache/servletInstance_3)not displayed in CacheInstance Selection", ""),
                   instancesList.contains("services/cache/servletInstance_3"));
        assertTrue(msg("Unconfigured instance (services/cache/servletInstance_CM) not displayed in CacheInstance Selection", ""),
                   instancesList.contains("services/cache/servletInstance_CM"));

        System.out.println("********* Verify that cache monitor shows an error when trying to view statistics, contents or policies for an unconfigured instance *********");

        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services%2Fcache%2FservletInstance_CM");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        frame = getInstanceStatisticsFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance Statistics page did not display a error", ""), error);

        frame = getInstanceContentsFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance Contents page did not display a error", ""), error);

        frame = getInstanceDependencyIdsFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance DependencyIds page did not display a error", ""), error);

        frame = getDiskTemplatesFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance Disk Templates page did not display a error", ""), error);

        frame = getDiskContentsFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance Disk Contents page did not display a error", ""), error);

        frame = getDiskDependencyIdsFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance Disk DependencyIds page did not display a error", ""), error);

        frame = getInstancePoliciesFrame(wc);
        error = frame.getImageWithAltText("Error");
        assertNotNull(msg("Unconfigured instance Cache Policies page did not display a error", ""), error);
        /*
         * System.out.println("********* Verify that cache monitor shows a warning when trying to view statistics, contents or policies for an unconfigured instance *********");
         * 
         * req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services%2Fcache%2FservletInstance_3");
         * resp = wc.getResponse(req);
         * assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
         * 
         * frame = getInstanceStatisticsFrame(wc);
         * WebImage wi[] = frame.getImages();
         * for ( int i = 0; i<wi.length; i++)
         * System.out.println(wi[i].getAltText());
         * warning = frame.getImageWithAltText("\'Warning\'");
         * assertNotNull(msg("Unconfigured instance Statistics page did not display a warning", ""), warning);
         * 
         * frame = getInstanceContentsFrame(wc);
         * warning = frame.getImageWithAltText("Warning");
         * assertNotNull(msg("Unconfigured instance Contents page did not display a warning", ""), warning);
         * 
         * frame = getInstanceDependencyIdsFrame(wc);
         * warning = frame.getImageWithAltText("Warning");
         * assertNotNull(msg("Unconfigured instance DependencyIds page did not display a warning", ""), warning);
         * 
         * frame = getDiskTemplatesFrame(wc);
         * warning = frame.getImageWithAltText("Warning");
         * assertNotNull(msg("Unconfigured instance Disk Templates page did not display a warning", ""), warning);
         * 
         * frame = getDiskContentsFrame(wc);
         * warning = frame.getImageWithAltText("Warning");
         * assertNotNull(msg("Unconfigured instance Disk Contents page did not display a warning", ""), warning);
         * 
         * frame = getDiskDependencyIdsFrame(wc);
         * warning = frame.getImageWithAltText("Warning");
         * assertNotNull(msg("Unconfigured instance Disk DependencyIds page did not display a waring", ""), warning);
         */

        System.out.println("********* Verify that contents and statistics can be seen for individual web instances *********");

        clearAndResetCacheAllInstances(wc);

        resp = getWebResponse(wc, "/dynacachetests/TimeStampCM2?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        for (int i = 1; i <= 2; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStampCM3?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        for (int i = 1; i <= 3; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        //hit the same entries to again to get cache hits
        resp = getWebResponse(wc, "/dynacachetests/TimeStampCM2?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        for (int i = 1; i <= 2; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStampCM3?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        for (int i = 1; i <= 3; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        //set instance to services/cache/servletInstance_2
        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services/cache/servletInstance_2");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getInstanceStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not correct for services/cache/servletInstance_2", ""), "1", usedEntries);

        cacheHits = statistics.getCellAsText(3, 1);
        assertEquals(msg("Cache Hits was not correct for services/cache/servletInstance_2", ""), "1", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not correct for services/cache/servletInstance_2", ""), "1", cacheMisses);

        resp = getInstanceContentsFrame(wc);

        link = resp.getLinkWithID("/dynacachetests/TimeStampCM2:arg1=1:requestType=GET");
        assertNotNull(msg("Cache Entry for /dynacachetests/TimeStampCM2:arg1=1 is missing from services/cache/servletInstance_2 contents", ""), link);

        //set instance to services/cache/servletInstance_3
        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services/cache/servletInstance_3");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getInstanceStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not correct for services/cache/servletInstance_3", ""), "2", usedEntries);

        //cacheHits will fail because ObjectGrid does not provide getEntry() function to ignore counting.
        //cacheHits = statistics.getCellAsText(3, 1);
        //assertEquals(msg("Cache Hits was not correct for services/cache/servletInstance_3", ""), "2", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not correct for services/cache/servletInstance_3", ""), "2", cacheMisses);

        resp = getInstanceContentsFrame(wc);

        link = resp.getLinkWithID("/dynacachetests/TimeStampCM3:arg1=1:requestType=GET");
        assertNotNull(msg("Cache Entry for /dynacachetests/TimeStampCM3:arg1=1 is missing from services/cache/servletInstance_3 contents", ""), link);

        link = resp.getLinkWithID("/dynacachetests/TimeStampCM3:arg1=2:requestType=GET");
        assertNotNull(msg("Cache Entry for /dynacachetests/TimeStampCM3:arg1=2 is missing from services/cache/servletInstance_3 contents", ""), link);

        //set instance to baseCache
        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=baseCache");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getInstanceStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not correct for baseCache", ""), "3", usedEntries);

        cacheHits = statistics.getCellAsText(3, 1);
        assertEquals(msg("Cache Hits was not correct for baseCache", ""), "3", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not correct for baseCache", ""), "3", cacheMisses);

        resp = getInstanceContentsFrame(wc);

        link = resp.getLinkWithID("/dynacachetests/TimeStamp:arg1=1:requestType=GET");
        assertNotNull(msg("Cache Entry for /dynacachetests/TimeStamp:arg1=1 is missing from baseCache contents", ""), link);

        link = resp.getLinkWithID("/dynacachetests/TimeStamp:arg1=2:requestType=GET");
        assertNotNull(msg("Cache Entry for /dynacachetests/TimeStamp:arg1=2 is missing from baseCache contents", ""), link);

        link = resp.getLinkWithID("/dynacachetests/TimeStamp:arg1=3:requestType=GET");
        assertNotNull(msg("Cache Entry for /dynacachetests/TimeStamp:arg1=3 is missing from baseCache contents", ""), link);

        System.out.println("********* Verify that cache can be cleared and statistics can be reset for individual instances. *********");

        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services/cache/servletInstance_2");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        frame = getInstanceStatisticsFrame(wc);
        webForm = frame.getFormWithName("clearForm");
        if (webForm != null) {
            resp = webForm.submit();
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        statistics = getInstanceStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not updated by Clear Cache", ""), "0", usedEntries);

        explicitRemovals = statistics.getCellAsText(6, 1);
        assertEquals(msg("Explicit Removals was not updated by Clear Cache", ""), "1", explicitRemovals);

        frame = getInstanceStatisticsFrame(wc);
        webForm = frame.getFormWithName("resetForm");
        if (webForm != null) {
            resp = webForm.submit();
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        statistics = getInstanceStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not reset", ""), "0", usedEntries);

        cacheHits = statistics.getCellAsText(3, 1);
        assertEquals(msg("Cache Hits was not reset", ""), "0", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not reset", ""), "0", cacheMisses);

        explicitRemovals = statistics.getCellAsText(6, 1);
        assertEquals(msg("Explicit Removals was not reset", ""), "0", explicitRemovals);

        //Check that other cache instance were not reset
        //set instance to services/cache/servletInstance_3
        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services/cache/servletInstance_3");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getInstanceStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not correct for services/cache/servletInstance_3", ""), "2", usedEntries);

        cacheHits = statistics.getCellAsText(3, 1);
        //assertEquals(msg("Cache Hits was not correct for services/cache/servletInstance_3", ""), "2", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not correct for services/cache/servletInstance_3", ""), "2", cacheMisses);

        System.out.println("********* Verify that policies can be seen for individual web instances *********");

        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services/cache/servletInstance_2");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        frame = getInstancePoliciesFrame(wc);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        link = frame.getLinkWithID("/dynacachetests/TimeStampCM2");
        assertNotNull(msg("/dynacachetests/TimeStampCM2 policy does not exist for services/cache/servletInstance_2", ""), link);

        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=services/cache/servletInstance_3");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        frame = getInstancePoliciesFrame(wc);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        link = frame.getLinkWithID("/dynacachetests/TimeStampCM3");
        assertNotNull(msg("/dynacachetests/TimeStampCM3 policy does not exist for services/cache/servletInstance_3", ""), link);

    }

}