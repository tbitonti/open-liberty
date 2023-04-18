//IBM Confidential OCO Source Material
//5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business
// Machines Corp. 1997, 2002
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.
package com.ibm.ws.cache.cachemonitor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class DiskContents extends CacheMonitorTest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("DiskContents", "cachemonitor");

    @Test
    public void testDiskContents() throws Exception {

        WebResponse resp = null;
        WebResponse frame = null;
        WebTable table = null;
        WebTable contents = null;
        WebLink link = null;
        WebForm webForm = null;

        String diskOffloadEnabled = null;

        String template = null;
        String cacheId = null;
        String timeout = null;
        String dependencyId = null;
        String replication = null;
        String priority = null;

        WebConversation wc = startNewConversation();
        Log.info(DiskContents.class, "testDiskContents", "DiskContents Test");

        clearAndResetCache(wc);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Offload Enabled *********");
        contents = getStatisticsTable(wc);
        diskOffloadEnabled = contents.getCellAsText(9, 1);
        assertEquals(msg("Disk Offload Enabled value was incorrect", ""), "Yes", diskOffloadEnabled);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Contents Display Correctly *********");

        for (int i = 1; i <= 4; i++) {
            resp = getWebResponse(wc, "/dynacachetests/TimeStamp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }
        for (int i = 0; i <= 4; i++)
            resp = getWebResponse(wc, "/dynacachetests/SaveAttributesParent.jsp?arg1=" + i);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        for (int i = 5; i <= 2004; i++) {
            resp = getWebResponse(wc, "/dynacachetests/SaveAttributesParent.jsp?arg1=" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        contents = getDiskContentsTable(wc);

        cacheId = contents.getCellAsText(2, 0);
        assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/SaveAttributesParent.jsp:arg1=1:requestType=GET", cacheId);

        cacheId = contents.getCellAsText(6, 0);
        assertEquals(msg("Cache ID Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp:arg1=1:requestType=GET", cacheId);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Cache Entry Display Correctly *********");

        frame = getDiskContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp:arg1=1:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        contents = frame.getTableWithID("metaData");
        assertNotNull(msg("Meta Data table for the Disk Cache Entry was null", ""), contents);

        contents = frame.getTableWithID("cacheEntry");
        assertNotNull(msg("Cache Entry table for the Disk Cache Entry was null", ""), contents);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Cache Entry Invalidates Correctly *********");
        webForm = frame.getFormWithName("invalidateForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        contents = getDiskContentsTable(wc);

        cacheId = contents.getCellAsText(3, 0);
        assertEquals(msg("Invalidated Cache Entry was not removed from the contents page", ""), "/dynacachetests/SaveAttributesParent.jsp:arg1=2:requestType=GET", cacheId);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Cache DependencyIds *********");

        contents = getDiskDepdendencyIdsTable(wc);
        dependencyId = contents.getCellAsText(2, 0);
        assertEquals(msg("Dependency IDs Field Displays Incorrectly", ""), "timestamp:1", dependencyId);

        dependencyId = contents.getCellAsText(3, 0);
        assertEquals(msg("DependencyIds IDs Field Displays Incorrectly", ""), "timestamp:2", dependencyId);

        frame = getDiskDependencyIdsFrame(wc);
        link = frame.getLinkWithID("timestamp:2");
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        contents = frame.getTableWithID("cacheIds");
        assertNotNull(msg("Cache Ids table for the Disk DependencyId was null", ""), contents);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk DependencyId Invalidates Correctly *********");
        webForm = frame.getFormWithName("invalidateForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        // 675111
        Thread.sleep(20000);

        contents = getDiskContentsTable(wc);
        cacheId = contents.getCellAsText(3, 0);
        assertEquals(msg("Cache Entry Invalidated by DependencyId was not removed from the contents page", ""), "/dynacachetests/SaveAttributesParent.jsp:arg1=3:requestType=GET",
                     cacheId);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Cache Templates *********");

        contents = getDiskTemplatesTable(wc);
        template = contents.getCellAsText(1, 0);
        assertEquals(msg("Template Field Displays Incorrectly", ""), "/dynacachetests/SaveAttributesParent.jsp", template);

        template = contents.getCellAsText(2, 0);
        assertEquals(msg("Template Field Displays Incorrectly", ""), "/dynacachetests/TimeStamp", template);

        frame = getDiskTemplatesFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/TimeStamp");
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        contents = frame.getTableWithID("cacheIds");
        assertNotNull(msg("Cache Ids table for the Disk Templates was null", ""), contents);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Disk Templates Invalidates Correctly *********");

        webForm = frame.getFormWithName("invalidateForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        // Added this for 640942
        Thread.sleep(40000);

        contents = getDiskContentsTable(wc);
        cacheId = (new Integer(contents.getRowCount())).toString();
        assertEquals(msg("Cache Entry Invalidated by Template was not removed from the contents page", ""), "5", cacheId);

        Log.info(DiskContents.class, "testDiskContents", "********* Verify Send to Memory *********");

        frame = getDiskContentsFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/SaveAttributesParent.jsp:arg1=0:requestType=GET");
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        webForm = frame.getFormWithName("refresh");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        contents = getDiskContentsTable(wc);
        cacheId = contents.getCellAsText(1, 0).trim();
        assertEquals(msg("Cache ID " + cacheId + " was removed from contents when Refreshed to Memory", ""), "/dynacachetests/SaveAttributesParent.jsp:arg1=0:requestType=GET",
                     cacheId);

        contents = getContentsTable(wc);
        cacheId = contents.getCellAsText(1, 1).trim();
        assertEquals(msg("Entry " + cacheId + " NOT found in Memory Contents", ""), "/dynacachetests/SaveAttributesParent.jsp:arg1=0:requestType=GET", cacheId);

    }

    public WebTable getDiskTemplatesTable(WebConversation wc) throws Exception {
        WebResponse frame = getDiskTemplatesFrame(wc);
        return frame.getTableWithID("templates");
    }

    public WebTable getDiskContentsTable(WebConversation wc) throws Exception {
        WebResponse frame = getDiskContentsFrame(wc);
        return frame.getTableWithID("cacheIds");
    }

    public WebTable getDiskDepdendencyIdsTable(WebConversation wc) throws Exception {
        WebResponse frame = getDiskDependencyIdsFrame(wc);
        return frame.getTableWithID("dataIds");
    }

}
