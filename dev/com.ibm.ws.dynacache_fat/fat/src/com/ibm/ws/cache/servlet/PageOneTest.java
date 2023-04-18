package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class PageOneTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("PageOneTest");

    public static String URI = "/dynacachetests/servlet/page1";

    // includeservlet == com.ibm.ws.cache.servlet.IncludeTestServlet

    @Test
    public void testPageOne() throws Exception {
        clearCache();
        WebConversation wc = startNewConversation();

        WebResponse resp = null;

        PageOneResult nocache = new PageOneResult(getWebResponse(wc, URI + "?cache=no"), this);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        PageOneResult miss = new PageOneResult(resp = getWebResponse(wc, URI), this);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        if (!nocache.attrsAreEqual(miss)) {
            fail(msg("cache miss was not equal to uncached run", resp));
        }
        PageOneResult hit = new PageOneResult(resp = getWebResponse(wc, URI), this);

        if (!miss.equals(hit)) {
            fail(msg("cache hit was not equal to cache miss", resp));
        }
    }
}

class PageOneResult {

    int NUM_INCLUDES = 9;

    ArrayList tracks = null;

    ArrayList cachedStamps = null;
    ArrayList uncachedStamps = null;

    ArrayList stampsToPageMap = null;

    WebResponse resp = null;

    HashMap subPages = null;

    PageOneTest parent = null;

    PageOneResult(WebResponse resp, PageOneTest pot) {
        this.parent = pot;
        this.resp = resp;
        tracks = new ArrayList();
        cachedStamps = new ArrayList();
        uncachedStamps = new ArrayList();
        stampsToPageMap = new ArrayList();

        buildTree();

        try {
            doPage("page1");
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
            fail(parent.msg("SAX exception: " + e.getMessage(), resp));
        }
    }

    void buildTree() {
        subPages = new HashMap();
        subPages.put("page1", new String[] { "page3", "page4", "page2" });
        subPages.put("page2", new String[] { "page4" });
        subPages.put("page3", new String[] { "page4", "page5", "page6" });
        subPages.put("page4", new String[] { "page7" });
        //subPages.put("page5",{});
        //subPages.put("page6",{});
        //subPages.put("page7",{});  
    }

    boolean attrsAreEqual(PageOneResult r) {
        return listEquals(tracks, r.tracks);
    }

    boolean uncachedAreEqual(PageOneResult r) {
        return uncachedStamps.size() == r.uncachedStamps.size();
    }

    boolean listEquals(ArrayList a, ArrayList b) {
        boolean bool = a != null && a.size() > 0 && b != null && b.size() > 0;
        Iterator i = a.iterator();
        Iterator ii = b.iterator();
        Iterator pages = stampsToPageMap.iterator();
        while (bool && i.hasNext() && ii.hasNext()) {
            String s = (String) i.next();
            String ss = (String) ii.next();
            String curPage = (String) pages.next();
            bool = s.equals(ss);
            if (!bool) {
                System.out.println(curPage + ": " + s + " != " + ss);
            }
        }
        if (bool) {
            bool = !i.hasNext() && !ii.hasNext();
            if (!bool) {
                if (i.hasNext())
                    System.out.print("1st ");
                else
                    System.out.print("2nd ");
                System.out.println("arraylist not finished, " + a.size() + ", " + b.size());
            }
        }
        return bool;
    }

    boolean equals(PageOneResult r) {
        boolean t = listEquals(tracks, r.tracks);
        if (!t)
            System.out.println("tracks not equal");
        boolean c = listEquals(cachedStamps, r.cachedStamps);
        if (!c)
            System.out.println("cached stamps not equal");
        boolean u = uncachedAreEqual(r);
        if (!u)
            System.out.println("uncached not equal");
        return t && c && u;
    }

    void doPage(String thisPage) throws org.xml.sax.SAXException {
        WebTable page = resp.getTableWithID(thisPage);
        assertNotNull(parent.msg("missing " + thisPage + " invocation", resp), page);

        stampsToPageMap.add(thisPage);

        TableCell cell = page.getTableCell(0, 1);
        WebTable tracker = cell.getTableWithID("tracker");
        assertNotNull(parent.msg("missing tracker in " + thisPage, resp), cell);
        tracks.add(tracker.asText()[0][0]);

        WebTable stamp = cell.getTableWithID("Cached TimeStamp");
        if (stamp != null) {
            cachedStamps.add(stamp.asText()[0][0]);
        } else {
            stamp = cell.getTableWithID("Uncached TimeStamp");
            assertNotNull(parent.msg("missing TimeStamp in " + thisPage, resp), cell);
            uncachedStamps.add(stamp.asText()[0][0]);
        }

        Object[] includes = (Object[]) subPages.get(thisPage);
        if (includes != null) {
            for (int i = 0; i < includes.length; i++) {
                doPage((String) includes[i]);
            }
        }
    }
}
