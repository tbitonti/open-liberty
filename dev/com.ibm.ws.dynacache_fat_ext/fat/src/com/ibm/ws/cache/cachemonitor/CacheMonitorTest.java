// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business
// Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.cachemonitor;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class CacheMonitorTest extends ServletTestCase {

    public String url;

    public CacheMonitorTest() {
        url = TestConfig.getBaseURL() + "/cachemonitor";
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
    }

    public void selectBaseCacheInstance(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=baseCache");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
    }

    public WebResponse getDetailsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url);
        WebResponse resp = wc.getResponse(req);
        resp = resp.getSubframeContents("detail");
        return resp;
    }

    //clear cache and reset statistics
    public void clearAndResetCache(WebConversation wc) throws Exception {

        selectBaseCacheInstance(wc);
        WebResponse frame = getStatisticsFrame(wc);
        clearCache(wc);
        WebForm form = getDetailsFrame(wc).getFormWithName("resetForm");
        WebResponse resp = form.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

    }

    public void clearCache(WebConversation wc) throws SAXException, Exception, IOException {
        WebForm form = getDetailsFrame(wc).getFormWithName("clearForm");
        WebResponse resp = form.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
    }

    public void clearAndResetCacheAllInstances(WebConversation wc) throws Exception {
        //clear cache and reset statistics for all instances
        WebRequest req = new GetMethodWebRequest(url + "/selectInstance.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebForm webForm = resp.getFormWithName("selectInstance");
        String instances[] = webForm.getOptionValues("instance");

        WebResponse frame = null;

        for (int i = 0; i < instances.length; i++) {
            req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=" + instances[i]);
            resp = wc.getResponse(req);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            frame = getInstanceStatisticsFrame(wc);
            webForm = frame.getFormWithName("clearForm");
            if (webForm != null) {
                resp = webForm.submit();
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            }

            frame = getInstanceStatisticsFrame(wc);
            webForm = frame.getFormWithName("resetForm");
            if (webForm != null) {
                resp = webForm.submit();
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            }
        }
    }

    public void clearAndResetEdge(WebConversation wc) throws Exception {

        WebResponse frame = getEdgeStatisticsFrame(wc);
        WebForm webForm = frame.getFormWithName("clearForm");
        WebResponse resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        frame = getEdgeStatisticsFrame(wc);
        webForm = frame.getFormWithName("resetForm");
        resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

    }

    public WebResponse getStatisticsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url);
        WebResponse resp = wc.getResponse(req);
        WebResponse frame = resp.getSubframeContents("navigation_tree");

        WebLink link = frame.getLinkWithID("Cache Statistics");
        resp = link.click();

        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebTable getStatisticsTable(WebConversation wc) throws Exception {
        WebResponse frame = getDetailsFrame(wc);
        return frame.getTableWithID("statistics");
    }

    public WebResponse getEdgeStatisticsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url);
        WebResponse resp = wc.getResponse(req);
        WebResponse frame = resp.getSubframeContents("navigation_tree");

        WebLink link = frame.getLinkWithID("Edge Statistics");
        resp = link.click();
        resp = wc.getFrameContents("detail");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebTable getEdgeStatisticsTable(WebConversation wc) throws Exception {
        WebResponse frame = getEdgeStatisticsFrame(wc);
        WebForm webForm = frame.getFormWithName("refreshForm");
        WebResponse resp = webForm.submit();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        frame = getEdgeStatisticsFrame(wc);
        return frame.getTableWithID("statistics");
    }

    public WebResponse getContentsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url);
        WebResponse resp = wc.getResponse(req);
        WebResponse frame = resp.getSubframeContents("navigation_tree");

        WebLink link = frame.getLinkWithID("Cache Contents");
        resp = link.click();
        resp = wc.getFrameContents("detail");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebTable getContentsTable(WebConversation wc) throws Exception {
        WebResponse frame = getContentsFrame(wc);
        return frame.getTableWithID("contents");
    }

    public List<WebResponse> getEdgeContentsFrame(WebConversation wc) throws Exception {

        List<WebResponse> frames = new ArrayList<WebResponse>();
        WebResponse resp = getEdgeStatisticsFrame(wc);
        WebForm[] forms = resp.getForms();
        for (int i = 0; i < forms.length; i++) {
            if (forms[i].getName().equalsIgnoreCase("viewProcessorEntries")) {
                WebResponse frame = forms[i].submit();
                assertEquals(msg("Response code was not OK", resp), frame.getResponseCode(), 200);
                WebForm webForm = frame.getFormWithName("resetForm");
                frame = webForm.submit();
                assertEquals(msg("Response code was not OK", resp), frame.getResponseCode(), 200);
                frames.add(frame);
            }
        }

        return frames;
    }

    public List<WebTable> getEdgeContentsTables(WebConversation wc) throws Exception {

        List<WebTable> edgeTables = new ArrayList<WebTable>();
        List<WebResponse> frames = getEdgeContentsFrame(wc);
        for (Iterator iter = frames.iterator(); iter.hasNext();) {
            WebResponse frame = (WebResponse) iter.next();
            WebTable table = frame.getTableWithID("contents");
            edgeTables.add(table);
        }
        return edgeTables;
    }

    public WebResponse getDependencyIdsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url);
        WebResponse resp = wc.getResponse(req);
        WebResponse frame = resp.getSubframeContents("navigation_tree");

        WebLink link = frame.getLinkWithID("Dependency IDs");
        resp = link.click();
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    //due to the way the links are displayed for Disk Offload we will have to
    // directly access the jsps
    public WebResponse getDiskTemplatesFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/diskTemplates.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebResponse getDiskContentsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/diskContents.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebResponse getDiskDependencyIdsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/diskDataIds.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebResponse getPoliciesFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url);
        WebResponse resp = wc.getResponse(req);
        WebResponse frame = resp.getSubframeContents("navigation_tree");

        WebLink link = frame.getLinkWithID("Cache Policies");
        resp = link.click();
        resp = wc.getFrameContents("detail");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    //  When hitting the cache monitor url the instnace is reset to basecache
    // need to access the jsps directly for instances testcases
    public WebResponse getInstanceStatisticsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/statistics.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebTable getInstanceStatisticsTable(WebConversation wc) throws Exception {
        WebResponse frame = getInstanceStatisticsFrame(wc);
        return frame.getTableWithID("statistics");
    }

    public WebResponse getInstanceContentsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/contents.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebResponse getInstanceDependencyIdsFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/dataIds.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

    public WebResponse getInstancePoliciesFrame(WebConversation wc) throws Exception {
        WebRequest req = new GetMethodWebRequest(url + "/policies.jsp");
        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        return resp;
    }

}