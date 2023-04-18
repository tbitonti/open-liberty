package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class BufferTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("BufferTest");

    @Test
    public void testBufferWriter() throws Exception {
        String URI = "/dynacachetests/bufferwriter";
        int requests = 4;
        while (requests-- > 0) {
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            // make sure badtable is not present
            WebTable badtable = resp.getTableWithID("badtable");
            assertNull(msg("bad table was found! reset failed", resp), badtable);

            //make sure goodtable is present
            WebTable goodtable = resp.getTableWithID("goodtable");
            assertNotNull(msg("missing good table", resp), goodtable);
        }
    }

    @Test
    public void testBufferWriterCached() throws Exception {
        String URI = "/dynacachetests/bufferwritercached";
        int requests = 4;
        while (requests-- > 0) {
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            // make sure badtable is not present
            WebTable badtable = resp.getTableWithID("badtable");
            assertNull(msg("bad table was found! reset failed", resp), badtable);

            //make sure goodtable is present
            WebTable goodtable = resp.getTableWithID("goodtable");
            assertNotNull(msg("missing good table", resp), goodtable);
        }
    }

}
