// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.cachemonitor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class CacheContents extends CacheMonitorTest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("CacheContents", "cachemonitor");

    @Test
    public void testCacheContents() throws Exception {

        WebResponse resp = null;
        WebResponse frame = null;
        WebTable contents = null;
        WebTable table = null;
        WebLink link = null;
        WebForm webForm = null;

        String template = null;
        String cacheId = null;
        String timeout = null;
        String dependencyId = null;
        String replication = null;
        String priority = null;

        String ts_1 = null;
        String ts_2 = null;
        String ts1_1 = null;
        String ts1_2 = null;

        String cm_1 = null;
        String cm_2 = null;
        String cm1_1 = null;
        String cm1_2 = null;

        WebConversation wc = startNewConversation();
        System.out.println("CacheContents Test");

        clearAndResetCache(wc);

        System.out.println("********* Verify Cached Content Table are displayed correctly *********");

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp");
        ts_1 = table.getCellAsText(0, 0);

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp");
        ts_2 = table.getCellAsText(0, 0);

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp1?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp1");
        ts1_1 = table.getCellAsText(0, 0);

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp1?arg1=2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp1");
        ts1_1 = table.getCellAsText(0, 0);

        contents = getContentsTable(wc);

        for (int i = 1; i <= 2; i++) {
            template = contents.getCellAsText(i, 0);
            assertEquals(msg("Template Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp1", template.trim());
            cacheId = contents.getCellAsText(i, 1);
            assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp1:arg1=" + i + ":requestType=GET", cacheId.trim());
            timeout = contents.getCellAsText(i, 2);
            assertEquals(msg("Timeout Field Displays Incorrectly", ""), "0", timeout.trim());
            dependencyId = contents.getCellAsText(i, 3);
            assertEquals(msg("Dependency IDs Field Displays Incorrectly", ""), "timestamp:" + i, dependencyId.trim());
            replication = contents.getCellAsText(i, 4);
            assertEquals(msg("Replication Field Displays Incorrectly", ""), "None", replication.trim());
            priority = contents.getCellAsText(i, 5);
            assertEquals(msg("Priority Field Displays Incorrectly", ""), "1", priority.trim());
        }

        System.out.println("********* Verify Cached Entry is displayed correctly  *********");

        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp:arg1=1:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        table = frame.getTableWithID("TimeStamp");
        cm_1 = table.getCellAsText(0, 0);
        assertEquals(msg("CachedMonitor value does not match CachedValue", ""), ts_1, cm_1.trim());

        System.out.println("********* Verify Cached Entry is invalidated correctly  *********");

        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp:arg1=1:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        webForm = frame.getFormWithName("invalidateForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        contents = getContentsTable(wc);

        cacheId = contents.getCellAsText(3, 1);
        assertEquals(msg("Invalidated Entry still appears in contents", ""), "/dynacachetests/TimeStamp:arg1=2:requestType=GET", cacheId.trim());

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp");
        ts_1 = table.getCellAsText(0, 0);
        assertFalse(msg("Invalidated Entry was served from Cache", ""), ts_1.equals(cm_1));

        System.out.println("********* Verify DependencyIds are displayed and invalidated correctly  *********");
        // Sleeping for 5 seconds. This is required for solaris runs.
        Thread.sleep(5000);
        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("timestamp:2");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        contents = frame.getTableWithID("content");

        cacheId = contents.getCellAsText(1, 1).trim();

        String[] cacheIds = { "/dynacachetests/TimeStamp:arg1=2:requestType=GET", "/dynacachetests/TimeStamp1:arg1=2:requestType=GET" };
        boolean found = false;
        for (int i = 0; i < cacheIds.length; i++) {
            if (cacheIds[i].equals(cacheId))
                found = true;
        }
        if (!found)
            System.out.println("Cache ID Field Displays Incorrectly " + cacheId);

        //assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp:arg1=2:requestType=GET", cacheId);

        cacheId = contents.getCellAsText(2, 1).trim();
        found = false;
        for (int i = 0; i < cacheIds.length; i++) {
            if (cacheIds[i].equals(cacheId))
                found = true;
        }
        if (!found)
            System.out.println("Cache ID Field Displays Incorrectly " + cacheId);

        //assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp1:arg1=2:requestType=GET", cacheId);

        //store the current timestamps in cachemonitor, invalidate the entries
        //then make the requests again and verify the timestamps are different
        link = frame.getLinkWithID("/dynacachetests/TimeStamp:arg1=2:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        table = frame.getTableWithID("TimeStamp");
        cm_2 = table.getCellAsText(0, 0);

        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("timestamp:2");
        frame = link.click();

        webForm = frame.getFormWithName("invalidateForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        contents = getContentsTable(wc);
        cacheId = contents.getCellAsText(2, 1);
        assertEquals(msg("Entry Invalidated by dependeny id still appears in contents", ""), "/dynacachetests/TimeStamp:arg1=1:requestType=GET", cacheId.trim());

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp");
        ts_2 = table.getCellAsText(0, 0).trim();

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp1?arg1=2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp1");
        ts1_2 = table.getCellAsText(0, 0).trim();

        assertFalse(msg("Entry Invalidated by dependeny id was served from Cache", ""), ts_2.equals(cm_2));

        System.out.println("********* Verify Templates are displayed and invalidated correctly  *********");

        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp1");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), frame.getResponseCode(), 200);

        //contents = frame.getTableWithID("content");
        contents = getContentsTable(wc);

        cacheId = contents.getCellAsText(1, 1).trim();
        assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp1:arg1=1:requestType=GET", cacheId);

        cacheId = contents.getCellAsText(2, 1).trim();
        assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp1:arg1=2:requestType=GET", cacheId);

        //store the current timestamps in cachemonitor, invalidate the entries
        //then make the requests again and verify the timestamps are different

        link = frame.getLinkWithID("/dynacachetests/TimeStamp1:arg1=1:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), frame.getResponseCode(), 200);

        table = frame.getTableWithID("TimeStamp1");
        cm_1 = table.getCellAsText(0, 0).trim();

        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp1");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), frame.getResponseCode(), 200);

        webForm = frame.getFormWithName("invalidateForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        contents = getContentsTable(wc);
        cacheId = contents.getCellAsText(1, 1).trim();
        assertEquals(msg("Entry Invalidated by template still appears in contents", ""), "/dynacachetests/TimeStamp:arg1=1:requestType=GET", cacheId);

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp1?arg1=1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp1");
        ts_1 = table.getCellAsText(0, 0).trim();

        resp = getWebResponse(wc, "/dynacachetests/TimeStamp1?arg1=2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        table = resp.getTableWithID("TimeStamp1");
        ts_2 = table.getCellAsText(0, 0).trim();

        assertFalse(msg("Entry Invalidated by template was served from Cache", ""), ts_1.equals(cm_1));

        System.out.println("********* Verify Refresh Button *********");

        clearAndResetCache(wc);

        for (int i = 0; i <= 1999; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        frame = getContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp:arg1=0:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        webForm = frame.getFormWithName("refresh");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        for (int i = 2000; i <= 3998; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        contents = getContentsTable(wc);
        cacheId = contents.getCellAsText(1, 1).trim();
        assertEquals(msg("Refreshed Entry was Invalidated", ""), "/dynacachetests/TimeStamp:arg1=0:requestType=GET", cacheId);

    }
}
