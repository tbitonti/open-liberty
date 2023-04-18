// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.cachemonitor;

import static junit.framework.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class CacheStatistics extends CacheMonitorTest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("CacheStatistics", "cachemonitor");

    @Test
    public void testCacheStatistics() throws Exception {

        WebResponse resp = null;
        WebResponse frame = null;
        WebTable statistics = null;
        WebForm webForm = null;
        String cacheSize = null;
        String usedEntries = null;
        String cacheHits = null;
        String cacheMisses = null;
        String LRUEvictions = null;
        String explicitRemovals = null;
        String defaultPriority = null;
        String servletCachingEnabled = null;
        String diskOffloadEnabled = null;

        WebConversation wc = startNewConversation();
        System.out.println("CacheStatistics Test");

        clearAndResetCache(wc);

        System.out.println("********* Verify Servlet Caching Enabled and Disk Offload Disabled *********");

        statistics = getStatisticsTable(wc);

        servletCachingEnabled = statistics.getCellAsText(8, 1);
        assertEquals(msg("Servlet Caching Enabled value was incorrect", statistics.getText()), "Yes", servletCachingEnabled);

        diskOffloadEnabled = statistics.getCellAsText(9, 1);
        assertEquals(msg("Disk Offload Enabled value was incorrect", statistics.getText()), "No", diskOffloadEnabled);

        System.out.println("********* Verify CacheSize and Default Priority *********");

        statistics = getStatisticsTable(wc);

        cacheSize = statistics.getCellAsText(1, 1);
        assertEquals(msg("Cache Size was incorrect", statistics.getText()), "2000", cacheSize);

        defaultPriority = statistics.getCellAsText(7, 1);
        assertEquals(msg("Default priority is incorrect", statistics.getText()), "1", defaultPriority);

        System.out.println("********* Verify Used Entries and Cache Misses are incremented  *********");

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not incremented by 1", statistics.getText()), "1", usedEntries);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not incremented by 1", statistics.getText()), "1", cacheMisses);

        System.out.println("********* Verify Cache Hits is incremented *********");

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getStatisticsTable(wc);

        cacheHits = statistics.getCellAsText(3, 1);
        assertEquals(msg("Cache Hits was not incremented", statistics.getText()), "1", cacheHits);

        System.out.println("********* Verify counters after uncacheable request *********");

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was incremented", statistics.getText()), "1", usedEntries);

        cacheHits = statistics.getCellAsText(3, 1);
        assertEquals(msg("Cache Hits was incremented", statistics.getText()), "1", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was incremented", statistics.getText()), "1", cacheMisses);

        LRUEvictions = statistics.getCellAsText(5, 1);
        assertEquals(msg("LRU Evictions was incremented", statistics.getText()), "0", LRUEvictions);

        explicitRemovals = statistics.getCellAsText(6, 1);
        assertEquals(msg("Explicit Removals was incremented", statistics.getText()), "0", explicitRemovals);

        System.out.println("********* Verify LRU Evictions  *********");

        for (int i = 2; i <= 2001; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        sleep(200);
        statistics = getStatisticsTable(wc);

        LRUEvictions = statistics.getCellAsText(5, 1);
        assertEquals(msg("LRU Evictions was not incremented", statistics.getText()), "1", LRUEvictions);

        System.out.println("********* Verify Explicit Removals  *********");

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?inv=2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        statistics = getStatisticsTable(wc);
        explicitRemovals = statistics.getCellAsText(6, 1);
        assertEquals(msg("Explicit Removals was not incremented", statistics.getText()), "1", explicitRemovals);

        System.out.println("********* Verify Clear Cache  *********");
        clearCache(wc);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        statistics = getStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not reset", statistics.getText()), "0", usedEntries);

        explicitRemovals = statistics.getCellAsText(6, 1);
        assertEquals(msg("Explicit Removals value was incorrect", statistics.getText()), "2000", explicitRemovals);

        System.out.println("********* Verify Reset Statistics  *********");
        frame = getDetailsFrame(wc);
        webForm = frame.getFormWithName("resetForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        statistics = getStatisticsTable(wc);

        usedEntries = statistics.getCellAsText(2, 1);
        assertEquals(msg("Used Entries was not reset", statistics.getText()), "0", usedEntries);

        cacheHits = statistics.getCellAsText(3, 1);
        assertEquals(msg("Cache Hits was not reset", statistics.getText()), "0", cacheHits);

        cacheMisses = statistics.getCellAsText(4, 1);
        assertEquals(msg("Cache Misses was not reset", statistics.getText()), "0", cacheMisses);

        LRUEvictions = statistics.getCellAsText(5, 1);
        assertEquals(msg("LRU Evictions was not reset", statistics.getText()), "0", LRUEvictions);

        explicitRemovals = statistics.getCellAsText(6, 1);
        assertEquals(msg("Explicit Removals was not reset", statistics.getText()), "0", explicitRemovals);

    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}